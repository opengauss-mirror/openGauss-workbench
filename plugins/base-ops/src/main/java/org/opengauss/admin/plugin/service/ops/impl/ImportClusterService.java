/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * ImportClusterService.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/ImportClusterService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.EnterpriseInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.ImportClusterBody;
import org.opengauss.admin.plugin.domain.model.ops.LiteInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.MinimalistInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.LiteInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.utils.DBUtil;
import org.opengauss.admin.plugin.utils.DecryptionUtil;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.plugin.facade.JschExecutorFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.Resource;

/**
 * ImportClusterService
 *
 * @author: wangchao
 * @Date: 2024/11/4 12:02
 * @Description: ImportClusterService
 * @since 7.0.0
 **/
@Slf4j
@Service
public class ImportClusterService extends ServiceImpl<OpsClusterMapper, OpsClusterEntity> {
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private DecryptionUtil decryptionUtil;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JschExecutorFacade jschExecutorFacade;
    @Resource
    private DBUtil dbUtil;
    @Resource
    private IOpsClusterNodeService opsClusterNodeService;

    /**
     * importCluster
     *
     * @param importClusterBody importClusterBody
     */
    public void importCluster(ImportClusterBody importClusterBody) {
        importClusterBody.checkConfig();
        decryptionUtil.decryptDatabasePassword(importClusterBody);
        OpenGaussVersionEnum openGaussVersion = importClusterBody.getOpenGaussVersion();
        ClusterConfig clusterConfig = getClusterConfig(importClusterBody, openGaussVersion);
        OpsHostUserEntity masterNodeInstallUser = getMasterNodeInstallUser(clusterConfig.masterNodeInstallUserId);
        OpsHostEntity hostEntity = getHostEntity(clusterConfig.masterHostId);
        try (Connection connection = createConnection(hostEntity, clusterConfig.port, clusterConfig.databaseUsername,
            clusterConfig.databasePassword)) {
            SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(),
                masterNodeInstallUser.getUsername(), encryptionUtils.decrypt(masterNodeInstallUser.getPassword()));
            String versionNum = getVersionNum(sshLogin, importClusterBody.getEnvPath());
            importClusterBody.setOpenGaussVersionNum(versionNum);
            Integer majorVersion = Integer.valueOf(versionNum.substring(0, 1));
            OpenGaussVersionEnum actualVersion = judgeOpenGaussVersion(majorVersion, sshLogin, connection,
                importClusterBody.getEnvPath());
            validateVersion(majorVersion, importClusterBody.getOpenGaussVersion(), actualVersion);
        } catch (OpsException e) {
            log.error("ops exception ", e);
            throw e;
        } catch (Exception e) {
            log.error("get connection fail", e);
            throw new OpsException("connection fail：" + e.getMessage());
        }
        OpsClusterEntity opsClusterEntity = importClusterBody.toOpsClusterEntity();
        opsClusterEntity.setDatabasePassword(encryptionUtils.encrypt(opsClusterEntity.getDatabasePassword()));
        save(opsClusterEntity);
        saveClusterNodes(importClusterBody, opsClusterEntity);
    }

    private OpsHostUserEntity getMasterNodeInstallUser(String masterNodeInstallUserId) {
        OpsHostUserEntity masterNodeInstallUser = hostUserFacade.getById(masterNodeInstallUserId);
        if (Objects.isNull(masterNodeInstallUser)) {
            throw new OpsException("install user not found");
        }
        return masterNodeInstallUser;
    }

    private OpsHostEntity getHostEntity(String masterHostId) {
        OpsHostEntity hostEntity = hostFacade.getById(masterHostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host not found");
        }
        return hostEntity;
    }

    private Connection createConnection(OpsHostEntity hostEntity, Integer port, String databaseUsername,
        String databasePassword) throws OpsException, SQLException, ClassNotFoundException {
        return dbUtil.getSession(hostEntity.getPublicIp(), port, databaseUsername, databasePassword)
            .orElseThrow(() -> new OpsException("Connection failed"));
    }

    private String getVersionNum(SshLogin sshLogin, String envPath) {
        return jschExecutorFacade.getOpenGaussMainVersionNum(sshLogin, envPath);
    }

    private OpenGaussVersionEnum judgeOpenGaussVersion(Integer majorVersion, SshLogin sshLogin, Connection connection,
        String envPath) {
        boolean isEnterprise = enterpriseVersion(sshLogin, envPath);
        if (isEnterprise) {
            return OpenGaussVersionEnum.ENTERPRISE;
        }
        if (majorVersion >= 5) {
            boolean isLite = liteVersion(connection);
            if (isLite) {
                return OpenGaussVersionEnum.LITE;
            }
            return OpenGaussVersionEnum.MINIMAL_LIST;
        } else {
            return OpenGaussVersionEnum.MINIMAL_LIST;
        }
    }

    private boolean enterpriseVersion(SshLogin sshLogin, String envPath) {
        String command = "which gs_om";
        try {
            String jschResult = jschExecutorFacade.execCommand(sshLogin, command, envPath);
            if (jschResult.contains("script/gs_om")) {
                return true;
            }
            if (jschResult.contains("command not found")) {
                throw new OpsException("Failed to check openGauss version, detail: " + jschResult);
            }
        } catch (OpsException opsException) {
            log.error("select enterprise version command error:", opsException);
        }
        return false;
    }

    private boolean liteVersion(Connection connection) {
        String sql = "select version()";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                String version = resultSet.getString("version");
                return version.contains("lite");
            }
        } catch (SQLException e) {
            log.error("query version fail", e);
            throw new OpsException("query version fail：" + e);
        }
        return false;
    }

    private ClusterConfig getClusterConfig(ImportClusterBody importClusterBody, OpenGaussVersionEnum openGaussVersion) {
        if (Objects.equals(openGaussVersion, OpenGaussVersionEnum.MINIMAL_LIST)) {
            return new ClusterConfig(importClusterBody.getMinimalistInstallConfig());
        } else if (Objects.equals(openGaussVersion, OpenGaussVersionEnum.LITE)) {
            return new ClusterConfig(importClusterBody.getLiteInstallConfig());
        } else if (Objects.equals(openGaussVersion, OpenGaussVersionEnum.ENTERPRISE)) {
            return new ClusterConfig(importClusterBody.getEnterpriseInstallConfig());
        } else {
            throw new OpsException("Unsupported OpenGauss version");
        }
    }

    private void validateVersion(Integer majorVersion, OpenGaussVersionEnum selectedVersion,
        OpenGaussVersionEnum actualVersion) throws OpsException {
        boolean isVersionMatch = false;
        if (majorVersion >= 5) {
            if (Objects.equals(selectedVersion, actualVersion)) {
                isVersionMatch = true;
            }
        } else {
            if (Objects.equals(selectedVersion, OpenGaussVersionEnum.ENTERPRISE)) {
                if (Objects.equals(actualVersion, OpenGaussVersionEnum.ENTERPRISE)) {
                    isVersionMatch = true;
                }
            } else {
                if (!Objects.equals(actualVersion, OpenGaussVersionEnum.ENTERPRISE)) {
                    isVersionMatch = true;
                }
            }
        }
        if (!selectedVersion.equals(actualVersion)) {
            log.error("The selected version does not match the actual version, select version:{}, actual version:{}",
                selectedVersion, actualVersion);
            throw new OpsException("The selected version does not match the actual version");
        }
    }

    private void saveClusterNodes(ImportClusterBody importClusterBody, OpsClusterEntity opsClusterEntity) {
        List<OpsClusterNodeEntity> opsClusterNodeEntities = toOpsClusterNodeEntityList(importClusterBody,
            opsClusterEntity);
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
        }
        opsClusterNodeService.saveBatch(opsClusterNodeEntities);
    }

    private List<OpsClusterNodeEntity> toOpsClusterNodeEntityList(ImportClusterBody importClusterBody,
                                                                  OpsClusterEntity opsClusterEntity) {
        if (Objects.equals(OpenGaussVersionEnum.LITE, importClusterBody.getOpenGaussVersion())) {
            return importClusterBody.getLiteInstallConfig().toOpsClusterNodeEntityList();
        } else if (Objects.equals(OpenGaussVersionEnum.MINIMAL_LIST, importClusterBody.getOpenGaussVersion())) {
            return importClusterBody.getMinimalistInstallConfig().toOpsClusterNodeEntityList(opsClusterEntity);
        } else if (Objects.equals(OpenGaussVersionEnum.ENTERPRISE, importClusterBody.getOpenGaussVersion())) {
            return importClusterBody.getEnterpriseInstallConfig().toOpsClusterNodeEntityList();
        } else {
            throw new OpsException("Unsupported openGauss version: " + importClusterBody.getOpenGaussVersion());
        }
    }

    private class ClusterConfig {
        Integer port;
        String databaseUsername;
        String databasePassword;
        String masterHostId;
        String masterNodeInstallUserId;

        ClusterConfig(MinimalistInstallConfig config) {
            this.port = config.getPort();
            this.databaseUsername = config.getDatabaseUsername();
            this.databasePassword = config.getDatabasePassword();
            MinimalistInstallNodeConfig masterNode = config.getNodeConfigList()
                .stream()
                .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
                .findFirst()
                .orElseThrow(() -> new OpsException("masternode not found"));
            this.masterHostId = masterNode.getHostId();
            this.masterNodeInstallUserId = masterNode.getInstallUserId();
        }

        ClusterConfig(LiteInstallConfig config) {
            this.port = config.getPort();
            this.databaseUsername = config.getDatabaseUsername();
            this.databasePassword = config.getDatabasePassword();
            LiteInstallNodeConfig masterNode = config.getNodeConfigList()
                .stream()
                .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
                .findFirst()
                .orElseThrow(() -> new OpsException("masternode not found"));
            this.masterHostId = masterNode.getHostId();
            this.masterNodeInstallUserId = masterNode.getInstallUserId();
        }

        ClusterConfig(EnterpriseInstallConfig config) {
            this.port = config.getPort();
            this.databaseUsername = config.getDatabaseUsername();
            this.databasePassword = config.getDatabasePassword();
            EnterpriseInstallNodeConfig masterNode = config.getNodeConfigList()
                .stream()
                .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
                .findFirst()
                .orElseThrow(() -> new OpsException("masternode not found"));
            this.masterHostId = masterNode.getHostId();
            this.masterNodeInstallUserId = masterNode.getInstallUserId();
        }
    }
}
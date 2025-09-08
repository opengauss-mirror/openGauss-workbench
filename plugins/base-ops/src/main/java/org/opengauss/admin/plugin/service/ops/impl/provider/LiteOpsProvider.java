/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * LiteOpsProvider.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/provider/LiteOpsProvider.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.provider;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.HostInfoHolder;
import org.opengauss.admin.plugin.domain.model.ops.InstallContext;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.LiteInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.OpsClusterContext;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.domain.model.ops.UnInstallContext;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.node.LiteInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/8/12 09:26
 **/
@Slf4j
@Service
public class LiteOpsProvider extends AbstractOpsProvider {
    @Autowired
    private IOpsClusterService opsClusterService;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private WsUtil wsUtil;

    @Override
    public OpenGaussVersionEnum version() {
        return OpenGaussVersionEnum.LITE;
    }

    @Override
    public void install(InstallContext installContext) {
        DeployTypeEnum deployType = installContext.getDeployType();
        if (DeployTypeEnum.CLUSTER == deployType) {
            installCluster(installContext);
        } else {
            installSingleNode(installContext);
        }

        wsUtil.sendText(installContext.getRetSession(), "CREATE_REMOTE_USER");
        OpsClusterContext opsClusterContext = new OpsClusterContext();
        OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = installContext.getLiteInstallConfig().toOpsClusterNodeEntityList();
        opsClusterContext.setOpsClusterEntity(opsClusterEntity);
        opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntities);

        createRemoteUser(installContext, opsClusterContext);

        wsUtil.sendText(installContext.getRetSession(), "SAVE_INSTALL_CONTEXT");
        saveContext(installContext);
        wsUtil.sendText(installContext.getRetSession(), "FINISH");
    }

    private void createRemoteUser(InstallContext installContext, OpsClusterContext opsClusterContext) {
        WsSession retSession = installContext.getRetSession();
        opsClusterContext.setRetSession(retSession);
        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());

        LiteInstallConfig liteInstallConfig = installContext.getLiteInstallConfig();
        List<LiteInstallNodeConfig> nodeConfigList = liteInstallConfig.getNodeConfigList();
        String databasePassword = liteInstallConfig.getDatabasePassword();
        Integer port = liteInstallConfig.getPort();
        for (LiteInstallNodeConfig liteInstallNodeConfig : nodeConfigList) {
            String hostId = liteInstallNodeConfig.getHostId();
            String installUserId = liteInstallNodeConfig.getInstallUserId();
            String dataPath = liteInstallNodeConfig.getDataPath();


            Session session = loginWithUser(jschUtil, encryptionUtils, installContext.getHostInfoHolders(), false, hostId, installUserId);
            String listenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath);
            try {
                JschResult jschResult = jschUtil.executeCommand(listenerAddress, installContext.getEnvPath(), session, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to modify listening address, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to modify listening address");
                }

            } catch (Exception e) {
                log.error("Failed to modify listening address", e);
                throw new OpsException("Failed to modify listening address");
            }

            String hba = MessageFormat.format(SshCommandConstants.HBA, dataPath);
            try {
                JschResult jschResult = jschUtil.executeCommand(hba, installContext.getEnvPath(), session, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to modify host, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to modify host");
                }

            } catch (Exception e) {
                log.error("Failed to modify host", e);
                throw new OpsException("Failed to modify host");
            }

            String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(port));
            try {
                Map<String, String> response = new HashMap<>();
                String createUser = MessageFormat.format(
                    "CREATE USER gaussdb WITH MONADMIN AUDITADMIN SYSADMIN PASSWORD \"{0}\";\\q",
                    encryptionUtils.decrypt(databasePassword));
                response.put("openGauss=#", createUser);
                JschResult jschResult = jschUtil.executeCommand(installContext.getEnvPath(), clientLoginOpenGauss, session, retSession, response);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to create user, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to create user");
                }

            } catch (Exception e) {
                log.error("Failed to create user", e);
                throw new OpsException("Failed to create user");
            }
        }

        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());
        restart(opsClusterContext);
    }

    private void installSingleNode(InstallContext installContext) {
        wsUtil.sendText(installContext.getRetSession(), "START_SINGLE");
        log.info("Start installing Single Node Lite");
        WsSession retSession = installContext.getRetSession();

        LiteInstallNodeConfig installNodeConfig = installContext.getLiteInstallConfig().getNodeConfigList().get(0);

        String installPath = preparePath(installNodeConfig.getInstallPath());
        String pkgPath = preparePath(installContext.getLiteInstallConfig().getInstallPackagePath());
        String dataPath = preparePath(installNodeConfig.getDataPath());
        String hostId = installNodeConfig.getHostId();
        String databasePassword = installContext.getLiteInstallConfig().getDatabasePassword();
        Integer port = installContext.getLiteInstallConfig().getPort();

        String installUserId = installNodeConfig.getInstallUserId();
        String installUserName = null;
        if (StrUtil.isEmpty(installUserId)) {
            wsUtil.sendText(installContext.getRetSession(), "CREATE_INSTALL_USER");
            OpsHostUserEntity installUser = createInstallUser(installContext, hostId);
            installUserId = installUser.getHostUserId();
            installUserName = installUser.getUsername();
        } else {
            final String uid = installUserId;
            OpsHostUserEntity installUser = installContext.getHostInfoHolders()
                    .stream()
                    .filter(hostInfoHolder -> hostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("host information does not exist"))
                    .getHostUserEntities()
                    .stream()
                    .filter(userInfo -> uid.equals(userInfo.getHostUserId()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("Installation user information does not exist"));
            installUserName = installUser.getUsername();
        }

        if (StrUtil.isEmpty(installUserId)) {
            throw new OpsException("Installation user ID does not exist");
        }

        Session installUserSession = beforeInstall(jschUtil, encryptionUtils, installContext, pkgPath, dataPath, pkgPath, hostId, installUserId, installUserName, "-xvf");

        try {
            log.info("perform installation");
            // install
            String command = MessageFormat.format(SshCommandConstants.LITE_SINGLE_INSTALL, databasePassword, pkgPath, dataPath, installPath, String.valueOf(port));

            command = wrapperLiteEnvSep(command, installContext.getEnvPath());

            wsUtil.sendText(installContext.getRetSession(), "START_EXE_INSTALL_COMMAND");
            try {
                JschResult jschResult = jschUtil.executeCommand(command, installUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Lite single node installation log: {}", jschResult.getResult());
                    throw new OpsException("Lite single node installation failed with exit code：" + jschResult.getExitCode());
                }
            } catch (Exception e) {
                log.error("Lite single node installation fails", e);
                throw new OpsException("Lite single node installation fails");
            }
            wsUtil.sendText(installContext.getRetSession(), "END_EXE_INSTALL_COMMAND");

            log.info("The installation is complete");
        } finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()) {
                installUserSession.disconnect();
            }
        }
    }

    private OpsHostUserEntity createInstallUser(InstallContext installContext, String hostId) {
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        OpsHostUserEntity hostUserEntity = hostUserFacade.getOmmUserByHostId(hostId);
        if (Objects.nonNull(hostUserEntity)) {
            return hostUserEntity;
        }

        String rootPassword = installContext.getLiteInstallConfig().getNodeConfigList().stream().filter(nodeConfig -> nodeConfig.getHostId().equalsIgnoreCase(hostId)).map(LiteInstallNodeConfig::getRootPassword).findFirst().orElseThrow(() -> new OpsException("root password not found"));
        Session rootSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), "root", encryptionUtils.decrypt(rootPassword)).orElseThrow(() -> new OpsException("with the host[" + hostEntity.getPublicIp() + "]Failed to establish session"));
        try {
            try {
                JschResult jschResult = jschUtil.executeCommand(SshCommandConstants.OMM_USER, rootSession, installContext.getRetSession());
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to query omm user, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to query omm user");
                }

                if (StrUtil.isNotEmpty(jschResult.getResult()) && "1".equals(jschResult.getResult())) {
                    throw new OpsException("Please enter the omm user login password");
                }
            } catch (Exception e) {
                log.error("Failed to query omm user", e);
                throw new OpsException("Failed to query omm user");
            }

            String password = StrUtil.uuid();

            try {
                JschResult jschResult = null;
                try {
                    jschResult = jschUtil.executeCommand(SshCommandConstants.CREATE_OMM_USER, rootSession, installContext.getRetSession());
                } catch (InterruptedException e) {
                    throw new OpsException("thread is interrupted");
                }
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to create omm user, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to create omm user");
                }
            } catch (IOException e) {
                log.error("Failed to create omm user", e);
                throw new OpsException("Failed to create omm user");
            }

            try {
                Map<String, String> autoResponse = new HashMap<>();
                autoResponse.put("password:", password);
                autoResponse.put("password:", password);
                JschResult jschResult = null;
                try {
                    jschResult = jschUtil.executeCommand(SshCommandConstants.CHANGE_OMM_PASSWORD, rootSession, installContext.getRetSession(), autoResponse);
                } catch (InterruptedException e) {
                    throw new OpsException("thread is interrupted");
                }
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to modify omm user password, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to modify omm user password");
                }
            } catch (IOException e) {
                log.error("Failed to modify omm user password", e);
                throw new OpsException("Failed to modify omm user password");
            }

            hostUserEntity = new OpsHostUserEntity();
            hostUserEntity.setHostId(hostId);
            hostUserEntity.setUsername("omm");
            hostUserEntity.setPassword(encryptionUtils.encrypt(password));

            hostUserFacade.save(hostUserEntity);

            List<HostInfoHolder> hostInfoHolders = installContext.getHostInfoHolders();
            if (Objects.isNull(hostInfoHolders)) {
                hostInfoHolders = new ArrayList<>();
                installContext.setHostInfoHolders(hostInfoHolders);
            }

            HostInfoHolder hostHolder = hostInfoHolders.stream().filter(holder -> holder.getHostEntity().getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("host information not found"));
            List<OpsHostUserEntity> hostUserEntities = hostHolder.getHostUserEntities();
            if (Objects.isNull(hostUserEntities)) {
                hostUserEntities = new ArrayList<>();
                hostHolder.setHostUserEntities(hostUserEntities);
            }

            hostUserEntities.add(hostUserEntity);
            return hostUserEntity;
        } finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()) {
                rootSession.disconnect();
            }
        }
    }

    private void installCluster(InstallContext installContext) {
        wsUtil.sendText(installContext.getRetSession(), "START_CLUSTER");
        log.info("Start installing the Lite cluster");
        List<LiteInstallNodeConfig> installNodeConfigList = installContext.getLiteInstallConfig().getNodeConfigList();

        LiteInstallNodeConfig masterNodeConfig = installNodeConfigList.stream().filter(nodeConfig -> ClusterRoleEnum.MASTER == nodeConfig.getClusterRole()).findFirst().orElseThrow(() -> new OpsException("Master node configuration not found"));
        LiteInstallNodeConfig slaveNodeConfig = installNodeConfigList.stream().filter(nodeConfig -> ClusterRoleEnum.SLAVE == nodeConfig.getClusterRole()).findFirst().orElseThrow(() -> new OpsException("Master node configuration not found"));

        installMaster(masterNodeConfig, slaveNodeConfig, installContext);
        installSlave(masterNodeConfig, slaveNodeConfig, installContext);

        log.info("Cluster installation is complete");
    }

    private void installSlave(LiteInstallNodeConfig masterNodeConfig, LiteInstallNodeConfig slaveNodeConfig, InstallContext installContext) {
        wsUtil.sendText(installContext.getRetSession(), "START_SLAVE");
        log.info("Start installing the standby node");
        String installPath = preparePath(slaveNodeConfig.getInstallPath());
        String pkgPath = preparePath(installContext.getLiteInstallConfig().getInstallPackagePath());
        String dataPath = preparePath(slaveNodeConfig.getDataPath());
        String masterHostId = masterNodeConfig.getHostId();
        String slaveHostId = slaveNodeConfig.getHostId();
        String hostId = slaveNodeConfig.getHostId();
        String databasePassword = installContext.getLiteInstallConfig().getDatabasePassword();
        HostInfoHolder masterHostInfo = installContext.getHostInfoHolders().stream().filter(hostInfoHolder -> masterHostId.equals(hostInfoHolder.getHostEntity().getHostId())).findFirst().orElseThrow(() -> new OpsException("Master node configuration not found"));
        HostInfoHolder slaveHostInfo = installContext.getHostInfoHolders().stream().filter(hostInfoHolder -> slaveHostId.equals(hostInfoHolder.getHostEntity().getHostId())).findFirst().orElseThrow(() -> new OpsException("The host information of the standby node does not exist"));

        String installUserId = slaveNodeConfig.getInstallUserId();
        String installUserName = null;
        if (StrUtil.isEmpty(installUserId)) {
            wsUtil.sendText(installContext.getRetSession(), "CREATE_INSTALL_USER");
            OpsHostUserEntity installUser = createInstallUser(installContext, hostId);
            installUserId = installUser.getHostUserId();
            installUserName = installUser.getUsername();
        } else {
            final String uid = installUserId;
            OpsHostUserEntity installUser = installContext.getHostInfoHolders()
                    .stream()
                    .filter(hostInfoHolder -> hostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("host information does not exist"))
                    .getHostUserEntities()
                    .stream()
                    .filter(userInfo -> uid.equals(userInfo.getHostUserId()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("Installation user information does not exist"));
            installUserName = installUser.getUsername();
        }

        if (StrUtil.isEmpty(installUserId)) {
            throw new OpsException("Installation user ID does not exist");
        }

        slaveNodeConfig.setInstallUserId(installUserId);
        Session installUserSession = beforeInstall(jschUtil, encryptionUtils, installContext, pkgPath, dataPath, pkgPath, slaveHostId, installUserId, installUserName, "-xvf");

        try {
            log.info("perform installation");
            // install
            String command = MessageFormat.format(SshCommandConstants.LITE_SLAVE_INSTALL, databasePassword, pkgPath, dataPath, installPath,
                    slaveHostInfo.getHostEntity().getPrivateIp(),
                    String.valueOf(installContext.getLiteInstallConfig().getPort() + 1),
                    masterHostInfo.getHostEntity().getPrivateIp(),
                    String.valueOf(installContext.getLiteInstallConfig().getPort() + 1),
                    String.valueOf(installContext.getLiteInstallConfig().getPort()));
            command = wrapperLiteEnvSep(command, installContext.getEnvPath());
            wsUtil.sendText(installContext.getRetSession(), "START_EXE_INSTALL_COMMAND");
            try {
                JschResult jschResult = jschUtil.executeCommand(command, installUserSession, installContext.getRetSession());
                if (0 != jschResult.getExitCode()) {
                    log.error("Lite{}node installation log:{}", ClusterRoleEnum.SLAVE, jschResult.getResult());
                    throw new OpsException("light version" + ClusterRoleEnum.SLAVE + "Node installation failed with exit code：" + jschResult.getExitCode());
                }
            } catch (Exception e) {
                log.error("light version" + ClusterRoleEnum.SLAVE + "Node installation failed", e);
                throw new OpsException("light version" + ClusterRoleEnum.SLAVE + "Node installation failed");
            }
            wsUtil.sendText(installContext.getRetSession(), "END_EXE_INSTALL_COMMAND");
            log.info("Standby node installation is complete");
        } finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()) {
                installUserSession.disconnect();
            }
        }
    }

    private void installMaster(LiteInstallNodeConfig masterNodeConfig, LiteInstallNodeConfig slaveNodeConfig, InstallContext installContext) {
        wsUtil.sendText(installContext.getRetSession(), "START_MASTER");
        log.info("Start installing the master node");
        String installPath = preparePath(masterNodeConfig.getInstallPath());
        String pkgPath = preparePath(installContext.getLiteInstallConfig().getInstallPackagePath());
        String dataPath = preparePath(masterNodeConfig.getDataPath());
        String masterHostId = masterNodeConfig.getHostId();
        String slaveHostId = slaveNodeConfig.getHostId();
        String hostId = masterNodeConfig.getHostId();
        String databasePassword = installContext.getLiteInstallConfig().getDatabasePassword();
        HostInfoHolder masterHostInfo = installContext.getHostInfoHolders().stream().filter(hostInfoHolder -> masterHostId.equals(hostInfoHolder.getHostEntity().getHostId())).findFirst().orElseThrow(() -> new OpsException("Master node host information does not exist"));
        HostInfoHolder slaveHostInfo = installContext.getHostInfoHolders().stream().filter(hostInfoHolder -> slaveHostId.equals(hostInfoHolder.getHostEntity().getHostId())).findFirst().orElseThrow(() -> new OpsException("The host information of the standby node does not exist"));

        String installUserId = masterNodeConfig.getInstallUserId();
        String installUserName = null;
        if (StrUtil.isEmpty(installUserId)) {
            wsUtil.sendText(installContext.getRetSession(), "CREATE_INSTALL_USER");
            OpsHostUserEntity installUser = createInstallUser(installContext, hostId);
            installUserId = installUser.getHostUserId();
            installUserName = installUser.getUsername();
        } else {
            final String uid = installUserId;
            OpsHostUserEntity installUser = installContext.getHostInfoHolders()
                    .stream()
                    .filter(hostInfoHolder -> hostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("host information does not exist"))
                    .getHostUserEntities()
                    .stream()
                    .filter(userInfo -> uid.equals(userInfo.getHostUserId()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("Installation user information does not exist"));
            installUserName = installUser.getUsername();
        }

        if (StrUtil.isEmpty(installUserId)) {
            throw new OpsException("Installation user ID does not exist");
        }

        masterNodeConfig.setInstallUserId(installUserId);

        Session installUserSession = beforeInstall(jschUtil, encryptionUtils, installContext, pkgPath, dataPath, pkgPath, masterHostId, installUserId, installUserName, "-xvf");
        try {
            log.info("perform installation");
            // install
            String command = MessageFormat.format(SshCommandConstants.LITE_MASTER_INSTALL, databasePassword, pkgPath, dataPath, installPath,
                    masterHostInfo.getHostEntity().getPrivateIp(),
                    String.valueOf(installContext.getLiteInstallConfig().getPort() + 1),
                    slaveHostInfo.getHostEntity().getPrivateIp(),
                    String.valueOf(installContext.getLiteInstallConfig().getPort() + 1),
                    String.valueOf(installContext.getLiteInstallConfig().getPort()));
            command = wrapperLiteEnvSep(command, installContext.getEnvPath());
            wsUtil.sendText(installContext.getRetSession(), "START_EXE_INSTALL_COMMAND");
            try {
                JschResult jschResult = jschUtil.executeCommand(command, installUserSession, installContext.getRetSession());
                if (0 != jschResult.getExitCode()) {
                    log.error("Lite{}node installation log:{}", ClusterRoleEnum.MASTER, jschResult.getResult());
                    throw new OpsException("light version" + ClusterRoleEnum.MASTER + "Node installation failed with exit code：" + jschResult.getExitCode());
                }
            } catch (Exception e) {
                log.error("light version" + ClusterRoleEnum.MASTER + "Node installation failed", e);
                throw new OpsException("light version" + ClusterRoleEnum.MASTER + "Node installation failed");
            }
            wsUtil.sendText(installContext.getRetSession(), "END_EXE_INSTALL_COMMAND");
            log.info("Master node installation is complete");
        } finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()) {
                installUserSession.disconnect();
            }
        }
    }

    private String judgePkgPath(String installPath) {
        while (installPath.endsWith("/")) {
            installPath = installPath.substring(0, installPath.length() - 1);
        }

        return installPath.substring(0, installPath.lastIndexOf("/"));
    }

    private void saveContext(InstallContext installContext) {
        OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = installContext.getLiteInstallConfig().toOpsClusterNodeEntityList();

        opsClusterService.save(opsClusterEntity);
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
        }
        opsClusterNodeService.saveBatch(opsClusterNodeEntities);
    }

    @Override
    public void uninstall(UnInstallContext unInstallContext) {
        OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
        if (Objects.isNull(opsClusterEntity)) {
            throw new OpsException("Uninstall cluster does not exist");
        }

        DeployTypeEnum deployType = opsClusterEntity.getDeployType();
        if (DeployTypeEnum.CLUSTER == deployType) {
            unInstallCluster(unInstallContext);
        } else {
            unInstallSingleNode(unInstallContext);
        }
    }

    @Override
    public void restart(OpsClusterContext opsClusterContext) {
        log.info("Start restarting the lite version");
        List<String> restartNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(restartNodeIds)) {
            restartNodeIds = opsClusterContext.getOpsClusterNodeEntityList().stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(restartNodeIds)) {
            log.error("No nodes to restart");
        }

        for (String restartNodeId : restartNodeIds) {
            doRestartNode(restartNodeId, opsClusterContext);
        }
    }

    @Override
    public void start(OpsClusterContext opsClusterContext) {
        log.info("start up lite version");
        List<String> startNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(startNodeIds)) {
            startNodeIds = opsClusterContext.getOpsClusterNodeEntityList().stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(startNodeIds)) {
            log.error("No node to start");
        }

        for (String startNodeId : startNodeIds) {
            doStartNode(startNodeId, opsClusterContext);
        }
    }

    @Override
    public void stop(OpsClusterContext opsClusterContext) {
        log.info("start stop lite version");
        List<String> stopNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(stopNodeIds)) {
            stopNodeIds = opsClusterContext.getOpsClusterNodeEntityList()
                    .stream()
                    .map(OpsClusterNodeEntity::getClusterNodeId)
                    .collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(stopNodeIds)) {
            log.error("no node to stop");
        }

        for (String stopNodeId : stopNodeIds) {
            doStopNode(stopNodeId, opsClusterContext);
        }
    }

    @Override
    public void enableWdrSnapshot(Session session, OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities, WdrScopeEnum scope, String dataPath) {
        if (StrUtil.isEmpty(dataPath)) {
            dataPath = opsClusterNodeEntities.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("Master node configuration not found")).getDataPath();
        }

        String checkCommand = "gs_guc check -D " + dataPath + " -c \"enable_wdr_snapshot\"";
        try {
            JschResult jschResult = jschUtil.executeCommand(checkCommand, session, clusterEntity.getEnvPath());
            if (jschResult.getResult().contains("enable_wdr_snapshot=on")) {
                return;
            }
        } catch (Exception e) {
            log.error("Failed to set the enable_wdr_snapshot parameter", e);
            throw new OpsException("Failed to set the enable_wdr_snapshot parameter");
        }

        String command = "gs_guc reload -D " + dataPath + " -c \"enable_wdr_snapshot=on\"";

        try {
            JschResult jschResult = jschUtil.executeCommand(command, session, clusterEntity.getEnvPath());
            if (0 != jschResult.getExitCode()) {
                log.error("set enable_wdr_snapshot parameter failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to set the enable_wdr_snapshot parameter");
            }
        } catch (Exception e) {
            log.error("Failed to set the enable_wdr_snapshot parameter", e);
            throw new OpsException("Failed to set the enable_wdr_snapshot parameter");
        }
    }

    private void doStopNode(String stopNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity stopNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(stopNodeId)).findFirst().orElse(null);
        if (Objects.isNull(stopNodeEntity)) {
            log.error("Stop node info not found: {}", stopNodeId);
        }

        WsSession retSession = opsClusterContext.getRetSession();
        String dataPath = stopNodeEntity.getDataPath();
        log.info("login stop user");
        Session restartUserSession = loginWithUser(jschUtil, encryptionUtils, opsClusterContext.getHostInfoHolders(), false, stopNodeEntity.getHostId(), stopNodeEntity.getInstallUserId());

        String restartCommand = MessageFormat.format(SshCommandConstants.LITE_STOP, dataPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(restartCommand, opsClusterContext.getOpsClusterEntity().getEnvPath(), restartUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("stop error, exit code" + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("An exception occurred during the stop process", e);
            throw new OpsException("stop exception");
        }
    }

    private void doStartNode(String startNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity startNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(startNodeId)).findFirst().orElse(null);
        if (Objects.isNull(startNodeEntity)) {
            log.error("Boot node information not found:{}", startNodeId);
        }

        WsSession retSession = opsClusterContext.getRetSession();
        String dataPath = startNodeEntity.getDataPath();
        log.info("Login to start user");
        Session restartUserSession = loginWithUser(jschUtil, encryptionUtils, opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());

        String restartCommand = MessageFormat.format(SshCommandConstants.LITE_START, dataPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(restartCommand, opsClusterContext.getOpsClusterEntity().getEnvPath(), restartUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("startup error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("startup error", e);
            throw new OpsException("startup error");
        }
    }

    private void doRestartNode(String restartNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity restartNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(restartNodeId)).findFirst().orElse(null);
        if (Objects.isNull(restartNodeEntity)) {
            log.error("No restart node information found:{}", restartNodeId);
        }

        WsSession retSession = opsClusterContext.getRetSession();
        String dataPath = restartNodeEntity.getDataPath();
        log.info("login restart user");
        Session restartUserSession = loginWithUser(jschUtil, encryptionUtils, opsClusterContext.getHostInfoHolders(), false, restartNodeEntity.getHostId(), restartNodeEntity.getInstallUserId());

        String restartCommand = MessageFormat.format(SshCommandConstants.LITE_RESTART, dataPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(restartCommand, opsClusterContext.getOpsClusterEntity().getEnvPath(), restartUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("restart error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("restart error", e);
            throw new OpsException("restart error");
        }
    }

    private void unInstallSingleNode(UnInstallContext unInstallContext) {
        WsSession retSession = unInstallContext.getRetSession();

        OpsClusterNodeEntity opsClusterNodeEntity = unInstallContext.getOpsClusterNodeEntityList().get(0);

        HostInfoHolder hostInfoHolder = unInstallContext.getHostInfoHolders().get(0);
        OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
        OpsHostUserEntity hostUserEntity = hostInfoHolder.getHostUserEntities().stream().filter(userInfo -> opsClusterNodeEntity.getInstallUserId().equals(userInfo.getHostUserId())).findFirst().orElseThrow(() -> new OpsException("Installation user info user not found"));
        Session session = sshLogin(jschUtil, encryptionUtils, hostEntity, hostUserEntity);

        doUnInstall(retSession, session, unInstallContext.getOpsClusterEntity().getInstallPackagePath(), unInstallContext.getOpsClusterEntity().getEnvPath());
        removeContext(unInstallContext);
    }

    private void removeContext(UnInstallContext unInstallContext) {
        OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
        opsClusterService.removeById(opsClusterEntity.getClusterId());

        List<OpsClusterNodeEntity> opsClusterNodeEntityList = unInstallContext.getOpsClusterNodeEntityList();
        opsClusterNodeService.removeBatchByIds(opsClusterNodeEntityList.stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList()));
    }

    private void doUnInstall(WsSession retSession, Session session, String path, String envPath) {
        String command = MessageFormat.format(SshCommandConstants.LITE_UNINSTALL, path);

        try {
            JschResult result = null;
            try {
                result = jschUtil.executeCommand(command, envPath, session, retSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != result.getExitCode()) {
                log.error("Lite version uninstall failed, exit code {}, log content: {}",
                        result.getExitCode(), result.getResult());
                throw new OpsException("Uninstallation of Lite version failed");
            }
        } catch (IOException e) {
            log.error("Uninstallation of Lite version failed", e);
            throw new OpsException("Uninstallation of Lite version failed");
        }
    }

    private void unInstallCluster(UnInstallContext unInstallContext) {
        WsSession retSession = unInstallContext.getRetSession();
        List<OpsClusterNodeEntity> opsClusterNodeEntityList = unInstallContext.getOpsClusterNodeEntityList();
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntityList) {
            HostInfoHolder hostInfoHolder = unInstallContext.getHostInfoHolders().stream().filter(hostHolder -> hostHolder.getHostEntity().getHostId().equals(opsClusterNodeEntity.getHostId())).findFirst().orElseThrow(() -> new OpsException("Host information not found"));
            OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
            OpsHostUserEntity hostUserEntity = hostInfoHolder.getHostUserEntities().stream().filter(userInfo -> opsClusterNodeEntity.getInstallUserId().equals(userInfo.getHostUserId())).findFirst().orElseThrow(() -> new OpsException("Installation user info user not found"));
            Session session = sshLogin(jschUtil, encryptionUtils, hostEntity, hostUserEntity);

            doUnInstall(retSession, session, unInstallContext.getOpsClusterEntity().getInstallPackagePath(), unInstallContext.getOpsClusterEntity().getEnvPath());
        }

        removeContext(unInstallContext);
    }
}

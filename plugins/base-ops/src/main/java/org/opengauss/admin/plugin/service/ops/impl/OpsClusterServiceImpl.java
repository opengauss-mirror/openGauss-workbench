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
 * OpsClusterServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsClusterServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import org.opengauss.admin.common.constant.CommonConstants;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.cache.SSHChannelManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.domain.model.ops.env.HostEnv;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.LiteInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.*;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.opengauss.admin.plugin.utils.DBUtil;
import org.opengauss.admin.plugin.utils.DownloadUtil;
import org.opengauss.admin.plugin.utils.DecryptionUtil;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.OpsAssert;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.plugin.vo.ops.*;
import org.opengauss.admin.plugin.vo.ops.SessionVO;
import org.opengauss.admin.plugin.vo.ops.SlowSqlVO;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostMonitorFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.plugin.facade.JschExecutorFacade;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.plugin.facade.SysSettingFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * OpsClusterServiceImpl
 *
 * @author lhf
 * @date 2022/8/6 17:38
 **/
@Service
public class OpsClusterServiceImpl extends ServiceImpl<OpsClusterMapper, OpsClusterEntity>
    implements IOpsClusterService {
    private static final List<String> MONITOR_DEPENDENCY_LIST = List.of("procps-ng", "grep", "coreutils");
    private static final Logger log = LoggerFactory.getLogger(OpsClusterServiceImpl.class);

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostMonitorFacade hostMonitorFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsFacade opsFacade;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private DownloadUtil downloadUtil;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    private DBUtil DBUtil;
    @Autowired
    private SSHChannelManager sshChannelManager;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private DecryptionUtil decryptionUtil;
    @Autowired
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private ClusterOpsProviderManager clusterOpsProviderManager;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private SysSettingFacade sysSettingFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JschExecutorFacade jschExecutorFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsPackageManagerService pkgManagerService;

    @Override
    public void download(DownloadBody downloadBody) {
        WsSession wsSession = wsConnectorManager.getSession(downloadBody.getBusinessId())
            .orElseThrow(() -> new OpsException("No output websocket session found"));
        Future<?> future = threadPoolTaskExecutor.submit(
            () -> downloadUtil.download(downloadBody.getResourceUrl(), downloadBody.getTargetPath(),
                downloadBody.getFileName(), wsSession));
        TaskManager.registry(downloadBody.getBusinessId(), future);
    }

    @Override
    public void install(InstallBody installBody) {
        log.info("install:{}", JSON.toJSONString(installBody));
        decryptionUtil.decryptDatabasePassword(installBody);
        InstallContext installContext = installBody.getInstallContext();
        WsSession wsSession = wsConnectorManager.getSession(installBody.getBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        installContext.setRetSession(wsSession);
        populateHostInfo(installContext);
        installContext.setOs(checkOS(installBody.getInstallContext().getHostInfoHolders(), true));
        if (Boolean.TRUE.equals(installBody.getQuickInstall())) {
            downloadInstallPackage(installBody);
        }
        checkInstallConfig(installContext);
        InstallContext clone = installContext.clone();
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            doInstall(clone);
        });
        TaskManager.registry(installBody.getBusinessId(), future);
    }

    @Override
    public UpgradeOsCheckVO upgradeOsCheck(String clusterId, String rootPassword) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information cannot be empty");
        }
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
            .findFirst()
            .orElseThrow(() -> new OpsException("Masternode information not found"));
        String hostId = nodeEntity.getHostId();
        String installUserId = nodeEntity.getInstallUserId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }
        OpsHostUserEntity rootUserEntity = hostUserFacade.getRootUserByHostId(hostId);
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("User root was not found");
        }
        if (StrUtil.isEmpty(rootUserEntity.getPassword()) && StrUtil.isEmpty(rootPassword)) {
            throw new OpsException("root password cannot be empty");
        }
        if (StrUtil.isNotEmpty(rootUserEntity.getPassword())) {
            rootPassword = rootUserEntity.getPassword();
        }
        Session rootSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), "root",
                encryptionUtils.decrypt(rootPassword))
            .orElseThrow(
                () -> new OpsException("with the host[" + hostEntity.getPublicIp() + "]Failed to establish session"));
        String checkOsRes = upgradeCheckOs(rootSession, hostUserEntity.getUsername());
        String diskInfo = getDiskInfo(rootSession);
        UpgradeOsCheckVO upgradeOsCheckVO = new UpgradeOsCheckVO();
        upgradeOsCheckVO.setOsInfo(checkOsRes);
        upgradeOsCheckVO.setDiskUsed(calcDiskUsed(diskInfo, clusterEntity.getInstallPath()));
        return upgradeOsCheckVO;
    }

    private String getDiskInfo(Session session) {
        String command = "df -h";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, session);
            if (0 != jschResult.getExitCode()) {
                log.error("Query disk fail, exit code: {}, message: {}", jschResult.getExitCode(),
                    jschResult.getResult());
                throw new OpsException("Query disk size failed");
            } else {
                return jschResult.getResult();
            }
        } catch (Exception e) {
            log.error("Failed to query disk size", e);
            throw new OpsException("Failed to query disk size");
        }
    }

    private String upgradeCheckOs(Session session, String installUsername) {
        String command = "cd /root/gauss_om/" + installUsername + "/script/ && ./gs_checkos -i A";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, session);
            if (0 != jschResult.getExitCode()) {
                log.error("checkos fail, exit code: {}, message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("checkos failed");
            } else {
                return jschResult.getResult();
            }
        } catch (Exception e) {
            log.error("Failed to checkos", e);
            return StrUtil.EMPTY;
        }
    }

    private OpenGaussSupportOSEnum checkOS(List<HostInfoHolder> hostInfoHolderList, boolean root) {
        Set<String> os = new HashSet<>();
        Set<String> osVersion = new HashSet<>();
        Set<String> cpuArch = new HashSet<>();
        for (HostInfoHolder hostInfoHolder : hostInfoHolderList) {
            OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
            os.add(getOS(hostEntity.getHostId()));
            osVersion.add(getOSVersion(hostEntity.getHostId()));
            cpuArch.add(getCpuArch(hostEntity.getHostId()));
        }
        OpsAssert.isTrue(os.size() == 1, "The system information of hosts is inconsistent");
        OpsAssert.isTrue(osVersion.size() == 1, "System version information is inconsistent");
        OpsAssert.isTrue(cpuArch.size() == 1, "CPU architecture information is inconsistent");
        String osInfo = null;
        for (String osInfo0 : os) {
            osInfo = osInfo0;
            break;
        }
        String osVersionInfo = null;
        for (String osVersionInfo0 : osVersion) {
            osVersionInfo = osVersionInfo0;
            break;
        }
        String cpuArchInfo = null;
        for (String cpuArchInfo0 : cpuArch) {
            cpuArchInfo = cpuArchInfo0;
            break;
        }
        OpenGaussSupportOSEnum osInfoEnum = OpenGaussSupportOSEnum.of(osInfo, osVersionInfo, cpuArchInfo);
        if (osInfoEnum == null) {
            throw new OpsException(
                "unsupported operating system，system id " + osInfo + ",system version id" + osVersionInfo
                    + ", cpu Architecture " + cpuArchInfo);
        }
        return osInfoEnum;
    }

    private String getCpuArch(String hostId) {
        return hostMonitorFacade.getCpuArch(hostId);
    }

    private String getOSVersion(String hostId) {
        return hostMonitorFacade.getOsVersion(hostId);
    }

    private String getOS(String hostId) {
        return hostMonitorFacade.getOs(hostId);
    }

    @Override
    public void upgrade(UpgradeBody upgradeBody) {
        log.info("upgrade:{}", JSON.toJSONString(upgradeBody));
        OpsClusterEntity clusterEntity = getById(upgradeBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        if (clusterEntity.getVersion() != OpenGaussVersionEnum.ENTERPRISE) {
            throw new OpsException("Only supports Enterprise Edition upgrades");
        }
        if (Objects.isNull(upgradeBody.getUpgradeType())) {
            throw new OpsException("Please select an upgrade type");
        }
        if (StrUtil.isEmpty(upgradeBody.getUpgradePackagePath()) || !FileUtil.exist(
            upgradeBody.getUpgradePackagePath())) {
            throw new OpsException("Upgrade package not found");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(
            upgradeBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("No cluster node information found");
        }
        OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
            .findFirst()
            .orElseThrow(() -> new OpsException("The node information corresponding to the host is not found"));
        String installUserId = opsClusterNodeEntity.getInstallUserId();
        OpsHostUserEntity installUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installation user information not found");
        }
        OpsHostEntity hostEntity = hostFacade.getById(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information not found");
        }
        OpsHostUserEntity rootUserEntity = hostUserFacade.getRootUserByHostId(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("User root was not found");
        }
        if (StrUtil.isEmpty(rootUserEntity.getPassword()) && StrUtil.isEmpty(upgradeBody.getHostRootPassword())) {
            throw new OpsException("root password cannot be empty");
        }
        if (StrUtil.isNotEmpty(rootUserEntity.getPassword())) {
            upgradeBody.setHostRootPassword(rootUserEntity.getPassword());
        }
        UpgradeContext upgradeContext = new UpgradeContext();
        upgradeContext.setVersionNum(upgradeBody.getVersionNum());
        upgradeContext.setHostPublicIp(hostEntity.getPublicIp());
        upgradeContext.setRootPassword(upgradeBody.getHostRootPassword());
        WsSession wsSession = wsConnectorManager.getSession(upgradeBody.getBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        upgradeContext.setRetSession(wsSession);
        upgradeContext.setClusterConfigXmlPath(clusterEntity.getXmlConfigPath());
        upgradeContext.setUpgradeType(upgradeBody.getUpgradeType());
        upgradeContext.setUpgradePackagePath(upgradeBody.getUpgradePackagePath());
        upgradeContext.setSepEnvFile(clusterEntity.getEnvPath());
        upgradeContext.setInstallUsername(installUserEntity.getUsername());
        upgradeContext.setInstallUserPassword(installUserEntity.getPassword());
        upgradeContext.setHostPort(hostEntity.getPort());
        upgradeContext.setClusterEntity(clusterEntity);
        upgradeContext.setOpsClusterNodeEntity(opsClusterNodeEntity);
        HostInfoHolder hostInfoHolder = new HostInfoHolder();
        hostInfoHolder.setHostEntity(hostEntity);
        hostInfoHolder.setHostUserEntities(List.of(rootUserEntity));
        upgradeContext.setOs(checkOS(List.of(hostInfoHolder), true));
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            doUpgrade(upgradeContext);
        });
        TaskManager.registry(upgradeBody.getBusinessId(), future);
    }

    @Override
    public void upgradeCommit(UpgradeBody upgradeBody) {
        log.info("upgradeCommit:{}", JSON.toJSONString(upgradeBody));
        OpsClusterEntity clusterEntity = getById(upgradeBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(
            upgradeBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("No cluster node information found");
        }
        OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
            .findFirst()
            .orElseThrow(() -> new OpsException("The node information corresponding to the host is not found"));
        String installUserId = opsClusterNodeEntity.getInstallUserId();
        OpsHostUserEntity installUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installation user information not found");
        }
        OpsHostEntity hostEntity = hostFacade.getById(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information not found");
        }
        OpsHostUserEntity rootUserEntity = hostUserFacade.getRootUserByHostId(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("User root was not found");
        }
        if (StrUtil.isEmpty(rootUserEntity.getPassword()) && StrUtil.isEmpty(upgradeBody.getHostRootPassword())) {
            throw new OpsException("root password cannot be empty");
        }
        if (StrUtil.isNotEmpty(rootUserEntity.getPassword())) {
            upgradeBody.setHostRootPassword(rootUserEntity.getPassword());
        }
        Session ommSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(),
                installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword()))
            .orElseThrow(() -> new OpsException("Install user connection failed"));
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            WsSession wsSession = wsConnectorManager.getSession(upgradeBody.getBusinessId())
                .orElseThrow(() -> new OpsException("websocket session not exist"));
            doUpgradeCommit(ommSession, wsSession, clusterEntity, upgradeBody.getVersionNum());
            if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                ommSession.disconnect();
            }
        });
        TaskManager.registry(upgradeBody.getBusinessId(), future);
    }

    private void doUpgradeCommit(Session ommSession, WsSession wsSession, OpsClusterEntity clusterEntity,
        String versionNum) {
        wsUtil.sendText(wsSession, "COMMIT_UPGRADE");
        try {
            String command = "gs_upgradectl -t commit-upgrade  -X " + clusterEntity.getXmlConfigPath();
            JschResult jschResult = jschUtil.executeCommand(clusterEntity.getEnvPath(), command, ommSession, wsSession,
                false);
            if (5 != jschResult.getExitCode()) {
                log.error("upgradeCommit failed, exit code: {}, error message: {}", jschResult.getExitCode(),
                    jschResult.getResult());
                throw new OpsException("upgradeCommit failed");
            }
            LambdaUpdateWrapper<OpsClusterEntity> updateWrapper = Wrappers.lambdaUpdate(OpsClusterEntity.class)
                .set(OpsClusterEntity::getVersionNum, versionNum)
                .eq(OpsClusterEntity::getClusterId, clusterEntity.getClusterId());
            update(updateWrapper);
            wsUtil.sendText(wsSession, "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("upgradeCommit failed", e);
            wsUtil.sendText(wsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
            throw new OpsException("upgradeCommit failed");
        }
    }

    @Override
    public void upgradeRollback(UpgradeBody upgradeBody) {
        log.info("upgradeRollback:{}", JSON.toJSONString(upgradeBody));
        OpsClusterEntity clusterEntity = getById(upgradeBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(
            upgradeBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("No cluster node information found");
        }
        OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
            .findFirst()
            .orElseThrow(() -> new OpsException("The node information corresponding to the host is not found"));
        String installUserId = opsClusterNodeEntity.getInstallUserId();
        OpsHostUserEntity installUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installation user information not found");
        }
        OpsHostEntity hostEntity = hostFacade.getById(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information not found");
        }
        Session ommSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(),
                installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword()))
            .orElseThrow(() -> new OpsException("Install user connection failed"));
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            WsSession wsSession = wsConnectorManager.getSession(upgradeBody.getBusinessId())
                .orElseThrow(() -> new OpsException("websocket session not exist"));
            doUpgradeRollback(ommSession, wsSession, clusterEntity.getEnvPath(), clusterEntity.getXmlConfigPath());
            if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                ommSession.disconnect();
            }
        });
        TaskManager.registry(upgradeBody.getBusinessId(), future);
    }

    private void doUpgradeRollback(Session ommSession, WsSession wsSession, String envPath,
        String clusterConfigXmlPath) {
        try {
            String command = "gs_upgradectl -t auto-rollback  -X " + clusterConfigXmlPath + " --force";
            JschResult jschResult = jschUtil.executeCommand(envPath, command, ommSession, wsSession, false);
            if (2 != jschResult.getExitCode()) {
                log.error("Failed to create xml configuration file, exit code: {}, error message: {}",
                    jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to create xml configuration file");
            }
            wsUtil.sendText(wsSession, "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("Failed to create xml configuration file：", e);
            wsUtil.sendText(wsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
        } finally {
            if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                ommSession.disconnect();
            }
        }
    }

    private void doUpgrade(UpgradeContext upgradeContext) {
        try {
            clusterOpsProviderManager.provider(OpenGaussVersionEnum.ENTERPRISE)
                .orElseThrow(() -> new OpsException("The current version does not support"))
                .upgrade(upgradeContext);
            wsUtil.sendText(upgradeContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("Upgrade fail：", e);
            wsUtil.sendText(upgradeContext.getRetSession(), e.getMessage());
            wsUtil.sendText(upgradeContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    @Override
    public void quickInstall(InstallBody installBody) {
        WsSession wsSession = wsConnectorManager.getSession(installBody.getBusinessId())
            .orElseThrow(() -> new OpsException("No output websocket session found"));
        wsUtil.sendText(wsSession, "START");
        installBody.setQuickInstall(Boolean.TRUE);
        if (StrUtil.isEmpty(installBody.getQuickInstallResourceUrl())) {
            throw new OpsException("installation package path cannot be empty");
        }
        List<MinimalistInstallNodeConfig> nodeConfigList = installBody.getInstallContext()
            .getMinimalistInstallConfig()
            .getNodeConfigList();
        if (CollUtil.isNotEmpty(nodeConfigList)) {
            MinimalistInstallNodeConfig minimalistInstallNodeConfig = nodeConfigList.get(0);
            String installUserId = minimalistInstallNodeConfig.getInstallUserId();
            if (StrUtil.isEmpty(installUserId)) {
                List<OpsHostUserEntity> opsHostUserEntities = hostUserFacade.listHostUserByHostId(
                    minimalistInstallNodeConfig.getHostId());
                if (CollUtil.isNotEmpty(opsHostUserEntities)) {
                    OpsHostUserEntity opsHostUserEntity = opsHostUserEntities.stream()
                        .filter(user -> !"root".equals(user.getUsername()))
                        .findFirst()
                        .orElse(null);
                    if (Objects.nonNull(opsHostUserEntity)) {
                        minimalistInstallNodeConfig.setInstallUserId(opsHostUserEntity.getHostUserId());
                    }
                }
            }
        }
        install(installBody);
    }

    private void downloadInstallPackage(InstallBody installBody) {
        String installPackagePath = installBody.getInstallContext().getInstallPackagePath();
        String resourceUrl = installBody.getQuickInstallResourceUrl();
        String fileName = resourceUrl.substring(resourceUrl.lastIndexOf("/") + 1);
        WsSession wsSession = wsConnectorManager.getSession(installBody.getBusinessId())
            .orElseThrow(() -> new OpsException("No output websocket session found"));
        downloadUtil.download(resourceUrl, installPackagePath, fileName, wsSession);
        installBody.getInstallContext().setInstallPackagePath(installPackagePath + "/" + fileName);
    }

    private void populateHostInfo(InstallContext installContext) {
        int hostLen;
        List<String> hostIdList;
        Map<String, String> hostPasswdMap = new HashMap<>();
        OpenGaussVersionEnum openGaussVersion = installContext.getOpenGaussVersion();
        if (openGaussVersion == OpenGaussVersionEnum.ENTERPRISE) {
            List<EnterpriseInstallNodeConfig> nodeConfigList = installContext.getEnterpriseInstallConfig()
                .getNodeConfigList();
            hostLen = nodeConfigList.size();
            hostIdList = nodeConfigList.stream()
                .map(EnterpriseInstallNodeConfig::getHostId)
                .collect(Collectors.toList());
            for (EnterpriseInstallNodeConfig enterpriseInstallNodeConfig : nodeConfigList) {
                hostPasswdMap.put(enterpriseInstallNodeConfig.getHostId(),
                    enterpriseInstallNodeConfig.getRootPassword());
            }
        } else if (openGaussVersion == OpenGaussVersionEnum.MINIMAL_LIST) {
            List<MinimalistInstallNodeConfig> nodeConfigList = installContext.getMinimalistInstallConfig()
                .getNodeConfigList();
            hostLen = nodeConfigList.size();
            hostIdList = nodeConfigList.stream()
                .map(MinimalistInstallNodeConfig::getHostId)
                .collect(Collectors.toList());
            for (MinimalistInstallNodeConfig minimalistInstallNodeConfig : nodeConfigList) {
                hostPasswdMap.put(minimalistInstallNodeConfig.getHostId(),
                    minimalistInstallNodeConfig.getRootPassword());
            }
        } else if (openGaussVersion == OpenGaussVersionEnum.LITE) {
            List<LiteInstallNodeConfig> nodeConfigList = installContext.getLiteInstallConfig().getNodeConfigList();
            hostLen = nodeConfigList.size();
            hostIdList = nodeConfigList.stream().map(LiteInstallNodeConfig::getHostId).collect(Collectors.toList());
            for (LiteInstallNodeConfig liteInstallNodeConfig : nodeConfigList) {
                hostPasswdMap.put(liteInstallNodeConfig.getHostId(), liteInstallNodeConfig.getRootPassword());
            }
        } else {
            throw new OpsException("unsupported version：[" + installContext.getOpenGaussVersion() + "]");
        }
        if (CollUtil.isNotEmpty(hostIdList)) {
            List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdList);
            if (CollUtil.isEmpty(opsHostEntities)) {
                log.info("Host information not found,hostIds{}", hostIdList);
                throw new OpsException("Host information not found");
            }
            if (opsHostEntities.size() != hostLen) {
                String errMsg = "Cannot select the same host as the installation target";
                log.info(errMsg);
                throw new OpsException(errMsg);
            }
            List<OpsHostUserEntity> hostUserEntities = hostUserFacade.listHostUserByHostIdList(hostIdList);
            List<HostInfoHolder> hostInfoHolderList = opsHostEntities.stream()
                .map(host -> new HostInfoHolder(host, hostUserEntities.stream()
                    .filter(hostUser -> hostUser.getHostId().equals(host.getHostId()))
                    .collect(Collectors.toList())))
                .collect(Collectors.toList());
            if (!Objects.equals(OpenGaussVersionEnum.MINIMAL_LIST, openGaussVersion)) {
                for (HostInfoHolder hostInfoHolder : hostInfoHolderList) {
                    List<OpsHostUserEntity> userEntities = hostInfoHolder.getHostUserEntities();
                    OpsHostUserEntity rootUserEntity = userEntities.stream()
                        .filter(userEntity -> "root".equals(userEntity.getUsername()))
                        .findFirst()
                        .orElseThrow(() -> new OpsException(
                            "Host not found[" + hostInfoHolder.getHostEntity().getPublicIp()
                                + "]root user information"));
                    if (StrUtil.isEmpty(rootUserEntity.getPassword())) {
                        if (StrUtil.isNotEmpty(hostPasswdMap.get(rootUserEntity.getHostId()))) {
                            rootUserEntity.setPassword(hostPasswdMap.get(rootUserEntity.getHostId()));
                        } else {
                            throw new OpsException("root password does not exist");
                        }
                    }
                }
            }
            installContext.setHostInfoHolders(hostInfoHolderList);
        } else {
            throw new OpsException("Node host configuration information cannot be empty");
        }
    }

    private void checkInstallConfig(InstallContext installContext) {
        String clusterId = installContext.getClusterId();
        clusterIdUniqueCheck(clusterId);
        installContext.checkConfig();
    }

    private void clusterIdUniqueCheck(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            throw new OpsException("Cluster ID error");
        }
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.nonNull(clusterEntity)) {
            throw new OpsException("Cluster ID already exists");
        }
    }

    private void doInstall(InstallContext installContext) {
        try {
            clusterOpsProviderManager.provider(installContext.getOpenGaussVersion())
                .orElseThrow(() -> new OpsException("The current version does not support"))
                .install(installContext);
            wsUtil.sendText(installContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("Installation exception：", e);
            wsUtil.sendText(installContext.getRetSession(), e.getMessage());
            wsUtil.sendText(installContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    @Override
    public void ssh(SSHBody sshBody) {
        OpsHostEntity opsHostEntity = hostFacade.getById(sshBody.getHostId());
        if (Objects.isNull(opsHostEntity)) {
            throw new OpsException("host information does not exist");
        }
        if (StrUtil.isEmpty(sshBody.getSshUsername())) {
            sshBody.setSshUsername(CommonConstants.ROOT_USER);
        }
        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(opsHostEntity.getHostId());
        OpsHostUserEntity sshUserEntity = hostUserList.stream()
            .filter(opsHostUserEntity -> StrUtil.equalsIgnoreCase(sshBody.getSshUsername(),
                opsHostUserEntity.getUsername()))
            .findFirst()
            .orElseThrow(() -> new OpsException(
                "[" + opsHostEntity.getHostname() + " user" + sshBody.getSshUsername() + "] user not found"));
        OpsAssert.isTrue(StrUtil.isNotEmpty(sshUserEntity.getPassword()), "user password not found");
        WsSession wsSession = wsConnectorManager.getSession(sshBody.getBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        ChannelShell channelShell = sshChannelManager.initChannelShell(sshBody, opsHostEntity, sshUserEntity)
            .orElseThrow(() -> new OpsException("Connection establishment failed"));
        Future<?> future = threadPoolTaskExecutor.submit(() -> jschUtil.channelToWsSession(channelShell, wsSession));
        TaskManager.registry(sshBody.getBusinessId(), future);
    }

    @Override
    public List<HostFile> ls(String hostId, String path) {
        OpsHostEntity opsHostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(opsHostEntity)) {
            throw new OpsException("host information does not exist");
        }
        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(opsHostEntity.getHostId());
        OpsHostUserEntity installUserEntity = hostUserList.stream()
            .filter(opsHostUserEntity -> !"root".equalsIgnoreCase(opsHostUserEntity.getUsername()))
            .findFirst()
            .orElseThrow(() -> new OpsException("[" + opsHostEntity.getHostname() + "] user not found"));
        Session session = jschUtil.getSession(opsHostEntity.getPublicIp(), opsHostEntity.getPort(),
                installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword()))
            .orElseThrow(() -> new OpsException("user failed to establish connection"));
        List<HostFile> ls = jschUtil.ls(session, path);
        if (Objects.nonNull(session) && session.isConnected()) {
            session.disconnect();
        }
        return ls;
    }

    @Override
    public OpsNodeLogVO logPath(String clusterId, String hostId) {
        OpsNodeLogVO opsNodeLogVO = new OpsNodeLogVO();
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("The current cluster node was not found on the host");
        }
        OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream()
            .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId))
            .findFirst()
            .orElseThrow(() -> new OpsException("The current cluster node was not found on the host"));
        String installUserId = clusterNode.getInstallUserId();
        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }
        if (OpenGaussVersionEnum.ENTERPRISE == clusterEntity.getVersion()) {
            opsNodeLogVO.setSystemLogPath(clusterEntity.getLogPath());
            opsNodeLogVO.setDumpLogPath(clusterEntity.getCorePath());
            Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(),
                hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword())).orElse(null);
            try {
                String command = "echo $GAUSSLOG";
                JschResult jschResult = jschUtil.executeCommand(command, session);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to get log path, exit code: {}, log: {}", jschResult.getExitCode(),
                        jschResult.getResult());
                    throw new OpsException("Failed to get log path");
                }
                String result = jschResult.getResult();
                if (StrUtil.isNotEmpty(result)) {
                    opsNodeLogVO.setOpLogPath(result);
                    opsNodeLogVO.setPerformanceLogPath(result + "/gs_profile");
                }
            } catch (Exception e) {
                log.error("Failed to get operation log path", e);
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }
        } else {
            String dataPath = clusterNode.getDataPath();
            if (OpenGaussVersionEnum.MINIMAL_LIST == clusterEntity.getVersion()) {
                if (DeployTypeEnum.CLUSTER == clusterEntity.getDeployType()) {
                    ClusterRoleEnum clusterRole = clusterNode.getClusterRole();
                    if (clusterRole == ClusterRoleEnum.MASTER) {
                        dataPath = dataPath + "/master";
                    } else {
                        dataPath = dataPath + "/slave";
                    }
                } else {
                    dataPath = dataPath + "/single_node";
                }
            }
            opsNodeLogVO.setSystemLogPath(dataPath + "/pg_log");
            Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(),
                hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword())).orElse(null);
            try {
                String command = "echo $GAUSSLOG";
                JschResult jschResult = jschUtil.executeCommand(command, session);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to get log path, exit code: {}, log: {}", jschResult.getExitCode(),
                        jschResult.getResult());
                    throw new OpsException("Failed to get log path");
                }
                String result = jschResult.getResult();
                if (StrUtil.isNotEmpty(result)) {
                    opsNodeLogVO.setOpLogPath(result);
                    opsNodeLogVO.setPerformanceLogPath(result);
                    opsNodeLogVO.setDumpLogPath(result);
                }
            } catch (Exception e) {
                log.error("Failed to get operation log path", e);
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }
        }
        return opsNodeLogVO;
    }

    @Override
    public List<AuditLogVO> auditLog(Page page, String clusterId, String hostId, String start, String end) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("The current cluster node was not found on the host");
        }
        OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream()
            .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId))
            .findFirst()
            .orElseThrow(() -> new OpsException("The current cluster node was not found on the host"));
        String installUserId = clusterNode.getInstallUserId();
        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }
        Connection connection = null;
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(),
                    clusterEntity.getDatabaseUsername(), encryptionUtils.decrypt(clusterEntity.getDatabasePassword()))
                .orElseThrow(() -> new OpsException("Failed to establish connection to database"));
            return querySession(connection, start, end);
        } catch (Exception e) {
            log.error("get connection fail");
        } finally {
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<SessionVO> listSession(String clusterId, String hostId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("The current cluster node was not found on the host");
        }
        OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream()
            .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId))
            .findFirst()
            .orElseThrow(() -> new OpsException("The current cluster node was not found on the host"));
        String installUserId = clusterNode.getInstallUserId();
        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }
        Connection connection = null;
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(),
                    clusterEntity.getDatabaseUsername(), encryptionUtils.decrypt(clusterEntity.getDatabasePassword()))
                .orElseThrow(() -> new OpsException("Failed to establish connection to database"));
            return querySession(connection);
        } catch (Exception e) {
            log.error("get connection fail");
        } finally {
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        return Collections.emptyList();
    }

    private List<SessionVO> querySession(Connection connection) {
        List<SessionVO> res = new ArrayList<>();
        String sql = "SELECT * FROM pg_stat_activity";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                SessionVO sessionVO = new SessionVO();
                sessionVO.setDatid(resultSet.getString("datid"));
                sessionVO.setDatname(resultSet.getString("datname"));
                sessionVO.setPid(resultSet.getString("pid"));
                sessionVO.setSessionid(resultSet.getString("sessionid"));
                sessionVO.setUsesysid(resultSet.getString("usesysid"));
                sessionVO.setUsename(resultSet.getString("usename"));
                sessionVO.setApplication_name(resultSet.getString("application_name"));
                sessionVO.setClient_addr(resultSet.getString("client_addr"));
                sessionVO.setClient_hostname(resultSet.getString("client_hostname"));
                sessionVO.setClient_port(resultSet.getString("client_port"));
                sessionVO.setBackend_start(resultSet.getString("backend_start"));
                sessionVO.setXact_start(resultSet.getString("xact_start"));
                sessionVO.setQuery_start(resultSet.getString("query_start"));
                sessionVO.setState_change(resultSet.getString("state_change"));
                sessionVO.setWaiting(resultSet.getString("waiting"));
                sessionVO.setEnqueue(resultSet.getString("enqueue"));
                sessionVO.setState(resultSet.getString("state"));
                sessionVO.setResource_pool(resultSet.getString("resource_pool"));
                sessionVO.setQuery_id(resultSet.getString("query_id"));
                sessionVO.setQuery(resultSet.getString("query"));
                sessionVO.setConnection_info(resultSet.getString("connection_info"));
                sessionVO.setUnique_sql_id(resultSet.getString("unique_sql_id"));
                sessionVO.setTrace_id(resultSet.getString("trace_id"));
                res.add(sessionVO);
            }
        } catch (Exception e) {
            log.error("Querying the Audit Log Exception", e);
        }
        return res;
    }

    @Override
    public List<SlowSqlVO> slowSql(Page page, String clusterId, String hostId, String start, String end) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("The current cluster node was not found on the host");
        }
        OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream()
            .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId))
            .findFirst()
            .orElseThrow(() -> new OpsException("The current cluster node was not found on the host"));
        String installUserId = clusterNode.getInstallUserId();
        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }
        Connection connection = null;
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(),
                    clusterEntity.getDatabaseUsername(), encryptionUtils.decrypt(clusterEntity.getDatabasePassword()))
                .orElseThrow(() -> new OpsException("Failed to establish connection to database"));
            return querySlowSql(connection, start, end);
        } catch (Exception e) {
            log.error("get connection fail");
        } finally {
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        return Collections.emptyList();
    }

    private List<SlowSqlVO> querySlowSql(Connection connection, String start, String end) {
        List<SlowSqlVO> res = new ArrayList<>();
        String sql = "select * from DBE_PERF.get_global_full_sql_by_timestamp(?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, start);
            pstmt.setString(2, end);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    SlowSqlVO slowSqlVO = new SlowSqlVO();
                    slowSqlVO.setNode_name(resultSet.getString("node_name"));
                    slowSqlVO.setDb_name(resultSet.getString("db_name"));
                    slowSqlVO.setStart_time(resultSet.getString("start_time"));
                    slowSqlVO.setFinish_time(resultSet.getString("finish_time"));
                    slowSqlVO.setSlow_sql_threshold(resultSet.getString("slow_sql_threshold"));
                    slowSqlVO.setQuery(resultSet.getString("query"));
                    slowSqlVO.setQuery_plan(resultSet.getString("query_plan"));
                    res.add(slowSqlVO);
                }
            }
        } catch (Exception e) {
            log.error("Querying the Audit Log Exception", e);
        }
        return res;
    }

    private List<AuditLogVO> querySession(Connection connection, String start, String end) {
        List<AuditLogVO> res = new ArrayList<>();
        String sql = "select * from pg_query_audit(?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, start);
            pstmt.setString(2, end);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    AuditLogVO auditLogVO = new AuditLogVO();
                    auditLogVO.setTime(resultSet.getString("time"));
                    auditLogVO.setType(resultSet.getString("type"));
                    auditLogVO.setResult(resultSet.getString("result"));
                    auditLogVO.setUserid(resultSet.getString("userid"));
                    auditLogVO.setUsername(resultSet.getString("username"));
                    auditLogVO.setDatabase(resultSet.getString("database"));
                    auditLogVO.setClient_conninfo(resultSet.getString("client_conninfo"));
                    auditLogVO.setObject_name(resultSet.getString("object_name"));
                    auditLogVO.setDetail_info(resultSet.getString("detail_info"));
                    auditLogVO.setNode_name(resultSet.getString("node_name"));
                    auditLogVO.setThread_id(resultSet.getString("thread_id"));
                    auditLogVO.setLocal_port(resultSet.getString("local_port"));
                    auditLogVO.setRemote_port(resultSet.getString("remote_port"));
                    res.add(auditLogVO);
                }
            }
        } catch (SQLException e) {
            log.error("Querying the Audit Log Exception", e);
        }
        return res;
    }

    @Override
    public void download(String hostId, String path, String filename, HttpServletResponse response) {
        OpsHostEntity opsHostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(opsHostEntity)) {
            throw new OpsException("host information does not exist");
        }
        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(opsHostEntity.getHostId());
        OpsHostUserEntity installUser = hostUserList.stream()
            .filter(opsHostUserEntity -> !"root".equalsIgnoreCase(opsHostUserEntity.getUsername()))
            .findFirst()
            .orElseThrow(() -> new OpsException("[" + opsHostEntity.getHostname() + "] user not found"));
        Session session = jschUtil.getSession(opsHostEntity.getPublicIp(), opsHostEntity.getPort(),
                installUser.getUsername(), encryptionUtils.decrypt(installUser.getPassword()))
            .orElseThrow(() -> new OpsException("root user failed to establish connection"));
        jschUtil.download(session, path, filename, response);
        if (Objects.nonNull(session) && session.isConnected()) {
            session.disconnect();
        }
    }

    @Override
    public void uninstall(UnInstallBody unInstallBody) {
        UnInstallContext unInstallContext = new UnInstallContext();
        OpsClusterEntity clusterEntity = getById(unInstallBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("cluster does not exist");
        }
        unInstallContext.setOpsClusterEntity(clusterEntity);
        List<OpsClusterNodeEntity> opsClusterNodeEntityList = opsClusterNodeService.listClusterNodeByClusterId(
            unInstallBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntityList)) {
            throw new OpsException("Cluster node information does not exist");
        }
        unInstallContext.setOpsClusterNodeEntityList(opsClusterNodeEntityList);
        WsSession wsSession = wsConnectorManager.getSession(unInstallBody.getBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        unInstallContext.setRetSession(wsSession);
        constructUninstallContext(opsClusterNodeEntityList, clusterEntity, unInstallBody, unInstallContext);
    }

    private void constructUninstallContext(List<OpsClusterNodeEntity> opsClusterNodeEntityList,
        OpsClusterEntity clusterEntity, UnInstallBody unInstallBody, UnInstallContext unInstallContext) {
        try {
            List<String> hostIdList = collectClusterNodeHostIdList(opsClusterNodeEntityList);
            if (CollUtil.isEmpty(hostIdList)) {
                throw new OpsException("Node host configuration information cannot be empty");
            }
            List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdList);
            List<OpsHostUserEntity> hostUserEntities = hostUserFacade.listHostUserByHostIdList(hostIdList);
            List<HostInfoHolder> hostInfoHolderList = opsHostEntities.stream()
                .map(host -> new HostInfoHolder(host, hostUserEntities.stream()
                    .filter(hostUser -> hostUser.getHostId().equals(host.getHostId()))
                    .collect(Collectors.toList())))
                .collect(Collectors.toList());

            unInstallContext.setHostInfoHolders(hostInfoHolderList);
            unInstallContext.setOs(checkOS(unInstallContext.getHostInfoHolders(), false));
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                RequestContextHolder.setRequestAttributes(context);
                doUnInstall(unInstallContext, unInstallBody.getForce());
            });
            TaskManager.registry(unInstallBody.getBusinessId(), future);
        } catch (OpsException e) {
            if (Objects.nonNull(unInstallBody.getForce()) && unInstallBody.getForce()) {
                OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
                removeById(opsClusterEntity);
                opsClusterNodeService.removeByIds(opsClusterNodeEntityList);
            }
            log.error("Uninstall exception：", e);
            wsUtil.sendText(unInstallContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    @Override
    public void restart(OpsClusterBody restartBody) {
        OpsClusterContext opsClusterContext = new OpsClusterContext();
        opsClusterContext.setRole(restartBody.getRole());
        opsClusterContext.setOpNodeIds(restartBody.getNodeIds());
        OpsClusterEntity clusterEntity = getById(restartBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("cluster does not exist");
        }
        opsClusterContext.setOpsClusterEntity(clusterEntity);
        List<OpsClusterNodeEntity> opsClusterNodeEntityList = opsClusterNodeService.listClusterNodeByClusterId(
            restartBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntityList)) {
            throw new OpsException("Cluster node information does not exist");
        }
        opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntityList);
        if (StrUtil.isNotEmpty(restartBody.getBusinessId())) {
            WsSession wsSession = wsConnectorManager.getSession(restartBody.getBusinessId())
                .orElseThrow(() -> new OpsException("websocket session not exist"));
            opsClusterContext.setRetSession(wsSession);
        }
        List<String> hostIdList = collectClusterNodeHostIdList(opsClusterNodeEntityList);
        if (CollUtil.isNotEmpty(hostIdList)) {
            setClusterContextOfHostInfoHolders(hostIdList, opsClusterContext);
        } else {
            throw new OpsException("Node host configuration information cannot be empty");
        }
        opsClusterContext.setOs(checkOS(opsClusterContext.getHostInfoHolders(), false));
        if (Objects.nonNull(restartBody.getSync()) && restartBody.getSync()) {
            doRestart(opsClusterContext, true);
        } else {
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                RequestContextHolder.setRequestAttributes(context);
                doRestart(opsClusterContext, false);
            });
            TaskManager.registry(restartBody.getBusinessId(), future);
        }
    }

    private void setClusterContextOfHostInfoHolders(List<String> hostIdList, OpsClusterContext opsClusterContext) {
        List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdList);
        List<OpsHostUserEntity> hostUserEntities = hostUserFacade.listHostUserByHostIdList(hostIdList);
        List<HostInfoHolder> hostInfoHolderList = opsHostEntities.stream()
            .map(host -> new HostInfoHolder(host, hostUserEntities.stream()
                .filter(hostUser -> hostUser.getHostId().equals(host.getHostId()))
                .collect(Collectors.toList())))
            .collect(Collectors.toList());
        for (HostInfoHolder hostInfoHolder : hostInfoHolderList) {
            List<OpsHostUserEntity> userEntities = hostInfoHolder.getHostUserEntities();
            OpsHostUserEntity rootUserEntity = userEntities.stream()
                .filter(userEntity -> "root".equals(userEntity.getUsername()))
                .findFirst()
                .orElse(null);
            if (Objects.nonNull(rootUserEntity)) {
                userEntities.remove(rootUserEntity);
            }
        }
        opsClusterContext.setHostInfoHolders(hostInfoHolderList);
    }

    private static List<String> collectClusterNodeHostIdList(List<OpsClusterNodeEntity> opsClusterNodeEntityList) {
        return opsClusterNodeEntityList.stream().map(OpsClusterNodeEntity::getHostId).collect(Collectors.toList());
    }

    private void doRestart(OpsClusterContext opsClusterContext, boolean sync) {
        try {
            OpsClusterContext clone = opsClusterContext.clone();
            clusterOpsProviderManager.provider(clone.getOpsClusterEntity().getVersion())
                .orElseThrow(() -> new OpsException("The current version does not support"))
                .restart(clone);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
            }
        } catch (Exception e) {
            log.error("Cluster restart failed：", e);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
            } else {
                throw new OpsException("Cluster restart failed");
            }
        }
    }

    @Override
    public void start(OpsClusterBody startBody) {
        OpsClusterContext opsClusterContext = new OpsClusterContext();
        opsClusterContext.setRole(startBody.getRole());
        opsClusterContext.setOpNodeIds(startBody.getNodeIds());
        OpsClusterEntity clusterEntity = getById(startBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("cluster does not exist");
        }
        opsClusterContext.setOpsClusterEntity(clusterEntity);
        List<OpsClusterNodeEntity> opsClusterNodeEntityList = opsClusterNodeService.listClusterNodeByClusterId(
            startBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntityList)) {
            throw new OpsException("Cluster node information does not exist");
        }
        opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntityList);
        WsSession wsSession = wsConnectorManager.getSession(startBody.getBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        opsClusterContext.setRetSession(wsSession);
        List<String> hostIdList = collectClusterNodeHostIdList(opsClusterNodeEntityList);
        if (CollUtil.isNotEmpty(hostIdList)) {
            setClusterContextOfHostInfoHolders(hostIdList, opsClusterContext);
        } else {
            throw new OpsException("Node host configuration information cannot be empty");
        }
        opsClusterContext.setOs(checkOS(opsClusterContext.getHostInfoHolders(), false));
        if (Objects.nonNull(startBody.getSync()) && startBody.getSync()) {
            doStart(opsClusterContext, true);
        } else {
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                RequestContextHolder.setRequestAttributes(context);
                doStart(opsClusterContext, false);
            });
            TaskManager.registry(startBody.getBusinessId(), future);
        }
    }

    private void doStart(OpsClusterContext opsClusterContext, boolean sync) {
        try {
            OpsClusterContext clone = opsClusterContext.clone();
            clusterOpsProviderManager.provider(clone.getOpsClusterEntity().getVersion())
                .orElseThrow(() -> new OpsException("The current version does not support"))
                .start(clone);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
            }
        } catch (Exception e) {
            log.error("startup exception：", e);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
            } else {
                throw new OpsException(e.getMessage());
            }
        }
    }

    @Override
    public void stop(OpsClusterBody stopBody) {
        OpsClusterContext opsClusterContext = new OpsClusterContext();
        opsClusterContext.setRole(stopBody.getRole());
        opsClusterContext.setOpNodeIds(
            Objects.isNull(stopBody.getNodeIds()) ? new ArrayList<>() : stopBody.getNodeIds());
        OpsClusterEntity clusterEntity = getById(stopBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("cluster does not exist");
        }
        opsClusterContext.setOpsClusterEntity(clusterEntity);
        List<OpsClusterNodeEntity> opsClusterNodeEntityList = opsClusterNodeService.listClusterNodeByClusterId(
            stopBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntityList)) {
            throw new OpsException("Cluster node information does not exist");
        }
        opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntityList);
        WsSession wsSession = wsConnectorManager.getSession(stopBody.getBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        opsClusterContext.setRetSession(wsSession);
        List<String> hostIdList = collectClusterNodeHostIdList(opsClusterNodeEntityList);
        if (CollUtil.isNotEmpty(hostIdList)) {
            setClusterContextOfHostInfoHolders(hostIdList, opsClusterContext);
        } else {
            throw new OpsException("Node host configuration information cannot be empty");
        }
        opsClusterContext.setOs(checkOS(opsClusterContext.getHostInfoHolders(), false));
        if (Objects.nonNull(stopBody.getSync()) && stopBody.getSync()) {
            doStop(opsClusterContext, true);
        } else {
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                RequestContextHolder.setRequestAttributes(context);
                doStop(opsClusterContext, false);
            });
            TaskManager.registry(stopBody.getBusinessId(), future);
        }
    }

    private void doStop(OpsClusterContext opsClusterContext, boolean sync) {
        try {
            OpsClusterContext clone = opsClusterContext.clone();
            clusterOpsProviderManager.provider(clone.getOpsClusterEntity().getVersion())
                .orElseThrow(() -> new OpsException("The current version does not support"))
                .stop(clone);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
            }
        } catch (Exception e) {
            log.error("stop exception：", e);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
            } else {
                throw new OpsException(e.getMessage());
            }
        }
    }

    @Override
    public List<OpsClusterVO> listEnterpriseCluster() {
        List<OpsClusterVO> enterpriseClusterList = listAllEnterpriseClusterList();
        if (CollUtil.isEmpty(enterpriseClusterList)) {
            return enterpriseClusterList;
        }
        List<Future<?>> taskResult = new ArrayList<>();
        for (OpsClusterVO cluster : enterpriseClusterList) {
            OpsClusterEntity clusterEntity = getById(cluster.getClusterId());
            if (Objects.isNull(clusterEntity)) {
                throw new OpsException("Cluster information does not exist");
            }
            OpsClusterNodeEntity clusterNode = queryOpsClusterNodeEntity(cluster);
            String installUserId = clusterNode.getInstallUserId();
            OpsHostEntity hostEntity = hostFacade.getById(clusterNode.getHostId());
            if (Objects.isNull(hostEntity)) {
                throw new OpsException("Node host information does not exist");
            }
            OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
            if (Objects.isNull(hostUserEntity)) {
                throw new OpsException("Installation user information does not exist");
            }
            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(),
                    hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword()));
                String stateResultStr = state(sshLogin, OpenGaussVersionEnum.ENTERPRISE, clusterNode.getDataPath(),
                    clusterEntity.getEnvPath());
                Map<String, Object> stateResult = JSON.parseObject(stateResultStr, Map.class);
                cluster.setClusterState((String) stateResult.get("cluster_state"));
            });
            taskResult.add(future);
        }
        for (Future<?> future : taskResult) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                String errMsg = "get cluster state from task failed: " + e.getMessage();
                log.error(errMsg);
                throw new OpsException(errMsg);
            }
        }
        return enterpriseClusterList;
    }

    private OpsClusterNodeEntity queryOpsClusterNodeEntity(OpsClusterVO cluster) {
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(
            cluster.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("The current cluster node was not found on the host");
        }
        OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream()
            .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterRole().equals(ClusterRoleEnum.MASTER))
            .findFirst()
            .orElseThrow(() -> new OpsException("The master cluster node was not found in the cluster"));
        return clusterNode;
    }

    private List<OpsClusterVO> listAllEnterpriseClusterList() {
        List<OpsClusterVO> allClusterList = opsFacade.listCluster();
        if (CollUtil.isEmpty(allClusterList)) {
            return allClusterList;
        }
        return allClusterList.stream()
            .filter(item -> item.getVersion().equals(OpenGaussVersionEnum.ENTERPRISE.name()))
            .collect(Collectors.toList());
    }

    @Override
    public ClusterSummaryVO summary() {
        ClusterSummaryVO clusterSummaryVO = new ClusterSummaryVO();
        clusterSummaryVO.setClusterNum(count());
        clusterSummaryVO.setHostNum(hostFacade.count());
        clusterSummaryVO.setNodeNum(opsClusterNodeService.count());
        return clusterSummaryVO;
    }

    @Override
    public String monitor(String clusterId, String hostId, String businessId, ClusterRoleEnum role) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId))
            .findFirst()
            .orElseThrow(() -> new OpsException("Cluster node information does not exist"));
        String installUserId = nodeEntity.getInstallUserId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("Node host information does not exist");
        }
        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Node installation user information does not exist");
        }
        WsSession wsSession = wsConnectorManager.getSession(businessId)
            .orElseThrow(() -> new OpsException("response session does not exist"));
        String dataPath = nodeEntity.getDataPath();
        SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(),
            encryptionUtils.decrypt(hostUserEntity.getPassword()));
        Future<?> future = getMonitorFuture(hostEntity, clusterEntity, wsSession, sshLogin, dataPath);
        TaskManager.registry(businessId, future);
        return checkDependencies(sshLogin);
    }

    private Future<?> getMonitorFuture(OpsHostEntity hostEntity, OpsClusterEntity clusterEntity, WsSession wsSession,
        SshLogin sshLogin, String dataPath) {
        return threadPoolTaskExecutor.submit(() -> {
            Connection connection = null;
            try {
                connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(),
                        clusterEntity.getDatabaseUsername(),
                        encryptionUtils.decrypt(clusterEntity.getDatabasePassword()))
                    .orElseThrow(() -> new OpsException("Unable to connect to the database"));
                MonitorPluginParam monitorParam = new MonitorPluginParam(hostEntity.getHostId(), dataPath,
                    clusterEntity.getVersion(), clusterEntity.getEnvPath());
                doMonitor(wsSession, sshLogin, monitorParam, connection);
            } catch (Exception e) {
                log.error("get connection fail , ip:{} , port:{}, username:{}", hostEntity.getPublicIp(),
                    clusterEntity.getPort(), clusterEntity.getDatabaseUsername(), e);
            } finally {
                if (Objects.nonNull(connection)) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        log.error("close connection occurred an error, error message:{}", e.getMessage());
                    }
                }
                wsUtil.close(wsSession);
            }
        });
    }

    /**
     * MonitorPluginParam 类的功能说明
     * <p>
     * 该类用于存储监控插件参数。
     */
    @AllArgsConstructor
    @Data
    private class MonitorPluginParam {
        private String hostId;
        private String dataPath;
        private OpenGaussVersionEnum version;
        private String envPath;
    }

    @Override
    public List<OpsHostEntity> listClusterHost(String clusterId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }
        Set<String> hostIds = opsClusterNodeEntities.stream()
            .map(OpsClusterNodeEntity::getHostId)
            .collect(Collectors.toSet());
        return hostFacade.listByIds(hostIds);
    }

    @Override
    public boolean hasName(String name) {
        OpsClusterEntity clusterEntity = getById(name);
        return Objects.nonNull(clusterEntity);
    }

    @Override
    public void generateconf(String clusterId, String hostId, String businessId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getHostId().equals(hostId))
            .findFirst()
            .orElseThrow(() -> new OpsException("Cluster node information does not exist"));
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        OpsHostUserEntity installUserEntity = hostUserFacade.getById(nodeEntity.getInstallUserId());
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installing user information does not exist");
        }
        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(),
                installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword()))
            .orElseThrow(() -> new OpsException("Failed to establish connection with host"));
        WsSession retWsSession = wsConnectorManager.getSession(businessId)
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                RequestContextHolder.setRequestAttributes(context);
                doGenerateconf(session, retWsSession, clusterEntity.getXmlConfigPath(), clusterEntity.getEnvPath());
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }

        });
        TaskManager.registry(businessId, future);
    }

    @Override
    public void switchover(String clusterId, String hostId, String businessId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getHostId().equals(hostId))
            .findFirst()
            .orElseThrow(() -> new OpsException("Cluster node information does not exist"));
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        OpsHostUserEntity installUserEntity = hostUserFacade.getById(nodeEntity.getInstallUserId());
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installing user information does not exist");
        }
        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(),
                installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword()))
            .orElseThrow(() -> new OpsException("Failed to establish connection with host"));
        WsSession retWsSession = wsConnectorManager.getSession(businessId)
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                RequestContextHolder.setRequestAttributes(context);
                doSwitchover(session, retWsSession, nodeEntity.getDataPath(), clusterEntity.getEnvPath());
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }

        });
        TaskManager.registry(businessId, future);
    }

    @Override
    public void build(String clusterId, String hostId, String businessId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getHostId().equals(hostId))
            .findFirst()
            .orElseThrow(() -> new OpsException("Cluster node information does not exist"));
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        OpsHostUserEntity installUserEntity = hostUserFacade.getById(nodeEntity.getInstallUserId());
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installing user information does not exist");
        }
        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(),
                installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword()))
            .orElseThrow(() -> new OpsException("Failed to establish connection with host"));
        WsSession retWsSession = wsConnectorManager.getSession(businessId)
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                RequestContextHolder.setRequestAttributes(context);
                doBuild(session, retWsSession, nodeEntity.getDataPath(), clusterEntity.getEnvPath());
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }

        });
        TaskManager.registry(businessId, future);
    }

    private void doBuild(Session session, WsSession retWsSession, String dataPath, String envPath) {
        String command = "gs_ctl build -D " + dataPath;
        try {
            JschResult jschResult = jschUtil.executeCommand(command, envPath, session, retWsSession);
            if (0 != jschResult.getExitCode()) {
                log.error("A build error occurred, exit code {}", jschResult.getExitCode());
                throw new OpsException("A build error occurred");
            }
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("build fail", e);
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    @Override
    public ListDir listInstallPackage(OpenGaussVersionEnum openGaussVersionEnum, Integer userId) {
        SysSettingEntity entity = sysSettingFacade.getSysSetting(userId);
        Assert.notNull(entity, "Failed to get installation package path");
        ListDir listDir = new ListDir();
        listDir.setPath(entity.getUploadPath());
        String[] extension = null;
        String[] namePart = null;
        if (OpenGaussVersionEnum.ENTERPRISE == openGaussVersionEnum) {
            extension = new String[] {"-all.tar.gz"};
        } else if (OpenGaussVersionEnum.LITE == openGaussVersionEnum) {
            namePart = new String[] {"-Lite-"};
        } else if (OpenGaussVersionEnum.MINIMAL_LIST == openGaussVersionEnum) {
            extension = new String[] {".tar.bz2"};
        } else {
            extension = new String[] {".tar.gz", ".tar.bz2"};
        }
        listDir.setFiles(listFiles(entity.getUploadPath(), openGaussVersionEnum, extension, namePart));
        return listDir;
    }

    private boolean nameFilter(File file, String[] namePart) {
        if (namePart == null || namePart.length < 1) {
            return true;
        }
        String name = file.getName();
        for (String s : namePart) {
            if (name.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean extensionFilter(File file, String[] extension) {
        if (extension == null || extension.length < 1) {
            return true;
        }
        String name = file.getName();
        for (String s : extension) {
            if (name.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private List<HostFile> listFiles(String uploadPath, OpenGaussVersionEnum openGaussVersionEnum, String[] extension,
        String[] namePart) {
        log.info("List the files under the path: {}", uploadPath);
        List<OpsPackageManagerEntity> pkgList = pkgManagerService.list();
        List<HostFile> result = new ArrayList<>();
        Assert.notEmpty(uploadPath, "Failed to enumerate files, path cannot be empty");
        File folderPath = new File(uploadPath);
        if (!folderPath.isDirectory()) {
            return result;
        }
        List<String> files = getAllFiles(uploadPath);
        if (files.size() <= 0) {
            return result;
        }
        for (String filePath : files) {
            File file = new File(filePath);
            if (!file.isFile() || !file.exists()) {
                continue;
            }
            OpsPackageManagerEntity entity = CollUtil.findOne(pkgList,
                item -> item.getName() != null && filePath.contains(item.getName()));
            // is managed, get info here
            if (entity != null && entity.getPackageVersion().equalsIgnoreCase(openGaussVersionEnum.toString())) {
                OpsPackageVO vo = entity.toVO();
                result.add(HostFile.build(file, vo));
            } else {
                // not managed, filter by name
                if (extensionFilter(file, extension) && nameFilter(file, namePart)) {
                    result.add(HostFile.build(file, new OpsPackageVO()));
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    private String getOrDefault(String path) {
        if (StrUtil.isNotEmpty(path)) {
            return path;
        }
        return System.getProperty("user.dir");
    }

    @Resource
    OpsClusterEnvService opsClusterEnvService;

    @Override
    public HostEnv env(String hostId, OpenGaussSupportOSEnum expectedOs, String rootPassword) {
        return opsClusterEnvService.env(hostId, expectedOs, rootPassword);
    }

    @Override
    public Map<String, Integer> threadPoolMonitor() {
        Map<String, Integer> res = new HashMap<>();
        int activeCount = threadPoolTaskExecutor.getActiveCount();
        int poolSize = threadPoolTaskExecutor.getPoolSize();
        int corePoolSize = threadPoolTaskExecutor.getCorePoolSize();
        int keepAliveSeconds = threadPoolTaskExecutor.getKeepAliveSeconds();
        res.put("activeCount", activeCount);
        res.put("poolSize", poolSize);
        res.put("corePoolSize", corePoolSize);
        res.put("keepAliveSeconds", keepAliveSeconds);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCluster(String clusterId) {
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isNotEmpty(opsClusterNodeEntities)) {
            opsClusterNodeService.removeBatchByIds(opsClusterNodeEntities.stream()
                .map(OpsClusterNodeEntity::getClusterNodeId)
                .collect(Collectors.toSet()));
        }
        removeById(clusterId);
    }

    private void doSwitchover(Session session, WsSession retWsSession, String dataPath, String envPath) {
        try {
            String command = "gs_ctl switchover -D " + dataPath;
            JschResult jschResult = jschUtil.executeCommand(command, envPath, session, retWsSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Active-standby switchover failed, exit code {}", jschResult.getExitCode());
                throw new OpsException("Active-standby switchover failed");
            }
        } catch (Exception e) {
            log.error("Active-standby switchover failed", e);
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
            return;
        }
        try {
            String command = "gs_om -t refreshconf";
            JschResult jschResult = jschUtil.executeCommand(command, envPath, session, retWsSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Active-standby switchover failed, exit code {}", jschResult.getExitCode());
                throw new OpsException("Active-standby switchover failed");
            }
        } catch (Exception e) {
            log.error("Active-standby switchover failed", e);
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
            return;
        }
        wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:0");
    }

    private void doGenerateconf(Session session, WsSession retWsSession, String xmlConfigPath, String envPath) {
        String command = "gs_om -t generateconf -X " + xmlConfigPath + " --distribute";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, envPath, session, retWsSession);
            if (0 != jschResult.getExitCode()) {
                log.error("A generateconf error occurred, exit code {}", jschResult.getExitCode());
                throw new OpsException("A generateconf error occurred");
            }
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("generateconf fail", e);
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    private String doCheck(OpsHostUserEntity rootUserEntity, Session session) {
        log.info("One-click self-test start");
        String command = "gs_check -e inspect";
        try {
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("Please enter root privileges user[root]:", "root");
            autoResponse.put("Please enter password for user[root]:", rootUserEntity.getPassword());
            JschResult jschResult = jschUtil.executeCommand(command, session, autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("A self-test error occurred, exit code {}", jschResult.getExitCode());
            } else {
                String result = jschResult.getResult();
                return result;

            }
        } catch (Exception e) {
            log.error("One-click self-test results", e);
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
            throw new OpsException("One-click self-test results");
        } finally {
            log.info("One-click self-test end");
        }
        return null;
    }

    private CheckVO parseCheckResToCheckVO(String result) {
        CheckVO checkVO = new CheckVO();
        // cluster
        CheckClusterVO checkClusterVO = new CheckClusterVO();
        checkClusterVO.setCheckClusterState(parseCheckItem(result, "CheckClusterState"));
        checkClusterVO.setCheckDBParams(parseCheckItem(result, "CheckDBParams"));
        checkClusterVO.setCheckDebugSwitch(parseCheckItem(result, "CheckDebugSwitch"));
        checkClusterVO.setCheckDirPermissions(parseCheckItem(result, "CheckDirPermissions"));
        checkClusterVO.setCheckEnvProfile(parseCheckItem(result, "CheckEnvProfile"));
        checkClusterVO.setCheckReadonlyMode(parseCheckItem(result, "CheckReadonlyMode"));
        checkClusterVO.setCheckDilateSysTab(parseCheckItem(result, "CheckDilateSysTab"));
        checkClusterVO.setCheckProStartTime(parseCheckItem(result, "CheckProStartTime"));
        checkClusterVO.setCheckMpprcFile(parseCheckItem(result, "CheckMpprcFile"));
        checkVO.setCluster(checkClusterVO);
        // database
        CheckDbVO checkDbVO = new CheckDbVO();
        checkDbVO.setCheckCurConnCount(parseCheckItem(result, "CheckCurConnCount"));
        checkDbVO.setCheckCursorNum(parseCheckItem(result, "CheckCursorNum"));
        checkDbVO.setCheckPgxcgroup(parseCheckItem(result, "CheckPgxcgroup"));
        checkDbVO.setCheckNodeGroupName(parseCheckItem(result, "CheckNodeGroupName"));
        checkDbVO.setCheckTableSpace(parseCheckItem(result, "CheckTableSpace"));
        checkDbVO.setCheckSysadminUser(parseCheckItem(result, "CheckSysadminUser"));
        checkDbVO.setCheckHashIndex(parseCheckItem(result, "CheckHashIndex"));
        checkDbVO.setCheckPgxcRedistb(parseCheckItem(result, "CheckPgxcRedistb"));
        checkDbVO.setCheckNodeGroupName(parseCheckItem(result, "CheckNodeGroupName"));
        checkDbVO.setCheckTDDate(parseCheckItem(result, "CheckTDDate"));
        checkVO.setDb(checkDbVO);
        // os
        CheckOSVO checkOSVO = new CheckOSVO();
        checkOSVO.setCheckEncoding(parseCheckItem(result, "CheckEncoding"));
        checkOSVO.setCheckFirewall(parseCheckItem(result, "CheckFirewall"));
        checkOSVO.setCheckKernelVer(parseCheckItem(result, "CheckKernelVer"));
        checkOSVO.setCheckMaxHandle(parseCheckItem(result, "CheckMaxHandle"));
        checkOSVO.setCheckNTPD(parseCheckItem(result, "CheckNTPD"));
        checkOSVO.setCheckOSVer(parseCheckItem(result, "CheckOSVer"));
        checkOSVO.setCheckSysParams(parseCheckItem(result, "CheckSysParams"));
        checkOSVO.setCheckTHP(parseCheckItem(result, "CheckTHP"));
        checkOSVO.setCheckTimeZone(parseCheckItem(result, "CheckTimeZone"));
        checkOSVO.setCheckCPU(parseCheckItem(result, "CheckCPU"));
        checkOSVO.setCheckSshdService(parseCheckItem(result, "CheckSshdService"));
        checkOSVO.setCheckSshdConfig(parseCheckItem(result, "CheckSshdConfig"));
        checkOSVO.setCheckCrondService(parseCheckItem(result, "CheckCrondService"));
        checkOSVO.setCheckStack(parseCheckItem(result, "CheckStack"));
        checkOSVO.setCheckSysPortRange(parseCheckItem(result, "CheckSysPortRange"));
        checkOSVO.setCheckMemInfo(parseCheckItem(result, "CheckMemInfo"));
        checkOSVO.setCheckHyperThread(parseCheckItem(result, "CheckHyperThread"));
        checkOSVO.setCheckMaxProcMemory(parseCheckItem(result, "CheckMaxProcMemory"));
        checkOSVO.setCheckBootItems(parseCheckItem(result, "CheckBootItems"));
        checkOSVO.setCheckKeyProAdj(parseCheckItem(result, "CheckKeyProAdj"));
        checkOSVO.setCheckFilehandle(parseCheckItem(result, "CheckFilehandle"));
        checkOSVO.setCheckDropCache(parseCheckItem(result, "CheckDropCache"));
        checkVO.setOs(checkOSVO);
        // device
        CheckDeviceVO checkDeviceVO = new CheckDeviceVO();
        checkDeviceVO.setCheckBlockdev(parseCheckItem(result, "CheckBlockdev"));
        checkDeviceVO.setCheckDiskFormat(parseCheckItem(result, "CheckDiskFormat"));
        checkDeviceVO.setCheckSpaceUsage(parseCheckItem(result, "CheckSpaceUsage"));
        checkDeviceVO.setCheckInodeUsage(parseCheckItem(result, "CheckInodeUsage"));
        checkDeviceVO.setCheckSwapMemory(parseCheckItem(result, "CheckSwapMemory"));
        checkDeviceVO.setCheckLogicalBlock(parseCheckItem(result, "CheckLogicalBlock"));
        checkDeviceVO.setCheckIOrequestqueue(parseCheckItem(result, "CheckIOrequestqueue"));
        checkDeviceVO.setCheckMaxAsyIOrequests(parseCheckItem(result, "CheckMaxAsyIOrequests"));
        checkDeviceVO.setCheckIOConfigure(parseCheckItem(result, "CheckIOConfigure"));
        checkVO.setDevice(checkDeviceVO);
        // network
        CheckNetworkVO checkNetworkVO = new CheckNetworkVO();
        checkNetworkVO.setCheckMTU(parseCheckItem(result, "CheckMTU"));
        checkNetworkVO.setCheckPing(parseCheckItem(result, "CheckPing"));
        checkNetworkVO.setCheckRXTX(parseCheckItem(result, "CheckRXTX"));
        checkNetworkVO.setCheckNetWorkDrop(parseCheckItem(result, "CheckNetWorkDrop"));
        checkNetworkVO.setCheckMultiQueue(parseCheckItem(result, "CheckMultiQueue"));
        checkNetworkVO.setCheckRouting(parseCheckItem(result, "CheckRouting"));
        checkNetworkVO.setCheckNICModel(parseCheckItem(result, "CheckNICModel"));
        checkVO.setNetwork(checkNetworkVO);
        return checkVO;
    }

    private CheckItemVO parseCheckItem(String result, String item) {
        item = item + ".";
        CheckItemVO checkItemVO = new CheckItemVO();
        checkItemVO.setName(item);
        int indexStart = result.indexOf(item);
        int reIndex = result.indexOf("\n", indexStart);
        int indexEnd = result.indexOf(".............", reIndex);
        if ("CheckMpprcFile.".equals(item)) {
            indexEnd = result.indexOf("Analysis the check result", reIndex);
        }
        if (indexEnd <= indexStart) {
            log.error("index end le index start");
            return checkItemVO;
        }
        String substring = result.substring(indexStart, indexEnd);
        if (StrUtil.isNotEmpty(substring)) {
            String[] split = substring.split("\n");
            if (split.length < 2) {
                return checkItemVO;
            }
            StringBuffer msg = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                String line = split[i];
                if (i == 0) {
                    String status = line.substring(line.lastIndexOf(".") + 1, line.indexOf("\r"));
                    checkItemVO.setStatus(status);
                } else {
                    if (StrUtil.isNotEmpty(line.trim())) {
                        msg.append(line).append("\n");
                    } else {
                        break;
                    }
                }
            }
            checkItemVO.setMsg(msg.toString());
        }
        return checkItemVO;
    }

    private void doMonitor(WsSession wsSession, SshLogin sshLogin, MonitorPluginParam monitorParam,
        Connection connection) {
        AtomicBoolean hasError = new AtomicBoolean(false);
        while (wsSession.getSession().isOpen() && !hasError.get()) {
            NodeMonitorVO nodeMonitorVO = new NodeMonitorVO();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setTime(System.currentTimeMillis());
                    nodeMonitorVO.setCpu(cpu(monitorParam.getHostId()));
                    nodeMonitorVO.setMemory(memory(monitorParam.getHostId()));
                    nodeMonitorVO.setNet(net(monitorParam.getHostId()));
                    nodeMonitorVO.setState(
                        state(sshLogin, monitorParam.version, monitorParam.dataPath, monitorParam.envPath));
                    nodeMonitorVO.setLock(lock(connection));
                    nodeMonitorVO.setSession(session(connection));
                    nodeMonitorVO.setConnectNum(connectNum(connection));
                    nodeMonitorVO.setSessionMemoryTop10(sessionMemoryTop10(connection));
                    nodeMonitorVO.setKernel(kernel(monitorParam.getHostId()));
                    nodeMonitorVO.setMemorySize(memorySize(monitorParam.getHostId()));
                } catch (Exception e) {
                    log.error("cpu monitor error : {}", e.getMessage());
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("waiting for thread to be interrupted {}", e.getMessage());
            }
            wsUtil.sendText(wsSession, JSON.toJSONString(nodeMonitorVO));
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                log.error("waiting for monitor thread to be interrupted {}", e.getMessage());
            }
        }
    }

    private String checkDependencies(SshLogin sshLogin) {
        String dependencies = jschExecutorFacade.execCommand(sshLogin, SshCommandConstants.MONITOR_DEPENDENCY);
        StringBuilder missDependency = new StringBuilder();
        for (String dependency : MONITOR_DEPENDENCY_LIST) {
            if (!dependencies.contains(dependency)) {
                missDependency.append(dependency).append(" ");
            }
        }
        return missDependency.toString();
    }

    private List<Map<String, String>> sessionMemoryTop10(Connection connection) {
        String sql = "SELECT * FROM pv_session_memory_detail() ORDER BY usedsize desc limit 10";
        List<Map<String, String>> res = new ArrayList<>();
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                HashMap<String, String> model = new HashMap<>();
                model.put(resultSet.getString("sessid"), resultSet.getString("usedsize"));
                res.add(model);
            }
        } catch (Exception e) {
            log.error("Failed to query session memory top10", e);
            throw new OpsException("Failed to query session memory top10");
        }
        return res;
    }

    private String connectNum(Connection connection) {
        String sql = "SELECT count(*) FROM (SELECT pg_stat_get_backend_idset() AS backendid) AS s";
        String res = null;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections", e);
            throw new OpsException("Failed to query the number of connections");
        }
        return res;
    }

    private String session(Connection connection) {
        String sql = "SELECT count(*) FROM pg_stat_activity";
        String res = null;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections", e);
            throw new OpsException("Failed to query the number of connections");
        }
        return res;
    }

    private String lock(Connection connection) {
        String sql = "SELECT count(*) FROM pg_locks";
        String res = null;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections", e);
            throw new OpsException("Failed to query the number of connections");
        }
        return res;
    }

    private String memorySize(String hostId) {
        return hostMonitorFacade.getMemoryTotal(hostId);
    }

    private String kernel(String hostId) {
        return hostMonitorFacade.getCpuCoreNum(hostId);
    }

    private String state(SshLogin sshLogin, OpenGaussVersionEnum version, String dataPath, String envPath) {
        if (OpenGaussVersionEnum.ENTERPRISE == version) {
            String command = "gs_om -t status --detail";
            try {
                Map<String, Object> res = new HashMap<>();
                String result = jschExecutorFacade.execCommand(sshLogin, command, envPath);
                parseCmStatus(result, res);
                parseClusterState(result, res);
                parseClusterDataNodeStateAndRole(result, res);
                return JSON.toJSONString(res);
            } catch (Exception e) {
                log.error("Failed to get status information", e);
                throw new OpsException("Failed to get status information");
            }
        } else {
            String command = "gs_ctl status -D " + dataPath;
            String result = jschExecutorFacade.execCommand(sshLogin, command, envPath);
            return result.contains("no server running") ? "false" : "true";
        }
    }

    private static void parseClusterDataNodeStateAndRole(String result, Map<String, Object> res) {
        Map<String, String> nodeState = new HashMap<>(1);
        Map<String, String> nodeRole = new HashMap<>(1);
        int datanodeStateIndex = result.indexOf("Datanode State");
        if (datanodeStateIndex >= 0) {
            int splitIndex = result.indexOf("------------------", datanodeStateIndex);
            String dataNodeStateStr = result.substring(splitIndex);
            String[] dataNode = dataNodeStateStr.split("\n");
            parseClusterDataNodeStateAndRole(dataNode, nodeState, nodeRole);
        }
        res.put("nodeState", nodeState);
        res.put("nodeRole", nodeRole);
    }

    private static void parseClusterDataNodeStateAndRole(String[] dataNode, Map<String, String> nodeState,
        Map<String, String> nodeRole) {
        for (String s : dataNode) {
            String[] s1 = s.replaceAll(" +", " ").split(" ");
            String state = "";
            if (s1.length >= 9) {
                for (int i = 8; i < s1.length; i++) {
                    state += (s1[i] + " ");
                }
                nodeState.put(s1[1], state.trim());
                nodeRole.put(s1[1], s1[7]);
            } else if (s1.length >= 8) {
                for (int i = 7; i < s1.length; i++) {
                    state += (s1[i] + " ");
                }
                nodeState.put(s1[1], state.trim());
                nodeRole.put(s1[1], s1[6]);
            }
        }
    }

    private static void parseClusterState(String result, Map<String, Object> res) {
        int clusterStateIndex = result.indexOf("cluster_state");
        String clusterState = null;
        if (clusterStateIndex >= 0) {
            int splitIndex = result.indexOf(":", clusterStateIndex);
            int lineEndIndex = result.indexOf("\n", clusterStateIndex);
            clusterState = result.substring(splitIndex + 1, lineEndIndex).trim();
            res.put("cluster_state", clusterState);
        }
    }

    private static void parseCmStatus(String result, Map<String, Object> res) {
        Map<String, String> cmState = new HashMap<>(1);
        int cmIndex = result.indexOf("CMServer State");
        if (cmIndex >= 0) {
            int splitIndex = result.indexOf("------------------", cmIndex);
            String dataNodeStateStr = result.substring(splitIndex);
            String[] dataNode = dataNodeStateStr.split("\n");
            for (String s : dataNode) {
                String[] s1 = s.replaceAll(" +", " ").split(" ");
                if (s1.length == 6) {
                    cmState.put(s1[1], s1[5].trim());
                }
            }
        }
        res.put("cmState", cmState);
    }

    private List<NodeNetMonitor> net(String hostId) {
        List<NodeNetMonitor> res = new ArrayList<>();
        boolean hasNetName = true;
        String[] split = hostMonitorFacade.getNetMonitor(hostId, hasNetName);
        NodeNetMonitor nodeNetMonitor = new NodeNetMonitor();
        nodeNetMonitor.setFace(StrUtil.trim(split[0]));
        nodeNetMonitor.setReceive(split[1]);
        nodeNetMonitor.setTransmit(split[2]);
        res.add(nodeNetMonitor);
        return res;
    }

    private String memory(String hostId) {
        return hostMonitorFacade.getMemoryUsing(hostId);
    }

    private String cpu(String hostId) {
        return hostMonitorFacade.getCpuUsing(hostId);
    }

    private void doUnInstall(UnInstallContext unInstallContext, Boolean force) {
        try {
            UnInstallContext clone = unInstallContext.clone();
            clusterOpsProviderManager.provider(clone.getOpsClusterEntity().getVersion())
                .orElseThrow(() -> new OpsException("The current version does not support"))
                .uninstall(clone);
            wsUtil.sendText(unInstallContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            if (Objects.nonNull(force) && force) {
                OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
                List<OpsClusterNodeEntity> opsClusterNodeEntityList = unInstallContext.getOpsClusterNodeEntityList();
                removeById(opsClusterEntity);
                opsClusterNodeService.removeByIds(opsClusterNodeEntityList);
            }
            log.error("Uninstall exception：", e);
            wsUtil.sendText(unInstallContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    private List<String> getAllFiles(String path) {
        List<String> fileList = new ArrayList<>();
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    fileList.addAll(getAllFiles(f.getPath()));
                } else {
                    try {
                        fileList.add(f.getCanonicalPath());
                    } catch (IOException ex) {
                        log.error("get file path error: " + ex.getMessage());
                    }
                }
            }
        }
        return fileList;
    }

    @Override
    public void batchConfigGucSetting(GucSettingDto gucSettingDto) {
        OpsClusterEntity clusterEntity = getById(gucSettingDto.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        OpsHostEntity hostEntity = hostFacade.getById(gucSettingDto.getHostId());
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(
            clusterEntity.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information is empty");
        }
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getHostId().equals(hostEntity.getHostId()))
            .findFirst()
            .orElseThrow(() -> new OpsException("Cluster node configuration not found"));
        String installUserId = nodeEntity.getInstallUserId();
        OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(userEntity)) {
            throw new OpsException("Installation user information does not exist");
        }
        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
                encryptionUtils.decrypt(userEntity.getPassword()))
            .orElseThrow(() -> new OpsException("Failed to establish session with host"));
        boolean needRestart = false;
        for (GucSettingVO settingVO : gucSettingDto.getSettings()) {
            if (!settingVO.getHasChanged()) {
                continue;
            }
            if (settingVO.getContext().equals(GucSettingContextEnum.POSTMASTER.getCode())) {
                needRestart = true;
            }
            String dataPath = gucSettingDto.getDataPath();
            if (clusterEntity.getVersion().equals(OpenGaussVersionEnum.MINIMAL_LIST)) {
                dataPath = opsClusterNodeEntities.stream()
                    .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
                    .findFirst()
                    .orElseThrow(() -> new OpsException("Master node configuration not found"))
                    .getDataPath();
                if (clusterEntity.getDeployType() == DeployTypeEnum.CLUSTER) {
                    dataPath = dataPath + "/master";
                } else {
                    dataPath = dataPath;
                }
            }
            clusterOpsProviderManager.provider(clusterEntity.getVersion())
                .orElseThrow(() -> new OpsException("The current version does not support"))
                .configGucSetting(jschUtil, session, clusterEntity, gucSettingDto.getIsApplyToAllNode(), dataPath,
                    settingVO);
        }
        if (needRestart) {
            OpsClusterBody clusterBody = new OpsClusterBody();
            clusterBody.setClusterId(clusterEntity.getClusterId());
            clusterBody.setSync(true);
            List<String> nodeIds = new ArrayList<>();
            opsClusterNodeEntities.forEach((item) -> {
                nodeIds.add(item.getClusterNodeId());
            });
            clusterBody.setNodeIds(nodeIds);
            try {
                restart(clusterBody);
            } catch (OpsException ex) {
                String errMsg = "Restart cluster failed: " + ex.getMessage();
                log.error(errMsg);
                throw new OpsException(errMsg);
            }
        }
    }

    @Override
    public List<GucSettingVO> getGucSettingList(String clusterId, String hostId) throws OpsException {
        Connection connection = null;
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(),
                    clusterEntity.getDatabaseUsername(), encryptionUtils.decrypt(clusterEntity.getDatabasePassword()))
                .orElseThrow(() -> new OpsException("Unable to connect to the database"));
            return queryGucSettingList(connection);
        } catch (SQLException | ClassNotFoundException e) {
            String errMsg = "get connection fail: " + e.getMessage();
            log.error(errMsg);
            throw new OpsException(errMsg);
        } finally {
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    log.error("close connection failed: " + ex.getMessage());
                }
            }
        }
    }

    private List<GucSettingVO> queryGucSettingList(Connection connection) throws OpsException {
        List<GucSettingVO> res = new ArrayList<>();
        String sql = "select * from pg_settings";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                GucSettingVO gucSettingVO = new GucSettingVO();
                gucSettingVO.setName(resultSet.getString("name"));
                gucSettingVO.setValue(resultSet.getString("setting"));
                gucSettingVO.setVarType(resultSet.getString("vartype"));
                gucSettingVO.setUnit(resultSet.getString("unit"));
                gucSettingVO.setMinVal(resultSet.getString("min_val"));
                gucSettingVO.setMaxVal(resultSet.getString("max_val"));
                gucSettingVO.setShortDesc(resultSet.getString("short_desc"));
                gucSettingVO.setExtraDesc(resultSet.getString("extra_desc"));
                gucSettingVO.setEnumVals(resultSet.getString("enumvals"));
                gucSettingVO.setDefaultVal(resultSet.getString("boot_val"));
                gucSettingVO.setContext(resultSet.getString("context"));
                res.add(gucSettingVO);
            }
        } catch (SQLException ex) {
            String errMsg = "Querying guc setting failed: " + ex.getMessage();
            log.error(errMsg);
            throw new OpsException(errMsg);
        }
        return res;
    }

    private Integer calcDiskUsed(String diskInfo, String path) {
        Integer res = 0;
        String[] split = diskInfo.split("\n");
        for (String s : split) {
            String[] s1 = s.replaceAll("\\s+", " ").split(" ");
            if ("/".equals(s1[5])) {
                return Integer.parseInt(s1[4].substring(0, s1[4].length() - 1));
            }
            if (path.contains(s1[5])) {
                return Integer.parseInt(s1[4].substring(0, s1[4].length() - 1));
            }
        }
        if (res == 0) {
            for (String s : split) {
                String[] s1 = s.replaceAll("\\s+", " ").split(" ");
                int used = Integer.parseInt(s1[4].substring(0, s1[4].length() - 1));
                if (used > res) {
                    res = used;
                }
            }
        }
        return res;
    }
}
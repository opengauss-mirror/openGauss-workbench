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
 * TaskLiteProvider.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/provider/TaskLiteProvider.java
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
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.node.LiteInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cluster Installation Service Provider Specification
 *
 * @author wangchao
 * @since 2024/6/22 9:41
 **/
@Slf4j
@Service
public class TaskLiteProvider extends AbstractTaskProvider {
    private static final String LITE_SINGLE_INSTALL = "echo {0} | sh {1}install.sh --mode single -D {2} -R {3}"
            + "  --start -C port={4}";
    private static final String LITE_MASTER_INSTALL = "echo {0} | sh {1}install.sh --mode primary -D {2} -R {3} "
            + "-C \"replconninfo1=''localhost={4} localport={5} remotehost={6} remoteport={7}''\" "
            + "-C port={8}  --start";
    private static final String LITE_SLAVE_INSTALL = "echo {0} | sh {1}install.sh --mode standby -D {2} -R {3} "
            + "-C \"replconninfo1=''localhost={4} localport={5} remotehost={6} remoteport={7}''\" "
            + "-C port={8}  --start";

    @Resource
    private IOpsClusterService opsClusterService;
    @Resource
    private IOpsClusterNodeService opsClusterNodeService;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

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
        sendOperateLog(installContext, "CREATE_REMOTE_USER");
        OpsClusterContext opsClusterContext = new OpsClusterContext();
        OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
        List<OpsClusterNodeEntity> clusterNodes = installContext.getLiteInstallConfig().toOpsClusterNodeEntityList();
        opsClusterContext.setOpsClusterEntity(opsClusterEntity);
        opsClusterContext.setOpsClusterNodeEntityList(clusterNodes);

        createRemoteUser(installContext, opsClusterContext);

        sendOperateLog(installContext, "SAVE_INSTALL_CONTEXT");
        saveContext(installContext);
        sendOperateLog(installContext, "FINISH");
    }

    private void createRemoteUser(InstallContext installContext, OpsClusterContext opsClusterContext) {
        RetBuffer retBuffer = installContext.getRetBuffer();
        opsClusterContext.setRetBuffer(retBuffer);
        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());

        LiteInstallConfig liteInstallConfig = installContext.getLiteInstallConfig();
        List<LiteInstallNodeConfig> nodeConfigList = liteInstallConfig.getNodeConfigList();
        String databasePassword = liteInstallConfig.getDatabasePassword();
        Integer port = liteInstallConfig.getPort();
        for (LiteInstallNodeConfig liteInstallNodeConfig : nodeConfigList) {
            String hostId = liteInstallNodeConfig.getHostId();
            String installUserId = liteInstallNodeConfig.getInstallUserId();
            String dataPath = liteInstallNodeConfig.getDataPath();

            Session session = createSessionWithUserId(installContext.getHostInfoHolders(), false, hostId, installUserId);
            String listenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath);
            listenerAddress = addCommandOfLoadEnvironmentVariable(listenerAddress, installContext.getEnvPath());
            opsHostRemoteService.executeCommand(listenerAddress, session, retBuffer, "modify listening address");


            String hba = MessageFormat.format(SshCommandConstants.HBA, dataPath);
            hba = addCommandOfLoadEnvironmentVariable(hba, installContext.getEnvPath());
            opsHostRemoteService.executeCommand(hba, session, retBuffer, "modify hba host");

            String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(port));
            try {
                Map<String, String> response = new HashMap<>();
                String createUser = MessageFormat.format(
                    "CREATE USER gaussdb WITH MONADMIN AUDITADMIN SYSADMIN" + " PASSWORD \"{0}\";\\q",
                    encryptionUtils.decrypt(databasePassword));
                response.put("openGauss=#", createUser);
                clientLoginOpenGauss = addCommandOfLoadEnvironmentVariable(clientLoginOpenGauss, installContext.getEnvPath());
                opsHostRemoteService.executeCommand(clientLoginOpenGauss, session, retBuffer, response);
            } catch (Exception e) {
                log.error("Failed to create user", e);
                throw new OpsException("Failed to create user");
            }
        }

        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());
        restart(opsClusterContext);
    }

    private void installSingleNode(InstallContext installContext) {
        boolean isInstallSucc = true;
        Session installUserSession = null;
        try {
            sendOperateLog(installContext, "START_SINGLE");
            log.info("Start installing Single Node Lite");
            LiteInstallNodeConfig installNodeConfig = installContext.getLiteInstallConfig().getNodeConfigList().get(0);

            String pkgPath = preparePath(installContext.getLiteInstallConfig().getInstallPackagePath());

            String hostId = installNodeConfig.getHostId();

            String installUserId = installNodeConfig.getInstallUserId();
            String installUserName;
            if (StrUtil.isEmpty(installUserId)) {
                sendOperateLog(installContext, "CREATE_INSTALL_USER");
                OpsHostUserEntity installUser = createInstallUser(installContext, hostId);
                installUserId = installUser.getHostUserId();
                installUserName = installUser.getUsername();
            } else {
                HostInfoHolder hostInfoHolder = getHostInfoHolder(installContext, hostId);
                OpsHostUserEntity installUser = getHostUserInfo(hostInfoHolder, installUserId);
                installUserName = installUser.getUsername();
            }

            if (StrUtil.isEmpty(installUserId)) {
                throw new OpsException("Installation user ID does not exist");
            }

            String dataPath = preparePath(installNodeConfig.getDataPath());
            installUserSession = beforeInstall(installContext, pkgPath, dataPath, pkgPath, hostId, installUserId,
                    installUserName, "-xvf");

            log.info("perform installation");
            sendOperateLog(installContext, "START_EXE_INSTALL_COMMAND");
            // install
            String databasePassword = installContext.getLiteInstallConfig().getDatabasePassword();
            Integer port = installContext.getLiteInstallConfig().getPort();
            String installPath = preparePath(installNodeConfig.getInstallPath());
            String command = MessageFormat.format(LITE_SINGLE_INSTALL, encryptionUtils.decrypt(databasePassword),
                pkgPath, dataPath, installPath, String.valueOf(port));
            command = wrapperLiteEnvSep(command, installContext.getEnvPath());
            opsHostRemoteService.executeCommand(command, installUserSession, installContext.getRetBuffer(),
                    "Lite single node installation");
            sendOperateLog(installContext, "END_EXE_INSTALL_COMMAND");
            log.info("The installation is complete");
        } catch (OpsException ex) {
            isInstallSucc = false;
            throw new OpsException(ex.getMessage());
        } finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()) {
                installUserSession.disconnect();
            }
            if (!isInstallSucc) {
                cleanResource(installContext);
            }
        }
    }

    private OpsHostUserEntity createInstallUser(InstallContext installContext, String hostId) {
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        OpsHostUserEntity hostUserEntity = hostUserFacade.getOmmUserByHostId(hostId);
        if (Objects.nonNull(hostUserEntity)) {
            return hostUserEntity;
        }
        OpsHostUserEntity hostRootUser = opsHostRemoteService.getHostRootUser(hostId);
        Session rootSession = opsHostRemoteService.createHostUserSession(hostEntity, hostRootUser);
        try {
            String result = opsHostRemoteService.executeCommand(SshCommandConstants.OMM_USER, rootSession,
                    installContext.getRetBuffer(), "query omm user");
            if (StrUtil.isNotEmpty(result) && "1".equals(result)) {
                throw new OpsException("Please enter the omm user login password");
            }

            String password = StrUtil.uuid();
            opsHostRemoteService.executeCommand(SshCommandConstants.CREATE_OMM_USER, rootSession,
                    installContext.getRetBuffer(), "create omm user");

            try {
                Map<String, String> autoResponse = new HashMap<>();
                autoResponse.put("password:", password);
                opsHostRemoteService.executeCommand(SshCommandConstants.CHANGE_OMM_PASSWORD, rootSession,
                        installContext.getRetBuffer(), autoResponse);
            } catch (OpsException e) {
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

            HostInfoHolder hostHolder = hostInfoHolders
                    .stream()
                    .filter(holder -> holder.getHostEntity().getHostId().equals(hostId))
                    .findFirst().orElseThrow(() -> new OpsException("host information not found"));
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
        boolean isInstallSucc = true;
        try {
            sendOperateLog(installContext, "START_CLUSTER");
            log.info("Start installing the Lite cluster");
            List<LiteInstallNodeConfig> installNodeConfigList = installContext
                    .getLiteInstallConfig()
                    .getNodeConfigList();

            LiteInstallNodeConfig masterNodeConfig = installNodeConfigList.stream()
                    .filter(nodeConfig -> Objects.equals(ClusterRoleEnum.MASTER, nodeConfig.getClusterRole()))
                    .findFirst().orElseThrow(() -> new OpsException("Master node configuration not found"));
            LiteInstallNodeConfig slaveNodeConfig = installNodeConfigList.stream()
                    .filter(nodeConfig -> Objects.equals(ClusterRoleEnum.SLAVE, nodeConfig.getClusterRole()))
                    .findFirst().orElseThrow(() -> new OpsException("Master node configuration not found"));

            installMaster(masterNodeConfig, slaveNodeConfig, installContext);
            installSlave(masterNodeConfig, slaveNodeConfig, installContext);

            log.info("Cluster installation is complete");
        } catch (OpsException ex) {
            isInstallSucc = false;
            throw new OpsException(ex.getMessage());
        } finally {
            if (!isInstallSucc) {
                cleanResource(installContext);
            }
        }
    }

    private void installSlave(LiteInstallNodeConfig masterNodeConfig, LiteInstallNodeConfig slaveNodeConfig,
                              InstallContext installContext) {
        sendOperateLog(installContext, "START_SLAVE");
        log.info("Start installing the standby node");
        String installPath = preparePath(slaveNodeConfig.getInstallPath());

        String dataPath = preparePath(slaveNodeConfig.getDataPath());
        String masterHostId = masterNodeConfig.getHostId();
        String slaveHostId = slaveNodeConfig.getHostId();
        String hostId = slaveNodeConfig.getHostId();
        String databasePassword = installContext.getLiteInstallConfig().getDatabasePassword();
        HostInfoHolder masterHostInfo = installContext.getHostInfoHolders()
                .stream()
                .filter(hostInfoHolder -> masterHostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                .findFirst()
                .orElseThrow(() -> new OpsException("Master node configuration not found"));
        HostInfoHolder slaveHostInfo = installContext.getHostInfoHolders()
                .stream()
                .filter(hostInfoHolder -> slaveHostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                .findFirst()
                .orElseThrow(() -> new OpsException("The host information of the standby node does not exist"));

        String installUserId = slaveNodeConfig.getInstallUserId();
        String installUserName = null;
        if (StrUtil.isEmpty(installUserId)) {
            sendOperateLog(installContext, "CREATE_INSTALL_USER");
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
        String pkgPath = preparePath(installContext.getLiteInstallConfig().getInstallPackagePath());
        Session installUserSession = beforeInstall(installContext, pkgPath, dataPath, pkgPath, slaveHostId,
                installUserId, installUserName, "-xvf");

        try {
            log.info("perform installation");
            // install
            String command = MessageFormat.format(LITE_SLAVE_INSTALL, databasePassword,
                    pkgPath, dataPath, installPath, slaveHostInfo.getHostEntity().getPrivateIp(),
                    String.valueOf(installContext.getLiteInstallConfig().getPort() + 1),
                    masterHostInfo.getHostEntity().getPrivateIp(),
                    String.valueOf(installContext.getLiteInstallConfig().getPort() + 1),
                    String.valueOf(installContext.getLiteInstallConfig().getPort()));
            command = wrapperLiteEnvSep(command, installContext.getEnvPath());
            sendOperateLog(installContext, "START_EXE_INSTALL_COMMAND");
            opsHostRemoteService.executeCommand(command, installUserSession, installContext.getRetBuffer(),
                    "light version" + ClusterRoleEnum.SLAVE + "Node installation ");
            sendOperateLog(installContext, "END_EXE_INSTALL_COMMAND");
            log.info("Standby node installation is complete");
        } finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()) {
                installUserSession.disconnect();
            }
        }
    }

    private void installMaster(LiteInstallNodeConfig masterNodeConfig, LiteInstallNodeConfig slaveNodeConfig,
                               InstallContext installContext) {
        sendOperateLog(installContext, "START_MASTER");
        log.info("Start installing the master node");
        String installPath = preparePath(masterNodeConfig.getInstallPath());
        String pkgPath = preparePath(installContext.getLiteInstallConfig().getInstallPackagePath());
        String dataPath = preparePath(masterNodeConfig.getDataPath());
        String masterHostId = masterNodeConfig.getHostId();
        String slaveHostId = slaveNodeConfig.getHostId();
        String hostId = masterNodeConfig.getHostId();
        String databasePassword = installContext.getLiteInstallConfig().getDatabasePassword();
        HostInfoHolder masterHostInfo = installContext.getHostInfoHolders()
                .stream()
                .filter(hostInfoHolder -> masterHostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                .findFirst()
                .orElseThrow(() -> new OpsException("Master node host information does not exist"));
        HostInfoHolder slaveHostInfo = installContext.getHostInfoHolders()
                .stream()
                .filter(hostInfoHolder -> slaveHostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                .findFirst()
                .orElseThrow(() -> new OpsException("The host information of the standby node does not exist"));

        String installUserId = masterNodeConfig.getInstallUserId();
        String installUserName = null;
        if (StrUtil.isEmpty(installUserId)) {
            installContext.getRetBuffer().sendText("CREATE_INSTALL_USER");
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

        Session installUserSession = beforeInstall(installContext, pkgPath, dataPath, pkgPath, masterHostId,
                installUserId, installUserName, "-xvf");
        try {
            log.info("perform installation");
            // install
            String command = MessageFormat.format(LITE_MASTER_INSTALL, databasePassword,
                    pkgPath, dataPath, installPath, masterHostInfo.getHostEntity().getPrivateIp(),
                    String.valueOf(installContext.getLiteInstallConfig().getPort() + 1),
                    slaveHostInfo.getHostEntity().getPrivateIp(),
                    String.valueOf(installContext.getLiteInstallConfig().getPort() + 1),
                    String.valueOf(installContext.getLiteInstallConfig().getPort()));
            command = wrapperLiteEnvSep(command, installContext.getEnvPath());
            installContext.getRetBuffer().sendText("START_EXE_INSTALL_COMMAND");
            opsHostRemoteService.executeCommand(command, installUserSession, installContext.getRetBuffer(),
                    "light version" + ClusterRoleEnum.MASTER + "Node installation");
            installContext.getRetBuffer().sendText("END_EXE_INSTALL_COMMAND");
            log.info("Master node installation is complete");
        } finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()) {
                installUserSession.disconnect();
            }
        }
    }

    private void saveContext(InstallContext installContext) {
        OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = installContext
                .getLiteInstallConfig()
                .toOpsClusterNodeEntityList();

        opsClusterService.save(opsClusterEntity);
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
        }
        opsClusterNodeService.saveBatch(opsClusterNodeEntities);
    }

    @Override
    public void restart(OpsClusterContext opsClusterContext) {
        log.info("Start restarting the lite version");
        List<String> restartNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(restartNodeIds)) {
            restartNodeIds = opsClusterContext.getOpsClusterNodeEntityList()
                    .stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
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
            startNodeIds = opsClusterContext.getOpsClusterNodeEntityList()
                    .stream()
                    .map(OpsClusterNodeEntity::getClusterNodeId)
                    .collect(Collectors.toList());
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
                    .stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(stopNodeIds)) {
            log.error("no node to stop");
        }

        for (String stopNodeId : stopNodeIds) {
            doStopNode(stopNodeId, opsClusterContext);
        }
    }


    private void doStopNode(String stopNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity stopNodeEntity = opsClusterContext.getOpsClusterNodeEntityList()
                .stream()
                .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(stopNodeId))
                .findFirst().orElse(null);
        if (Objects.isNull(stopNodeEntity)) {
            log.error("Stop node info not found: {}", stopNodeId);
        }

        RetBuffer retBuffer = opsClusterContext.getRetBuffer();
        String dataPath = stopNodeEntity.getDataPath();
        log.info("login stop user");
        Session restartUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false,
                stopNodeEntity.getHostId(), stopNodeEntity.getInstallUserId());
        String restartCommand = MessageFormat.format(SshCommandConstants.LITE_STOP, dataPath);
        String envPath = opsClusterContext.getOpsClusterEntity().getEnvPath();
        restartCommand = addCommandOfLoadEnvironmentVariable(restartCommand, envPath);
        opsHostRemoteService.executeCommand(restartCommand, restartUserSession, retBuffer, "stop cluster node");
    }

    private void doStartNode(String startNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity startNodeEntity = opsClusterContext.getOpsClusterNodeEntityList()
                .stream()
                .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(startNodeId))
                .findFirst().orElse(null);
        if (Objects.isNull(startNodeEntity)) {
            log.error("Boot node information not found:{}", startNodeId);
        }

        String dataPath = startNodeEntity.getDataPath();
        log.info("Login to start user");
        Session restartUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false,
                startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());
        String restartCommand = MessageFormat.format(SshCommandConstants.LITE_START, dataPath);
        String envPath = opsClusterContext.getOpsClusterEntity().getEnvPath();
        restartCommand = addCommandOfLoadEnvironmentVariable(restartCommand, envPath);
        opsHostRemoteService.executeCommand(restartCommand, restartUserSession, opsClusterContext.getRetBuffer(),
                "start cluster node");
    }

    private void doRestartNode(String restartNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity restartNodeEntity = opsClusterContext.getOpsClusterNodeEntityList()
                .stream()
                .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(restartNodeId))
                .findFirst().orElse(null);
        if (Objects.isNull(restartNodeEntity)) {
            log.error("No restart node information found:{}", restartNodeId);
        }
        log.info("login restart user");
        Session restartUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false,
                restartNodeEntity.getHostId(), restartNodeEntity.getInstallUserId());
        String restartCommand = MessageFormat.format(SshCommandConstants.LITE_RESTART, restartNodeEntity.getDataPath());
        String envPath = opsClusterContext.getOpsClusterEntity().getEnvPath();
        restartCommand = addCommandOfLoadEnvironmentVariable(restartCommand, envPath);
        opsHostRemoteService.executeCommand(restartCommand, restartUserSession, opsClusterContext.getRetBuffer(),
                "restart cluster node");
    }

    @Override
    protected String prepareCleanClusterDir(InstallContext installContext) {
        String delCmd = "";
        try {
            LiteInstallNodeConfig nodeConfig = getSingleInstallNodeConfig(installContext);
            String installPath = preparePath(nodeConfig.getInstallPath());
            String pkgPath = preparePath(installContext.getLiteInstallConfig().getInstallPackagePath());
            // remove intall path software
            log.info("install package path : {}", pkgPath);

            String delInstallPath = MessageFormat.format(SshCommandConstants.DEL_FILE, pkgPath + "*");
            log.info("delete install package path : {}", delInstallPath);

            delCmd = delInstallPath + " || echo \"delInstallPath failed\"; ";

            String delDataPath = MessageFormat.format(SshCommandConstants.DEL_FILE, installPath);

            delCmd += delDataPath + " || echo \"delDataPath failed\"; ";

            return delCmd;
        } catch (OpsException e) {
            log.error("delete cmd : {}", delCmd);
            return delCmd;
        }
    }

    private LiteInstallNodeConfig getSingleInstallNodeConfig(InstallContext installContext) {
        return installContext
                .getLiteInstallConfig()
                .getNodeConfigList()
                .stream()
                .filter(config -> Objects.equals(ClusterRoleEnum.MASTER, config.getClusterRole()))
                .findFirst()
                .orElseThrow(() -> new OpsException("Master node configuration not found"));
    }
}

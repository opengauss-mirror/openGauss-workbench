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
 * TaskMinimaListProvider.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/provider/TaskMinimaListProvider.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.provider;

import cn.hutool.core.util.StrUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;

/**
 * TaskMinimaListProvider
 *
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Slf4j
@Service
public class TaskMinimaListProvider extends AbstractTaskProvider {
    @Resource
    private IOpsClusterService opsClusterService;
    @Resource
    private IOpsClusterNodeService opsClusterNodeService;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    @Override
    public OpenGaussVersionEnum version() {
        return OpenGaussVersionEnum.MINIMAL_LIST;
    }

    @Override
    public void install(InstallContext installContext) {
        log.info("Start installing the minimalist version");
        RetBuffer retBuffer = installContext.getRetBuffer();

        MinimalistInstallNodeConfig nodeConfig = getSingleInstallNodeConfig(installContext);
        String hostId = nodeConfig.getHostId();
        String installPath = preparePath(nodeConfig.getInstallPath());
        String dataPath = preparePath(installPath + "data");
        String pkgPath = preparePath(installContext.getMinimalistInstallConfig().getInstallPackagePath());

        String installUserId = nodeConfig.getInstallUserId();
        String installUserName = null;
        if (StrUtil.isEmpty(installUserId)) {
            sendOperateLog(installContext, "CREATE_INSTALL_USER");
            OpsHostUserEntity installUser = createInstallUser(installContext, hostId);
            installUserId = installUser.getHostUserId();
            installUserName = installUser.getUsername();
        } else {
            String uid = installUserId;
            OpsHostUserEntity installUser = installContext.getHostInfoHolders()
                    .stream()
                    .filter(hostInfoHolder -> hostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("host information does not exist : " + hostId))
                    .getHostUserEntities()
                    .stream()
                    .filter(userInfo -> uid.equals(userInfo.getHostUserId()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("Installation user information does not exist :" + uid));
            installUserName = installUser.getUsername();
        }

        if (StrUtil.isEmpty(installUserId)) {
            throw new OpsException("Installation user ID does not exist :" + installUserId);
        }

        nodeConfig.setInstallUserId(installUserId);
        sendOperateLog(installContext, "BEFORE INSTALL");
        Session installUserSession = beforeInstall(installContext, installPath, dataPath, pkgPath, hostId, installUserId, installUserName, "-jxf");
        boolean isInstallSucc = true;
        try {
            log.info("perform installation");
            // install
            sendOperateLog(installContext, "INSTALL");
            doInstall(installUserSession, retBuffer, installPath, installContext, nodeConfig);

            OpsClusterContext opsClusterContext = new OpsClusterContext();
            OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
            List<OpsClusterNodeEntity> opsClusterNodeEntities = installContext.getMinimalistInstallConfig()
                .toOpsClusterNodeEntityList(opsClusterEntity);
            opsClusterContext.setOpsClusterEntity(opsClusterEntity);
            opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntities);

            sendOperateLog(installContext, "CREATE REMOTE USER");
            sendOperateLog(installContext, "CREATE_REMOTE_USER");
            createRemoteUser(installUserSession, retBuffer, installContext, dataPath, opsClusterContext);

            sendOperateLog(installContext, "SAVE CONTEXT");
            sendOperateLog(installContext, "SAVE_INSTALL_CONTEXT");

            saveContext(opsClusterEntity, opsClusterNodeEntities);
            sendOperateLog(installContext, "FINISH");
            log.info("The installation is complete");
        } catch (OpsException ex) {
            isInstallSucc = false;
            sendOperateLog(installContext, ex.getMessage());
            log.info("The installation is error ", ex);
            throw new OpsException("Installation failed " + ex.getMessage());
        } finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()) {
                installUserSession.disconnect();
            }
            if (!isInstallSucc) {
                cleanResource(installContext);
            }
        }
    }

    private void createRemoteUser(Session installUserSession, RetBuffer retBuffer, InstallContext installContext, String dataPath, OpsClusterContext opsClusterContext) {
        if (DeployTypeEnum.CLUSTER == installContext.getDeployType()) {
            String masterListenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath + "master");
            opsHostRemoteService.executeCommand(masterListenerAddress, installUserSession, retBuffer, "modify listening address");

            String masterHba = MessageFormat.format(SshCommandConstants.HBA, dataPath + "master");
            opsHostRemoteService.executeCommand(masterHba, installUserSession, retBuffer, "modify hba host");

            String slaveListenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath + "slave");
            opsHostRemoteService.executeCommand(slaveListenerAddress, installUserSession, retBuffer, "modify listening address");

            String slaveHba = MessageFormat.format(SshCommandConstants.HBA, dataPath + "slave");
            opsHostRemoteService.executeCommand(slaveHba, installUserSession, retBuffer, "modify hba host");

        } else {
            String listenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath + "single_node");
            opsHostRemoteService.executeCommand(listenerAddress, installUserSession, retBuffer, "modify listening address");

            String hba = MessageFormat.format(SshCommandConstants.HBA, dataPath + "single_node");
            opsHostRemoteService.executeCommand(hba, installUserSession, retBuffer, "modify hba host");
        }

        opsClusterContext.setRetBuffer(retBuffer);
        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());
        restart(opsClusterContext);

        try {
            String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(installContext.getMinimalistInstallConfig().getPort()));
            Map<String, String> response = new HashMap<>();
            String createUser = MessageFormat.format(
                "CREATE USER gaussdb WITH MONADMIN AUDITADMIN SYSADMIN PASSWORD \"{0}\";\\q",
                encryptionUtils.decrypt(installContext.getMinimalistInstallConfig().getDatabasePassword()));
            response.put("openGauss=#", createUser);
            opsHostRemoteService.executeCommand(clientLoginOpenGauss, installUserSession, retBuffer, response);
        } catch (Exception e) {
            log.error("Failed to create user", e);
            throw new OpsException("Failed to create user");
        }
    }

    private OpsHostUserEntity createInstallUser(InstallContext installContext, String hostId) {
        OpsHostUserEntity hostUserEntity = hostUserFacade.getOmmUserByHostId(hostId);
        if (Objects.nonNull(hostUserEntity)) {
            return hostUserEntity;
        }


        Session rootSession = createSessionWithUserId(installContext, true, hostId, null);
        try {
            String ommUserName = null;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {

                if (i == 0) {
                    ommUserName = "omm";
                } else {
                    ommUserName = "omm" + i;
                }
                String command = "cat /etc/passwd | awk -F \":\" \"{print $1}\"|grep " + ommUserName + " | wc -l";
                String result = opsHostRemoteService.executeCommand(command, rootSession, installContext.getRetBuffer(), "query omm user");
                log.info("user count {},{}", ommUserName, result);
                if (StrUtil.isNotEmpty(result) && 0 == Integer.parseInt(result)) {
                    log.info("ommUserName：{}", ommUserName);
                    break;
                }
            }

            String password = StrUtil.uuid();
            String createUserCommand = "useradd " + ommUserName;
            opsHostRemoteService.executeCommand(createUserCommand, rootSession, installContext.getRetBuffer(), "create omm user");

            try {
                String passwdCommand = "passwd " + ommUserName;
                Map<String, String> autoResponse = new HashMap<>();
                autoResponse.put("password:", password);
                autoResponse.put("password:", password);
                opsHostRemoteService.executeCommand(passwdCommand, rootSession, installContext.getRetBuffer(), autoResponse);
            } catch (OpsException e) {
                log.error("Failed to modify omm user password", e);
                throw new OpsException("Failed to modify omm user password");
            }

            hostUserEntity = new OpsHostUserEntity();
            hostUserEntity.setHostId(hostId);
            hostUserEntity.setUsername(ommUserName);
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


    @Override
    public void restart(OpsClusterContext opsClusterContext) {
        log.info("Start restarting the minimalist version");
        RetBuffer retBuffer = opsClusterContext.getRetBuffer();

        HostInfoHolder hostInfoHolder = opsClusterContext.getHostInfoHolders().get(0);
        if (Objects.isNull(hostInfoHolder)) {
            throw new OpsException("host information does not exist");
        }

        OpsClusterNodeEntity clusterNode = opsClusterContext.getOpsClusterNodeEntityList().get(0);
        if (Objects.isNull(clusterNode)) {
            throw new OpsException("Cluster node information does not exist");
        }

        log.info("login restart user");
        Session restartUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false, clusterNode.getHostId(), clusterNode.getInstallUserId());
        log.info("perform a restart");
        doRestart(restartUserSession, retBuffer, opsClusterContext);
        log.info("restart complete");
    }

    @Override
    public void start(OpsClusterContext opsClusterContext) {
        log.info("Start the minimalist version");
        RetBuffer retBuffer = opsClusterContext.getRetBuffer();

        HostInfoHolder hostInfoHolder = opsClusterContext.getHostInfoHolders().get(0);
        if (Objects.isNull(hostInfoHolder)) {
            throw new OpsException("host information does not exist");
        }

        OpsClusterNodeEntity clusterNode = opsClusterContext.getOpsClusterNodeEntityList().get(0);
        if (Objects.isNull(clusterNode)) {
            throw new OpsException("Cluster node information does not exist");
        }

        log.info("Login to start user");
        Session startUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false, clusterNode.getHostId(), clusterNode.getInstallUserId());
        log.info("execute start");
        doStart(startUserSession, retBuffer, opsClusterContext);
        log.info("Startup complete");
    }

    private void doStart(Session startUserSession, RetBuffer retBuffer, OpsClusterContext opsClusterContext) {
        OpsClusterEntity opsClusterEntity = opsClusterContext.getOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterContext.getOpsClusterNodeEntityList();
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            String dataPath = opsClusterNodeEntity.getDataPath();
            String startCommand = MessageFormat.format(SshCommandConstants.LITE_START, dataPath);
            startCommand = addCommandOfLoadEnvironmentVariable(startCommand, opsClusterEntity.getEnvPath());
            opsHostRemoteService.executeCommand(startCommand, startUserSession, retBuffer, "startup node");
        }
    }

    @Override
    public void stop(OpsClusterContext opsClusterContext) {
        log.info("start stop minimalist");
        RetBuffer retBuffer = opsClusterContext.getRetBuffer();

        HostInfoHolder hostInfoHolder = opsClusterContext.getHostInfoHolders().get(0);
        if (Objects.isNull(hostInfoHolder)) {
            throw new OpsException("host information does not exist");
        }

        OpsClusterNodeEntity clusterNode = opsClusterContext.getOpsClusterNodeEntityList().get(0);
        if (Objects.isNull(clusterNode)) {
            throw new OpsException("Cluster node information does not exist");
        }

        log.info("login stop user");
        Session stopUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false, clusterNode.getHostId(), clusterNode.getInstallUserId());
        log.info("execution stops");
        doStop(stopUserSession, retBuffer, opsClusterContext);
        log.info("stop complete");
    }

    private void doStop(Session stopUserSession, RetBuffer retBuffer, OpsClusterContext opsClusterContext) {
        OpsClusterEntity opsClusterEntity = opsClusterContext.getOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterContext.getOpsClusterNodeEntityList();
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            String dataPath = opsClusterNodeEntity.getDataPath();
            String stopCommand = MessageFormat.format(SshCommandConstants.LITE_STOP, dataPath);
            stopCommand = addCommandOfLoadEnvironmentVariable(stopCommand, opsClusterEntity.getEnvPath());
            opsHostRemoteService.executeCommand(stopCommand, stopUserSession, retBuffer, "stop node");
        }
    }

    private void doRestart(Session restartUserSession, RetBuffer retBuffer, OpsClusterContext opsClusterContext) {
        OpsClusterEntity opsClusterEntity = opsClusterContext.getOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterContext.getOpsClusterNodeEntityList();
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            String dataPath = opsClusterNodeEntity.getDataPath();
            String restartCommand = MessageFormat.format(SshCommandConstants.LITE_RESTART, dataPath);
            restartCommand = addCommandOfLoadEnvironmentVariable(restartCommand, opsClusterEntity.getEnvPath());
            opsHostRemoteService.executeCommand(restartCommand, restartUserSession, retBuffer, "restart node");
        }
    }

    private void saveContext(OpsClusterEntity opsClusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities) {
        opsClusterService.save(opsClusterEntity);
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
        }
        opsClusterNodeService.saveBatch(opsClusterNodeEntities);
    }

    private void doInstall(Session installUserSession, RetBuffer retBuffer, String pkgPath,
                           InstallContext installContext, MinimalistInstallNodeConfig nodeConfig) {
        Integer port = installContext.getMinimalistInstallConfig().getPort();
        String databasePassword = installContext.getMinimalistInstallConfig().getDatabasePassword();
        String command = getMinimaLitInstallCommandTemplate(installContext.getDeployType(), pkgPath,
            encryptionUtils.decrypt(databasePassword), port);
        HashMap<String, String> autoResponse = new HashMap<>();
        autoResponse.put("Would you like to create a demo database (yes/no)? ",
                (nodeConfig.getIsInstallDemoDatabase() ? "yes" : "no"));

        sendOperateLog(installContext, "START_EXE_INSTALL_COMMAND");
        try {
            opsHostRemoteService.executeCommand(command, installUserSession, retBuffer, autoResponse);
        } catch (Exception e) {
            log.error("installation error", e);
            throw new OpsException("installation error " + e.getMessage());
        }
        sendOperateLog(installContext, "END_EXE_INSTALL_COMMAND");
    }

    private String getMinimaLitInstallCommandTemplate(DeployTypeEnum deployType, String pkgPath,
                                                      String dbPwd, Integer port) {
        String installCommandTemplate = "";
        if (Objects.equals(DeployTypeEnum.SINGLE_NODE, deployType)) {
            installCommandTemplate = SshCommandConstants.MINIMAL_LIST_SINGLE_INSTALL;
        } else if (Objects.equals(DeployTypeEnum.CLUSTER, deployType)) {
            installCommandTemplate = SshCommandConstants.MINIMAL_LIST_CLUSTER_INSTALL;
        } else {
            throw new OpsException("Unsupported deployment method：" + deployType);
        }
        return MessageFormat.format(installCommandTemplate,
                pkgPath + "simpleInstall/", dbPwd, String.valueOf(port));
    }

    private MinimalistInstallNodeConfig getSingleInstallNodeConfig(InstallContext installContext) {
        return installContext
                .getMinimalistInstallConfig()
                .getNodeConfigList()
                .stream()
                .filter(config -> ClusterRoleEnum.MASTER == config.getClusterRole())
                .findFirst()
                .orElseThrow(() -> new OpsException("Master node configuration not found"));
    }

    @Override
    protected String prepareCleanClusterDir(InstallContext installContext) {
        String delCmd = "";
        try {
            MinimalistInstallNodeConfig nodeConfig = getSingleInstallNodeConfig(installContext);
            String installPath = preparePath(nodeConfig.getInstallPath());
            String pkgPath = preparePath(installContext.getMinimalistInstallConfig().getInstallPackagePath());
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
}

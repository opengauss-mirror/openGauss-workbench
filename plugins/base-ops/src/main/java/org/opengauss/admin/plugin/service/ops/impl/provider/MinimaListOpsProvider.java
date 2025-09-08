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
 * MinimaListOpsProvider.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/provider/MinimaListOpsProvider.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.provider;

import cn.hutool.core.util.StrUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.*;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/8/12 09:25
 **/
@Slf4j
@Service
public class MinimaListOpsProvider extends AbstractOpsProvider {

    @Autowired
    private IOpsClusterService opsClusterService;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Override
    public OpenGaussVersionEnum version() {
        return OpenGaussVersionEnum.MINIMAL_LIST;
    }

    @Override
    public void install(InstallContext installContext) {
        log.info("Start installing the minimalist version");
        WsSession retSession = installContext.getRetSession();

        MinimalistInstallNodeConfig nodeConfig = getSingleInstallNodeConfig(installContext);
        String hostId = nodeConfig.getHostId();
        String installPath = preparePath(nodeConfig.getInstallPath());
        String dataPath = installPath + "data";

        String installUserId = nodeConfig.getInstallUserId();
        String installUserName = null;
        if (StrUtil.isEmpty(installUserId)) {
            wsUtil.sendText(installContext.getRetSession(), "CREATE_INSTALL_USER");
            OpsHostUserEntity installUser = createInstallUser(installContext, hostId);
            installUserId = installUser.getHostUserId();
            installUserName = installUser.getUsername();
        } else {
            String uid = installUserId;
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

        if (StrUtil.isEmpty(installUserId)){
            throw new OpsException("Installation user ID does not exist");
        }

        nodeConfig.setInstallUserId(installUserId);

        wsUtil.sendText(installContext.getRetSession(), "BEFORE INSTALL");
        Session installUserSession = beforeInstall(
                jschUtil, encryptionUtils, installContext, installPath, dataPath,
                installContext.getMinimalistInstallConfig().getInstallPackagePath(),
                hostId, installUserId, installUserName, "-xvf");
        try {
            log.info("perform installation");
            // install
            wsUtil.sendText(installContext.getRetSession(), "INSTALL");
            doInstall(installUserSession, retSession, installPath, installContext, nodeConfig);

            OpsClusterContext opsClusterContext = new OpsClusterContext();
            OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
            List<OpsClusterNodeEntity> opsClusterNodeEntities = installContext.getMinimalistInstallConfig()
                    .toOpsClusterNodeEntityList(opsClusterEntity);

            opsClusterContext.setOpsClusterEntity(opsClusterEntity);
            opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntities);

            wsUtil.sendText(installContext.getRetSession(), "CREATE REMOTE USER");
            wsUtil.sendText(installContext.getRetSession(),"CREATE_REMOTE_USER");
            createRemoteUser(installUserSession, retSession, installContext, dataPath, opsClusterContext);

            wsUtil.sendText(installContext.getRetSession(), "SAVE CONTEXT");
            wsUtil.sendText(installContext.getRetSession(),"SAVE_INSTALL_CONTEXT");
            saveContext(opsClusterEntity, opsClusterNodeEntities);
            wsUtil.sendText(installContext.getRetSession(),"FINISH");
            log.info("The installation is complete");
        }finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()){
                installUserSession.disconnect();
            }
        }
    }

    private void createRemoteUser(Session installUserSession, WsSession retSession, InstallContext installContext, String dataPath, OpsClusterContext opsClusterContext) {
        if (DeployTypeEnum.CLUSTER == installContext.getDeployType()) {
            String masterListenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath + "/master");
            try {
                JschResult jschResult = jschUtil.executeCommand(masterListenerAddress, installUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to modify listening address, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to modify listening address");
                }

            } catch (Exception e) {
                log.error("Failed to modify listening address", e);
                throw new OpsException("Failed to modify listening address");
            }

            String masterHba = MessageFormat.format(SshCommandConstants.HBA, dataPath + "/master");
            try {
                JschResult jschResult = jschUtil.executeCommand(masterHba, installUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to modify host, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to modify host");
                }

            } catch (Exception e) {
                log.error("Failed to modify host", e);
                throw new OpsException("Failed to modify host");
            }

            String slaveListenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath + "/slave");
            try {
                JschResult jschResult = jschUtil.executeCommand(slaveListenerAddress, installUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to modify listening address, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to modify listening address");
                }

            } catch (Exception e) {
                log.error("Failed to modify listening address", e);
                throw new OpsException("Failed to modify listening address");
            }

            String slaveHba = MessageFormat.format(SshCommandConstants.HBA, dataPath + "/slave");
            try {
                JschResult jschResult = jschUtil.executeCommand(slaveHba, installUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to modify host, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to modify host");
                }

            } catch (Exception e) {
                log.error("Failed to modify host", e);
                throw new OpsException("Failed to modify host");
            }

        } else {
            String listenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath + "/single_node");
            try {
                JschResult jschResult = jschUtil.executeCommand(listenerAddress, installUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to modify listening address, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to modify listening address");
                }

            } catch (Exception e) {
                log.error("Failed to modify listening address", e);
                throw new OpsException("Failed to modify listening address");
            }

            String hba = MessageFormat.format(SshCommandConstants.HBA, dataPath + "/single_node");
            try {
                JschResult jschResult = jschUtil.executeCommand(hba, installUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to modify host, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to modify host");
                }

            } catch (Exception e) {
                log.error("Failed to modify host", e);
                throw new OpsException("Failed to modify host");
            }
        }

        opsClusterContext.setRetSession(retSession);
        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());
        restart(opsClusterContext);

        String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(installContext.getMinimalistInstallConfig().getPort()));
        try {
            Map<String, String> response = new HashMap<>();
            String createUser = MessageFormat.format(
                "CREATE USER gaussdb WITH MONADMIN AUDITADMIN SYSADMIN PASSWORD \"{0}\";\\q",
                encryptionUtils.decrypt(installContext.getMinimalistInstallConfig().getDatabasePassword()));
            response.put("openGauss=#", createUser);
            JschResult jschResult = jschUtil.executeCommand(clientLoginOpenGauss, installUserSession, retSession, response);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to create user, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to create user");
            }

        } catch (Exception e) {
            log.error("Failed to create user", e);
            throw new OpsException("Failed to create user");
        }
    }

    private OpsHostUserEntity createInstallUser(InstallContext installContext, String hostId) {
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        OpsHostUserEntity hostUserEntity = hostUserFacade.getOmmUserByHostId(hostId);
        if (Objects.nonNull(hostUserEntity)) {
            return hostUserEntity;
        }

        String rootPassword = null;

        for (HostInfoHolder hostInfoHolder : installContext.getHostInfoHolders()) {
            final OpsHostEntity currentHost = hostInfoHolder.getHostEntity();
            if (hostId.equalsIgnoreCase(currentHost.getHostId())){
                rootPassword = hostInfoHolder.getHostUserEntities().stream().filter(user->"root".equalsIgnoreCase(user.getUsername())).map(OpsHostUserEntity::getPassword).findFirst().orElseThrow(()->new OpsException("root password not found"));
            }
        }

        if (StrUtil.isEmpty(rootPassword)){
            throw new OpsException("root password not found");
        }

        Session rootSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), "root", encryptionUtils.decrypt(rootPassword)).orElseThrow(() -> new OpsException("with the host[" + hostEntity.getPublicIp() + "]Failed to establish session"));
        try {
            String ommUserName = null;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {

                if (i==0){
                    ommUserName = "omm";
                }else {
                    ommUserName = "omm"+i;
                }
                try {
                    String command = "cat /etc/passwd | awk -F \":\" \"{print $1}\"|grep "+ommUserName+" | wc -l";
                    JschResult jschResult = jschUtil.executeCommand(command, rootSession, installContext.getRetSession());
                    if (0 != jschResult.getExitCode()) {
                        log.error("Failed to query omm user, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                        throw new OpsException("Failed to query omm user");
                    }

                    log.info("user count {},{}",ommUserName,jschResult.getResult());
                    if (StrUtil.isNotEmpty(jschResult.getResult()) && 0 == Integer.parseInt(jschResult.getResult())) {
                        log.info("ommUserName：{}",ommUserName);
                        break;
                    }
                } catch (Exception e) {
                    log.error("Failed to query omm user", e);
                }
            }


            String password = StrUtil.uuid();

            try {
                JschResult jschResult = null;
                try {
                    String command = "useradd "+ommUserName;
                    jschResult = jschUtil.executeCommand(command, rootSession, installContext.getRetSession());
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
                    String command = "passwd "+ommUserName;
                    jschResult = jschUtil.executeCommand(command, rootSession, installContext.getRetSession(), autoResponse);
                } catch (InterruptedException e) {
                    throw new OpsException("thread interruption");
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
            if (Objects.isNull(hostUserEntities)){
                hostUserEntities = new ArrayList<>();
                hostHolder.setHostUserEntities(hostUserEntities);
            }

            hostUserEntities.add(hostUserEntity);
            return hostUserEntity;
        }finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()){
                rootSession.disconnect();
            }
        }
    }

    @Override
    public void uninstall(UnInstallContext unInstallContext) {
        log.info("Start uninstalling the minimalist version");
        WsSession retSession = unInstallContext.getRetSession();

        HostInfoHolder hostInfoHolder = unInstallContext.getHostInfoHolders().get(0);
        if (Objects.isNull(hostInfoHolder)) {
            throw new OpsException("host information does not exist");
        }

        OpsClusterNodeEntity clusterNode = unInstallContext.getOpsClusterNodeEntityList().get(0);
        if (Objects.isNull(clusterNode)) {
            throw new OpsException("Cluster node information does not exist");
        }

        log.info("Login to uninstall user");
        Session unInstallUserSession = loginWithUser(jschUtil,encryptionUtils,unInstallContext.getHostInfoHolders(), false, clusterNode.getHostId(), clusterNode.getInstallUserId());
        // uninstall
        log.info("perform uninstall");
        doUnInstall(unInstallUserSession, retSession, unInstallContext);
        log.info("Uninstall is complete");
        removeContext(unInstallContext);

        log.info("Uninstall complete");
    }

    @Override
    public void restart(OpsClusterContext opsClusterContext) {
        log.info("Start restarting the minimalist version");
        WsSession retSession = opsClusterContext.getRetSession();

        HostInfoHolder hostInfoHolder = opsClusterContext.getHostInfoHolders().get(0);
        if (Objects.isNull(hostInfoHolder)) {
            throw new OpsException("host information does not exist");
        }

        OpsClusterNodeEntity clusterNode = opsClusterContext.getOpsClusterNodeEntityList().get(0);
        if (Objects.isNull(clusterNode)) {
            throw new OpsException("Cluster node information does not exist");
        }

        log.info("login restart user");
        Session restartUserSession = loginWithUser(jschUtil,encryptionUtils,opsClusterContext.getHostInfoHolders(), false, clusterNode.getHostId(), clusterNode.getInstallUserId());
        log.info("perform a restart");
        doRestart(restartUserSession, retSession, opsClusterContext);
        log.info("restart complete");
    }

    @Override
    public void start(OpsClusterContext opsClusterContext) {
        log.info("Start the minimalist version");
        WsSession retSession = opsClusterContext.getRetSession();

        HostInfoHolder hostInfoHolder = opsClusterContext.getHostInfoHolders().get(0);
        if (Objects.isNull(hostInfoHolder)) {
            throw new OpsException("host information does not exist");
        }

        OpsClusterNodeEntity clusterNode = opsClusterContext.getOpsClusterNodeEntityList().get(0);
        if (Objects.isNull(clusterNode)) {
            throw new OpsException("Cluster node information does not exist");
        }

        log.info("Login to start user");
        Session startUserSession = loginWithUser(jschUtil,encryptionUtils,opsClusterContext.getHostInfoHolders(), false, clusterNode.getHostId(), clusterNode.getInstallUserId());
        log.info("execute start");
        doStart(startUserSession, retSession, opsClusterContext);
        log.info("Startup complete");
    }

    private void doStart(Session startUserSession, WsSession retSession, OpsClusterContext opsClusterContext) {
        OpsClusterEntity opsClusterEntity = opsClusterContext.getOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterContext.getOpsClusterNodeEntityList();
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            String dataPath = opsClusterNodeEntity.getDataPath();
            String startCommand = MessageFormat.format(SshCommandConstants.LITE_START, dataPath);
            try {
                JschResult jschResult = jschUtil.executeCommand(startCommand, opsClusterEntity.getEnvPath(),
                    startUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    throw new OpsException("startup error，exit code " + jschResult.getExitCode());
                }
            } catch (Exception e) {
                log.error("startup error", e);
                throw new OpsException("startup error");
            }
        }
    }

    @Override
    public void stop(OpsClusterContext opsClusterContext) {
        log.info("start stop minimalist");
        WsSession retSession = opsClusterContext.getRetSession();

        HostInfoHolder hostInfoHolder = opsClusterContext.getHostInfoHolders().get(0);
        if (Objects.isNull(hostInfoHolder)) {
            throw new OpsException("host information does not exist");
        }

        OpsClusterNodeEntity clusterNode = opsClusterContext.getOpsClusterNodeEntityList().get(0);
        if (Objects.isNull(clusterNode)) {
            throw new OpsException("Cluster node information does not exist");
        }

        log.info("login stop user");
        Session stopUserSession = loginWithUser(jschUtil,encryptionUtils,opsClusterContext.getHostInfoHolders(), false, clusterNode.getHostId(), clusterNode.getInstallUserId());
        log.info("execution stops");
        doStop(stopUserSession, retSession, opsClusterContext);
        log.info("stop complete");
    }

    @Override
    public void enableWdrSnapshot(Session session, OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities, WdrScopeEnum scope, String dataPath) {
        if (StrUtil.isEmpty(dataPath)) {
            dataPath = opsClusterNodeEntities.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("Master node configuration not found")).getDataPath();

            if (clusterEntity.getDeployType() == DeployTypeEnum.CLUSTER) {
                dataPath = dataPath + "/master";
            } else {
                dataPath = dataPath + "/single_node";
            }
        }

        String checkCommand = "gs_guc check -D " + dataPath + " -c \"enable_wdr_snapshot\"";
        try {
            JschResult jschResult = jschUtil.executeCommand(checkCommand, session, clusterEntity.getEnvPath());
            if (jschResult.getResult().contains("enable_wdr_snapshot=on")){
                return;
            }
        } catch (Exception e) {
            String msg = "Failed to set the enable_wdr_snapshot parameter";
            if (e instanceof OpsException) {
                msg = e.getMessage();
            }
            log.error(msg, e);
            throw new OpsException(msg);
        }

        String command = "gs_guc reload -D " + dataPath + " -c \"enable_wdr_snapshot=on\"";

        try {
            JschResult jschResult = jschUtil.executeCommand(command, session, clusterEntity.getEnvPath());
            if (0 != jschResult.getExitCode()) {
                log.error("set enable_wdr_snapshot parameter failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to query the enable_wdr_snapshot parameter");
            }
        } catch (Exception e) {
            String msg = "Failed to set the enable_wdr_snapshot parameter";
            if (e instanceof OpsException) {
                msg = e.getMessage();
            }
            log.error(msg, e);
            throw new OpsException(msg);
        }
    }

    private void doStop(Session stopUserSession, WsSession retSession, OpsClusterContext opsClusterContext) {
        OpsClusterEntity opsClusterEntity = opsClusterContext.getOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterContext.getOpsClusterNodeEntityList();
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            String dataPath = opsClusterNodeEntity.getDataPath();
            String stopCommand = MessageFormat.format(SshCommandConstants.LITE_STOP, dataPath);
            try {
                JschResult jschResult = jschUtil.executeCommand(stopCommand,opsClusterEntity.getEnvPath(), stopUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    throw new OpsException("stop error，exit code " + jschResult.getExitCode());
                }
            } catch (Exception e) {
                log.error("stop error", e);
                throw new OpsException("stop error");
            }
        }
    }

    private void doRestart(Session restartUserSession, WsSession retSession, OpsClusterContext opsClusterContext) {
        OpsClusterEntity opsClusterEntity = opsClusterContext.getOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterContext.getOpsClusterNodeEntityList();
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            String dataPath = opsClusterNodeEntity.getDataPath();
            String restartCommand = MessageFormat.format(SshCommandConstants.LITE_RESTART, dataPath);
            try {
                JschResult jschResult = jschUtil.executeCommand(restartCommand, opsClusterEntity.getEnvPath(),
                    restartUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    throw new OpsException("restart error，exit code " + jschResult.getExitCode());
                }
            } catch (Exception e) {
                log.error("restart error", e);
                throw new OpsException("restart error");
            }
        }
    }

    private void doUnInstall(Session unInstallUserSession, WsSession retSession, UnInstallContext unInstallContext) {
        OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = unInstallContext.getOpsClusterNodeEntityList();
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            String dataPath = opsClusterNodeEntity.getDataPath();
            // single uninstall
            String command = MessageFormat.format(SshCommandConstants.LITE_STOP, dataPath);
            try {
                JschResult jschResult = jschUtil.executeCommand(command, opsClusterEntity.getEnvPath(), unInstallUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    throw new OpsException("Uninstall error，exit code " + jschResult.getExitCode());
                }
            } catch (Exception e) {
                log.error("Uninstall error", e);
                throw new OpsException("Uninstall error");
            }

            try {
                JschResult jschResult = jschUtil.executeCommand("rm -rf " + dataPath, unInstallUserSession, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to clean data, response code: {}, response message: {}", jschResult.getExitCode(), jschResult.getResult());
                }
            } catch (Exception e) {
                log.error("Failed to clean data", e);
            }
        }
    }

    private void removeContext(UnInstallContext unInstallContext) {
        OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
        opsClusterService.removeById(opsClusterEntity.getClusterId());

        List<OpsClusterNodeEntity> opsClusterNodeEntityList = unInstallContext.getOpsClusterNodeEntityList();
        opsClusterNodeService.removeBatchByIds(opsClusterNodeEntityList.stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList()));
    }

    private void saveContext(OpsClusterEntity opsClusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities) {
        opsClusterEntity.setDatabasePassword(encryptionUtils.encrypt(opsClusterEntity.getDatabasePassword()));
        opsClusterService.save(opsClusterEntity);
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
        }
        opsClusterNodeService.saveBatch(opsClusterNodeEntities);
    }

    private void doInstall(Session installUserSession, WsSession retSession, String pkgPath, InstallContext installContext, MinimalistInstallNodeConfig nodeConfig) {
        String installCommandTemplate = getInstallCommandTemplate(installContext.getDeployType());

        Integer port = installContext.getMinimalistInstallConfig().getPort();
        String databasePassword = installContext.getMinimalistInstallConfig().getDatabasePassword();

        String command = MessageFormat.format(installCommandTemplate, pkgPath + "simpleInstall/", databasePassword, String.valueOf(port));

        HashMap<String, String> autoResponse = new HashMap<>();
        autoResponse.put("Would you like to create a demo database (yes/no)? ", (nodeConfig.getIsInstallDemoDatabase() ? "yes" : "no"));

        wsUtil.sendText(installContext.getRetSession(),"START_EXE_INSTALL_COMMAND");
        try {
            JschResult jschResult = jschUtil.executeCommand(command, installUserSession, retSession, autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("install output：{}", jschResult.getResult());
                throw new OpsException("installation error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("installation error", e);
            throw new OpsException("installation error");
        }
        wsUtil.sendText(installContext.getRetSession(),"END_EXE_INSTALL_COMMAND");
    }

    private void backOld(Session rootSession, String targetPath, WsSession retSession) {
        String command = MessageFormat.format(SshCommandConstants.MV, targetPath);
        try {
            try {
                jschUtil.executeCommand(command, rootSession, retSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
        } catch (IOException e) {
            log.error("Backup directory failed:", e);
            throw new OpsException("Backup directory failed");
        }
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

    private String getInstallCommandTemplate(DeployTypeEnum deployType) {
        if (DeployTypeEnum.SINGLE_NODE == deployType) {
            return SshCommandConstants.MINIMAL_LIST_SINGLE_INSTALL;
        }

        if (DeployTypeEnum.CLUSTER == deployType) {
            return SshCommandConstants.MINIMAL_LIST_CLUSTER_INSTALL;
        }

        throw new OpsException("Unsupported deployment method：" + deployType);
    }
}

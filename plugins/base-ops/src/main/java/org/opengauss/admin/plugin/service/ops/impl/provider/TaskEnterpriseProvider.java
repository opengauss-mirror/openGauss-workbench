/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * AbstractTaskProvider.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/provider/AbstractTaskProvider.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.provider;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.*;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.impl.function.GenerateClusterConfigXmlInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskEnterpriseProvider
 *
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Slf4j
@Service
public class TaskEnterpriseProvider extends AbstractTaskProvider {
    @Resource
    private IOpsClusterService opsClusterService;
    @Resource
    private IOpsClusterNodeService opsClusterNodeService;
    @Resource
    private GenerateClusterConfigXmlInstance generateClusterConfigXmlInstance;

    @Override
    public OpenGaussVersionEnum version() {
        return OpenGaussVersionEnum.ENTERPRISE;
    }

    @Override
    public void install(InstallContext installContext) {
        log.info("Start installing Enterprise Edition");
        boolean isInstallSucc = true;
        try {
            installContext.getRetBuffer().sendText("Start installing Enterprise Edition");
            doInstall(installContext);

            OpsClusterContext opsClusterContext = new OpsClusterContext();
            OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
            List<OpsClusterNodeEntity> opsClusterNodeEntities =
                    installContext.getEnterpriseInstallConfig().toOpsClusterNodeEntityList();
            opsClusterContext.setOpsClusterEntity(opsClusterEntity);
            opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntities);

            sendOperateLog(installContext, "CREATE_REMOTE_USER");
            createEnterpriseRemoteUser(installContext, opsClusterContext);

            sendOperateLog(installContext, "SAVE_INSTALL_CONTEXT");
            saveContext(installContext);
            sendOperateLog(installContext, "FINISH");
            log.info("The installation is complete");
        } catch (OpsException e) {
            log.error("install failed：", e);
            isInstallSucc = false;
            throw new OpsException("The installation is failed " + e.getMessage());
        } finally {
            if (!isInstallSucc) {
                cleanResource(installContext);
            }
        }
    }

    protected String prepareCleanClusterDir(InstallContext installContext) {
        String delCmd = "";
        try {
            // remove intall path software
            String installPackagePathPath = installContext.getEnterpriseInstallConfig().getInstallPackagePath();
            log.info("install package path : {}", installPackagePathPath);

            String delInstallPath = MessageFormat.format(SshCommandConstants.DEL_FILE, installPackagePathPath + "/*");
            log.info("delete install package path : {}", delInstallPath);
            String installDataPath = installContext.getEnterpriseInstallConfig().getInstallPath();
            String delAppPath = MessageFormat.format(SshCommandConstants.DEL_FILE, installDataPath);
            log.info("delete app path : {}", delAppPath);
            String delTmpPath = MessageFormat.format(SshCommandConstants.DEL_FILE,
                    installContext.getEnterpriseInstallConfig().getTmpPath());
            log.info("delete tmp path : {}", delTmpPath);
            String delOmPath = MessageFormat.format(SshCommandConstants.DEL_FILE,
                    installContext.getEnterpriseInstallConfig().getOmToolsPath());
            log.info("delete om path : {}", delOmPath);
            delCmd = delInstallPath + " || echo \"delInstallPath failed\"; "
                    + delAppPath + " || echo \"delAppPath failed\"; "
                    + delTmpPath + " || echo \"delTmpPath failed\"; "
                    + delOmPath + " || echo \"delOmPath failed\"; ";
            if (installContext.getEnterpriseInstallConfig().getDatabaseKernelArch() ==
                    DatabaseKernelArch.SHARING_STORAGE) {
                String dssHome = installContext.getEnterpriseInstallConfig().
                        getSharingStorageInstallConfig().getDssHome();
                String delDssHome = MessageFormat.format(SshCommandConstants.DEL_FILE, dssHome);
                delCmd += delDssHome + " || echo \"delDssHome failed\"; ";
            }

            EnterpriseInstallNodeConfig masterNodeConfig = getMasterInstallNodeConfig(installContext);
            String delDataPath = MessageFormat.format(SshCommandConstants.DEL_FILE, masterNodeConfig.getDataPath());
            delCmd += delDataPath + " || echo \"delDataPath failed\"; ";
            if (installContext.getEnterpriseInstallConfig().getIsInstallCM()) {
                String delCmPath = MessageFormat.format(SshCommandConstants.DEL_FILE, masterNodeConfig.getCmDataPath());
                delCmd += delCmPath + " || echo \"delCmPath failed\"; ";

            }
            return delCmd;
        } catch (OpsException e) {
            log.error("delete cmd : {}", delCmd);
            return delCmd;
        }
    }


    private void doInstall(InstallContext installContext) {
        // Master node configuration information
        Set<String> top2DirSet = new HashSet<String>();
        getClusterCommonTopTwoPath(installContext, top2DirSet);
        EnterpriseInstallNodeConfig masterNodeConfig = getMasterInstallNodeConfig(installContext);
        String installUsername = masterNodeConfig.getInstallUsername();
        RetBuffer retBuffer = installContext.getRetBuffer();
        for (HostInfoHolder hostInfoHolder : installContext.getHostInfoHolders()) {
            Session currentRoot = createSessionWithRootUser(hostInfoHolder);
            installDependency(currentRoot, retBuffer, installContext.getOs());
            top2DirSet.forEach(top2Path -> {
                ensureDirExist(currentRoot, top2Path, retBuffer);
                ensureLevel2DirPermission(currentRoot, installUsername, top2Path, retBuffer);
            });
            closeSession(currentRoot);
        }
        String masterHostId = masterNodeConfig.getHostId();
        HostInfoHolder masterHostInfoHolder = getHostInfoHolder(installContext, masterHostId);
        Session rootSession = createSessionWithRootUser(masterHostInfoHolder);
        String pkgPath = preparePath(installContext.getEnterpriseInstallConfig().getInstallPackagePath());
        ensureDirExist(rootSession, pkgPath, retBuffer);

        // scp
        sendOperateLog(installContext, "START_SCP_INSTALL_PACKAGE");
        String localPath = installContext.getInstallPackageLocalPath();
        String installPackageFullPath = scpInstallPackageToMasterNode(rootSession, localPath, pkgPath, retBuffer);
        sendOperateLog(installContext, "END_SCP_INSTALL_PACKAGE");

        // unzip install pack
        sendOperateLog(installContext, "START_UNZIP_INSTALL_PACKAGE");
        decompress(rootSession, pkgPath, installPackageFullPath, retBuffer, "-xvf");
        // unzip CM
        String omPackage = getOMPackage(pkgPath, rootSession);
        decompress(rootSession, pkgPath, pkgPath + omPackage, retBuffer, "-zxvf");
        sendOperateLog(installContext, "END_UNZIP_INSTALL_PACKAGE");

        // write xml
        sendOperateLog(installContext, "START_GEN_XML_CONFIG");
        String xml = generateClusterConfigXmlInstance.generate(installContext);
        log.info("Generated xml information：{}", xml);
        String xmlConfigFullPath = ensureXmlConfigFullPath(rootSession, pkgPath, xml, retBuffer);
        sendOperateLog(installContext, "END_GEN_XML_CONFIG");

        // 设置解压后安装包目录用户，用户组以及操作权限 仅需设置主节点权限，备机由预安装脚本设置
        ensureLevel2DirPermission(rootSession, installUsername, pkgPath, retBuffer);

        HostInfoHolder masterHostHolder = getHostInfoHolder(installContext, masterHostId);
        OpsHostUserEntity masterRootUserInfo = getHostUserInfoByUsername(masterHostHolder, "root");
        OpsHostUserEntity installUserInfo = getHostUserInfo(masterHostHolder, masterNodeConfig.getInstallUserId());

        sendOperateLog(installContext, "START_EXE_PREINSTALL_COMMAND");
        String group = checkInstallUserGroup(installUsername, rootSession, retBuffer);
        preInstall(group, pkgPath, masterNodeConfig, xmlConfigFullPath, rootSession, retBuffer,
            masterRootUserInfo.getPassword(), installUserInfo.getPassword(), installContext.getEnvPath());
        sendOperateLog(installContext, "END_EXE_PREINSTALL_COMMAND");

        Session ommSession = createSessionWithUserId(masterHostHolder, false, masterNodeConfig.getInstallUserId());
        // install
        sendOperateLog(installContext, "START_EXE_INSTALL_COMMAND");
        ensureInstallCommand(installContext, xmlConfigFullPath, ommSession, retBuffer);
        sendOperateLog(installContext, "END_EXE_INSTALL_COMMAND");
        closeSession(ommSession);
        closeSession(rootSession);
    }

    private EnterpriseInstallNodeConfig getMasterInstallNodeConfig(InstallContext installContext) {
        return installContext
                .getEnterpriseInstallConfig()
                .getNodeConfigList()
                .stream()
                .filter(nodeConfig -> nodeConfig.getClusterRole() == ClusterRoleEnum.MASTER)
                .findFirst()
                .orElseThrow(() -> new OpsException("Master node information not found"));
    }

    private void getClusterCommonTopTwoPath(InstallContext installContext, Set<String> top2DirSet) {
        String pkgPath = preparePath(installContext.getEnterpriseInstallConfig().getInstallPackagePath());
        String logPath = preparePath(installContext.getEnterpriseInstallConfig().getLogPath());
        String omPath = preparePath(installContext.getEnterpriseInstallConfig().getOmToolsPath());
        String installPath = preparePath(installContext.getEnterpriseInstallConfig().getInstallPath());
        String corePath = preparePath(installContext.getEnterpriseInstallConfig().getCorePath());
        String tmpPath = preparePath(installContext.getEnterpriseInstallConfig().getTmpPath());
        top2DirSet.add(getLevel2DirPath(pkgPath));
        top2DirSet.add(getLevel2DirPath(logPath));
        top2DirSet.add(getLevel2DirPath(omPath));
        top2DirSet.add(getLevel2DirPath(installPath));
        top2DirSet.add(getLevel2DirPath(corePath));
        top2DirSet.add(getLevel2DirPath(tmpPath));
    }

    private void closeSession(Session session) {
        opsHostRemoteService.closeSession(session);
    }

    private void ensureInstallCommand(InstallContext installContext, String xmlConfigFullPath,
                                      Session ommSession, RetBuffer retBuffer) {
        String installCommand = MessageFormat.format(SshCommandConstants.ENTERPRISE_INSTALL, xmlConfigFullPath);
        try {
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("(yes/no)?", "yes");
            String databasePassword = encryptionUtils.decrypt(
                installContext.getEnterpriseInstallConfig().getDatabasePassword());
            autoResponse.put("Please enter password for database:", databasePassword);
            autoResponse.put("Please repeat for database:", databasePassword);

            installCommand = addCommandOfLoadEnvironmentVariable(installCommand, installContext.getEnvPath());
            opsHostRemoteService.executeCommand(installCommand, ommSession, retBuffer, autoResponse);
        } catch (Exception e) {
            log.error("install failed：", e);
            throw new OpsException("install failed");
        }
    }

    private String getOMPackage(String path, Session rootSession) {
        String command = MessageFormat.format(SshCommandConstants.GET_OM_PACKAGE, path);
        try {
            String commandResult = opsHostRemoteService.executeCommand(command, rootSession, "get om package name");
            if (commandResult.contains(String.valueOf((char) 10))) {
                String errorMsg = "One OM package is expected to be queried, but multiple OM packages are queried. "
                        + "Please check the package path: " + path;
                log.error(errorMsg);
                throw new OpsException(errorMsg);
            }
            if (ObjectUtils.isEmpty(commandResult)) {
                String errorMsg = "No OM package is found.";
                log.error(errorMsg);
                throw new OpsException(errorMsg);
            }
            return commandResult;
        } catch (OpsException e) {
            log.error("Failed to obtain the OM package by command: {}. Error: {}", command, e.getMessage());
            throw new OpsException("Failed to obtain the OM package.");
        }
    }


    private String checkInstallUserGroup(String installUsername, Session rootSession, RetBuffer retBuffer) {
        String userGroupCommand = "groups " + installUsername + " | awk -F ':' '{print $2}' | sed 's/\\\"//g'";
        return opsHostRemoteService.executeCommand(userGroupCommand, rootSession, retBuffer, "query user group");
    }

    private String ensureXmlConfigFullPath(Session rootSession, String pkgPath, String xml, RetBuffer retBuffer) {
        String xmlConfigFullPath = pkgPath + "cluster_config.xml";
        String createClusterConfigXmlCommand = MessageFormat.format(SshCommandConstants.FILE_CREATE, xml, xmlConfigFullPath);
        opsHostRemoteService.executeCommand(createClusterConfigXmlCommand, rootSession, retBuffer, "create xml configuration file");
        return xmlConfigFullPath;
    }


    private void preInstall(String group, String pkgPath, EnterpriseInstallNodeConfig masterNodeConfig, String xmlConfigFullPath, Session rootSession, RetBuffer retBuffer, String rootPassword, String installUserPassword, String envPath) {
        String installUsername = masterNodeConfig.getInstallUsername();
        String gsPreInstall = MessageFormat.format(SshCommandConstants.GS_PREINSTALL_INTERACTIVE,
                pkgPath + "script/", installUsername,
                group, xmlConfigFullPath);
        gsPreInstall = wrapperEnvSep(gsPreInstall, envPath);
        Map<String, String> autoResponse = new HashMap<>();
        autoResponse.put("(yes/no)?", "yes");
        autoResponse.put("Please enter password for root\r\nPassword:", encryptionUtils.decrypt(rootPassword));
        autoResponse.put("Please enter password for current user[" + installUsername + "].\r\nPassword:",
            encryptionUtils.decrypt(installUserPassword));
        // for compatibility with 5.1.0
        autoResponse.put("Please enter password for current user[root].\r\nPassword:",
            encryptionUtils.decrypt(rootPassword));
        opsHostRemoteService.executeCommand(gsPreInstall, rootSession, retBuffer, autoResponse);
    }

    private void saveContext(InstallContext installContext) {
        OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = installContext.getEnterpriseInstallConfig().toOpsClusterNodeEntityList();

        opsClusterService.save(opsClusterEntity);
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
        }
        opsClusterNodeService.saveBatch(opsClusterNodeEntities);
    }

    @Override
    public void restart(OpsClusterContext opsClusterContext) {
        log.info("Start rebooting Enterprise Edition");
        List<String> restartNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(restartNodeIds)) {
            restartNodeIds = opsClusterContext.getOpsClusterNodeEntityList().stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(restartNodeIds)) {
            log.error("No nodes to restart");
        }

        if (opsClusterContext.getOpsClusterNodeEntityList().size() == restartNodeIds.size()) {
            doRestartCluster(opsClusterContext);
        } else {
            for (String restartNodeId : restartNodeIds) {
                doRestartNode(restartNodeId, opsClusterContext);
            }
        }
    }

    private void doRestartCluster(OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity startNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().get(0);
        RetBuffer retBuffer = opsClusterContext.getRetBuffer();
        Session ommUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());
        OmStatusModel omStatusModel = omStatus(ommUserSession, retBuffer, opsClusterContext.getOpsClusterEntity().getEnvPath());
        if (Objects.isNull(omStatusModel)) {
            throw new OpsException("gs_om status fail");
        }
        String command = omStatusModel.isInstallCm() ? "cm_ctl stop && cm_ctl start" : "gs_om -t restart";
        command = addCommandOfLoadEnvironmentVariable(command, opsClusterContext.getOpsClusterEntity().getEnvPath());
        opsHostRemoteService.executeCommand(command, ommUserSession, retBuffer, "restart cluster");
    }


    private void doRestartNode(String restartNodeId, OpsClusterContext opsClusterContext) {
        doStopNode(restartNodeId, opsClusterContext);
        doStartNode(restartNodeId, opsClusterContext);
    }

    @Override
    public void start(OpsClusterContext opsClusterContext) {
        log.info("Get started with Enterprise Edition");
        List<String> startNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(startNodeIds)) {
            startNodeIds = opsClusterContext.getOpsClusterNodeEntityList().stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(startNodeIds)) {
            log.error("No node to start");
        }

        if (opsClusterContext.getOpsClusterNodeEntityList().size() == startNodeIds.size()) {
            doStartCluster(opsClusterContext);
        } else {
            for (String startNodeId : startNodeIds) {
                doStartNode(startNodeId, opsClusterContext);
            }
        }
    }

    private void doStartCluster(OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity startNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().get(0);
        RetBuffer retBuffer = opsClusterContext.getRetBuffer();
        Session ommUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());
        OmStatusModel omStatusModel = omStatus(ommUserSession, retBuffer, opsClusterContext.getOpsClusterEntity().getEnvPath());
        if (Objects.isNull(omStatusModel)) {
            throw new OpsException("gs_om status fail");
        }
        String command = omStatusModel.isInstallCm() ? " cm_ctl start " : " gs_om -t start ";
        command = addCommandOfLoadEnvironmentVariable(command, opsClusterContext.getOpsClusterEntity().getEnvPath());
        opsHostRemoteService.executeCommand(command, ommUserSession, retBuffer, "start cluster");
    }

    private void doStartNode(String startNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity startNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(startNodeId)).findFirst().orElse(null);
        if (Objects.isNull(startNodeEntity)) {
            log.error("Boot node information not found:{}", startNodeId);
        }

        RetBuffer retBuffer = opsClusterContext.getRetBuffer();
        String dataPath = startNodeEntity.getDataPath();
        log.info("Login to start user");
        Session ommUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());
        OmStatusModel omStatusModel = omStatus(ommUserSession, retBuffer, opsClusterContext.getOpsClusterEntity().getEnvPath());
        if (Objects.isNull(omStatusModel)) {
            throw new OpsException("gs_om status fail");
        }

        String command = null;
        if (omStatusModel.isInstallCm()) {
            String hostId = startNodeEntity.getHostId();
            OpsHostEntity opsHostEntity = opsClusterContext.getHostInfoHolders().stream().map(HostInfoHolder::getHostEntity).filter(host -> host.getHostId().equalsIgnoreCase(hostId)).findFirst().orElseThrow(() -> new OpsException("no host found"));
            String hostname = opsHostEntity.getHostname();
            String nodeId = omStatusModel.getHostnameMapNodeId().get(hostname);

            if (StrUtil.isNotEmpty(nodeId)) {
                command = "cm_ctl start -n " + nodeId + " -D " + dataPath;
            }
        } else {
            command = MessageFormat.format(SshCommandConstants.LITE_START, dataPath);
        }

        command = addCommandOfLoadEnvironmentVariable(command, opsClusterContext.getOpsClusterEntity().getEnvPath());
        opsHostRemoteService.executeCommand(command, ommUserSession, retBuffer, "start cluster node");
    }

    @Override
    public void stop(OpsClusterContext opsClusterContext) {
        log.info("Start Stop Enterprise Edition");
        List<String> stopNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(stopNodeIds)) {
            stopNodeIds = opsClusterContext.getOpsClusterNodeEntityList().stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(stopNodeIds)) {
            log.error("no node to stop");
        }

        if (opsClusterContext.getOpsClusterNodeEntityList().size() == stopNodeIds.size()) {
            doStopCluster(opsClusterContext);
        } else {
            for (String stopNodeId : stopNodeIds) {
                doStopNode(stopNodeId, opsClusterContext);
            }
        }
    }

    private void doStopCluster(OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity startNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().get(0);
        RetBuffer retBuffer = opsClusterContext.getRetBuffer();
        Session ommUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());
        OmStatusModel omStatusModel = omStatus(ommUserSession, retBuffer, opsClusterContext.getOpsClusterEntity().getEnvPath());
        if (Objects.isNull(omStatusModel)) {
            throw new OpsException("gs_om status fail");
        }

        String command = omStatusModel.isInstallCm() ? "cm_ctl stop" : "gs_om -t stop";
        command = addCommandOfLoadEnvironmentVariable(command, opsClusterContext.getOpsClusterEntity().getEnvPath());
        opsHostRemoteService.executeCommand(command, ommUserSession, retBuffer, "stop cluster");
    }

    private void doStopNode(String stopNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity stopNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(stopNodeId)).findFirst().orElse(null);
        if (Objects.isNull(stopNodeEntity)) {
            log.error("No stop node information found:{}", stopNodeId);
        }

        RetBuffer retBuffer = opsClusterContext.getRetBuffer();
        String dataPath = stopNodeEntity.getDataPath();
        log.info("login stop user");
        Session ommUserSession = createSessionWithUserId(opsClusterContext.getHostInfoHolders(), false, stopNodeEntity.getHostId(), stopNodeEntity.getInstallUserId());
        OmStatusModel omStatusModel = omStatus(ommUserSession, retBuffer, opsClusterContext.getOpsClusterEntity().getEnvPath());
        if (Objects.isNull(omStatusModel)) {
            throw new OpsException("gs_om status fail");
        }

        String command = null;
        if (omStatusModel.isInstallCm()) {
            String hostId = stopNodeEntity.getHostId();
            OpsHostEntity opsHostEntity = opsClusterContext.getHostInfoHolders().stream().map(HostInfoHolder::getHostEntity).filter(host -> host.getHostId().equalsIgnoreCase(hostId)).findFirst().orElseThrow(() -> new OpsException("no host found"));
            String hostname = opsHostEntity.getHostname();
            String nodeId = omStatusModel.getHostnameMapNodeId().get(hostname);

            if (StrUtil.isNotEmpty(nodeId)) {
                command = "cm_ctl stop -n " + nodeId + " -D " + dataPath;
            }
        } else {
            command = MessageFormat.format(SshCommandConstants.LITE_STOP, dataPath);
        }

        command = addCommandOfLoadEnvironmentVariable(command, opsClusterContext.getOpsClusterEntity().getEnvPath());
        opsHostRemoteService.executeCommand(command, ommUserSession, retBuffer, "stop cluster node");
    }
}

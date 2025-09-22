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
 * EnterpriseOpsProvider.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/provider/EnterpriseOpsProvider.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.provider;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.HostInfoHolder;
import org.opengauss.admin.plugin.domain.model.ops.InstallContext;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.LunPathManager;
import org.opengauss.admin.plugin.domain.model.ops.OmStatusModel;
import org.opengauss.admin.plugin.domain.model.ops.OpsClusterContext;
import org.opengauss.admin.plugin.domain.model.ops.SharingStorageInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.domain.model.ops.UnInstallContext;
import org.opengauss.admin.plugin.domain.model.ops.UpgradeContext;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.DatabaseKernelArch;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.enums.ops.UpgradeTypeEnum;
import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.impl.function.GenerateClusterConfigXmlInstance;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.system.service.ISysSettingService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/8/12 09:23
 **/
@Slf4j
@Service
public class EnterpriseOpsProvider extends AbstractOpsProvider {

    @Autowired
    private IOpsClusterService opsClusterService;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private ISysSettingService sysSettingService;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private WsUtil wsUtil;
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
            doInstall(installContext);

            OpsClusterContext opsClusterContext = new OpsClusterContext();
            OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
            List<OpsClusterNodeEntity> opsClusterNodeEntities =
                    installContext.getEnterpriseInstallConfig().toOpsClusterNodeEntityList();
            opsClusterContext.setOpsClusterEntity(opsClusterEntity);
            opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntities);

            wsUtil.sendText(installContext.getRetSession(), "CREATE_REMOTE_USER");
            createEnterpriseRemoteUser(installContext, opsClusterContext, jschUtil, encryptionUtils);

            wsUtil.sendText(installContext.getRetSession(), "SAVE_INSTALL_CONTEXT");
            saveContext(installContext);
            wsUtil.sendText(installContext.getRetSession(), "FINISH");
            log.info("The installation is complete");
        } catch (OpsException e) {
            log.error("install failed：", e);
            isInstallSucc = false;
            throw new OpsException("The installation is failed.");
        } finally {
            if (!isInstallSucc) {
                cleanResource(installContext);
            }
        }
    }

    private String prepareCleanClusterDir(InstallContext installContext) {
        String delCmd = "";
        try{
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
                delCmd += delDssHome +  " || echo \"delDssHome failed\"; ";
            }

            EnterpriseInstallNodeConfig masterNodeConfig = installContext.getEnterpriseInstallConfig().
                    getNodeConfigList()
                    .stream().filter(nodeConfig -> nodeConfig.getClusterRole() == ClusterRoleEnum.MASTER)
                    .findFirst().orElseThrow(() -> new OpsException("Master node information not found"));
            String delDataPath = MessageFormat.format(SshCommandConstants.DEL_FILE, masterNodeConfig.getDataPath());
            delCmd += delDataPath +  " || echo \"delDataPath failed\"; ";
            if (installContext.getEnterpriseInstallConfig().getIsInstallCM()) {
                String delCmPath = MessageFormat.format(SshCommandConstants.DEL_FILE, masterNodeConfig.getCmDataPath());
                delCmd += delCmPath +  " || echo \"delCmPath failed\"; ";

            }
            return delCmd;
        } catch (OpsException e) {
            log.error("delete cmd : {}", delCmd);
            return delCmd;
        }
    }

    private void cleanResource(InstallContext installContext) {
        String delCmd = prepareCleanClusterDir(installContext);
        for (HostInfoHolder hostInfoHolder : installContext.getHostInfoHolders()) {
            OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
            Session currentRoot = loginWithUser(jschUtil, encryptionUtils,
                    installContext.getHostInfoHolders(), true, hostEntity.getHostId(), null);
            try {
                JschResult jschResult = jschUtil.executeCommand(delCmd, currentRoot);
                if (0 != jschResult.getExitCode()) {
                    log.error("remove install path failed, exit code: {}, error message: {}",
                            jschResult.getExitCode(),
                            jschResult.getResult());
                }

            } catch (OpsException | IOException | InterruptedException e) {
                log.error("remove install path failed：", e);
            }

            // remove env file
            String delEnvFile = MessageFormat.format(SshCommandConstants.DEL_FILE, installContext.getEnvPath());
            try {
                JschResult jschResult = jschUtil.executeCommand(delEnvFile, currentRoot);
                if (0 != jschResult.getExitCode()) {
                    log.error("remove env file failed, exit code: {}, error message: {}", jschResult.getExitCode(),
                            jschResult.getResult());
                }
            } catch (OpsException | IOException | InterruptedException e) {
                log.error("remove env file failed：", e);
            }
            currentRoot.disconnect();
        }
        log.error("clean resource success");
    }

    private void removeSoftLinkAll(UnInstallContext unInstallContext) {
        OpsClusterNodeEntity opsClusterNodeEntity = unInstallContext.getOpsClusterNodeEntityList()
                .stream()
                .filter(opsClusterNodeEntity1 -> opsClusterNodeEntity1.getClusterRole() == ClusterRoleEnum.MASTER)
                .findFirst().orElseThrow(() -> new OpsException("master node not found"));
        if (opsClusterNodeEntity.getIsEnableDss() == null || !opsClusterNodeEntity.getIsEnableDss()) {
            log.info("enableDss is false.");
            return;
        }
        try {
            for (HostInfoHolder hostInfoHolder : unInstallContext.getHostInfoHolders()) {
                OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();

                Session rootSession = loginWithUser(jschUtil, encryptionUtils,
                        unInstallContext.getHostInfoHolders(), true, hostEntity.getHostId(), null);
                removeSoftLink(opsClusterNodeEntity.getDssDataLunLinkPath(), rootSession);

                String xlogLinkPaths = opsClusterNodeEntity.getXlogLunLinkPath();
                String[] xlogPaths = xlogLinkPaths.split(",");
                for (String xlogPath : xlogPaths) {
                    removeSoftLink(xlogPath, rootSession);
                }

                removeSoftLink(opsClusterNodeEntity.getCmSharingLunLinkPath(), rootSession);
                removeSoftLink(opsClusterNodeEntity.getCmVotingLunLinkPath(), rootSession);

                if (Objects.nonNull(rootSession) && rootSession.isConnected()) {
                    rootSession.disconnect();
                }
            }
        } catch (Exception e) {
            log.error("remove soft link failed", e);
        }
        log.info("remove soft links success");
    }

    private void removeSoftLink(String link, Session rootSession) {
        try {
            String command = MessageFormat.format(SshCommandConstants.DEL_FILE, link);
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (0 != jschResult.getExitCode()) {
                log.warn("remove soft link failed {}", link);
                throw new OpsException("remove soft link failed");
            }
        } catch (Exception e) {
            log.warn("remove soft link failed {}", link);
            throw new OpsException("remove soft link failed");
        }
    }

    /**
     * create soft link
     *
     * @param path        lun path
     * @param prefix      link path prefix
     * @param rootSession session
     * @param isDisasterNeed this parameter function has been deprecated
     * @param installUser install user
     * @return link path
     */
    private String createSoftLink(LunPathManager path, String prefix, Session rootSession, boolean isDisasterNeed,
        String installUser) {
        try {
            String exec = getDiskQueryCmd(path);
            JschResult jschResult = jschUtil.executeCommand(exec, rootSession);
            if (0 != jschResult.getExitCode()) {
                log.error("can not find path with wwn {}, exit code: {}, error message: {}", path.getWwn(),
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("can not find path with wwn");
            }

            String disk = LunPathManager.getLunDiskName(jschResult.getResult());
            String linkPath = LunPathManager.getLunLinkPath(path, prefix, isDisasterNeed, installUser);
            String command = "ls -l " + linkPath;
            try {
                if (jschUtil.executeCommand(command, rootSession).getExitCode() == 0) {
                    jschResult = jschUtil.executeCommand(
                            MessageFormat.format(SshCommandConstants.DEL_FILE, linkPath), rootSession);
                    if (jschResult.getExitCode() != 0) {
                        log.error("remove exist soft link failed, exit code: {}, error message: {}",
                                jschResult.getExitCode(), jschResult.getResult());
                        throw new OpsException("remove exist soft link failed");
                    }
                }
            } catch (Exception e) {
                log.info("command not exist : {}", command);
            }

            command = "ln -s " + disk.trim() + " " + linkPath;
            jschResult = jschUtil.executeCommand(command, rootSession);
            if (0 != jschResult.getExitCode()) {
                log.error("create soft link failed, exit code: {}, error message: {}",
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("create soft link failed");
            }
            log.info("create soft link {} success with host {}", command, rootSession.getHost());
            return linkPath;
        } catch (Exception e) {
            log.error("create soft link failed", e);
            throw new OpsException("create soft link failed");
        }
    }

    private static String getDiskQueryCmd(LunPathManager path) {
        String queryCommand = LunPathManager.lunQueryMethod.getQueryDiskVolumeCmd();
        return String.format(queryCommand, path.getWwn());
    }

    private void createSoftLink(InstallContext installContext) {
        if (installContext.getEnterpriseInstallConfig().getDatabaseKernelArch()
                .equals(DatabaseKernelArch.MASTER_SLAVE)) {
            return;
        }
        try {
            for (HostInfoHolder hostInfoHolder : installContext.getHostInfoHolders()) {
                OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();

                Session rootSession = loginWithUser(jschUtil, encryptionUtils,
                        installContext.getHostInfoHolders(), true, hostEntity.getHostId(), null);
                SharingStorageInstallConfig config =
                        installContext.getEnterpriseInstallConfig().getSharingStorageInstallConfig();
                String installUser = installContext.getEnterpriseInstallConfig()
                    .getNodeConfigList()
                    .stream()
                    .filter(node -> hostEntity.getHostId().equals(node.getHostId()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("root user information not found")).getInstallUsername();
                config.setDssDataLunLinkPath(createSoftLink(new LunPathManager(config.getDssDataLunPath()),
                        "data", rootSession, true, installUser));
                List<String> xlogPaths = config.getXlogLunPath();
                List<String> linkXlogPaths = new ArrayList<>();
                if (xlogPaths.size() == 1) {
                    linkXlogPaths.add(
                        createSoftLink(new LunPathManager(xlogPaths.get(0).trim()), "xlog", rootSession, true,
                            installUser));
                } else {
                    for (String xlogPath : xlogPaths) {
                        linkXlogPaths.add(
                            createSoftLink(new LunPathManager(xlogPath.trim()), "xlog", rootSession, false,
                                installUser));
                    }
                }
                config.setXlogLunLinkPath(linkXlogPaths);
                config.setCmSharingLunLinkPath(createSoftLink(new LunPathManager(
                        config.getCmSharingLunPath()), "cm_sharing", rootSession, false, installUser));
                config.setCmVotingLunLinkPath(createSoftLink(new LunPathManager(
                        config.getCmVotingLunPath()), "cm_voting", rootSession, false, installUser));

                if (Objects.nonNull(rootSession) && rootSession.isConnected()) {
                    rootSession.disconnect();
                }
            }
        } catch (Exception e) {
            log.error("create soft link failed", e);
            throw new OpsException("create soft link failed");
        }
    }

    private void doInstall(InstallContext installContext) {
        wsUtil.sendText(installContext.getRetSession(), "START_GEN_XML_CONFIG");
        createSoftLink(installContext);
        String xml = generateClusterConfigXmlInstance.generate(installContext);
        log.info("Generated xml information：{}", xml);
        wsUtil.sendText(installContext.getRetSession(), "END_GEN_XML_CONFIG");

        WsSession retSession = installContext.getRetSession();

        // Master node configuration information
        EnterpriseInstallNodeConfig masterNodeConfig = installContext.getEnterpriseInstallConfig().getNodeConfigList().stream().filter(nodeConfig -> nodeConfig.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("Master node information not found"));
        String masterHostId = masterNodeConfig.getHostId();

        for (HostInfoHolder hostInfoHolder : installContext.getHostInfoHolders()) {
            OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
            Session currentRoot = loginWithUser(jschUtil, encryptionUtils,
                    installContext.getHostInfoHolders(), true, hostEntity.getHostId(), null);
            installDependency( jschUtil, currentRoot, retSession, installContext.getOs());
            currentRoot.disconnect();
        }

        // root
        Session rootSession = loginWithUser(jschUtil, encryptionUtils, installContext.getHostInfoHolders(), true, masterHostId, null);

        String pkgPath = preparePath(installContext.getEnterpriseInstallConfig().getInstallPackagePath());
        ensureDirExist(jschUtil, rootSession, pkgPath, retSession);
        chmodFullPath(jschUtil, rootSession, pkgPath, retSession);
        ensureEnvPathPermission(jschUtil, rootSession, installContext.getEnvPath(), retSession);

        // scp
        wsUtil.sendText(installContext.getRetSession(), "START_SCP_INSTALL_PACKAGE");
        String installPackageFullPath = scpInstallPackageToMasterNode(jschUtil, rootSession, installContext.getInstallPackagePath(), pkgPath, retSession);
        wsUtil.sendText(installContext.getRetSession(), "END_SCP_INSTALL_PACKAGE");

        // unzip install pack
        wsUtil.sendText(installContext.getRetSession(), "START_UNZIP_INSTALL_PACKAGE");
        decompress(jschUtil, rootSession, pkgPath, installPackageFullPath, retSession, "-xvf");

        // unzip OM
        String omPackage = getOMPackage(pkgPath, rootSession);
        decompress(jschUtil, rootSession, pkgPath,
                pkgPath + System.getProperty("file.separator") + omPackage, retSession, "-zxvf");
        wsUtil.sendText(installContext.getRetSession(), "END_UNZIP_INSTALL_PACKAGE");

        // write xml
        String xmlConfigFullPath = pkgPath + "/cluster_config.xml";
        String createClusterConfigXmlCommand = MessageFormat.format(SshCommandConstants.FILE_CREATE, xml, xmlConfigFullPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(createClusterConfigXmlCommand, rootSession, retSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to create xml configuration file, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to create xml configuration file");
            }
        } catch (Exception e) {
            log.error("Failed to create xml configuration file：", e);
            throw new OpsException("Failed to create xml configuration file");
        }

        String group = null;
        String userGroupCommand = "groups " + masterNodeConfig.getInstallUsername() + " | awk -F ':' '{print $2}' | sed 's/\\\"//g'";
        try {
            JschResult jschResult = jschUtil.executeCommand(userGroupCommand, rootSession, retSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Querying user group failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to query user group");
            }

            group = jschResult.getResult();
        } catch (Exception e) {
            log.error("Failed to query user group：", e);
            throw new OpsException("Failed to query user group");
        }

        List<HostInfoHolder> hostInfoHolders = installContext.getHostInfoHolders();
        HostInfoHolder masterHostHolder = hostInfoHolders
                .stream()
                .filter(holder -> holder.getHostEntity().getHostId().equals(masterHostId))
                .findFirst()
                .orElseThrow(() -> new OpsException("Master node host information not found"));
        OpsHostUserEntity masterRootUserInfo = masterHostHolder
                .getHostUserEntities()
                .stream()
                .filter(hostUser -> hostUser.getUsername().equals("root"))
                .findFirst()
                .orElseThrow(
                        () -> new OpsException("The root user information of the primary node cannot be found"));
        OpsHostUserEntity installUserInfo = masterHostHolder.getHostUserEntities().stream().filter(hostUser -> hostUser.getHostUserId().equals(masterNodeConfig.getInstallUserId())).findFirst().orElseThrow(() -> new OpsException("No installation user information found"));

        wsUtil.sendText(installContext.getRetSession(), "START_EXE_PREINSTALL_COMMAND");
        preInstall(group, pkgPath, masterNodeConfig, xmlConfigFullPath, rootSession, retSession,
            masterRootUserInfo.getPassword(), installUserInfo.getPassword(), installContext.getEnvPath());
        wsUtil.sendText(installContext.getRetSession(), "END_EXE_PREINSTALL_COMMAND");

        Session ommSession = loginWithUser(jschUtil, encryptionUtils, installContext.getHostInfoHolders(), false, masterHostId, masterNodeConfig.getInstallUserId());

        // install
        wsUtil.sendText(installContext.getRetSession(), "START_EXE_INSTALL_COMMAND");
        String installCommand = MessageFormat.format(SshCommandConstants.ENTERPRISE_INSTALL, xmlConfigFullPath);
        try {
            String dbPassport = encryptionUtils.decrypt(
                installContext.getEnterpriseInstallConfig().getDatabasePassword());
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("(yes/no)?", "yes");
            autoResponse.put("Please enter password for database:", dbPassport);
            autoResponse.put("Please repeat for database:", dbPassport);

            JschResult jschResult = jschUtil.executeCommand(installContext.getEnvPath(), installCommand, ommSession, retSession, autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("install failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("install failed");
            }
        } catch (Exception e) {
            log.error("install failed：", e);
            throw new OpsException("install failed");
        }
        wsUtil.sendText(installContext.getRetSession(), "END_EXE_INSTALL_COMMAND");
    }

    private void chown(Session rootSession, String installUserName, String targetPath, WsSession wsSession) {
        String chown = MessageFormat.format(SshCommandConstants.CHOWN, installUserName, targetPath);

        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(chown, rootSession, wsSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to grant permission, exit code: {}, error message: {}",
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to grant permission");
            }
        } catch (IOException e) {
            log.error("Failed to grant permission", e);
            throw new OpsException("Failed to grant permission");
        }
    }

    @Override
    public void upgrade(UpgradeContext upgradeContext) {
        checkUpgrade(upgradeContext);

        Session rootSession = null;
        Session ommSession = null;
        try {
            rootSession = jschUtil.getSession(upgradeContext.getHostPublicIp(),
                    upgradeContext.getHostPort(),
                    "root",
                    encryptionUtils.decrypt(upgradeContext.getRootPassword()))
                    .orElseThrow(
                            () -> new OpsException("The root user failed to establish a connection"));
            ommSession = jschUtil.getSession(upgradeContext.getHostPublicIp(), upgradeContext.getHostPort(), upgradeContext.getInstallUsername(), encryptionUtils.decrypt(upgradeContext.getInstallUserPassword())).orElseThrow(() -> new OpsException("Install user connection connection failed"));
            enableStreamReplication(ommSession, upgradeContext);
            checkClusterStatus(ommSession, upgradeContext);
            upgradePreinstall(rootSession, upgradeContext);
            doUpgrade(ommSession, upgradeContext);
            upgradeCheck(ommSession, upgradeContext);
            // upgradeCommit(ommSession, upgradeContext);
            // updateClusterVersion(upgradeContext);
        } finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()) {
                rootSession.disconnect();
            }

            if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                ommSession.disconnect();
            }
        }
    }

    private void updateClusterVersion(UpgradeContext upgradeContext) {
        LambdaUpdateWrapper<OpsClusterEntity> updateWrapper = Wrappers.lambdaUpdate(OpsClusterEntity.class)
                .set(OpsClusterEntity::getVersionNum, upgradeContext.getVersionNum())
                .eq(OpsClusterEntity::getClusterId, upgradeContext.getClusterEntity().getClusterId());
        opsClusterService.update(updateWrapper);
    }

    private void upgradeCommit(Session ommSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "COMMIT_UPGRADE");
        try {
            String command = "gs_upgradectl -t commit-upgrade  -X " + upgradeContext.getClusterConfigXmlPath();
            JschResult jschResult = jschUtil.executeCommand(command, ommSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("upgradeCommit failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("upgradeCommit failed");
            }
        } catch (Exception e) {
            log.error("upgradeCommit failed", e);
            throw new OpsException("upgradeCommit failed");
        }
    }

    private void upgradeCheck(Session ommSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "UPGRADE_CHECK_CLUSTER_STATUS");
        try {
            String command = "gs_om -t status";
            JschResult jschResult = jschUtil.executeCommand(command,upgradeContext.getSepEnvFile(), ommSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("upgradeCheck failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("upgradeCheck failed");
            }

            String result = jschResult.getResult();
            if (!result.contains("cluster_state")
                    || !result.substring(result.indexOf("cluster_state"),
                    result.indexOf("\n",result.indexOf("cluster_state")))
                    .contains("Normal")) {
                log.error("cluster status:{}", result);
                throw new OpsException("cluster status check fail");
            }
        } catch (Exception e) {
            log.error("upgradeCheck failed", e);
            throw new OpsException("upgradeCheck failed");
        }
    }

    private void doUpgrade(Session ommSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "DO_UPGRADE");
        try {
            String command = "gs_upgradectl -t auto-upgrade -X " + upgradeContext.getClusterConfigXmlPath();
            if (upgradeContext.getUpgradeType() == UpgradeTypeEnum.GRAY_UPGRADE) {
                command = command + " --grey";
            }

            JschResult jschResult = jschUtil.executeCommand(command,upgradeContext.getSepEnvFile(), ommSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("gs_upgradectl failed, exit code: {}, error message: {}",
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("gs_upgradectl failed");
            }
        } catch (Exception e) {
            log.error("gs_upgradectl failed", e);
            throw new OpsException("gs_upgradectl failed");
        }
    }

    private void checkUpgrade(UpgradeContext upgradeContext) {
        if (upgradeContext.getUpgradeType() == null) {
            throw new OpsException("Upgrade context is null");
        }

        if (upgradeContext.getClusterEntity() == null) {
            throw new OpsException("Upgrade cluster entity is null");
        }
        String clusterId = upgradeContext.getClusterEntity().getClusterId();
        if (StrUtil.isEmpty(clusterId)) {
            throw new OpsException("Upgrade cluster id is null");
        }
        OpsClusterEntity clusterEntity = opsClusterService.getById(clusterId);
        if (clusterEntity == null) {
            throw new OpsException("Upgrade cluster entity not exist, cluster id: " + clusterId);
        }
        upgradeContext.setClusterConfigXmlPath(clusterEntity.getXmlConfigPath());
        upgradeContext.setSepEnvFile(clusterEntity.getEnvPath());

        String upgradePackagePath = upgradeContext.getUpgradePackagePath();
        Integer adminId = 1;
        String uploadPath = sysSettingService.getSetting(adminId).getUploadPath();
        if (StrUtil.isEmpty(upgradePackagePath)) {
            throw new OpsException("Upgrade package path is null");
        }
        if (!upgradePackagePath.startsWith(uploadPath)) {
            throw new OpsException("Upgrade package path is not invalid, package path is not in datakit system upload "
                    + "path, upload path: " + uploadPath + ", upgrade package path: " + upgradePackagePath);
        }
        Path sourcePath = Paths.get(upgradePackagePath);
        if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
            log.error("Upgrade source package not found, source path: {}", sourcePath);
            throw new OpsException("Upgrade source package not found, source path: " + sourcePath);
        }

        if (upgradeContext.getRetSession() == null) {
            throw new OpsException("Upgrade ret session is null");
        }
    }

    private void upgradePreinstall(Session rootSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "PREINSTALL");
        String installUser = upgradeContext.getInstallUsername();
        String installUserHome = getInstallUserHome(installUser, rootSession, upgradeContext);
        String targetPath = generateTargetPath(installUser, installUserHome);

        prepareUpgradePkgPath(targetPath, rootSession, upgradeContext);
        checkUpgradePkgPathSize(targetPath, rootSession, upgradeContext);
        uploadUpgradePackage(targetPath, rootSession, upgradeContext);

        decompress(jschUtil, rootSession, targetPath,
                targetPath + System.getProperty("file.separator")
                        + FileUtil.getName(upgradeContext.getUpgradePackagePath()),
                upgradeContext.getRetSession(), "-xvf");

        String omPackage = getOMPackage(targetPath, rootSession);
        decompress(jschUtil, rootSession, targetPath,
                targetPath + System.getProperty("file.separator") + omPackage,
                upgradeContext.getRetSession(), "-zxvf");

        String group = getUserGroup(installUser, rootSession, upgradeContext);
        changeUpgradePathPermission(installUser, group, targetPath, rootSession, upgradeContext);
        executeUpgradePreinstall(installUser, group, targetPath, rootSession, upgradeContext);
    }

    private void executeUpgradePreinstall(
            String installUser, String group, String targetPath, Session rootSession, UpgradeContext upgradeContext) {
        try {
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("cd ").append(targetPath).append("/script && ./gs_preinstall -U ").append(installUser)
                    .append(" -G ").append(group).append("  -X ").append(upgradeContext.getClusterConfigXmlPath())
                    .append(" --non-interactive");
            String sepEnvFile = upgradeContext.getSepEnvFile();
            if (StrUtil.isNotEmpty(sepEnvFile)) {
                commandBuilder.append(" --sep-env-file=").append(sepEnvFile);
            }

            JschResult jschResult = jschUtil.executeCommand(commandBuilder.toString(), sepEnvFile,
                    rootSession, upgradeContext.getRetSession());
            if (jschResult.getExitCode() != 0) {
                log.error("Execute upgrade preinstall failed, exit code: {}, error message: {}",
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Execute upgrade preinstall failed");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Execute upgrade preinstall failed", e);
            throw new OpsException("Execute upgrade preinstall failed");
        }
    }

    private void changeUpgradePathPermission(
            String installUser, String group, String targetPath, Session rootSession, UpgradeContext upgradeContext) {
        try {
            String chmod = MessageFormat.format(SshCommandConstants.CHMOD, targetPath);
            String chown = MessageFormat.format(SshCommandConstants.CHOWN_USER_GROUP, installUser, group, targetPath);
            JschResult jschResult = jschUtil.executeCommand(chmod + " && " + chown, upgradeContext.getSepEnvFile(),
                    rootSession, upgradeContext.getRetSession());
            if (jschResult.getExitCode() != 0) {
                log.error("Change upgrade path permission failed, exit code: {}, error message: {}",
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Change upgrade path permission failed");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Change upgrade path permission failed", e);
            throw new OpsException("Change upgrade path permission failed");
        }
    }

    private void uploadUpgradePackage(String targetPath, Session rootSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "BEFORE_UPLOAD_PACKAGE");
        jschUtil.upload(rootSession, upgradeContext.getRetSession(), upgradeContext.getUpgradePackagePath(),
                String.format("%s/%s", targetPath, FileUtil.getName(upgradeContext.getUpgradePackagePath())));
        wsUtil.sendText(upgradeContext.getRetSession(), "END_UPLOAD_PACKAGE");
    }

    private void checkUpgradePkgPathSize(String targetPath, Session rootSession, UpgradeContext upgradeContext) {
        try {
            String command = String.format("df %s | awk 'NR==2 {print $4}'", targetPath);
            JschResult jschResult = jschUtil.executeCommand(command, upgradeContext.getSepEnvFile(), rootSession,
                    upgradeContext.getRetSession());
            if (jschResult.getExitCode() != 0) {
                log.error("Failed to check the remaining space in upgrade package storage directory, path: {}, "
                        + "error: {}", targetPath, jschResult.getResult());
                throw new OpsException("Failed to check the remaining space in upgrade package storage directory");
            }
            long targetPathSize = Long.parseLong(jschResult.getResult().trim());

            Path sourcePath = Paths.get(upgradeContext.getUpgradePackagePath());
            if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
                log.error("Upgrade source package not found, source path: {}", sourcePath);
                throw new OpsException("Upgrade source package not found, source path: " + sourcePath);
            }
            long sourcePkgSize = Files.size(sourcePath);

            if (targetPathSize * 1024 <= sourcePkgSize * 3) {
                String errorMsg = String.format("The remaining space is not enough to upload upgrade package, "
                                + "path: %s, at least: %sB", targetPath, sourcePkgSize * 3);
                log.error(errorMsg);
                throw new OpsException(errorMsg);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to check the remaining space in upgrade package storage directory", e);
            throw new OpsException("Failed to check the remaining space in upgrade package storage directory");
        }
    }

    private void prepareUpgradePkgPath(String targetPath, Session rootSession, UpgradeContext upgradeContext) {
        try {
            String command = MessageFormat.format("[ -d \"{0}\" ] && rm -rf \"{0}\"/* 2>/dev/null || mkdir -p \"{0}\"",
                    targetPath);
            JschResult jschResult = jschUtil.executeCommand(command, upgradeContext.getSepEnvFile(), rootSession,
                    upgradeContext.getRetSession());
            if (jschResult.getExitCode() != 0) {
                log.error("Prepare upgrade package path failed, exit code: {}, error message: {}",
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Prepare upgrade package path failed");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Prepare upgrade package path failed", e);
            throw new OpsException("Prepare upgrade package path failed");
        }
    }

    private String getUserGroup(String installUser, Session rootSession, UpgradeContext upgradeContext) {
        String userGroupCommand = "groups " + installUser + " | awk -F ':' '{print $2}' | sed 's/\\\"//g'";
        try {
            JschResult jschResult = jschUtil.executeCommand(userGroupCommand, upgradeContext.getSepEnvFile(),
                    rootSession, upgradeContext.getRetSession());
            if (jschResult.getExitCode() != 0) {
                log.error("Querying user group failed, exit code: {}, error message: {}", jschResult.getExitCode(),
                        jschResult.getResult());
                throw new OpsException("Failed to query user group");
            }
            return jschResult.getResult().trim();
        } catch (IOException | InterruptedException e) {
            log.error("Failed to query user group. Error: {}", e.getMessage());
            throw new OpsException("Failed to query user group.");
        }
    }

    private String generateTargetPath(String installUser, String installUserHome) {
        String homePath = installUserHome.trim();
        if (StrUtil.isEmpty(homePath)) {
            homePath = "/home/" + installUser;
        }
        return String.format("%s/gaussdb_upgrade", homePath);
    }

    private String getInstallUserHome(String installUser, Session rootSession, UpgradeContext upgradeContext) {
        String command = MessageFormat.format(SshCommandConstants.GET_INSTALL_USER_HOME, installUser);
        try {
            JschResult jschResult = jschUtil.executeCommand(command, upgradeContext.getSepEnvFile(), rootSession,
                    upgradeContext.getRetSession());
            if (jschResult.getExitCode() != 0) {
                log.error("Failed to obtain the install user home by command: {}. Exit code: {}",
                        command, jschResult.getExitCode());
                throw new OpsException("Failed to obtain the install user home.");
            }
            return jschResult.getResult().trim();
        } catch (IOException | InterruptedException e) {
            log.error("Failed to obtain the install user home by command: {}. Error: {}", command, e.getMessage());
            throw new OpsException("Failed to obtain the install user home.");
        }
    }

    private String getOMPackage(String path, Session rootSession) {
        String command = MessageFormat.format(SshCommandConstants.GET_OM_PACKAGE, path);

        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);

            if (jschResult.getExitCode() != 0) {
                log.error("Failed to obtain the OM package by command: {}. Exit code: {}",
                        command, jschResult.getExitCode());
                throw new OpsException("Failed to obtain the OM package.");
            }

            String commandResult = jschResult.getResult().trim();

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
        } catch (IOException | InterruptedException e) {
            log.error("Failed to obtain the OM package by command: {}. Error: {}", command, e.getMessage());
            throw new OpsException("Failed to obtain the OM package.");
        }
    }

    private void checkClusterStatus(Session ommSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "CHECK_CLUSTER_STATUS");
        try {
            String command = "gs_om -t status";
            JschResult jschResult = jschUtil.executeCommand(command, upgradeContext.getSepEnvFile(), ommSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("checkClusterStatus failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("checkClusterStatus failed");
            }

            String result = jschResult.getResult();
            if (!result.contains("cluster_state") || !result.substring(result.indexOf("cluster_state"), result.indexOf("\n",result.indexOf("cluster_state"))).contains("Normal")) {
                log.error("cluster status:{}", result);
                throw new OpsException("cluster status check fail");
            }
        } catch (Exception e) {
            log.error("checkClusterStatus failed", e);
            throw new OpsException("checkClusterStatus failed");
        }
    }

    private void enableStreamReplication(Session rootSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "ENABLE_STREAM_REPLICATION");
        OpsClusterNodeEntity opsClusterNodeEntity = upgradeContext.getOpsClusterNodeEntity();
        gucCheck(opsClusterNodeEntity, rootSession, upgradeContext);

        gucReload(rootSession, upgradeContext);
    }

    private void gucReload(Session rootSession, UpgradeContext upgradeContext) {
        try {
            String command = "gs_guc reload -I all -c \"enable_stream_replication=on\"";
            JschResult jschResult = jschUtil.executeCommand(command,upgradeContext.getSepEnvFile(), rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("enableStreamReplication failed, exit code: {}, error message: {}",
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("enableStreamReplication failed");
            }
        } catch (Exception e) {
            log.error("enableStreamReplication failed", e);
            throw new OpsException("enableStreamReplication failed");
        }

        try {
            String command = "gs_guc check -I all -c \"enable_stream_replication\"";
            JschResult jschResult = jschUtil.executeCommand(command,upgradeContext.getSepEnvFile(), rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("enableStreamReplication failed, exit code: {}, error message: {}",
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("enableStreamReplication failed");
            }
        } catch (Exception e) {
            log.error("enableStreamReplication failed", e);
            throw new OpsException("enableStreamReplication failed");
        }
    }

    private void gucCheck(OpsClusterNodeEntity opsClusterNodeEntity, Session rootSession, UpgradeContext upgradeContext) {
        try {
            String command = "gs_guc reload -D " + opsClusterNodeEntity.getDataPath() + " -c \"enable_stream_replication=on\"";
            JschResult jschResult = jschUtil.executeCommand(command, upgradeContext.getSepEnvFile(), rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("enableStreamReplication failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("enableStreamReplication failed");
            }
        } catch (Exception e) {
            log.error("enableStreamReplication failed", e);
            throw new OpsException("enableStreamReplication failed");
        }

        try {
            String command = "gs_guc check -D "
                    + opsClusterNodeEntity.getDataPath()
                    + " -c \"enable_stream_replication\"";
            JschResult jschResult = jschUtil.executeCommand(command, upgradeContext.getSepEnvFile(),rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("enableStreamReplication failed, exit code: {}, error message: {}",
                        jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("enableStreamReplication failed");
            }
        } catch (Exception e) {
            log.error("enableStreamReplication failed", e);
            throw new OpsException("enableStreamReplication failed");
        }
    }

    private void preInstall(String group, String pkgPath, EnterpriseInstallNodeConfig masterNodeConfig, String xmlConfigFullPath, Session rootSession, WsSession retSession, String rootPassword, String installUserPassword, String envPath) {
        String installUsername = masterNodeConfig.getInstallUsername();
        String gsPreInstall = MessageFormat.format(SshCommandConstants.GS_PREINSTALL_INTERACTIVE,
                pkgPath + "/script/", installUsername,
                group, xmlConfigFullPath);
        gsPreInstall = wrapperEnvSep(gsPreInstall, envPath);
        try {
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("(yes/no)?", "yes");
            autoResponse.put("Please enter password for root\r\nPassword:", encryptionUtils.decrypt(rootPassword));
            autoResponse.put("Please enter password for current user[" + installUsername + "].\r\nPassword:",
                encryptionUtils.decrypt(installUserPassword));
            // for compatibility with 5.1.0
            autoResponse.put("Please enter password for current user[root].\r\nPassword:",
                encryptionUtils.decrypt(rootPassword));
            JschResult jschResult = jschUtil.executeCommand(gsPreInstall, rootSession, retSession, autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("gs_preinstall failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("gs_preinstall failed");
            }
        } catch (Exception e) {
            log.error("gs_preinstall failed：", e);
            throw new OpsException("gs_preinstall failed");
        }
    }

    private void saveContext(InstallContext installContext) {
        OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
        opsClusterEntity.setDatabasePassword(encryptionUtils.encrypt(opsClusterEntity.getDatabasePassword()));
        List<OpsClusterNodeEntity> opsClusterNodeEntities = installContext.getEnterpriseInstallConfig().toOpsClusterNodeEntityList();

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

        OpsClusterNodeEntity opsClusterNodeEntity = unInstallContext.getOpsClusterNodeEntityList()
                .stream()
                .filter(opsClusterNodeEntity1 -> opsClusterNodeEntity1.getClusterRole() == ClusterRoleEnum.MASTER)
                .findFirst().orElseThrow(() -> new OpsException("master node not found"));

        HostInfoHolder hostInfoHolder = unInstallContext.getHostInfoHolders().stream().filter(hostInfoHolder1 -> hostInfoHolder1.getHostEntity().getHostId().equalsIgnoreCase(opsClusterNodeEntity.getHostId())).findFirst().orElseThrow(() -> new OpsException("host information not found"));
        OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
        OpsHostUserEntity hostUserEntity = hostInfoHolder.getHostUserEntities()
                .stream()
                .filter(userInfo -> opsClusterNodeEntity.getInstallUserId()
                        .equals(userInfo.getHostUserId())).findFirst()
                .orElseThrow(() -> new OpsException("No install user info user found"));
        Session session = sshLogin(jschUtil, encryptionUtils, hostEntity, hostUserEntity);

        String uninstallCommand = "gs_uninstall --delete-data";
        WsSession retSession = unInstallContext.getRetSession();
        try {
            JschResult jschResult = jschUtil.executeCommand(uninstallCommand,
                    opsClusterEntity.getEnvPath(), session, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("Uninstall error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("Uninstall error", e);
            throw new OpsException("Uninstall error");
        }

        try {
            Optional<OpsHostUserEntity> rootUserEntity = hostInfoHolder.getHostUserEntities()
                    .stream()
                    .filter(userEntity -> "root".equalsIgnoreCase(userEntity.getUsername()))
                    .findFirst();

            CleanEnvClass cleanEnvParam = CleanEnvClass.builder()
                    .hostInfoHolders(unInstallContext.getHostInfoHolders())
                    .hostEntity(hostEntity)
                    .rootUserEntity(rootUserEntity)
                    .hostUserEntity(hostUserEntity)
                    .retSession(retSession)
                    .installPackagePath(opsClusterEntity.getInstallPackagePath())
                    .xmlConfigPath(opsClusterEntity.getXmlConfigPath())
                    .envPath(opsClusterEntity.getEnvPath())
                    .build();

            cleanEnv(cleanEnvParam);
            wsUtil.sendText(retSession, "\nENV_CLEAN_SUCCESS\n");
        } catch (Exception e) {
            log.error("env clean fail:", e);
            wsUtil.sendText(retSession, "\nENV_CLEAN_FAIL\n");
        }

        removeSoftLinkAll(unInstallContext);
        removeContext(unInstallContext);
    }

    @Data
    @Builder
    static class CleanEnvClass {
        private List<HostInfoHolder> hostInfoHolders;
        private OpsHostEntity hostEntity;
        private Optional<OpsHostUserEntity> rootUserEntity;
        private OpsHostUserEntity hostUserEntity;
        private WsSession retSession;
        private String installPackagePath;
        private String xmlConfigPath;
        private String envPath;
    }

    private void cleanEnv(CleanEnvClass param) throws IOException, InterruptedException {
        OpsHostEntity hostEntity = param.getHostEntity();
        Optional<OpsHostUserEntity> rootUserEntityOption = param.getRootUserEntity();
        OpsHostUserEntity hostUserEntity = param.getHostUserEntity();
        WsSession retSession = param.getRetSession();
        String installPackagePath = param.getInstallPackagePath();
        String xmlConfigPath = param.getXmlConfigPath();
        String envPath = param.getEnvPath();

        OpsHostUserEntity rootUserEntity = rootUserEntityOption
                .orElseThrow(() -> new OpsException("root user information not found"));
        final Session rootSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), rootUserEntity.getUsername(), encryptionUtils.decrypt(rootUserEntity.getPassword())).orElseThrow(() -> new OpsException("The root user failed to establish a connection"));

        try {
            String commandTemplate = "cd {0} && ./gs_postuninstall -U {1} -X {2}";

            String command = MessageFormat.format(commandTemplate, installPackagePath + "/script", hostUserEntity.getUsername(), xmlConfigPath);
            Map<String, String> authResponse = new HashMap<>();
            authResponse.put("(yes/no)?", "yes");
            authResponse.put("Password:", encryptionUtils.decrypt(rootUserEntity.getPassword()));
            final JschResult jschResult = jschUtil.executeCommand(envPath, command, rootSession, retSession, authResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("clean env fail,exitCode:{},exitMsg:{}", jschResult.getExitCode(), jschResult.getExitCode());
                throw new OpsException("clean env fail");
            }
        } finally {
            rootSession.disconnect();
        }

        List<HostInfoHolder> hostInfoHolders = param.getHostInfoHolders();
        rmSsh(hostInfoHolders, retSession);
    }

    private void rmSsh(List<HostInfoHolder> hostInfoHolders,
        WsSession retSession) throws IOException, InterruptedException {
        String delMutualTrustCommand = "rm -rf ~/.ssh";
        for (HostInfoHolder hostInfoHolder : hostInfoHolders) {
            OpsHostEntity currentHost = hostInfoHolder.getHostEntity();
            List<OpsHostUserEntity> hostUserEntities = hostInfoHolder.getHostUserEntities();
            final OpsHostUserEntity rootUser = hostUserEntities.stream().filter(user -> "root".equalsIgnoreCase(user.getUsername())).findFirst().orElseThrow(() -> new OpsException("root user information not found"));

            final Session session = jschUtil.getSession(currentHost.getPublicIp(), currentHost.getPort(), rootUser.getUsername(), encryptionUtils.decrypt(rootUser.getPassword())).orElseThrow(() -> new OpsException("The root user failed to establish a connection"));
            try {
                final JschResult jschResult = jschUtil.executeCommand(delMutualTrustCommand, session, retSession);
                if (0 != jschResult.getExitCode()) {
                    log.error("del MutualTrust fail,exitCode:{},exitMsg:{}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("del MutualTrust fail");
                }
            } finally {
                session.disconnect();
            }
        }
    }

    private void removeContext(UnInstallContext unInstallContext) {
        OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
        opsClusterService.removeById(opsClusterEntity.getClusterId());

        List<OpsClusterNodeEntity> opsClusterNodeEntityList = unInstallContext.getOpsClusterNodeEntityList();
        opsClusterNodeService.removeBatchByIds(opsClusterNodeEntityList.stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList()));
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
        WsSession retSession = opsClusterContext.getRetSession();
        Session ommUserSession = loginWithUser(jschUtil, encryptionUtils, opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());
        OmStatusModel omStatusModel = omStatus(jschUtil, ommUserSession, retSession, opsClusterContext.getOpsClusterEntity().getEnvPath());
        if (Objects.isNull(omStatusModel)) {
            throw new OpsException("gs_om status fail");
        }

        String command = null;
        if (omStatusModel.isInstallCm()) {
            command = "cm_ctl stop && cm_ctl start";
        } else {
            command = "gs_om -t restart";
        }

        try {
            JschResult jschResult = jschUtil.executeCommand(command, opsClusterContext.getOpsClusterEntity().getEnvPath(), ommUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("startup error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("An exception occurred during startup", e);
            throw new OpsException("An exception occurred during startup");
        }
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
        WsSession retSession = opsClusterContext.getRetSession();
        Session ommUserSession = loginWithUser(jschUtil, encryptionUtils, opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());
        OmStatusModel omStatusModel = omStatus(jschUtil, ommUserSession, retSession, opsClusterContext.getOpsClusterEntity().getEnvPath());
        if (Objects.isNull(omStatusModel)) {
            throw new OpsException("gs_om status fail");
        }

        String command = null;
        if (omStatusModel.isInstallCm()) {
            command = "cm_ctl start";
        } else {
            command = "gs_om -t start";
        }

        try {
            JschResult jschResult = jschUtil.executeCommand(command, opsClusterContext.getOpsClusterEntity().getEnvPath(), ommUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("startup error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("An exception occurred during startup", e);
            throw new OpsException("An exception occurred during startup");
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
        Session ommUserSession = loginWithUser(jschUtil, encryptionUtils, opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());

        OmStatusModel omStatusModel = omStatus(jschUtil, ommUserSession, retSession, opsClusterContext.getOpsClusterEntity().getEnvPath());
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

        try {
            JschResult jschResult = jschUtil.executeCommand(command, opsClusterContext.getOpsClusterEntity().getEnvPath(), ommUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("startup error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("An exception occurred during startup", e);
            throw new OpsException("An exception occurred during startup");
        }
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
        WsSession retSession = opsClusterContext.getRetSession();
        Session ommUserSession = loginWithUser(jschUtil, encryptionUtils, opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());
        OmStatusModel omStatusModel = omStatus(jschUtil, ommUserSession, retSession, opsClusterContext.getOpsClusterEntity().getEnvPath());
        if (Objects.isNull(omStatusModel)) {
            throw new OpsException("gs_om status fail");
        }

        String command = null;
        if (omStatusModel.isInstallCm()) {
            command = "cm_ctl stop";
        } else {
            command = "gs_om -t stop";
        }

        try {
            JschResult jschResult = jschUtil.executeCommand(command, opsClusterContext.getOpsClusterEntity().getEnvPath(), ommUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("startup error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("An exception occurred during startup", e);
            throw new OpsException("An exception occurred during startup");
        }
    }

    @Override
    public void enableWdrSnapshot(Session session, OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities, WdrScopeEnum scope, String dataPath) {
        String checkCommand;
        if (StrUtil.isEmpty(dataPath)) {
            checkCommand = "gs_guc check -I all -c \"enable_wdr_snapshot\"";
        } else {
            checkCommand = "gs_guc check -D " + dataPath + " -c \"enable_wdr_snapshot\"";
        }
        try {
            JschResult jschResult = jschUtil.executeCommand(checkCommand, session, clusterEntity.getEnvPath());

            if (jschResult.getResult().contains("enable_wdr_snapshot=on")) {
                return;
            }
        } catch (Exception e) {
            log.error("Failed to set the enable_wdr_snapshot parameter", e);
            throw new OpsException("Failed to set the enable_wdr_snapshot parameter");
        }

        String command;
        if (StrUtil.isEmpty(dataPath)) {
            command = "gs_guc reload -I all -c \"enable_wdr_snapshot=on\"";
        } else {
            command = "gs_guc reload -D " + dataPath + " -c \"enable_wdr_snapshot=on\"";
        }

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
            log.error("No stop node information found:{}", stopNodeId);
        }

        WsSession retSession = opsClusterContext.getRetSession();
        String dataPath = stopNodeEntity.getDataPath();
        log.info("login stop user");
        Session ommUserSession = loginWithUser(jschUtil, encryptionUtils, opsClusterContext.getHostInfoHolders(), false, stopNodeEntity.getHostId(), stopNodeEntity.getInstallUserId());

        OmStatusModel omStatusModel = omStatus(jschUtil, ommUserSession, retSession, opsClusterContext.getOpsClusterEntity().getEnvPath());
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

        try {
            JschResult jschResult = jschUtil.executeCommand(command, opsClusterContext.getOpsClusterEntity().getEnvPath(), ommUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("stop error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("An exception occurred during the stop process", e);
            throw new OpsException("stop exception");
        }
    }
}

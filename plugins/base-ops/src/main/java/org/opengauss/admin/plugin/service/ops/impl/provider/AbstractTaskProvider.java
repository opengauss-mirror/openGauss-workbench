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

import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.DatabaseKernelArch;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.service.ops.ClusterTaskProvider;
import org.opengauss.admin.plugin.service.ops.impl.ClusterTaskProviderManager;
import org.opengauss.admin.plugin.service.ops.impl.OpsHostRemoteService;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AbstractTaskProvider
 *
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Slf4j
public abstract class AbstractTaskProvider implements ClusterTaskProvider, InitializingBean {
    @Resource
    protected OpsHostRemoteService opsHostRemoteService;

    protected Session beforeInstall(InstallContext installContext, String installPath, String dataPath, String pkgPath,
                                    String hostId, String installUserId, String installUserName, String decompressArgs) {
        RetBuffer retBuffer = installContext.getRetBuffer();

        log.info("The root user logs in to the host");
        // root session
        HostInfoHolder hostInfoHolder = getHostInfoHolder(installContext, hostId);
        Session rootSession = createSessionWithRootUser(hostInfoHolder);
        try {
            installDependency(rootSession, retBuffer, installContext.getOs());
            ensureDirExist(rootSession, pkgPath, retBuffer);
            ensureDirExist(rootSession, installPath, retBuffer);
            ensureDirExist(rootSession, dataPath, retBuffer);
            ensureLimits(rootSession, retBuffer);
            retBuffer.sendText("START_SCP_INSTALL_PACKAGE");

            log.info("Copy the installation package to the target host");
            // scp
            String installPackageFullPath = scpInstallPackageToMasterNode(rootSession, installContext.getInstallPackageLocalPath(), pkgPath, retBuffer);
            retBuffer.sendText("END_SCP_INSTALL_PACKAGE");


            log.info("set kernel.sem");
            // SEM
            sem(rootSession, retBuffer);

            log.info("Unzip the installation package");
            // unzip
            retBuffer.sendText("START_UNZIP_INSTALL_PACKAGE");
            decompress(rootSession, installPath, installPackageFullPath, retBuffer, decompressArgs);
            retBuffer.sendText("END_UNZIP_INSTALL_PACKAGE");

            ensureStrictPermission(rootSession, installUserName, pkgPath, retBuffer);
            ensureStrictPermission(rootSession, installUserName, installPath, retBuffer);
            ensureLevel2DirPermission(rootSession, installUserName, dataPath, retBuffer);
            ensureDataPathPermission(rootSession, installUserName, dataPath, retBuffer);
            ensureEnvPathPermission(rootSession, installContext.getEnvPath(), retBuffer);
            log.info("Login and install user");
        } finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()) {
                rootSession.disconnect();
            }
        }
        return createSessionWithUserId(installContext.getHostInfoHolders(), false, hostId, installUserId);
    }

    protected void ensureLevel2DirPermission(Session rootSession, String installUserName, String targetPath, RetBuffer retBuffer) {
        String[] split = targetPath.split("/");
        if (split.length >= 3) {
            String targetParent2LevelPath = "/" + split[1] + "/" + split[2];
            String chown = MessageFormat.format(SshCommandConstants.CHOWN_USER_GROUP, installUserName, installUserName, targetParent2LevelPath);
            opsHostRemoteService.executeCommand(chown, rootSession, retBuffer);
        }
    }

    /**
     * Enterpres Version env sep file param name : --sep-env-file
     *
     * @param command command
     * @param envPath envPath
     * @return String
     */
    protected String wrapperEnvSep(String command, String envPath) {
        if (StrUtil.isNotEmpty(envPath)) {
            command = command + " --sep-env-file=" + envPath;
        }
        return command;
    }

    /**
     * Lite Version env sep file param name :  --env-sep-file
     *
     * @param command command
     * @param envPath envPath
     * @return String
     */
    protected String wrapperLiteEnvSep(String command, String envPath) {
        if (StrUtil.isNotEmpty(envPath)) {
            command = command + " --env-sep-file " + envPath;
        }
        return command;
    }

    protected void ensureEnvPathPermission(Session rootSession, String envPath, RetBuffer retSession) {
        if (StrUtil.isEmpty(envPath)) {
            return;
        }
        String touchEnvSep = "touch \"" + envPath + "\"";
        opsHostRemoteService.executeCommand(touchEnvSep, rootSession, retSession, "touch envFile");
        String permissionEnv = "chmod 755 -R " + envPath;
        opsHostRemoteService.executeCommand(permissionEnv, rootSession, retSession, "chmod envFile");
    }

    protected void installDependency(Session rootSession, RetBuffer retBuffer,
                                     OpenGaussSupportOSEnum expectedOs) {
        boolean dependencyCorrect = false;
        String[] dependencyPackageNames = {"libaio-devel", "flex", "bison", "ncurses-devel", "glibc-devel",
                "patch", "readline-devel"};
        try {
            List<String> dependencyPackages = Arrays.stream(dependencyPackageNames).map(
                    dependency -> dependency + "." + expectedOs.getCpuArch()).collect(Collectors.toList());
            String dependency = opsHostRemoteService.executeCommand(SshCommandConstants.DEPENDENCY, rootSession, retBuffer);
            List<String> notInstalledPackages = new ArrayList<>();
            for (String dependencyPackage : dependencyPackages) {
                if (!dependency.contains(dependencyPackage)) {
                    notInstalledPackages.add(dependencyPackage);
                }
            }
            if (notInstalledPackages.isEmpty()) {
                dependencyCorrect = true;
            }
        } catch (Exception e) {
            log.error("Execute dependencyPackageNames command exception：", e);
        }
        if (dependencyCorrect) {
            log.info("dependencyCorrect，skip install dependency");
            return;
        }
        retBuffer.sendText("START_INSTALL_DEPENDENCY");
        boolean errorFlag = false;
        try {
            String command = dependencyCommand(expectedOs);
            opsHostRemoteService.executeCommand(command, rootSession, retBuffer, "install depencency");
        } catch (OpsException e) {
            errorFlag = true;
        }

        if (errorFlag) {
            retBuffer.sendText("error occurred, skip install dependency");
        }
        retBuffer.sendText("END_INSTALL_DEPENDENCY");
    }

    protected void ensureLimits(Session rootSession, RetBuffer retBuffer) {
        String limitsCheck = SshCommandConstants.LIMITS_CHECK;
        String limitsCheckRes = opsHostRemoteService.executeCommand(limitsCheck, rootSession, retBuffer, "Detect ulimit");
        if (StrUtil.isNotEmpty(limitsCheckRes)) {
            return;
        }
        String limits = SshCommandConstants.LIMITS;
        opsHostRemoteService.executeCommand(limits, rootSession, retBuffer, "set ulimit");
    }

    protected String scpInstallPackageToMasterNode(Session rootSession, String sourcePath, String targetPath, RetBuffer retSession) {
        String installPackageFileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
        String installPackageFullPath = targetPath + installPackageFileName;
        opsHostRemoteService.executeUpload(rootSession, retSession, sourcePath, installPackageFullPath);
        return installPackageFullPath;
    }

    protected void sem(Session rootSession, RetBuffer retBuffer) {
        String command = SshCommandConstants.SEM;
        opsHostRemoteService.executeCommand(command, rootSession, retBuffer, "set kernel.sem");
    }

    protected void decompress(Session rootSession, String targetPath, String installPackageFullPath, RetBuffer retBuffer, String decompressArgs) {
        String command = MessageFormat.format(SshCommandConstants.DECOMPRESS, decompressArgs, installPackageFullPath, targetPath);
        opsHostRemoteService.executeCommand(command, rootSession, retBuffer, "Unzip the installation package");
    }

    protected Session createSessionWithUserId(InstallContext installContext, boolean root, String hostId, String userId) {
        HostInfoHolder hostInfoHolder = getHostInfoHolder(installContext, hostId);
        return createSessionWithUserId(hostInfoHolder, root, userId);
    }

    protected Session createSessionWithUserId(HostInfoHolder hostInfoHolder, boolean root, String userId) {
        if (root) {
            return createSessionWithRootUser(hostInfoHolder);
        }
        OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
        OpsHostUserEntity hostUser = getHostUserInfo(hostInfoHolder, userId);
        return opsHostRemoteService.getHostUserSession(hostEntity, hostUser);
    }

    protected Session createSessionWithUserId(List<HostInfoHolder> hostInfoHolders, boolean root, String hostId, String installUserId) {
        HostInfoHolder hostInfoHolder = getHostInfoHolder(hostInfoHolders, hostId);
        return createSessionWithUserId(hostInfoHolder, root, installUserId);
    }

    protected Session createSessionWithRootUser(HostInfoHolder hostInfoHolder) {
        OpsHostUserEntity hostUser = getHostUserInfoByUsername(hostInfoHolder, "root");
        if (Objects.isNull(hostUser) || StrUtil.isEmpty(hostUser.getPassword())) {
            throw new OpsException("root password not found");
        }
        return opsHostRemoteService.getHostUserSession(hostInfoHolder.getHostEntity(), hostUser);
    }

    protected void chmodFullPath(Session rootSession, String path, RetBuffer wsSession) {
        String chmod = MessageFormat.format(SshCommandConstants.CHMOD, path);
        opsHostRemoteService.executeCommand(chmod, rootSession, wsSession, "grant permission");
    }

    protected void ensureDirUserGroupPermission(Session rootSession, String installUserName, String targetPath, RetBuffer wsSession) {
        chmodFullPathAndParentPath(rootSession, targetPath, wsSession);
        String chown = MessageFormat.format(SshCommandConstants.CHOWN_USER_GROUP, installUserName, installUserName, targetPath);
        opsHostRemoteService.executeCommand(chown, rootSession, wsSession, "grant permission");
        try {
        } catch (OpsException e) {
            log.error("Failed to grant permission", e);
            throw new OpsException("Failed to grant permission");
        }
    }

    protected void chmodFullPathAndParentPath(Session rootSession, String path, RetBuffer wsSession) {
        int parentPermission = getPathPermission(rootSession, path, wsSession);
        if (parentPermission < 755) {
            chmodFullPath(rootSession, path, wsSession);
        }
        String parentPath = getParentPath(path);
        if (StrUtil.isNotEmpty(parentPath) && !parentPath.equals("/")) {
            chmodFullPathAndParentPath(rootSession, parentPath, wsSession);
        }
    }

    protected int getPathPermission(Session rootSession, String path, RetBuffer wsSession) {
        String checkPermissionCommand = MessageFormat.format(SshCommandConstants.CHECK_PERMISSION, path);
        int parentPermission = 41;
        try {
            String result = opsHostRemoteService.executeCommand(checkPermissionCommand, rootSession, wsSession);
            String[] lines = result.split("\n");
            if (lines.length > 0) {
                parentPermission = convertPermission(lines[0].substring(0, 10));
            }
        } catch (Exception e) {
            log.error("Failed to grant permission", e);
        }
        return parentPermission;
    }

    private int convertPermission(String permissionString) {
        if (permissionString == null || permissionString.length() != 10) {
            throw new IllegalArgumentException("Invalid permission string");
        }
        int owner = convertOwnerPermission(permissionString);
        int group = convertGroupPermission(permissionString);
        int others = convertOtherPermission(permissionString);
        return Integer.valueOf(owner + String.valueOf(group) + others);
    }

    private int convertOtherPermission(String permissionString) {
        int others = 0;
        if (permissionString.charAt(7) == 'r') {
            others += 4;
        }
        if (permissionString.charAt(8) == 'w') {
            others += 2;
        }
        if (permissionString.charAt(9) == 'x') {
            others += 1;
        }
        return others;
    }

    private int convertGroupPermission(String permissionString) {
        int group = 0;
        if (permissionString.charAt(4) == 'r') {
            group += 4;
        }
        if (permissionString.charAt(5) == 'w') {
            group += 2;
        }
        if (permissionString.charAt(6) == 'x') {
            group += 1;
        }
        return group;
    }

    private int convertOwnerPermission(String permissionString) {
        int owner = 0;
        if (permissionString.charAt(1) == 'r') {
            owner += 4;
        }
        if (permissionString.charAt(2) == 'w') {
            owner += 2;
        }
        if (permissionString.charAt(3) == 'x') {
            owner += 1;
        }
        return owner;
    }

    public static String getParentPath(String dir) {
        String[] split = dir.split("/");
        int length = split.length;
        if (length > 1) {
            String[] subString = new String[length - 1];
            System.arraycopy(split, 0, subString, 0, length - 1);
            return String.join("/", subString);
        }
        return "";
    }

    protected void chmod(Session rootSession, String path, RetBuffer wsSession) {
        if (StrUtil.isNotEmpty(path) && path.indexOf("/", 1) > 0) {
            path = path.substring(0, path.indexOf("/", 1));
        }
        String chmod = MessageFormat.format(SshCommandConstants.CHMOD, path);
        opsHostRemoteService.executeCommand(chmod, rootSession, wsSession, "grant permission");
    }

    protected void chmodDataPath(Session rootSession, String path, RetBuffer wsSession) {
        String chmod = MessageFormat.format(SshCommandConstants.CHMOD_DATA_PATH, path);
        opsHostRemoteService.executeCommand(chmod, rootSession, wsSession, "grant permission");
    }

    protected void ensureStrictPermission(Session rootSession, String installUserName, String targetPath, RetBuffer wsSession) {
        chmodFullPath(rootSession, targetPath, wsSession);
        String chown = MessageFormat.format(SshCommandConstants.CHOWN_USER_GROUP, installUserName, installUserName, targetPath);
        opsHostRemoteService.executeCommand(chown, rootSession, wsSession, "grant permission");
    }

    protected void ensureDataPathPermission(Session rootSession, String installUserName, String targetPath, RetBuffer wsSession) {
        chmodDataPath(rootSession, targetPath, wsSession);
        String chown = MessageFormat.format(SshCommandConstants.CHOWN_USER_GROUP, installUserName, installUserName, targetPath);
        opsHostRemoteService.executeCommand(chown, rootSession, wsSession, "grant permission");
    }

    protected void ensureDirExist(Session rootSession, String targetPath, RetBuffer retBuffer) {
        String command = MessageFormat.format(SshCommandConstants.MK_DIR, targetPath);
        opsHostRemoteService.executeCommand(command, rootSession, retBuffer, "create installation directory");
    }

    protected String preparePath(String path) {
        if (StrUtil.isEmpty(path) || path.endsWith("/")) {
            return path;
        }

        return path + "/";
    }

    protected OmStatusModel omStatus(Session ommUserSession, RetBuffer retBuffer, String envPath) {
        OmStatusModel omStatusModel = new OmStatusModel();
        try {
            String statusCommand = "gs_om -t status --detail";
            statusCommand = addCommandOfLoadEnvironmentVariable(statusCommand, envPath);
            String result = opsHostRemoteService.executeCommand(statusCommand, ommUserSession, retBuffer);
            Map<String, String> nodeIdMapHostname = new HashMap<>();
            Map<String, String> hostnameMapNodeId = new HashMap<>();
            omStatusModel.setInstallCm(result.contains("[  CMServer State   ]"));
            omStatusModel.setNodeIdMapHostname(nodeIdMapHostname);
            omStatusModel.setHostnameMapNodeId(hostnameMapNodeId);

            if (omStatusModel.isInstallCm()) {
                int datanodeStateIndex = result.indexOf("[  CMServer State   ]");
                if (datanodeStateIndex < 0) {

                } else {
                    int splitIndex = result.indexOf("------------------", datanodeStateIndex);
                    String dataNodeStateStr = result.substring(splitIndex);
                    String[] dataNode = dataNodeStateStr.split("\n");
                    for (String s : dataNode) {
                        if (StrUtil.isEmpty(s)) {
                            break;
                        }
                        String[] s1 = s.replaceAll(" +", " ").split(" ");
                        if (s1.length > 2) {
                            nodeIdMapHostname.put(s1[0], s1[1]);
                            hostnameMapNodeId.put(s1[1], s1[0]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("An exception occurred during startup,gs_om status fail", e);
            throw new OpsException("An exception occurred during startup,gs_om status fail");
        }

        return omStatusModel;
    }

    protected void createEnterpriseRemoteUser(InstallContext installContext, OpsClusterContext opsClusterContext) {
        RetBuffer retSession = installContext.getRetBuffer();
        opsClusterContext.setRetBuffer(retSession);
        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());

        EnterpriseInstallConfig enterpriseInstallConfig = installContext.getEnterpriseInstallConfig();
        List<EnterpriseInstallNodeConfig> nodeConfigList = enterpriseInstallConfig.getNodeConfigList();
        String databasePassword = enterpriseInstallConfig.getDatabasePassword();
        Integer port = enterpriseInstallConfig.getPort();

        for (EnterpriseInstallNodeConfig enterpriseInstallNodeConfig : nodeConfigList) {
            String hostId = enterpriseInstallNodeConfig.getHostId();
            String installUserId = enterpriseInstallNodeConfig.getInstallUserId();
            String dataPath = enterpriseInstallNodeConfig.getDataPath();


            Session session = createSessionWithUserId(installContext, false, hostId, installUserId);
            String listenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath);
            listenerAddress = addCommandOfLoadEnvironmentVariable(listenerAddress, installContext.getEnvPath());
            opsHostRemoteService.executeCommand(listenerAddress, session, retSession, "modify listening address");


            String hba = MessageFormat.format(SshCommandConstants.HBA, dataPath);
            hba = addCommandOfLoadEnvironmentVariable(hba, installContext.getEnvPath());
            opsHostRemoteService.executeCommand(hba, session, retSession, "modify hba host");


            String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(port));
            try {
                Map<String, String> response = new HashMap<>();
                String createUser = MessageFormat.format("CREATE USER gaussdb WITH MONADMIN AUDITADMIN SYSADMIN PASSWORD \"{0}\";\\q", databasePassword);
                response.put("openGauss=#", createUser);
                if (enterpriseInstallConfig.getDatabaseKernelArch().equals(DatabaseKernelArch.MASTER_SLAVE)
                        || enterpriseInstallNodeConfig.getIsCMMaster()) {
                    clientLoginOpenGauss = addCommandOfLoadEnvironmentVariable(clientLoginOpenGauss, installContext.getEnvPath());
                    opsHostRemoteService.executeCommand(clientLoginOpenGauss, session, retSession, response);
                }
            } catch (Exception e) {
                log.error("Failed to create user", e);
                throw new OpsException("Failed to create user");
            }
        }

        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());
        restart(opsClusterContext);
    }

    protected HostInfoHolder getHostInfoHolder(InstallContext installContext, String hostId) {
        return installContext.getHostInfoHolders()
                .stream()
                .filter(hostInfoHolder -> hostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                .findFirst()
                .orElseThrow(() -> new OpsException("host information does not exist"));
    }

    protected HostInfoHolder getHostInfoHolder(List<HostInfoHolder> hostInfoHolders, String hostId) {
        return hostInfoHolders
                .stream()
                .filter(hostInfoHolder -> hostId.equals(hostInfoHolder.getHostEntity().getHostId()))
                .findFirst()
                .orElseThrow(() -> new OpsException("host information does not exist"));
    }

    protected OpsHostEntity getHostInfo(HostInfoHolder hostInfoHolder) {
        return hostInfoHolder.getHostEntity();
    }

    protected OpsHostUserEntity getHostUserInfoByUsername(HostInfoHolder hostInfoHolder, String username) {
        return hostInfoHolder.getHostUserEntities()
                .stream()
                .filter(userInfo -> username.equals(userInfo.getUsername()))
                .findFirst()
                .orElseThrow(() -> new OpsException("Installation user information does not exist"));
    }

    protected OpsHostUserEntity getHostUserInfo(HostInfoHolder hostInfoHolder, String userId) {
        return hostInfoHolder.getHostUserEntities()
                .stream()
                .filter(userInfo -> userId.equals(userInfo.getHostUserId()))
                .findFirst()
                .orElseThrow(() -> new OpsException("Installation user information does not exist"));
    }

    protected String addCommandOfLoadEnvironmentVariable(String command, String env) {
        if (StrUtil.isNotEmpty(env)) {
            command = "source " + env + " && " + command;
        }
        return command;
    }

    protected void sendOperateLog(InstallContext installContext, String context) {
        installContext.getRetBuffer().sendText(context);
    }

    @Override
    public void afterPropertiesSet() {
        ClusterTaskProviderManager.registry(version(), this);
    }
}

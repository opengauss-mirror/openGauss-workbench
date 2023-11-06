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
 * AbstractOpsProvider.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/provider/AbstractOpsProvider.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.provider;

import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.GucSettingContextEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.service.ops.ClusterOpsProvider;
import org.opengauss.admin.plugin.service.ops.impl.ClusterOpsProviderManager;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.vo.ops.GucSettingVO;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/8/12 09:20
 **/
@Slf4j
public abstract class AbstractOpsProvider implements ClusterOpsProvider, InitializingBean {

    protected Session beforeInstall(JschUtil jschUtil, EncryptionUtils encryptionUtils, InstallContext installContext, String installPath, String dataPath, String pkgPath, String hostId, String installUserId, String installUserName, String decompressArgs) {
        WsSession retSession = installContext.getRetSession();

        log.info("The root user logs in to the host");
        // root session
        Session rootSession = loginWithUser(jschUtil, encryptionUtils, installContext.getHostInfoHolders(), true, hostId, null);
        try {

            installDependency(jschUtil, rootSession, retSession, installContext.getOs());
            ensureDirExist(jschUtil, rootSession, pkgPath, retSession);

            ensureDirExist(jschUtil, rootSession, installPath, retSession);

            ensureDirExist(jschUtil, rootSession, dataPath, retSession);


            ensureLimits(jschUtil, rootSession, retSession);

            try {
                retSession.getSession().getBasicRemote().sendText("START_SCP_INSTALL_PACKAGE");
            } catch (IOException e) {
                log.error("send websocket text fail", e);
            }

            log.info("Copy the installation package to the target host");
            // scp
            String installPackageFullPath = scpInstallPackageToMasterNode(jschUtil, rootSession, installContext.getInstallPackagePath(), pkgPath, retSession);

            try {
                retSession.getSession().getBasicRemote().sendText("END_SCP_INSTALL_PACKAGE");
            } catch (IOException e) {
                log.error("send websocket text fail", e);
            }

            log.info("set kernel.sem");
            // SEM
            sem(jschUtil, rootSession, retSession);

            log.info("Unzip the installation package");

            // unzip
            try {
                retSession.getSession().getBasicRemote().sendText("START_UNZIP_INSTALL_PACKAGE");
            } catch (IOException e) {
                log.error("send websocket text fail", e);
            }
            decompress(jschUtil, rootSession, installPath, installPackageFullPath, retSession, decompressArgs);
            try {
                retSession.getSession().getBasicRemote().sendText("END_UNZIP_INSTALL_PACKAGE");
            } catch (IOException e) {
                log.error("send websocket text fail", e);
            }

            ensureStrictPermission(jschUtil, rootSession, installUserName, pkgPath, retSession);
            ensureStrictPermission(jschUtil, rootSession, installUserName, installPath, retSession);
            ensureDataPathPermission(jschUtil, rootSession, installUserName, dataPath, retSession);
            ensureEnvPathPermission(jschUtil, rootSession, installContext.getEnvPath(), retSession);
            log.info("Login and install user");
        } finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()) {
                rootSession.disconnect();
            }
        }

        return loginWithUser(jschUtil, encryptionUtils, installContext.getHostInfoHolders(), false, hostId, installUserId);
    }

    protected String wrapperEnvSep(String command, String envPath) {
        if (StrUtil.isNotEmpty(envPath)) {
            command = command + " --sep-env-file=" + envPath;
        }
        return command;
    }

    protected String wrapperLiteEnvSep(String command, String envPath) {
        if (StrUtil.isNotEmpty(envPath)) {
            command = command + " --env-sep-file " + envPath;
        }
        return command;
    }

    protected void ensureEnvPathPermission(JschUtil jschUtil, Session rootSession, String envPath, WsSession retSession) {
        if (StrUtil.isEmpty(envPath)) {
            return;
        }

        String touchEnvSep = "touch \"" + envPath + "\"";

        try {
            jschUtil.executeCommand(touchEnvSep, rootSession, retSession);
        } catch (Exception e) {
            log.error("touch envFile fail", e);
            throw new OpsException("touch envFile fail");
        }

        String permissionEnv = "chmod 755 -R " + envPath;
        try {
            jschUtil.executeCommand(permissionEnv, rootSession, retSession);
        } catch (Exception e) {
            log.error("chmod envFile fail", e);
            throw new OpsException("chmod envFile fail");
        }
    }

    protected void installDependency(JschUtil jschUtil, Session rootSession, WsSession retSession,
                                     OpenGaussSupportOSEnum expectedOs) {
        boolean dependencyCorrect = false;
        String[] dependencyPackageNames = {"libaio-devel", "flex", "bison", "ncurses-devel", "glibc-devel",
                "patch", "redhat-lsb-core", "readline-devel"};
        try {
            JschResult jschResult = jschUtil.executeCommand(SshCommandConstants.DEPENDENCY, rootSession);
            List<String> dependencyPackages = Arrays.stream(dependencyPackageNames).map(
                    dependency -> dependency + "." + expectedOs.getCpuArch()).collect(Collectors.toList());
            String dependency = jschResult.getResult();
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
            log.error("Execute command exception：", e);
        }
        if (dependencyCorrect) {
            log.info("dependencyCorrect，skip install dependency");
            return;
        }

        try {
            retSession.getSession().getBasicRemote().sendText("START_INSTALL_DEPENDENCY");
        } catch (IOException e) {
            log.error("send websocket fail", e);
        }
        String command = dependencyCommand(expectedOs);
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(command, rootSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("install depencency fail, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("install depencency fail");
            }

        } catch (IOException e) {
            log.error("install depencency fail", e);
            throw new OpsException("install depencency fail");
        }

        try {
            retSession.getSession().getBasicRemote().sendText("END_INSTALL_DEPENDENCY");
        } catch (IOException e) {
            log.error("send websocket fail", e);
        }
    }

    protected void ensureLimits(JschUtil jschUtil, Session rootSession, WsSession retSession) {
        String limitsCheck = SshCommandConstants.LIMITS_CHECK;
        try {
            JschResult jschResult = jschUtil.executeCommand(limitsCheck, rootSession, retSession);

            if (0 != jschResult.getExitCode()) {
                log.error("Detect ulimit exception, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
            }

            if (StrUtil.isNotEmpty(jschResult.getResult())) {
                return;
            }
        } catch (Exception e) {
            log.error("Detect ulimit error", e);
        }

        String limits = SshCommandConstants.LIMITS;
        try {
            jschUtil.executeCommand(limits, rootSession, retSession);
        } catch (IOException | InterruptedException e) {
            log.error("set ulimit exception", e);
            throw new OpsException("set ulimit exception");
        }
    }

    protected String scpInstallPackageToMasterNode(JschUtil jschUtil, Session rootSession, String sourcePath, String targetPath, WsSession retSession) {
        String installPackageFileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
        String installPackageFullPath = targetPath + "/" + installPackageFileName;
        jschUtil.upload(rootSession, retSession, sourcePath, installPackageFullPath);
        return installPackageFullPath;
    }

    protected void sem(JschUtil jschUtil, Session rootSession, WsSession retSession) {
        String command = SshCommandConstants.SEM;
        try {
            jschUtil.executeCommand(command, rootSession, retSession);
        } catch (IOException | InterruptedException | OpsException e) {
            log.error("set kernel.sem exception", e);
        }
    }

    protected void decompress(JschUtil jschUtil, Session rootSession, String targetPath, String installPackageFullPath, WsSession retSession, String decompressArgs) {
        String command = MessageFormat.format(SshCommandConstants.DECOMPRESS, decompressArgs, installPackageFullPath, targetPath);
        try {
            jschUtil.executeCommand(command, rootSession, retSession);
        } catch (Exception e) {
            log.error("Unzip the installation package failed：", e);
            throw new OpsException("Unzip the installation package failed");
        }
    }

    protected Session loginWithUser(JschUtil jschUtil, EncryptionUtils encryptionUtils, List<HostInfoHolder> hostInfoHolders, boolean root, String hostId, String userId) {
        HostInfoHolder hostInfoHolder = hostInfoHolders.stream().filter(host -> host.getHostEntity().getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("host information not found"));
        OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();

        if (root) {
            userId = hostInfoHolder
                    .getHostUserEntities()
                    .stream().filter(hostUser -> "root".equals(hostUser.getUsername()))
                    .findFirst()
                    .orElseThrow(() -> new OpsException("root user information not found")).getHostUserId();
        }

        OpsHostUserEntity userEntity = null;
        for (OpsHostUserEntity hostUserEntity : hostInfoHolder.getHostUserEntities()) {
            if (hostUserEntity.getHostUserId().equals(userId)) {
                userEntity = hostUserEntity;
                break;
            }
        }

        if (Objects.isNull(userEntity)) {
            throw new OpsException("No installation user information found");
        }

        return sshLogin(jschUtil, encryptionUtils, hostEntity, userEntity);
    }

    protected Session sshLogin(JschUtil jschUtil, EncryptionUtils encryptionUtils, OpsHostEntity hostEntity, OpsHostUserEntity userEntity) {
        return jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(), encryptionUtils.decrypt(userEntity.getPassword()))
                .orElseThrow(() -> new OpsException("Session establishment exception with host[" + hostEntity.getPublicIp() + "]"));
    }

    protected void chmodFullPath(JschUtil jschUtil, Session rootSession, String path, WsSession wsSession) {
        String chmod = MessageFormat.format(SshCommandConstants.CHMOD, path);

        try {
            try {
                jschUtil.executeCommand(chmod, rootSession, wsSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
        } catch (IOException e) {
            log.error("Failed to grant permission", e);
        }
    }

    protected void chmod(JschUtil jschUtil, Session rootSession, String path, WsSession wsSession) {
        if (StrUtil.isNotEmpty(path) && path.indexOf("/", 1) > 0) {
            path = path.substring(0, path.indexOf("/", 1));
        }
        String chmod = MessageFormat.format(SshCommandConstants.CHMOD, path);

        try {
            try {
                jschUtil.executeCommand(chmod, rootSession, wsSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
        } catch (IOException e) {
            log.error("Failed to grant permission", e);
        }
    }

    protected void chmodDataPath(JschUtil jschUtil, Session rootSession, String path, WsSession wsSession) {
        String chmod = MessageFormat.format(SshCommandConstants.CHMOD_DATA_PATH, path);

        try {
            try {
                jschUtil.executeCommand(chmod, rootSession, wsSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
        } catch (IOException e) {
            log.error("Failed to grant permission", e);
        }
    }

    protected void ensurePermission(JschUtil jschUtil, Session rootSession, String installUserName, String targetPath, WsSession wsSession) {
        chmod(jschUtil, rootSession, targetPath, wsSession);

        String chown = MessageFormat.format(SshCommandConstants.CHOWN, installUserName, targetPath);

        try {
            jschUtil.executeCommand(chown, rootSession, wsSession);
        } catch (IOException | InterruptedException | OpsException e) {
            log.error("Failed to grant permission", e);
            throw new OpsException("Failed to grant permission");
        }
    }

    protected void ensureStrictPermission(JschUtil jschUtil, Session rootSession, String installUserName, String targetPath, WsSession wsSession) {
        chmodFullPath(jschUtil, rootSession, targetPath, wsSession);

        String chown = MessageFormat.format(SshCommandConstants.CHOWN, installUserName, targetPath);

        try {
            jschUtil.executeCommand(chown, rootSession, wsSession);
        } catch (IOException | InterruptedException | OpsException e) {
            log.error("Failed to grant permission", e);
            throw new OpsException("Failed to grant permission");
        }
    }

    protected void ensureDataPathPermission(JschUtil jschUtil, Session rootSession, String installUserName, String targetPath, WsSession wsSession) {
        chmodDataPath(jschUtil, rootSession, targetPath, wsSession);

        String chown = MessageFormat.format(SshCommandConstants.CHOWN, installUserName, targetPath);

        try {
            jschUtil.executeCommand(chown, rootSession, wsSession);
        } catch (IOException | InterruptedException | OpsException e) {
            log.error("Failed to grant permission", e);
            throw new OpsException("Failed to grant permission");
        }
    }

    protected void ensureDirExist(JschUtil jschUtil, Session rootSession, String targetPath, WsSession retSession) {
        String command = MessageFormat.format(SshCommandConstants.MK_DIR, targetPath);
        try {
            jschUtil.executeCommand(command, rootSession, retSession);
        } catch (IOException | InterruptedException | OpsException e) {
            log.error("Failed to create installation directory:", e);
            throw new OpsException("Failed to create installation directory");
        }
    }

    protected String preparePath(String path) {
        if (StrUtil.isEmpty(path) || path.endsWith("/")) {
            return path;
        }

        return path + "/";
    }

    protected OmStatusModel omStatus(JschUtil jschUtil, Session ommUserSession, WsSession retSession, String envPath) {
        OmStatusModel omStatusModel = new OmStatusModel();
        try {
            String statusCommand = "gs_om -t status --detail";
            JschResult jschResult = jschUtil.executeCommand(statusCommand, ommUserSession, envPath);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("startup error,gs_om status fail,exit code " + jschResult.getExitCode());
            }

            String result = jschResult.getResult();
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

    protected void createEnterpriseRemoteUser(InstallContext installContext, OpsClusterContext opsClusterContext, JschUtil jschUtil, EncryptionUtils encryptionUtils) {
        WsSession retSession = installContext.getRetSession();
        opsClusterContext.setRetSession(retSession);
        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());

        EnterpriseInstallConfig enterpriseInstallConfig = installContext.getEnterpriseInstallConfig();
        List<EnterpriseInstallNodeConfig> nodeConfigList = enterpriseInstallConfig.getNodeConfigList();
        String databasePassword = enterpriseInstallConfig.getDatabasePassword();
        Integer port = enterpriseInstallConfig.getPort();

        for (EnterpriseInstallNodeConfig enterpriseInstallNodeConfig : nodeConfigList) {
            String hostId = enterpriseInstallNodeConfig.getHostId();
            String installUserId = enterpriseInstallNodeConfig.getInstallUserId();
            String dataPath = enterpriseInstallNodeConfig.getDataPath();


            Session session = loginWithUser(jschUtil, encryptionUtils, installContext.getHostInfoHolders(), false, hostId, installUserId);
            String listenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath);
            try {
                jschUtil.executeCommand(listenerAddress, installContext.getEnvPath(), session, retSession);
            } catch (Exception e) {
                log.error("Failed to modify listening address", e);
                throw new OpsException("Failed to modify listening address");
            }

            String hba = MessageFormat.format(SshCommandConstants.HBA, dataPath);
            try {
                jschUtil.executeCommand(hba, installContext.getEnvPath(), session, retSession);

            } catch (Exception e) {
                log.error("Failed to modify host", e);
                throw new OpsException("Failed to modify host");
            }

            String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(port));
            try {
                Map<String, String> response = new HashMap<>();
                String createUser = MessageFormat.format("CREATE USER gaussdb WITH MONADMIN AUDITADMIN SYSADMIN PASSWORD \"{0}\";\\q", databasePassword);
                response.put("openGauss=#", createUser);
                jschUtil.executeCommand(installContext.getEnvPath(), clientLoginOpenGauss, session, retSession, response);

            } catch (Exception e) {
                log.error("Failed to create user", e);
                throw new OpsException("Failed to create user");
            }
        }

        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());
        restart(opsClusterContext);
    }

    @Override
    public void configGucSetting(JschUtil jschUtil, Session session, OpsClusterEntity clusterEntity, boolean isApplyToAllNode, String dataPath, GucSettingVO gucSettingVO) {
        String command;
        if (gucSettingVO.getContext().equals(GucSettingContextEnum.POSTMASTER.getCode())) {
            if (isApplyToAllNode) {
                command = String.format("gs_guc set -N all -I all -c \"%s=%s\"", gucSettingVO.getName(), gucSettingVO.getValue());
            } else {
                command = String.format("gs_guc set -D %s -c \"%s=%s\"", dataPath, gucSettingVO.getName(), gucSettingVO.getValue());
            }
        } else {
            if (isApplyToAllNode) {
                command = String.format("gs_guc reload -N all -I all -c \"%s=%s\"", gucSettingVO.getName(), gucSettingVO.getValue());
            } else {
                command = String.format("gs_guc reload -D %s -c \"%s=%s\"", dataPath, gucSettingVO.getName(), gucSettingVO.getValue());
            }
        }

        try {
            JschResult jschResult = jschUtil.executeCommand(command, session, clusterEntity.getEnvPath());
            if (0 != jschResult.getExitCode()) {
                log.error("set guc parameter {} to {} failed, exit code: {}, error message: {}", gucSettingVO.getName(), gucSettingVO.getValue(), jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException(String.format("Failed to set guc parameter %s to %s failed: %s", gucSettingVO.getName(), gucSettingVO.getValue(), jschResult.getResult()));
            }

        } catch (Exception e) {
            log.error("set guc parameter {} to {} failed, error message: {}", gucSettingVO.getName(), gucSettingVO.getValue(), e.getMessage());
            throw new OpsException(String.format("Failed to set guc parameter %s to %s failed: %s", gucSettingVO.getName(), gucSettingVO.getValue(), e.getMessage()));
        }
    }

    @Override
    public void afterPropertiesSet() {
        ClusterOpsProviderManager.registry(version(), this);
    }
}

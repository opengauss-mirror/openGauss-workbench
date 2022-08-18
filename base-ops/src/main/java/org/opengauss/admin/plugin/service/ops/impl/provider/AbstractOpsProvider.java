package org.opengauss.admin.plugin.service.ops.impl.provider;

import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.service.ops.ClusterOpsProvider;
import org.opengauss.admin.plugin.service.ops.impl.ClusterOpsProviderManager;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

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
        Session rootSession = loginWithUser(jschUtil,encryptionUtils,installContext.getHostInfoHolders(), true, hostId, null);
        try {

            installDependency(jschUtil,rootSession,retSession);
            ensureDirExist(jschUtil,rootSession, pkgPath, retSession);

            ensureDirExist(jschUtil,rootSession, installPath, retSession);

            ensureDirExist(jschUtil,rootSession, dataPath, retSession);


            ensureLimits(jschUtil,rootSession, retSession);

            try {
                retSession.getSession().getBasicRemote().sendText("START_SCP_INSTALL_PACKAGE");
            } catch (IOException e) {
                log.error("send websocket text fail",e);
            }

            log.info("Copy the installation package to the target host");
            // scp
            String installPackageFullPath = scpInstallPackageToMasterNode(jschUtil,rootSession, installContext.getInstallPackagePath(), pkgPath, retSession);

            try {
                retSession.getSession().getBasicRemote().sendText("END_SCP_INSTALL_PACKAGE");
            } catch (IOException e) {
                log.error("send websocket text fail",e);
            }

            log.info("set kernel.sem");
            // SEM
            sem(jschUtil,rootSession, retSession);

            log.info("Unzip the installation package");

            // unzip
            try {
                retSession.getSession().getBasicRemote().sendText("START_UNZIP_INSTALL_PACKAGE");
            } catch (IOException e) {
                log.error("send websocket text fail",e);
            }
            decompress(jschUtil,rootSession, pkgPath, installPackageFullPath, retSession, decompressArgs);
            try {
                retSession.getSession().getBasicRemote().sendText("END_UNZIP_INSTALL_PACKAGE");
            } catch (IOException e) {
                log.error("send websocket text fail",e);
            }

            ensurePermission(jschUtil,rootSession, installUserName, pkgPath, retSession);
            ensurePermission(jschUtil,rootSession, installUserName, installPath, retSession);
            ensureDataPathPermission(jschUtil,rootSession, installUserName, dataPath, retSession);
            log.info("Login and install user");
        }finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()){
                rootSession.disconnect();
            }
        }

        return loginWithUser(jschUtil,encryptionUtils,installContext.getHostInfoHolders(), false, hostId, installUserId);
    }

    protected void installDependency(JschUtil jschUtil, Session rootSession, WsSession retSession){
        try {
            retSession.getSession().getBasicRemote().sendText("START_INSTALL_DEPENDENCY");
        } catch (IOException e) {
            log.error("send websocket fail",e);
        }
        String command = dependencyCommand();
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(command, rootSession, retSession);
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
            log.error("send websocket fail",e);
        }
    }

    protected void ensureLimits(JschUtil jschUtil,Session rootSession, WsSession retSession) {
        String limitsCheck = SshCommandConstants.LIMITS_CHECK;
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(limitsCheck, rootSession, retSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Detect ulimit exception, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
            }

            if (StrUtil.isNotEmpty(jschResult.getResult())) {
                return;
            }
        } catch (IOException e) {
            log.error("Detect ulimit error", e);
        }

        String limits = SshCommandConstants.LIMITS;
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(limits, rootSession, retSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("set ulimit exception, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("set ulimit exception");
            }
        } catch (IOException e) {
            log.error("set ulimit exception", e);
            throw new OpsException("set ulimit exception");
        }
    }

    protected String scpInstallPackageToMasterNode(JschUtil jschUtil,Session rootSession, String sourcePath, String targetPath, WsSession retSession) {
        String installPackageFileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
        String installPackageFullPath = targetPath + "/" + installPackageFileName;
        jschUtil.upload(rootSession, retSession, sourcePath, installPackageFullPath);
        return installPackageFullPath;
    }

    protected void sem(JschUtil jschUtil,Session rootSession, WsSession retSession) {
        String command = SshCommandConstants.SEM;
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(command, rootSession, retSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("set kernel.sem exception, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("set kernel.sem exception");
            }
        } catch (IOException e) {
            log.error("set kernel.sem exception", e);
        }
    }

    protected void decompress(JschUtil jschUtil,Session rootSession, String targetPath, String installPackageFullPath, WsSession retSession, String decompressArgs) {
        String command = MessageFormat.format(SshCommandConstants.DECOMPRESS, decompressArgs, installPackageFullPath, targetPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession, retSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to decompress installation package, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Unzip the installation package failed");
            }
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

        return sshLogin(jschUtil,encryptionUtils,hostEntity, userEntity);
    }

    protected Session sshLogin(JschUtil jschUtil,EncryptionUtils encryptionUtils,OpsHostEntity hostEntity, OpsHostUserEntity userEntity) {
        return jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(), encryptionUtils.decrypt(userEntity.getPassword()))
                .orElseThrow(() -> new OpsException("Session establishment exception with host[" + hostEntity.getPublicIp() + "]"));
    }

    protected void chmodFullPath(JschUtil jschUtil,Session rootSession, String path, WsSession wsSession) {
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

    protected void chmod(JschUtil jschUtil,Session rootSession, String path, WsSession wsSession) {
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

    protected void chmodDataPath(JschUtil jschUtil,Session rootSession, String path, WsSession wsSession) {
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

    protected void ensurePermission(JschUtil jschUtil,Session rootSession, String installUserName, String targetPath, WsSession wsSession) {
        chmod(jschUtil,rootSession, targetPath, wsSession);

        String chown = MessageFormat.format(SshCommandConstants.CHOWN, installUserName, targetPath);

        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(chown, rootSession, wsSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to grant permission, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to grant permission");
            }
        } catch (IOException e) {
            log.error("Failed to grant permission", e);
            throw new OpsException("Failed to grant permission");
        }
    }

    protected void ensureDataPathPermission(JschUtil jschUtil,Session rootSession, String installUserName, String targetPath, WsSession wsSession) {
        chmodDataPath(jschUtil,rootSession, targetPath, wsSession);

        String chown = MessageFormat.format(SshCommandConstants.CHOWN, installUserName, targetPath);

        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(chown, rootSession, wsSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to grant permission, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to grant permission");
            }
        } catch (IOException e) {
            log.error("Failed to grant permission", e);
            throw new OpsException("Failed to grant permission");
        }
    }

    protected void ensureDirExist(JschUtil jschUtil,Session rootSession, String targetPath, WsSession retSession) {
        String command = MessageFormat.format(SshCommandConstants.MK_DIR, targetPath);
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(command, rootSession, retSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to create directory, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to create installation directory");
            }
        } catch (IOException e) {
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

    @Override
    public void afterPropertiesSet() {
        ClusterOpsProviderManager.registry(version(),os(), this);
    }
}

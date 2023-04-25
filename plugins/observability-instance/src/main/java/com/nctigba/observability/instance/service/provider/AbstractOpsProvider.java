package com.nctigba.observability.instance.service.provider;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import com.nctigba.observability.instance.constants.CommonConstants;
import org.opengauss.admin.common.constant.ops.SshCommandConstants;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostInfoHolder;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.exception.ops.OpsException;
import com.nctigba.observability.instance.util.JschUtil;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.InitializingBean;

import com.jcraft.jsch.Session;
import com.nctigba.observability.instance.service.ClusterOpsProvider;
import com.nctigba.observability.instance.service.ClusterOpsProviderManager;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lhf
 * @date 2022/8/12 09:20
 **/
@Slf4j
public abstract class AbstractOpsProvider implements ClusterOpsProvider, InitializingBean {

	protected void ensureLimits(JschUtil jschUtil, Session rootSession, WsSession retSession) {
		String limitsCheck = SshCommandConstants.LIMITS_CHECK;
		try {
			JschResult jschResult = null;
			try {
				jschResult = jschUtil.executeCommand(limitsCheck, rootSession, retSession, null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException(CommonConstants.THREAD_IS_INTERRUPTED);
			}
			if (0 != jschResult.getExitCode()) {
				log.error("Detect ulimit exception, exit code: {}, error message: {}", jschResult.getExitCode(),
						jschResult.getResult());
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
				jschResult = jschUtil.executeCommand(limits, rootSession, retSession, null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException(CommonConstants.THREAD_IS_INTERRUPTED);
			}
			if (0 != jschResult.getExitCode()) {
				log.error("set ulimit exception, exit code: {}, error message: {}", jschResult.getExitCode(),
						jschResult.getResult());
				throw new OpsException(CommonConstants.SET_LIMIT_EXCEPTION);
			}
		} catch (IOException e) {
			log.error(CommonConstants.SET_LIMIT_EXCEPTION, e);
			throw new OpsException(CommonConstants.SET_LIMIT_EXCEPTION);
		}
	}

	protected String scpInstallPackageToMasterNode(JschUtil jschUtil, Session rootSession, String sourcePath,
			String targetPath, WsSession retSession) {
		String installPackageFileName = sourcePath.substring(sourcePath.lastIndexOf(CommonConstants.SLASH) + 1);
		String installPackageFullPath = targetPath + CommonConstants.SLASH + installPackageFileName;
		jschUtil.upload(rootSession, retSession, sourcePath, installPackageFullPath);
		return installPackageFullPath;
	}

	protected void sem(JschUtil jschUtil, Session rootSession, WsSession retSession) {
		String command = SshCommandConstants.SEM;
		try {
			JschResult jschResult = null;
			try {
				jschResult = jschUtil.executeCommand(command, rootSession, retSession, null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException(CommonConstants.THREAD_IS_INTERRUPTED);
			}
			if (0 != jschResult.getExitCode()) {
				log.error("set kernel.sem exception, exit code: {}, error message: {}", jschResult.getExitCode(),
						jschResult.getResult());
				throw new OpsException("set kernel.sem exception");
			}
		} catch (IOException e) {
			log.error("set kernel.sem exception", e);
		}
	}

	protected void decompress(JschUtil jschUtil, Session rootSession, String targetPath, String installPackageFullPath,
			WsSession retSession, String decompressArgs) {
		String command = MessageFormat.format(SshCommandConstants.DECOMPRESS, decompressArgs, installPackageFullPath,
				targetPath);
		try {
			JschResult jschResult = jschUtil.executeCommand(command, rootSession, retSession, null);
			if (0 != jschResult.getExitCode()) {
				log.error("Failed to decompress installation package, exit code: {}, error message: {}",
						jschResult.getExitCode(), jschResult.getResult());
				throw new OpsException("Unzip the installation package failed");
			}
		} catch (Exception e) {
			log.error("Unzip the installation package failed：", e);
			throw new OpsException("Unzip the installation package failed");
		}
	}

	protected Session loginWithUser(JschUtil jschUtil, EncryptionUtils encryptionUtils,
			List<HostInfoHolder> hostInfoHolders, boolean root, String hostId, String userId) {
		HostInfoHolder hostInfoHolder = hostInfoHolders.stream()
				.filter(host -> host.getHostEntity().getHostId().equals(hostId)).findFirst()
				.orElseThrow(() -> new OpsException("host information not found"));
		OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();

		if (root) {
			userId = hostInfoHolder.getHostUserEntities().stream()
					.filter(hostUser -> "root".equals(hostUser.getUsername())).findFirst()
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

	protected Session sshLogin(JschUtil jschUtil, EncryptionUtils encryptionUtils, OpsHostEntity hostEntity,
			OpsHostUserEntity userEntity) {
		return jschUtil
				.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
						encryptionUtils.decrypt(userEntity.getPassword()))
				.orElseThrow(() -> new OpsException(
						"Session establishment exception with host[" + hostEntity.getPublicIp() + "]"));
	}

	protected void chmodFullPath(JschUtil jschUtil, Session rootSession, String path, WsSession wsSession) {
		String chmod = MessageFormat.format(SshCommandConstants.CHMOD, path);

		try {
			try {
				jschUtil.executeCommand(chmod, rootSession, wsSession, null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException(CommonConstants.THREAD_IS_INTERRUPTED);
			}
		} catch (IOException e) {
			log.error(CommonConstants.FAILED_TO_GRANT_PERMISSION, e);
		}
	}

	protected void chmod(JschUtil jschUtil, Session rootSession, String path, WsSession wsSession) {
		if (StrUtil.isNotEmpty(path) && path.indexOf(CommonConstants.SLASH, 1) > 0) {
			path = path.substring(0, path.indexOf(CommonConstants.SLASH, 1));
		}
		String chmod = MessageFormat.format(SshCommandConstants.CHMOD, path);

		try {
			try {
				jschUtil.executeCommand(chmod, rootSession, wsSession, null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException(CommonConstants.THREAD_IS_INTERRUPTED);
			}
		} catch (IOException e) {
			log.error(CommonConstants.FAILED_TO_GRANT_PERMISSION, e);
		}
	}

	protected void chmodDataPath(JschUtil jschUtil, Session rootSession, String path, WsSession wsSession) {
		String chmod = MessageFormat.format(SshCommandConstants.CHMOD_DATA_PATH, path);

		try {
			try {
				jschUtil.executeCommand(chmod, rootSession, wsSession, null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException(CommonConstants.THREAD_IS_INTERRUPTED);
			}
		} catch (IOException e) {
			log.error(CommonConstants.FAILED_TO_GRANT_PERMISSION, e);
		}
	}

	protected void ensurePermission(JschUtil jschUtil, Session rootSession, String installUserName, String targetPath,
			WsSession wsSession) {
		chmod(jschUtil, rootSession, targetPath, wsSession);

		String chown = MessageFormat.format(SshCommandConstants.CHOWN, installUserName, targetPath);

		try {
			JschResult jschResult = null;
			try {
				jschResult = jschUtil.executeCommand(chown, rootSession, wsSession, null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException(CommonConstants.THREAD_IS_INTERRUPTED);
			}
			if (0 != jschResult.getExitCode()) {
				log.error("Failed to grant permission, exit code: {}, error message: {}", jschResult.getExitCode(),
						jschResult.getResult());
				throw new OpsException(CommonConstants.FAILED_TO_GRANT_PERMISSION);
			}
		} catch (IOException e) {
			log.error(CommonConstants.FAILED_TO_GRANT_PERMISSION, e);
			throw new OpsException(CommonConstants.FAILED_TO_GRANT_PERMISSION);
		}
	}

	protected void ensureDataPathPermission(JschUtil jschUtil, Session rootSession, String installUserName,
			String targetPath, WsSession wsSession) {
		chmodDataPath(jschUtil, rootSession, targetPath, wsSession);

		String chown = MessageFormat.format(SshCommandConstants.CHOWN, installUserName, targetPath);

		try {
			JschResult jschResult = null;
			try {
				jschResult = jschUtil.executeCommand(chown, rootSession, wsSession, null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException(CommonConstants.THREAD_IS_INTERRUPTED);
			}
			if (0 != jschResult.getExitCode()) {
				log.error("Failed to grant permission, exit code: {}, error message: {}", jschResult.getExitCode(),
						jschResult.getResult());
				throw new OpsException(CommonConstants.FAILED_TO_GRANT_PERMISSION);
			}
		} catch (IOException e) {
			log.error(CommonConstants.FAILED_TO_GRANT_PERMISSION, e);
			throw new OpsException(CommonConstants.FAILED_TO_GRANT_PERMISSION);
		}
	}

	protected void ensureDirExist(JschUtil jschUtil, Session rootSession, String targetPath, WsSession retSession) {
		String command = MessageFormat.format(SshCommandConstants.MK_DIR, targetPath);
		try {
			JschResult jschResult = null;
			try {
				jschResult = jschUtil.executeCommand(command, rootSession, retSession, null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OpsException(CommonConstants.THREAD_IS_INTERRUPTED);
			}
			if (0 != jschResult.getExitCode()) {
				log.error("Failed to create directory, exit code: {}, error message: {}", jschResult.getExitCode(),
						jschResult.getResult());
				throw new OpsException("Failed to create installation directory");
			}
		} catch (IOException e) {
			log.error("Failed to create installation directory:", e);
			throw new OpsException("Failed to create installation directory");
		}
	}

	protected String preparePath(String path) {
		if (StrUtil.isEmpty(path) || path.endsWith(CommonConstants.SLASH)) {
			return path;
		}

		return path + CommonConstants.SLASH;
	}

	@Override
	public void afterPropertiesSet() {
		ClusterOpsProviderManager.registry(version(), os(), this);
	}
}

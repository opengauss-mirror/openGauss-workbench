package com.nctigba.observability.sql.service;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostUserBody;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.service.AbstractInstaller.Step.status;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

public abstract class AbstractInstaller {
	protected static final String TAR = ".tar.gz";
	protected static final String ZIP = ".zip";
	@Autowired
	@AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
	protected HostFacade hostFacade;
	@Autowired
	@AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
	protected EncryptionUtils encryptionUtils;
	@Autowired
	@AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
	protected HostUserFacade hostUserFacade;
	@Autowired
	protected NctigbaEnvMapper envMapper;
	@Autowired
	@AutowiredType(Type.PLUGIN_MAIN)
	protected WsUtil wsUtil;

	protected String arch(String str) {
		return "aarch64".equals(str) ? "arm64" : "amd64";
	}

	protected OpsHostUserEntity getUser(OpsHostEntity hostEntity, String username, String rootPassword) {
		var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
			return username.equals(e.getUsername());
		}).findFirst().orElse(null);
		if (user == null && rootPassword != null) {
			var body = new HostUserBody();
			body.setHostId(hostEntity.getHostId());
			body.setPassword(encryptionUtils.encrypt(StrUtil.uuid()));
			body.setRootPassword(rootPassword);
			body.setUsername(username);
			hostUserFacade.add(body);
			user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
				return username.equals(e.getUsername());
			}).findFirst().orElse(null);
		}
		if (user == null)
			throw new RuntimeException("user not found");
		if ("root".equals(user.getUsername()))
			user.setPassword(rootPassword);
		return user;
	}

	protected int skipStep(WsSession wsSession, List<Step> steps, int curr) {
		steps.get(curr).setState(status.SKIP);
		curr++;
		sendMsg(wsSession, steps, curr, status.DOING);
		return curr;
	}

	/**
	 * change current step to {@code DONE} and next step to {@code DOING}, send to
	 * {@link WebSocket}
	 */
	protected int nextStep(WsSession wsSession, List<Step> steps, int curr) {
		steps.get(curr).setState(status.DONE);
		curr++;
		sendMsg(wsSession, steps, curr, status.DOING);
		return curr;
	}

	/**
	 * change current step to {@code DONE}, send to {@link WebSocket}
	 */
	protected synchronized void sendMsg(WsSession wsSession, List<Step> steps, int curr, status state) {
		steps.get(curr).setState(state);
		wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
	}

	protected synchronized void addMsg(WsSession wsSession, List<Step> steps, int curr, String msg) {
		steps.get(curr).getMsg().add(msg);
		wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
	}

	@Data
	@NoArgsConstructor
	@Accessors(chain = true)
	public static class Step {
		String name;
		status state = status.TODO;
		List<String> msg = new ArrayList<>();

		public Step(String name) {
			this.name = name;
		}

		public enum status {
			TODO,
			DOING,
			DONE,
			SKIP,
			ERROR
		}
	}
}
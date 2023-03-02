package com.nctigba.observability.sql.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.model.NctigbaEnv.type;
import com.nctigba.observability.sql.service.AbstractInstaller.Step.status;
import com.nctigba.observability.sql.util.SshSession;
import com.nctigba.observability.sql.util.SshSession.command;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AgentService extends AbstractInstaller {
	private static final String AGENT_USER = "root";
	private static final String NAME = "opengauss-ebpf-1.0.0-SNAPSHOT.jar";
	@Autowired
	private ResourceLoader loader;
	@Autowired
	private ClusterManager clusterManager;

	public void install(WsSession wsSession, String nodeId, String rootPassword) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("检查本机agent环境存在"),
				new Step("连接主机"),
				new Step("传输agent安装包"),
				new Step("启动agent"),
				new Step("安装完成"));
		// @formatter:on
		var curr = 0;

		curr = nextStep(wsSession, steps, curr);
		var node = clusterManager.getOpsNodeById(nodeId);
		var hostId = node.getHostId();
		check(hostId);

		log.info(encryptionUtils.decrypt(rootPassword));
		curr = nextStep(wsSession, steps, curr);
		var env = new NctigbaEnv().setHostid(hostId).setPort(2321).setUsername(AGENT_USER).setType(type.AGENT);
		try (var session = connect(env, rootPassword);) {
			curr = nextStep(wsSession, steps, curr);
			// upload
			File f = File.createTempFile("opengauss-ebpf-1.0.0-SNAPSHOT", ".jar");
			try (var in = loader.getResource(NAME).getInputStream(); var out = new FileOutputStream(f);) {
				IoUtil.copy(in, out);
			}
			session.upload(f.getCanonicalPath(), "./" + NAME);
			f.delete();
			curr = nextStep(wsSession, steps, curr);
			// exec
			session.executeNoWait("nohup java -jar " + NAME);
			env.setPath(".");
			curr = nextStep(wsSession, steps, curr);
			envMapper.insert(env);
			sendMsg(wsSession, steps, curr, status.DONE);
		} catch (Exception e) {
			steps.get(curr).setState(status.ERROR);
			wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
			var sw = new StringWriter();
			try (var pw = new PrintWriter(sw);) {
				e.printStackTrace(pw);
			}
			wsUtil.sendText(wsSession, sw.toString());
		}
	}

	private void check(String hostId) {
		var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getHostid, hostId)
				.eq(NctigbaEnv::getType, type.AGENT));
		if (env != null)
			throw new RuntimeException();
	}

	private SshSession connect(NctigbaEnv env, String rootPassword) throws IOException {
		OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
		if (hostEntity == null)
			throw new RuntimeException("host not found");
		env.setHost(hostEntity);
		var user = getUser(hostEntity, AGENT_USER, rootPassword);
		return SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), AGENT_USER,
				encryptionUtils.decrypt(user.getPassword()));
	}

	public void uninstall(WsSession wsSession, String nodeId, String rootPassword) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("连接主机"),
				new Step("查找agent进程号"),
				new Step("停止agent"),
				new Step("卸载完成"));
		// @formatter:on
		var curr = 0;

		try {
			curr = nextStep(wsSession, steps, curr);
			var node = clusterManager.getOpsNodeById(nodeId);
			var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.AGENT)
					.eq(NctigbaEnv::getHostid, node.getHostId()));
			if (env == null)
				throw new RuntimeException("agent not found");
			curr = nextStep(wsSession, steps, curr);
			OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
			if (hostEntity == null)
				throw new RuntimeException("host not found");
			env.setHost(hostEntity);
			try (var sshsession = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), "root",
					encryptionUtils.decrypt(rootPassword));) {
				curr = nextStep(wsSession, steps, curr);
				var nodePid = sshsession.execute(command.PS.parse(NAME));
				if (StrUtil.isNotBlank(nodePid)) {
					curr = nextStep(wsSession, steps, curr);
					sshsession.execute(command.KILL.parse(nodePid));
				} else
					curr = skipStep(wsSession, steps, curr);
				envMapper.deleteById(env);
				sendMsg(wsSession, steps, curr, status.DONE);
			}
		} catch (Exception e) {
			steps.get(curr).setState(status.ERROR);
			wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
			var sw = new StringWriter();
			try (var pw = new PrintWriter(sw);) {
				e.printStackTrace(pw);
			}
			wsUtil.sendText(wsSession, sw.toString());
		}
	}
}
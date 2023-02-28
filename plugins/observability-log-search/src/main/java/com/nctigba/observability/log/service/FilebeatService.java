package com.nctigba.observability.log.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.Arrays;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.log.env.NctigbaEnv;
import com.nctigba.observability.log.env.NctigbaEnv.type;
import com.nctigba.observability.log.service.AbstractInstaller.Step.status;
import com.nctigba.observability.log.util.SshSession;
import com.nctigba.observability.log.util.SshSession.command;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Service
public class FilebeatService extends AbstractInstaller {
	private static final String PATH = "https://artifacts.elastic.co/downloads/beats/filebeat/";
	private static final String NAME = "filebeat-8.3.3-linux-";
	@Autowired
	@AutowiredType(Type.PLUGIN_MAIN)
	private IOpsClusterNodeService opsClusterNodeService;
	@Autowired
	private ResourceLoader loader;
	@Autowired
	private ClusterManager clusterManager;

	public void install(WsSession wsSession, String nodeId) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("检查数据库集群信息"),
				new Step("检查filebeat环境存在"),
				new Step("检查安装用户"),
				new Step("连接主机"),
				new Step("安装filebeat"),
				new Step("修改filebeat配置"),
				new Step("启动filebeat"),
				new Step("验证安装"),
				new Step("安装完成"));
		// @formatter:on
		int curr = 0;

		curr = nextStep(wsSession, steps, curr);
		try {
			var node = clusterManager.getOpsNodeById(nodeId);
			if (node == null)
				throw new RuntimeException("node not found");
			curr = nextStep(wsSession, steps, curr);
			var hostId = node.getHostId();
			OpsHostEntity hostEntity = hostFacade.getById(hostId);
			var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getHostid, hostId)
					.eq(NctigbaEnv::getType, type.FILEBEAT));
			if (env != null)
				throw new RuntimeException("filebeat exists");
			var esEnv = envMapper
					.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.ELASTICSEARCH));
			if (esEnv == null)
				throw new RuntimeException("elasticsearch not exist");

			curr = nextStep(wsSession, steps, curr);
			System.out.println(opsClusterNodeService);
			var opsClusterNodeEntity = opsClusterNodeService.getById(nodeId);
			if (opsClusterNodeEntity == null)
				throw new RuntimeException("No cluster node information found");

			curr = nextStep(wsSession, steps, curr);
			String installUserId = opsClusterNodeEntity.getInstallUserId();
			OpsHostUserEntity user = hostUserFacade.getById(installUserId);
			if (hostEntity == null)
				throw new RuntimeException("host not found");

			curr = nextStep(wsSession, steps, curr);
			var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), user.getUsername(),
					encryptionUtils.decrypt(user.getPassword()));

			curr = nextStep(wsSession, steps, curr);
			var arch = session.execute(command.ARCH);
			String name = NAME + arch(arch);
			String tar = name + TAR;
			if (!session.test(command.STAT.parse(name))) {
				if (!session.test(command.STAT.parse(tar)))
					session.execute(command.WGET.parse(PATH + tar));
				session.execute(command.TAR.parse(tar));
			}
			curr = nextStep(wsSession, steps, curr);
			String cd = "cd " + name + " && ";
			// filebeat_conf.zip
			File f = File.createTempFile("filebeat_conf", ".tar.gz");
			try (var in = loader.getResource("filebeat_conf.tar.gz").getInputStream();
					var out = new FileOutputStream(f);) {
				System.out.println(f.getCanonicalPath());
				IoUtil.copy(in, out);
			}
			session.upload(f.getCanonicalPath(), "./filebeat_conf.tar.gz");
			f.delete();
			session.execute("tar -xvf filebeat_conf.tar.gz");
			var esHost = hostFacade.getById(esEnv.getHostid());

			// opengauss log path
			ResultSet rs = node.connection().createStatement()
					.executeQuery("select setting from pg_settings where name = 'log_directory'");
			rs.next();
			var logPath = rs.getString(1);

			session.execute("cd filebeat_conf && sh conf.sh " + esHost.getPublicIp() + ":" + esEnv.getPort() + " "
					+ opsClusterNodeEntity.getClusterNodeId() + " " + logPath + " " + logPath + " " + logPath);
			session.execute("cp -fr filebeat_conf/* " + name + "/");
			session.execute("rm -rf filebeat_conf");
			session.execute("rm -f filebeat_conf.tar.gz");

			curr = nextStep(wsSession, steps, curr);
			session.execute(
					cd + " ./filebeat modules enable system opengauss && nohup ./filebeat -e -c filebeat.yml 2>&1 &");
			// 调用http
			// 生成数据库记录,入库
			env = new NctigbaEnv().setHostid(hostId).setUsername(user.getUsername()).setType(type.FILEBEAT)
					.setPath(name);

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

	public void uninstall(WsSession wsSession, String nodeId) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("连接主机"),
				new Step("查找filebeat进程号"),
				new Step("停止filebeat"),
				new Step("卸载完成"));
		// @formatter:on
		var curr = 0;

		try {
			curr = nextStep(wsSession, steps, curr);
			var node = clusterManager.getOpsNodeById(nodeId);
			var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.FILEBEAT)
					.eq(NctigbaEnv::getHostid, node.getHostId()));
			if (env == null)
				throw new RuntimeException("filebeat not found");
			curr = nextStep(wsSession, steps, curr);
			OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
			if (hostEntity == null)
				throw new RuntimeException("host not found");
			env.setHost(hostEntity);
			var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
				return env.getUsername().equals(e.getUsername());
			}).findFirst().orElse(null);
			try (var sshsession = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), user.getUsername(),
					encryptionUtils.decrypt(user.getPassword()));) {
				curr = nextStep(wsSession, steps, curr);
				var nodePid = sshsession.execute(command.PS.parse("filebeat"));
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
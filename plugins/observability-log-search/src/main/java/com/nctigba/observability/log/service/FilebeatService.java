package com.nctigba.observability.log.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.log.env.NctigbaEnv;
import com.nctigba.observability.log.env.NctigbaEnv.type;
import com.nctigba.observability.log.service.AbstractInstaller.Step.status;
import com.nctigba.observability.log.util.SshSession;
import com.nctigba.observability.log.util.SshSession.command;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
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

	public void install(WsSession wsSession, String hostId) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("检查filebeat环境存在"),
				new Step("检查数据库集群信息"),
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
		var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getHostid, hostId)
				.eq(NctigbaEnv::getType, type.FILEBEAT));
		if (env != null)
			throw new RuntimeException("filebeat exists");
		var esEnv = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.ELASTICSEARCH));
		if (esEnv == null)
			throw new RuntimeException("elasticsearch not exist");

		curr = nextStep(wsSession, steps, curr);
		List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService
				.list(new LambdaQueryWrapper<OpsClusterNodeEntity>().eq(OpsClusterNodeEntity::getHostId, hostId));
		if (CollUtil.isEmpty(opsClusterNodeEntities))
			throw new RuntimeException("No cluster node information found");

		curr = nextStep(wsSession, steps, curr);
		OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeEntities.stream()
				.filter(node -> node.getHostId().equals(hostId)).findFirst()
				.orElseThrow(() -> new RuntimeException("The node information corresponding to the host is not found"));
		String installUserId = opsClusterNodeEntity.getInstallUserId();
		OpsHostUserEntity user = hostUserFacade.getById(installUserId);
		OpsHostEntity hostEntity = hostFacade.getById(hostId);
		if (hostEntity == null)
			throw new RuntimeException("host not found");
		try {
			curr = nextStep(wsSession, steps, curr);
			var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), user.getUsername(),
					encryptionUtils.decrypt(user.getPassword()));

			curr = nextStep(wsSession, steps, curr);
			var arch = session.execute(command.ARCH);
			String name = NAME + arch;
			String tar = name + TAR;
			if (!session.test(command.STAT.parse(name))) {
				if (!session.test(command.STAT.parse(tar)))
					session.execute(command.WGET.parse(PATH + tar));
				session.execute(command.TAR.parse(tar));
			}
			curr = nextStep(wsSession, steps, curr);
			String cd = "cd " + name + " && ";
			// fielbeat_conf.zip
			File f = File.createTempFile("filebeat_conf", "tar.gz");
			var in = loader.getResource("filebeat_conf.tar.gz").getInputStream();
			IoUtil.copy(in, new FileOutputStream(f));
			session.upload(f.getCanonicalPath(), "./");
			f.delete();
			session.execute("tar -xvf filebeat_conf.tar.gz");
			var esHost = hostFacade.getById(esEnv.getHostid());

			// opengauss log path
			var node = clusterManager.getOpsNodeById(opsClusterNodeEntity.getClusterNodeId());
			var logPath = node.connection().createStatement().executeQuery("select setting from pg_settings where name = 'log_directory'").getString(1);

			session.execute("cd filebeat_conf && ./conf.sh " + esHost.getPublicIp() + ":" + esEnv.getPort() + " "
					+ opsClusterNodeEntity.getClusterNodeId() + " " + logPath + " " + logPath + " " + logPath);
			session.execute("cp -fr filebeat_conf/* filebeat-8.3.3/");

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
		} catch (IOException e) {
			steps.get(curr).setState(status.ERROR);
			wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
		} catch (Exception e) {
			steps.get(curr).setState(status.ERROR);
			wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
		}
	}
}
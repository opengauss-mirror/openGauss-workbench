package com.nctigba.observability.instance.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.type;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.status;
import com.nctigba.observability.instance.service.ClusterManager.OpsClusterNodeVOSub;
import com.nctigba.observability.instance.service.PrometheusService.prometheusConfig;
import com.nctigba.observability.instance.service.PrometheusService.prometheusConfig.job;
import com.nctigba.observability.instance.util.SshSession;
import com.nctigba.observability.instance.util.SshSession.command;
import com.nctigba.observability.instance.util.YamlUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Service
public class ExporterService extends AbstractInstaller {
	private static final String EXPORTER_USER = "exporters";
	private static final String NODE_EXPORTER_PATH = "https://github.com/prometheus/node_exporter/releases/download/v1.3.1/";
	private static final String NODE_EXPORTER_NAME = "node_exporter-1.3.1.linux-";
	private static final String OPENGAUSS_EXPORTER_PATH = "https://gitee.com/opengauss/openGauss-prometheus-exporter/releases/download/v1.0.0/";
	private static final String OPENGAUSS_EXPORTER_NAME = "opengauss_exporter_1.0.0_linux_";

	@Autowired
	private ClusterManager clusterManager;
	@Autowired
	private ResourceLoader loader;

	public void install(WsSession wsSession, String nodeId, String rootPassword) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("检查prometheus环境存在"),
				new Step("连接主机"),
				new Step("检查安装用户"),
				new Step("安装nodeExporter"),
				new Step("安装opengaussExporter"),
				new Step("刷新prometheus配置"),
				new Step("安装完成"));
		// @formatter:on
		int curr = 0;

		curr = nextStep(wsSession, steps, curr);
		var promEnv = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.PROMETHEUS));
		if (promEnv == null)
			throw new RuntimeException("prometheus not exists");

		curr = nextStep(wsSession, steps, curr);
		var node = clusterManager.getOpsNodeById(nodeId);
		if (node == null)
			throw new RuntimeException("node not found");
		curr = nextStep(wsSession, steps, curr);
		var hostId = node.getHostId();
		OpsHostEntity hostEntity = hostFacade.getById(hostId);
		if (hostEntity == null)
			throw new RuntimeException("host not found");
		var user = getUser(hostEntity, EXPORTER_USER, rootPassword);
		try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), EXPORTER_USER,
				encryptionUtils.decrypt(user.getPassword()));) {

			curr = nextStep(wsSession, steps, curr);
			var nodeEnv = nodeExporter(hostId, user, session);

			curr = nextStep(wsSession, steps, curr);
			var gaussEnv = opengaussExporter(node, hostId, hostEntity, user, session);

			curr = nextStep(wsSession, steps, curr);
			// 修改prometheus,重启
			var promeHost = hostFacade.getById(promEnv.getHostid());
			var promUser = hostUserFacade.listHostUserByHostId(promEnv.getHostid()).stream()
					.filter(p -> p.getHostId().equals(hostId) && p.getUsername().equals(promEnv.getUsername()))
					.findFirst().orElseThrow(
							() -> new RuntimeException("The node information corresponding to the host is not found"));
			// reload prometheus
			try (var promSession = SshSession.connect(promeHost.getPublicIp(), promeHost.getPort(),
					promEnv.getUsername(), encryptionUtils.decrypt(promUser.getPassword()));) {
				var promYmlStr = promSession.execute("cat " + promEnv.getPath() + "/prometheus.yml");
				var conf = YamlUtil.loadAs(promYmlStr, prometheusConfig.class);
				conf.scrape_configs
						.add(generateConfig("opengauss", nodeId, node.getPublicIp() + ":" + gaussEnv.getPort()));
				conf.scrape_configs.add(generateConfig("node", nodeId, node.getPublicIp() + ":" + nodeEnv.getPort()));
				var prometheusConfigFile = File.createTempFile("prom", ".tmp");
				FileUtil.appendUtf8String(YamlUtil.dump(conf), prometheusConfigFile);
				promSession.execute("rm " + promEnv.getPath() + "/prometheus.yml");
				promSession.upload(prometheusConfigFile.getAbsolutePath(), promEnv.getPath() + "/prometheus.yml");
				prometheusConfigFile.delete();
			}

			// curl -X POST http://IP/-/reload
			var res = HttpUtil.post("http://" + promeHost.getPublicIp() + ":9090/-/reload", "");
			if ("Lifecycle API is not enabled.".equals(res)) {
				// TODO 未开启在线刷新
			}
			curr = nextStep(wsSession, steps, curr);
			sendMsg(wsSession, steps, curr, status.DONE);
		} catch (IOException e) {
			e.printStackTrace();
			steps.get(curr).setState(status.ERROR);
			wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
		}
	}

	private static job generateConfig(String name, String nodeId, String host) {
		var con = new job.conf();
		con.setLabels(Map.of("instance", nodeId));
		con.setTargets(Arrays.asList(host));
		var job = new job();
		job.setStatic_configs(Arrays.asList(con));
		job.setJob_name(name);
		return job;
	}

	private NctigbaEnv nodeExporter(String hostId, OpsHostUserEntity user, SshSession session) throws IOException {
		var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getHostid, hostId)
				.eq(NctigbaEnv::getType, type.NODE_EXPORTER));
		if (env != null)
			return env;
		var arch = session.execute(command.ARCH);
		String name = NODE_EXPORTER_NAME + arch(arch);
		String tar = name + TAR;
		if (!session.test(command.STAT.parse(name))) {
			if (!session.test(command.STAT.parse(tar)))
				session.execute(command.WGET.parse(NODE_EXPORTER_PATH + tar));
			session.execute(command.TAR.parse(tar));
		}
		session.execute("cd " + name + " && nohup ./node_exporter --collector.systemd 2>&1 & \r", false);
		var nodeEnv = new NctigbaEnv().setHostid(hostId).setPort(9100).setUsername(user.getUsername())
				.setType(type.NODE_EXPORTER).setPath(name);
		envMapper.insert(nodeEnv);
		// 验证
		return nodeEnv;
	}

	private NctigbaEnv opengaussExporter(OpsClusterNodeVOSub node, String hostId, OpsHostEntity hostEntity,
			OpsHostUserEntity user, SshSession session) throws IOException {
		var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getHostid, hostId)
				.eq(NctigbaEnv::getType, type.OPENGAUSS_EXPORTER));
		if (env != null)
			return env;
		var arch = session.execute(command.ARCH);
		String name = OPENGAUSS_EXPORTER_NAME + arch(arch);
		String zip = name + ZIP;
		if (!session.test(command.STAT.parse("opengauss_exporter"))) {
			if (!session.test(command.STAT.parse(zip)))
				session.execute(command.WGET.parse(OPENGAUSS_EXPORTER_PATH + zip));
			session.execute(command.UNZIP.parse(zip));
		}

		File f = File.createTempFile("og_exporter", "yml");
		var in = loader.getResource("og_exporter.yml").getInputStream();
		IoUtil.copy(in, new FileOutputStream(f));
		session.upload(f.getCanonicalPath(), "og_exporter.yml");
		f.delete();

		// 启动
		var url = MessageFormat.format("postgresql://{0}:{1}@{2}:{3,number,#}/{4}", node.getDbUser(),
				URLUtil.encodeAll(node.getDbUserPassword()), hostEntity.getPublicIp(), node.getDbPort(),
				node.getDbName());
		session.execute("export DATA_SOURCE_NAME='" + url
				+ "' && nohup ./opengauss_exporter --config=og_exporter.yml 2>&1 & \r", false);
		var gaussEnv = new NctigbaEnv().setHostid(hostId).setPort(9187).setUsername(user.getUsername())
				.setType(type.OPENGAUSS_EXPORTER).setPath("./");
		envMapper.insert(gaussEnv);
		// 验证
		return gaussEnv;
	}
}
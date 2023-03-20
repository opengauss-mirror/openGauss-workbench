package com.nctigba.observability.instance.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.type;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.status;
import com.nctigba.observability.instance.service.PrometheusService.prometheusConfig;
import com.nctigba.observability.instance.service.PrometheusService.prometheusConfig.job;
import com.nctigba.observability.instance.util.Download;
import com.nctigba.observability.instance.util.SshSession;
import com.nctigba.observability.instance.util.SshSession.command;
import com.nctigba.observability.instance.util.YamlUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Service
public class ExporterService extends AbstractInstaller {
	private static final String EXPORTER_USER = "exporters";
	public static final String NODE_EXPORTER_PATH = "https://github.com/prometheus/node_exporter/releases/download/v1.3.1/";
	public static final String NODE_EXPORTER_NAME = "node_exporter-1.3.1.linux-";
	public static final String OPENGAUSS_EXPORTER_PATH = "https://gitee.com/opengauss/openGauss-prometheus-exporter/releases/download/v1.0.0/";
	public static final String OPENGAUSS_EXPORTER_NAME = "opengauss_exporter_1.0.0_linux_";

	@Autowired
	private ClusterManager clusterManager;
	@Autowired
	private ResourceLoader loader;

	public void install(WsSession wsSession, String nodeId, String rootPassword, Integer nodeport, Integer openport) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("exporterinstall.step1"),
				new Step("exporterinstall.step2"),
				new Step("exporterinstall.step3"),
				new Step("exporterinstall.step4"),
				new Step("exporterinstall.step5"),
				new Step("exporterinstall.step6"),
				new Step("exporterinstall.step7"));
		// @formatter:on
		int curr = 0;

		curr = nextStep(wsSession, steps, curr);
		try {
			var promEnv = envMapper
					.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.PROMETHEUS));
			if (promEnv == null)
				throw new RuntimeException("prometheus not exists");
			if (StrUtil.isBlank(promEnv.getPath()))
				throw new RuntimeException("prometheus installing");

			curr = nextStep(wsSession, steps, curr);
			var node = clusterManager.getOpsNodeById(nodeId);
			if (node == null)
				throw new RuntimeException("node not found");
			var hostId = node.getHostId();
			OpsHostEntity hostEntity = hostFacade.getById(hostId);
			if (hostEntity == null)
				throw new RuntimeException("host not found");
			var user = getUser(hostEntity, EXPORTER_USER, rootPassword, steps, curr);
			try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), "root",
					encryptionUtils.decrypt(rootPassword));) {
				session.execute("yum install -y unzip zip");
			} catch (Exception e) {
				steps.get(curr).setState(status.ERROR).add(e.getMessage());
				wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
				var sw = new StringWriter();
				try (var pw = new PrintWriter(sw);) {
					e.printStackTrace(pw);
				}
				wsUtil.sendText(wsSession, sw.toString());
				return;
			}
			try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), EXPORTER_USER,
					encryptionUtils.decrypt(user.getPassword()));) {

				curr = nextStep(wsSession, steps, curr);
				var nodeEnv = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getHostid, hostId)
						.eq(NctigbaEnv::getType, type.NODE_EXPORTER));
				if (nodeEnv == null) {
					var arch = session.execute(command.ARCH);
					String name = NODE_EXPORTER_NAME + arch(arch);
					nodeEnv = new NctigbaEnv().setHostid(hostId).setPort(nodeport).setUsername(user.getUsername())
							.setType(type.NODE_EXPORTER).setPath(name);
					envMapper.insert(nodeEnv);
					try {
						String tar = name + TAR;
						if (!session.test(command.STAT.parse(name))) {
							if (!session.test(command.STAT.parse(tar))) {
								// session.execute(command.WGET.parse(NODE_EXPORTER_PATH + tar));
								var pkg = envMapper
										.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().like(NctigbaEnv::getPath, tar));
								if (pkg == null) {
									var f = Download.download(NODE_EXPORTER_PATH + tar, "pkg/" + tar);
									pkg = new NctigbaEnv().setPath(f.getCanonicalPath())
											.setType(type.NODE_EXPORTER_PKG);
									addMsg(wsSession, steps, curr, "exporterinstall.downloadsuccess");
									save(pkg);
								}
								session.upload(pkg.getPath(), tar);
								addMsg(wsSession, steps, curr, "exporterinstall.uploadsuccess");
							} else
								addMsg(wsSession, steps, curr, "exporterinstall.pkgexists");
							session.execute(command.TAR.parse(tar));
						}
						session.executeNoWait("cd " + name
								+ " && ./node_exporter --collector.systemd --web.listen-address=:" + nodeport + " &");
					} catch (Exception e) {
						envMapper.deleteById(nodeEnv);
						throw e;
					}
				} else
					addMsg(wsSession, steps, curr, "node exporter exists");

				curr = nextStep(wsSession, steps, curr);
				var gaussEnv = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getHostid, hostId)
						.eq(NctigbaEnv::getType, type.OPENGAUSS_EXPORTER));
				if (gaussEnv == null) {
					var arch = session.execute(command.ARCH);
					String name = OPENGAUSS_EXPORTER_NAME + arch(arch);
					gaussEnv = new NctigbaEnv().setHostid(hostId).setPort(openport).setUsername(user.getUsername())
							.setType(type.OPENGAUSS_EXPORTER).setPath(".");
					envMapper.insert(gaussEnv);
					try {
						String zip = name + ZIP;
						if (!session.test(command.STAT.parse("opengauss_exporter"))) {
							if (!session.test(command.STAT.parse(zip))) {
								// session.execute(command.WGET.parse(OPENGAUSS_EXPORTER_PATH + zip));
								var pkg = envMapper
										.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().like(NctigbaEnv::getPath, zip));
								if (pkg == null) {
									var f = Download.download(OPENGAUSS_EXPORTER_PATH + zip, "pkg/" + zip);
									pkg = new NctigbaEnv().setPath(f.getCanonicalPath())
											.setType(type.OPENGAUSS_EXPORTER_PKG);
									addMsg(wsSession, steps, curr, "exporterinstall.downloadsuccess");
									save(pkg);
								}
								session.upload(pkg.getPath(), zip);
								addMsg(wsSession, steps, curr, "exporterinstall.uploadsuccess");
							} else
								addMsg(wsSession, steps, curr, "exporterinstall.pkgexists");
							session.execute(command.UNZIP.parse(zip));
						}

						File f = File.createTempFile("og_exporter", "yml");
						var in = loader.getResource("og_exporter.yml").getInputStream();
						IoUtil.copy(in, new FileOutputStream(f));
						session.upload(f.getCanonicalPath(), "og_exporter.yml");
						f.delete();

						// 启动
						var url = MessageFormat.format(
								// "postgresql://{0}:{1}@{2}:{3,number,#}/{4}"
								"host={0} user={1} password={2} port={3,number,#} dbname={4} sslmode=disable",
								hostEntity.getPublicIp(), node.getDbUser(), node.getDbUserPassword(), node.getDbPort(),
								node.getDbName());
						session.executeNoWait("export DATA_SOURCE_NAME='" + url
								+ "' && ./opengauss_exporter --config=og_exporter.yml --web.listen-address=:" + openport
								+ " &");
					} catch (Exception e) {
						envMapper.deleteById(gaussEnv);
						throw e;
					}
				} else
					addMsg(wsSession, steps, curr, "opengauss exporter exists");

				curr = nextStep(wsSession, steps, curr);
				// reload prometheus
				var promeHost = hostFacade.getById(promEnv.getHostid());
				var promUser = hostUserFacade.listHostUserByHostId(promEnv.getHostid()).stream()
						.filter(p -> p.getUsername().equals(promEnv.getUsername())).findFirst()
						.orElseThrow(() -> new RuntimeException(
								"The node information corresponding to the host is not found"));
				try (var promSession = SshSession.connect(promeHost.getPublicIp(), promeHost.getPort(),
						promEnv.getUsername(), encryptionUtils.decrypt(promUser.getPassword()));) {
					var promYmlStr = promSession.execute("cat " + promEnv.getPath() + "/prometheus.yml");
					var conf = YamlUtil.loadAs(promYmlStr, prometheusConfig.class);

					var congauss = new job.conf();
					congauss.setLabels(Map.of("instance", nodeId, "type", "opengauss"));
					congauss.setTargets(Arrays.asList(node.getPublicIp() + ":" + gaussEnv.getPort()));
					var connode = new job.conf();
					connode.setLabels(Map.of("instance", nodeId, "type", "node"));
					connode.setTargets(Arrays.asList(node.getPublicIp() + ":" + nodeEnv.getPort()));
					var job = new job();
					job.setStatic_configs(Arrays.asList(connode, congauss));
					job.setJob_name(nodeId);
					conf.scrape_configs.add(job);
					conf.scrape_configs = new ArrayList<>(new LinkedHashSet<>(conf.scrape_configs));

					var prometheusConfigFile = File.createTempFile("prom", ".tmp");
					FileUtil.appendUtf8String(YamlUtil.dump(conf), prometheusConfigFile);
					promSession.execute("rm " + promEnv.getPath() + "/prometheus.yml");
					promSession.upload(prometheusConfigFile.getAbsolutePath(), promEnv.getPath() + "/prometheus.yml");
					prometheusConfigFile.delete();
				}

				// curl -X POST http://IP/-/reload
				var res = HttpUtil.post("http://" + promeHost.getPublicIp() + ":" + promEnv.getPort() + "/-/reload",
						"");
				if ("Lifecycle API is not enabled.".equals(res)) {
					// TODO reload fail
				}
				curr = nextStep(wsSession, steps, curr);
				sendMsg(wsSession, steps, curr, status.DONE);
			}
		} catch (IOException e) {
			steps.get(curr).setState(status.ERROR).add(e.getMessage());
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
				new Step("exporteruninstall.step1"),
				new Step("exporteruninstall.step2"),
				new Step("exporteruninstall.step3"),
				new Step("exporteruninstall.step4"),
				new Step("exporteruninstall.step5"),
				new Step("exporteruninstall.step6"),
				new Step("exporteruninstall.step7"));
		// @formatter:on
		var curr = 0;

		try {
			var node = clusterManager.getOpsNodeById(nodeId);
			var nodeenv = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery()
					.eq(NctigbaEnv::getType, type.NODE_EXPORTER).eq(NctigbaEnv::getHostid, node.getHostId())).get(0);
			var expenv = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery()
					.eq(NctigbaEnv::getType, type.OPENGAUSS_EXPORTER).eq(NctigbaEnv::getHostid, node.getHostId())).get(0);

			if (nodeenv == null && expenv == null)
				throw new RuntimeException("exporters not found");

			curr = nextStep(wsSession, steps, curr);
			OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
			if (hostEntity == null)
				throw new RuntimeException("host not found");

			var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
				return nodeenv.getUsername().equals(e.getUsername());
			}).findFirst().orElse(null);
			try (var sshsession = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), user.getUsername(),
					encryptionUtils.decrypt(user.getPassword()));) {
				curr = nextStep(wsSession, steps, curr);
				var nodePid = sshsession.execute(command.PS.parse("node_exporter"));
				if (StrUtil.isNotBlank(nodePid)) {
					curr = nextStep(wsSession, steps, curr);
					sshsession.execute(command.KILL.parse(nodePid));
				} else
					curr = skipStep(wsSession, steps, curr);
				envMapper.deleteById(nodeenv);

				curr = nextStep(wsSession, steps, curr);
				var gaussPid = sshsession.execute(command.PS.parse("opengauss_exporter"));
				if (StrUtil.isNotBlank(gaussPid)) {
					curr = nextStep(wsSession, steps, curr);
					sshsession.execute(command.KILL.parse(gaussPid));
				} else
					curr = skipStep(wsSession, steps, curr);
				envMapper.deleteById(expenv);

				sendMsg(wsSession, steps, curr, status.DONE);
			}
		} catch (Exception e) {
			steps.get(curr).setState(status.ERROR).add(e.getMessage());
			wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
			var sw = new StringWriter();
			try (var pw = new PrintWriter(sw);) {
				e.printStackTrace(pw);
			}
			wsUtil.sendText(wsSession, sw.toString());
		}
	}
}
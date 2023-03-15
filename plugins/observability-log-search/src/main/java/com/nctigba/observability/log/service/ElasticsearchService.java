package com.nctigba.observability.log.service;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.log.config.ElasticsearchProvider;
import com.nctigba.observability.log.env.NctigbaEnv;
import com.nctigba.observability.log.env.NctigbaEnv.type;
import com.nctigba.observability.log.service.AbstractInstaller.Step.status;
import com.nctigba.observability.log.util.Download;
import com.nctigba.observability.log.util.SshSession;
import com.nctigba.observability.log.util.SshSession.command;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Service
public class ElasticsearchService extends AbstractInstaller {
	private static final String ELASTICSEARCH_USER = "Elasticsearch";
	public static final String PATH = "https://artifacts.elastic.co/downloads/elasticsearch/";
	public static final String NAME = "elasticsearch-8.3.3-linux-";
	public static final String SRC = "elasticsearch-8.3.3";

	@Autowired
	private ResourceLoader loader;
	@Autowired
	private ElasticsearchProvider provider;

	public void install(WsSession wsSession, String hostId, String rootPassword, Integer port) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("elastic.install.step1"),
				new Step("elastic.install.step2"),
				new Step("elastic.install.step3"),
				new Step("elastic.install.step4"),
				new Step("elastic.install.step5"),
				new Step("elastic.install.step6"),
				new Step("elastic.install.step7"),
				new Step("elastic.install.step8"));
		// @formatter:on
		int curr = 0;

		try {
			curr = nextStep(wsSession, steps, curr);
			var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.ELASTICSEARCH));
			if (env != null)
				throw new RuntimeException("elasticsearch exists");
			env = new NctigbaEnv().setHostid(hostId).setPort(port).setType(type.ELASTICSEARCH).setPath(SRC);

			curr = nextStep(wsSession, steps, curr);
			OpsHostEntity hostEntity = hostFacade.getById(hostId);
			if (hostEntity == null)
				throw new RuntimeException("host not found");
			env.setHost(hostEntity);
			try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), "root",
					encryptionUtils.decrypt(rootPassword));) {
			} catch (Exception e) {
				throw new RuntimeException("root password error");
			}
			var user = getUser(hostEntity, ELASTICSEARCH_USER, rootPassword);
			env.setUsername(user.getUsername());
			var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), ELASTICSEARCH_USER,
					encryptionUtils.decrypt(user.getPassword()));

			curr = nextStep(wsSession, steps, curr);
			var arch = session.execute(command.ARCH);
			String name = NAME + arch;
			String tar = name + TAR;
			if (!session.test(command.STAT.parse(SRC))) {
				if (!session.test(command.STAT.parse(tar))) {
					var pkg = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().like(NctigbaEnv::getPath, tar));
					if (pkg == null) {
						var f = Download.download(PATH + tar, "pkg/" + tar);
						pkg = new NctigbaEnv().setPath(f.getCanonicalPath()).setType(type.ELASTICSEARCH_PKG);
						addMsg(wsSession, steps, curr, "elastic.install.download.success");
						save(pkg);
					}
					session.upload(pkg.getPath(), tar);
				}
				session.execute(command.TAR.parse(tar));
			}

			curr = nextStep(wsSession, steps, curr);
			// 生成配置文件
			var elasticConfigFile = File.createTempFile("elasticsearch", ".yml");
			// @formatter:off
			FileUtil.appendUtf8String(
					"path.data: " + "data" + System.lineSeparator()
					+ "path.logs: " + "logs" + System.lineSeparator()
					+ "cluster.name: dbTools-es" + System.lineSeparator()
					+ "cluster.initial_master_nodes: [\"node1\"]" + System.lineSeparator()
					+ "discovery.seed_hosts: [\"127.0.0.1\"]" + System.lineSeparator()
					+ "node.name: node1" + System.lineSeparator() 
					+ "network.host: 0.0.0.0" + System.lineSeparator()
					+ "http.port: " + port + System.lineSeparator()
					+ "http.host: 0.0.0.0" + System.lineSeparator() 
					+ "transport.host: 0.0.0.0" + System.lineSeparator()
					+ "http.cors.enabled: true" + System.lineSeparator()
					+ "http.cors.allow-origin: \"*\"" + System.lineSeparator() 
					+ "xpack.security.enrollment.enabled: true" + System.lineSeparator()
					+ "xpack.security.enabled: false" + System.lineSeparator()
					+ "xpack.security.http.ssl.enabled: false" + System.lineSeparator()
					+ "xpack.security.transport.ssl.enabled: false"
					, elasticConfigFile);
			// @formatter:on
			session.upload(elasticConfigFile.getAbsolutePath(), SRC + "/config/elasticsearch.yml");
			elasticConfigFile.delete();
			var in = loader.getResource("jvm.options").getInputStream();
			session.upload(in, SRC + "/config/jvm.options");

			curr = nextStep(wsSession, steps, curr);
			String cd = "cd " + SRC + " && ";
			session.executeNoWait(cd + " ./bin/elasticsearch -d &");

			curr = nextStep(wsSession, steps, curr);
			// 调用http验证es
			for (int i = 0; i < 10; i++) {
				ThreadUtil.sleep(3000L);
				try {
					HttpUtil.get("http://" + env.getHost().getPublicIp() + ":" + env.getPort());
				} catch (Exception e) {
					if (i == 9)
						throw new RuntimeException("elastic.install.start.fail");
				}
			}

			curr = nextStep(wsSession, steps, curr);
			// 生成数据库记录,入库
			envMapper.insert(env);
			sendMsg(wsSession, steps, curr, status.DONE);
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

	public void uninstall(WsSession wsSession, String id) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("elastic.uninstall.step1"),
				new Step("elastic.uninstall.step2"),
				new Step("elastic.uninstall.step3"),
				new Step("elastic.uninstall.step4"),
				new Step("elastic.uninstall.step5"));
		// @formatter:on
		var curr = 0;

		try {
			curr = nextStep(wsSession, steps, curr);
			var env = envMapper.selectById(id);
			if (env == null)
				throw new RuntimeException("id not found");

			curr = nextStep(wsSession, steps, curr);
			OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
			if (hostEntity == null)
				throw new RuntimeException("host not found");
			env.setHost(hostEntity);

			var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
				return env.getUsername().equals(e.getUsername());
			}).findFirst().orElse(null);
			try (var sshsession = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), user.getUsername(),
					encryptionUtils.decrypt(user.getPassword()));) {
				curr = nextStep(wsSession, steps, curr);
				var pid = sshsession.execute(command.PS.parse("elasticsearch"));
				if (StrUtil.isNotBlank(pid) && NumberUtil.isLong(pid)) {
					curr = nextStep(wsSession, steps, curr);
					sshsession.execute(command.KILL.parse(pid));
				} else
					curr = skipStep(wsSession, steps, curr);
				envMapper.deleteById(id);
				provider.clear();
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
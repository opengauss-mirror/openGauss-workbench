package com.nctigba.observability.log.service;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.utils.http.HttpUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.log.env.NctigbaEnv;
import com.nctigba.observability.log.env.NctigbaEnv.type;
import com.nctigba.observability.log.service.AbstractInstaller.Step.status;
import com.nctigba.observability.log.util.SshSession;
import com.nctigba.observability.log.util.SshSession.command;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Service
public class ElasticsearchService extends AbstractInstaller {
	private static final String ELASTICSEARCH_USER = "Elasticsearch";
	private static final String PATH = "https://artifacts.elastic.co/downloads/elasticsearch/";
	private static final String NAME = "elasticsearch-8.3.3-linux-";
	private static final String SRC = "elasticsearch-8.3.3";

	public void install(WsSession wsSession, String hostId, String rootPassword) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("检查elasticsearch环境存在"),
				new Step("连接主机"),
				new Step("下载elasticsearch安装包,解压缩"),
				new Step("配置elasticsearch"),
				new Step("启动elasticsearch"),
				new Step("验证elasticsearch启动状态"),
				new Step("安装完成"));
		// @formatter:on
		int curr = 0;

		try {
			curr = nextStep(wsSession, steps, curr);
			check();
			var env = new NctigbaEnv().setHostid(hostId).setPort(9200)
					.setType(type.ELASTICSEARCH).setPath(SRC);

			curr = nextStep(wsSession, steps, curr);
			OpsHostEntity hostEntity = hostFacade.getById(hostId);
			if (hostEntity == null)
				throw new RuntimeException("host not found");
			env.setHost(hostEntity);
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
					var esPkg = envMapper.selectOne(
							Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.ELASTICSEARCH_PKG));
					if (esPkg != null)
						session.upload(esPkg.getPath(), "./");
					else
						session.execute(command.WGET.parse(PATH + tar));
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
			session.upload(elasticConfigFile.getAbsolutePath(), SRC + "/elasticsearch.yml");
			elasticConfigFile.delete();
			String cd = "cd " + SRC + " && ";
			session.execute(cd + " echo '-Xms1g' >>jvm.options && echo '-Xmx1g' >>jvm.options");

			curr = nextStep(wsSession, steps, curr);
			session.execute(cd + " ./bin/elasticsearch -d &", false);

			curr = nextStep(wsSession, steps, curr);
			// 调用http验证es
			String str = HttpUtils.sendGet("http://" + env.getHost().getPublicIp() + ":"+env.getPort(),
					null);
			if(!JSONUtil.isJsonObj(str))
				throw new RuntimeException("elasticsearch 启动失败");

			curr = nextStep(wsSession, steps, curr);
			// 生成数据库记录,入库
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

	private void check() {
		var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.ELASTICSEARCH));
		if (env != null)
			throw new RuntimeException("elasticsearch exists");
	}

	public void uninstall(WsSession wsSession, String id) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("连接主机"),
				new Step("查找elasticsearch进程号"),
				new Step("停止elasticsearch"),
				new Step("卸载完成"));
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
				if(StrUtil.isNotBlank(pid)) {
					curr = nextStep(wsSession, steps, curr);
					sshsession.execute(command.KILL.parse(pid));
				} else
					curr = skipStep(wsSession, steps, curr);
				envMapper.deleteById(id);
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
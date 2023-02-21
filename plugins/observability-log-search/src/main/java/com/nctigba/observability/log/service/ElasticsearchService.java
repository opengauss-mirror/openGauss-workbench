package com.nctigba.observability.log.service;

import java.io.File;
import java.util.Arrays;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.log.env.NctigbaEnv;
import com.nctigba.observability.log.env.NctigbaEnv.type;
import com.nctigba.observability.log.service.AbstractInstaller.Step.status;
import com.nctigba.observability.log.util.SshSession;
import com.nctigba.observability.log.util.SshSession.command;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;

@Service
public class ElasticsearchService extends AbstractInstaller {
	private static final String ELASTICSEARCH_USER = "Elasticsearch";
	private static final String PATH = "https://artifacts.elastic.co/downloads/elasticsearch/";
	private static final String NAME = "elasticsearch-8.3.3-linux-";

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

			curr = nextStep(wsSession, steps, curr);
			OpsHostEntity hostEntity = hostFacade.getById(hostId);
			if (hostEntity == null)
				throw new RuntimeException("host not found");
			var user = getUser(hostEntity, ELASTICSEARCH_USER, rootPassword);
			var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), ELASTICSEARCH_USER,
					encryptionUtils.decrypt(user.getPassword()));

			curr = nextStep(wsSession, steps, curr);
			var arch = session.execute(command.ARCH);
			String name = NAME + arch;
			String tar = name + TAR;
			if (!session.test(command.STAT.parse(name))) {
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
			FileUtil.appendUtf8String("path.data: " + "data" + System.lineSeparator() + "path.logs: " + "log"
					+ System.lineSeparator() + "cluster.name: dbTools-es" + System.lineSeparator()
					+ "cluster.initial_master_nodes: [\"node1\"]" + System.lineSeparator()
					+ "discovery.seed_hosts: [\"127.0.0.1\"]" + System.lineSeparator() + "node.name: node1"
					+ System.lineSeparator() + "network.host: 0.0.0.0" + System.lineSeparator() + "http.host: 0.0.0.0"
					+ System.lineSeparator() + "transport.host: 0.0.0.0" + System.lineSeparator()
					+ "http.cors.enabled: true" + System.lineSeparator() + "http.cors.allow-origin: \"*\""
					+ System.lineSeparator() + "xpack.security.enrollment.enabled: true" + System.lineSeparator()
					+ "xpack.security.enabled: false" + System.lineSeparator()
					+ "xpack.security.http.ssl.enabled: false" + System.lineSeparator()
					+ "xpack.security.transport.ssl.enabled: false", elasticConfigFile);
			session.upload(elasticConfigFile.getAbsolutePath(), name + "elasticsearch.yml");
			elasticConfigFile.delete();
			String cd = "cd " + name + " && ";
			session.execute(cd + " echo '-Xms1g' >>jvm.options && echo '-Xmx1g' >>jvm.options");

			curr = nextStep(wsSession, steps, curr);
			session.execute(cd + " nohup ./bin/elasticsearch -d 2>&1 &");

			curr = nextStep(wsSession, steps, curr);
			// TODO 调用http验证es

			curr = nextStep(wsSession, steps, curr);
			// 生成数据库记录,入库
			var env = new NctigbaEnv().setHostid(hostId).setPort(9200).setUsername(user.getUsername())
					.setType(type.ELASTICSEARCH).setPath(name);
			envMapper.insert(env);
			sendMsg(wsSession, steps, curr, status.DONE);
		} catch (Exception e) {
			steps.get(curr).setState(status.ERROR);
			wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
		}
	}

	private void check() {
		var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.ELASTICSEARCH));
		if (env != null)
			throw new RuntimeException("elasticsearch exists");
	}
}
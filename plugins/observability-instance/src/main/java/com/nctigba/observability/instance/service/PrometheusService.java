package com.nctigba.observability.instance.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.type;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.status;
import com.nctigba.observability.instance.util.HttpUtils;
import com.nctigba.observability.instance.util.SshSession;
import com.nctigba.observability.instance.util.SshSession.command;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PrometheusService extends AbstractInstaller {
	private static final String PROMETHEUS_USER = "Prometheus";
	private static final String PATH = "https://mirrors.tuna.tsinghua.edu.cn/github-release/prometheus/prometheus/LatestRelease/";
	private static final String NAME = "prometheus-2.42.0.linux-";

	public void install(WsSession wsSession, String hostId, String rootPassword) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("检查本机prometheus环境存在"),
				new Step("连接主机"),
				new Step("下载prometheus安装包,解压缩"),
				new Step("启动prometheus"),
				new Step("验证prometheus启动状态"),
				new Step("安装完成"));
		// @formatter:on
		var curr = 0;

		curr = nextStep(wsSession, steps, curr);
		check(hostId);

		log.info(encryptionUtils.decrypt(rootPassword));
		curr = nextStep(wsSession, steps, curr);
		var env = new NctigbaEnv().setHostid(hostId).setPort(9090).setUsername(PROMETHEUS_USER)
				.setType(type.PROMETHEUS);
		try (var sshsession = connect(env, rootPassword);) {
			curr = nextStep(wsSession, steps, curr);
			env.setPath(wget(sshsession));

			curr = nextStep(wsSession, steps, curr);
			exec(sshsession, env);
			ThreadUtil.sleep(3000L);

			curr = nextStep(wsSession, steps, curr);
			check(env);

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
				.eq(NctigbaEnv::getType, type.PROMETHEUS));
		if (env != null)
			throw new RuntimeException();
	}

	private SshSession connect(NctigbaEnv env, String rootPassword) throws IOException {
		OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
		if (hostEntity == null)
			throw new RuntimeException("host not found");
		env.setHost(hostEntity);
		var user = getUser(hostEntity, PROMETHEUS_USER, rootPassword);
		return SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), PROMETHEUS_USER,
				encryptionUtils.decrypt(user.getPassword()));
	}

	private String wget(SshSession session) throws IOException {
		var arch = session.execute(command.ARCH);
		String name = NAME + arch(arch);
		String tar = name + TAR;
		if (!session.test(command.STAT.parse(name))) {
			if (!session.test(command.STAT.parse(tar)))
				session.execute(command.WGET.parse(PATH + tar));
			session.execute(command.TAR.parse(tar));
		}
		return name;
	}

	private void exec(SshSession session, NctigbaEnv env) throws IOException {
		session.execute(MessageFormat.format("echo ''{0}'' > {1}",
				"cd " + env.getPath() + "\n./prometheus --web.enable-lifecycle --config.file=prometheus.yml &",
				"start.sh"));
		session.execute("sh start.sh", false);
	}

	private void check(NctigbaEnv env) {
		String str = HttpUtils.sendGet("http://" + env.getHost().getPublicIp() + ":9090/api/v1/status/runtimeinfo",
				null);
		if (StringUtils.isBlank(str))
			throw new RuntimeException("prometheus 启动失败");
	}

	/**
	 * default prometheus config
	 * @formatter:off
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      - alertmanager: 9093
global:
  evaluation_interval: 15s
  scrape_interval: 15s
rule_files: null
scrape_configs:
- job_name: prometheus
  static_configs:
  - targets:
    - localhost:9090
	 * @formatter:on
	 */
	@Data
	public static class prometheusConfig {
		global global;
		alert alerting;
		List<String> rule_files;
		List<job> scrape_configs;

		@Data
		public static class global {
			String scrape_interval;
			String evaluation_interval;
		}

		@Data
		public static class alert {
			List<alertmanager> alertmanagers;

			@Data
			public static class alertmanager {
				List<conf> static_configs;

				@Data
				public static class conf {
					List<target> targets;

					@Data
					public static class target {
						int alertmanager;
					}
				}
			}
		}

		@Data
		public static class job {
			String job_name;
			List<conf> static_configs;

			@Data
			public static class conf {
				List<String> targets;
				Map<String, String> labels;
			}
		}
	}
}
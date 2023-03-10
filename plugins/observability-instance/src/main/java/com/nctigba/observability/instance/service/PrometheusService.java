package com.nctigba.observability.instance.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.common.web.exception.CustomException;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.type;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.status;
import com.nctigba.observability.instance.util.Download;
import com.nctigba.observability.instance.util.HttpUtils;
import com.nctigba.observability.instance.util.SshSession;
import com.nctigba.observability.instance.util.SshSession.command;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;

@Service
public class PrometheusService extends AbstractInstaller {
	private static final String PROMETHEUS_USER = "Prometheus";
	public static final String PATH = "https://github.com/prometheus/prometheus/releases/download/v2.42.0/";
	public static final String NAME = "prometheus-2.42.0.linux-";

	public void install(WsSession wsSession, String hostId, String rootPassword, Integer promport) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("开始安装"),
				new Step("检查Prometheus安装环境"),
				new Step("检查用户信息, 连接主机"),
				new Step("安装Prometheus"),
				new Step("启动prometheus"),
				new Step("验证prometheus启动状态"),
				new Step("安装完成"));
		// @formatter:on
		var curr = 0;
		var env = new NctigbaEnv().setHostid(hostId).setPort(promport).setUsername(PROMETHEUS_USER)
				.setType(type.PROMETHEUS);

		try {
			curr = nextStep(wsSession, steps, curr);

			if (envMapper
					.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.PROMETHEUS)) != null)
				throw new RuntimeException("只允许安装1个Prometheus实例");

			curr = nextStep(wsSession, steps, curr);
			OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
			if (hostEntity == null)
				throw new RuntimeException("host not found");
			env.setHost(hostEntity);
			try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), "root",
					encryptionUtils.decrypt(rootPassword));) {
			} catch (Exception e) {
				throw new RuntimeException("root password error");
			}
			try (var sshsession = connect(env, rootPassword, steps, curr);) {
				curr = nextStep(wsSession, steps, curr);
				var arch = sshsession.execute(command.ARCH);
				String name = NAME + arch(arch);
				String tar = name + TAR;
				if (!sshsession.test(command.STAT.parse(name))) {
					if (!sshsession.test(command.STAT.parse(tar))) {
						// sshsession.execute(command.WGET.parse(PATH + tar));
						var pkg = envMapper
								.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().like(NctigbaEnv::getPath, tar));
						if (pkg == null) {
							var f = Download.download(PATH + tar, "pkg/" + tar);
							pkg = new NctigbaEnv().setPath(f.getCanonicalPath()).setType(type.PROMETHEUS_PKG);
							addMsg(wsSession, steps, curr, "安装包下载成功");
							save(pkg);
						}
						sshsession.upload(pkg.getPath(), tar);
						addMsg(wsSession, steps, curr, "安装包传输成功");
					} else
						addMsg(wsSession, steps, curr, "当前路径下已存在安装包，使用该安装包安装成功");
					sshsession.execute(command.TAR.parse(tar));
				}
				env.setPath(name);

				curr = nextStep(wsSession, steps, curr);
				sshsession.executeNoWait(
						"cd " + env.getPath() + " && ./prometheus --web.enable-lifecycle --web.listen-address=:"
								+ promport + " --config.file=prometheus.yml &");
				ThreadUtil.sleep(3000L);

				curr = nextStep(wsSession, steps, curr);
				for (int i = 0; i < 10; i++) {
					ThreadUtil.sleep(3000L);
					try {
						String str = HttpUtils.sendGet("http://" + env.getHost().getPublicIp() + ":" + env.getPort()
								+ "/api/v1/status/runtimeinfo", null);
						if (StringUtils.isBlank(str))
							throw new CustomException();
					} catch (Exception e) {
						if (i == 9)
							throw new RuntimeException("prometheus 启动失败");
					}
				}

				curr = nextStep(wsSession, steps, curr);
				envMapper.insert(env);
				sendMsg(wsSession, steps, curr, status.DONE);
			}
		} catch (Exception e) {
			steps.get(curr).setState(status.ERROR).getMsg().add(e.getMessage());
			wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
			var sw = new StringWriter();
			try (var pw = new PrintWriter(sw);) {
				e.printStackTrace(pw);
			}
			wsUtil.sendText(wsSession, sw.toString());
		}
	}

	private SshSession connect(NctigbaEnv env, String rootPassword, List<Step> steps, int curr) throws IOException {
		OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
		if (hostEntity == null)
			throw new RuntimeException("host not found");
		env.setHost(hostEntity);
		var user = getUser(hostEntity, PROMETHEUS_USER, rootPassword, steps, curr);
		return SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), PROMETHEUS_USER,
				encryptionUtils.decrypt(user.getPassword()));
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

			@Override
			public int hashCode() {
				return job_name.hashCode();
			}

			@Override
			public boolean equals(Object o) {
				if (!(o instanceof job))
					return false;
				var j = (job) o;
				return j.getJob_name().equals(this.job_name);
			}
		}
	}

	public void uninstall(WsSession wsSession, String id) {
		// @formatter:off
		var steps = Arrays.asList(
				new Step("初始化"),
				new Step("连接主机"),
				new Step("查找prometheus进程号"),
				new Step("停止prometheus"),
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
				var pid = sshsession.execute(command.PS.parse("prometheus"));
				if (StrUtil.isNotBlank(pid)) {
					curr = nextStep(wsSession, steps, curr);
					sshsession.execute(command.KILL.parse(pid));
				} else
					curr = skipStep(wsSession, steps, curr);
				envMapper.deleteById(id);
				sendMsg(wsSession, steps, curr, status.DONE);
			}
		} catch (Exception e) {
			steps.get(curr).setState(status.ERROR).getMsg().add(e.getMessage());
			wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
			var sw = new StringWriter();
			try (var pw = new PrintWriter(sw);) {
				e.printStackTrace(pw);
			}
			wsUtil.sendText(wsSession, sw.toString());
		}
	}
}
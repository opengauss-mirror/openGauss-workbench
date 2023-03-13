package com.nctigba.observability.instance.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.common.web.exception.CustomException;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.type;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ExporterService;
import com.nctigba.observability.instance.service.PrometheusService;

import cn.hutool.core.collection.CollectionUtil;

@RestController
@RequestMapping("/observability/v1/environment")
public class EnvironmentController {
	@Autowired
	private NctigbaEnvMapper envMapper;
	@Autowired
	@AutowiredType(Type.PLUGIN_MAIN)
	private HostFacade hostFacade;
	@Autowired
	private ClusterManager clusterManager;
	@Autowired
	private PrometheusService prometheusService;

	@GetMapping("/prometheus")
	public List<NctigbaEnv> listPrometheus() {
		List<NctigbaEnv> env = envMapper
				.selectList(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.PROMETHEUS));
		env.forEach(e -> {
			e.setHost(hostFacade.getById(e.getHostid()));
		});
		return env;
	}

	@GetMapping("/exporter")
	public List<OpsClusterVO> listExporter() {
		var env = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery().in(NctigbaEnv::getType,
				List.of(type.NODE_EXPORTER, type.OPENGAUSS_EXPORTER)));
		var hosts = env.stream().map(NctigbaEnv::getHostid).collect(Collectors.toSet());
		var clusters = clusterManager.getAllOpsCluster();
		return clusters.stream().filter(c -> {
			var nodes = c.getClusterNodes().stream().filter(n -> {
				return hosts.contains(n.getHostId());
			}).collect(Collectors.toList());
			c.setClusterNodes(nodes);
			return nodes.size() > 0;
		}).collect(Collectors.toList());
	}

	@GetMapping("/hosts")
	public List<OpsHostEntity> hosts() {
		return hostFacade.listAll();
	}

	@GetMapping("/pkg")
	public Map<String, Object> listPkg(String key, String hostId) {
		Map<String, Object> map = new HashMap<>();
		var host = hostFacade.getById(hostId);
		LambdaQueryWrapper<NctigbaEnv> wrapper = Wrappers.<NctigbaEnv>lambdaQuery();
		boolean isPrometheus = "prometheus".equals(key);
		if (isPrometheus)
			wrapper.eq(NctigbaEnv::getType, type.PROMETHEUS_PKG);
		else
			wrapper.in(NctigbaEnv::getType, type.NODE_EXPORTER_PKG, type.OPENGAUSS_EXPORTER_PKG);
		var envs = envMapper.selectList(wrapper);
		if (isPrometheus) {
			var pkg = PrometheusService.NAME + PrometheusService.arch(host.getCpuArch()) + PrometheusService.TAR;
			map.put("pkg", pkg);
			map.put("url", PrometheusService.PATH + pkg);
			if (CollectionUtil.isNotEmpty(envs)) {
				var env = envs.stream().filter(e -> e.getPath().endsWith(pkg)).findFirst().orElse(null);
				map.put("flag", env != null);
				if (env != null)
					map.put("src", env.getPath());
			} else
				map.put("flag", false);
		} else {
			var nodepkg = ExporterService.NODE_EXPORTER_NAME + ExporterService.arch(host.getCpuArch())
					+ ExporterService.TAR;
			map.put("nodepkg", nodepkg);
			map.put("nodeurl", ExporterService.NODE_EXPORTER_PATH + nodepkg);
			var gausspkg = ExporterService.OPENGAUSS_EXPORTER_NAME + ExporterService.arch(host.getCpuArch())
					+ ExporterService.ZIP;
			map.put("gausspkg", gausspkg);
			map.put("gaussurl", ExporterService.OPENGAUSS_EXPORTER_PATH + gausspkg);
			if (CollectionUtil.isNotEmpty(envs)) {
				var nodeenv = envs.stream().filter(e -> e.getPath().endsWith(nodepkg)).findFirst().orElse(null);
				map.put("nodeflag", nodeenv != null);
				if (nodeenv != null)
					map.put("nodesrc", nodeenv.getPath());
				var gaussenv = envs.stream().filter(e -> e.getPath().endsWith(gausspkg)).findFirst().orElse(null);
				map.put("gaussflag", gaussenv != null);
				if (gaussenv != null)
					map.put("gausssrc", gaussenv.getPath());
			} else {
				map.put("nodeflag", false);
				map.put("gaussflag", false);
			}
		}
		return map;
	}

	@PostMapping("/upload")
	public void upload(@RequestParam String name, MultipartFile pkg) {
		var env = new NctigbaEnv();
		var file = new File("pkg/" + name);
		var parent = file.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		if (file.exists())
			file.delete();
		try {
			pkg.transferTo(file.getCanonicalFile());
			env.setPath(file.getCanonicalPath());
			if (name.startsWith("prometheus"))
				env.setType(type.PROMETHEUS_PKG);
			else if (name.startsWith("node_exporter"))
				env.setType(type.NODE_EXPORTER_PKG);
			else if (name.startsWith("opengauss_exporter"))
				env.setType(type.OPENGAUSS_EXPORTER_PKG);
			prometheusService.save(env);
		} catch (IllegalStateException | IOException e) {
			throw new CustomException("upload fail", e);
		}
	}
}
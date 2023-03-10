package com.nctigba.observability.log.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.CustomException;
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
import com.nctigba.observability.log.env.NctigbaEnv;
import com.nctigba.observability.log.env.NctigbaEnv.type;
import com.nctigba.observability.log.env.NctigbaEnvMapper;
import com.nctigba.observability.log.service.ClusterManager;
import com.nctigba.observability.log.service.ElasticsearchService;
import com.nctigba.observability.log.service.FilebeatService;

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
	private ElasticsearchService elasticsearchService;

	@GetMapping("/elasticsearch")
	public List<NctigbaEnv> listPrometheus() {
		List<NctigbaEnv> env = envMapper
				.selectList(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.ELASTICSEARCH));
		env.forEach(e -> {
			e.setHost(hostFacade.getById(e.getHostid()));
		});
		return env;
	}

	@GetMapping("/filebeat")
	public List<OpsClusterVO> listExporter() {
		var env = envMapper
				.selectList(Wrappers.<NctigbaEnv>lambdaQuery().in(NctigbaEnv::getType, List.of(type.FILEBEAT)));
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
		boolean isElasticsearch = "elasticsearch".equals(key);
		wrapper.eq(NctigbaEnv::getType, isElasticsearch ? type.ELASTICSEARCH_PKG : type.FILEBEAT_PKG);
		var envs = envMapper.selectList(wrapper);
		String pkg;
		if (isElasticsearch) {
			pkg = ElasticsearchService.NAME + host.getCpuArch() + ElasticsearchService.TAR;
			map.put("pkg", pkg);
			map.put("url", ElasticsearchService.PATH + pkg);
		} else {
			pkg = FilebeatService.NAME + FilebeatService.arch(host.getCpuArch()) + FilebeatService.TAR;
			map.put("pkg", pkg);
			map.put("url", FilebeatService.PATH + pkg);
		}
		if (CollectionUtil.isNotEmpty(envs)) {
			var env = envs.stream().filter(e -> e.getPath().endsWith(pkg)).findFirst().orElse(null);
			map.put("flag", env != null);
			if (env != null)
				map.put("src", env.getPath());
		} else
			map.put("flag", false);
		return map;
	}

	@PostMapping("/upload")
	public void upload(@RequestParam String name, MultipartFile pkg) {
		var env = new NctigbaEnv();
		env.setPath("localhost");
		var file = new File("pkg/" + name);
		var parent = file.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		if (file.exists())
			file.delete();
		try {
			pkg.transferTo(file.getCanonicalFile());
			env.setPath(file.getCanonicalPath());
			if (name.startsWith("elasticsearch"))
				env.setType(type.ELASTICSEARCH_PKG);
			else if (name.startsWith("filebeat"))
				env.setType(type.FILEBEAT_PKG);
			elasticsearchService.save(env);
		} catch (IllegalStateException | IOException e) {
			throw new CustomException("upload fail", e);
		}
	}
}
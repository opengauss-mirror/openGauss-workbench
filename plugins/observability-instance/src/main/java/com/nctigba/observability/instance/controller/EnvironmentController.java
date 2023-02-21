package com.nctigba.observability.instance.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.type;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.ClusterManager;

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

	@GetMapping("/prometheus")
	public List<NctigbaEnv> listPrometheus() {
		List<NctigbaEnv> env = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, type.PROMETHEUS));
		env.forEach(e->{
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
		return clusters.stream().filter(c->{
			var nodes = c.getClusterNodes().stream().filter(n->{
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
}
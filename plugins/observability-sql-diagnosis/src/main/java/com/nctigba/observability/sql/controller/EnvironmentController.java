package com.nctigba.observability.sql.controller;

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
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.model.NctigbaEnv.type;
import com.nctigba.observability.sql.service.ClusterManager;

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

	@GetMapping("/agent")
	public List<OpsClusterVO> listAgent() {
		var env = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery().in(NctigbaEnv::getType, List.of(type.AGENT)));
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
}
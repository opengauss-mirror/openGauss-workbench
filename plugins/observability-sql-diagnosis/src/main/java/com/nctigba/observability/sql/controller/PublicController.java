package com.nctigba.observability.sql.controller;

import java.util.List;
import java.util.Map;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.sql.model.DictionaryConfig;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.DictionaryConfigService;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sqlDiagnosis/api/v1")
@RequiredArgsConstructor
public class PublicController {
	private final ClusterManager clusterManager;
	private final DictionaryConfigService dictionaryConfigService;

	@GetMapping("/clusters")
	public List<OpsClusterVO> listCluster() {
		return clusterManager.getAllOpsCluster();
	}

	/**
	 * database
	 */
	@GetMapping("/clusters/{clusterId}/instances")
	@ApiOperation(value = "DATABASE", notes = "DATABASE")
	public List<String> datebaseList(@PathVariable String clusterId) {
		return clusterManager.databaseList(clusterId);
	}

	@GetMapping("/node-settings")
	public Map<String, Object> configList() {
		return dictionaryConfigService.listFromCache();
	}

	@PutMapping("/node-settings/{nodeid}")
	public List<DictionaryConfig> configSet(@PathVariable("nodeid") String nodeId, @RequestBody List<DictionaryConfig> configs) {
		configs.forEach(config -> dictionaryConfigService.set(config.setNodeId(nodeId)));
		return configs;
	}
}
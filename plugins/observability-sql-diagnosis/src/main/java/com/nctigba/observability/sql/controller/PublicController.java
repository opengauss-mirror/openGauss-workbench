/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  PublicController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/controller/PublicController.java
 *
 *  -------------------------------------------------------------------------
 */

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

import com.nctigba.observability.sql.model.entity.DictionaryConfigDO;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.DictionaryConfigServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sqlDiagnosis/api/v1")
@RequiredArgsConstructor
public class PublicController {
	private final ClusterManager clusterManager;
	private final DictionaryConfigServiceImpl dictionaryConfigServiceImpl;

	@GetMapping("/clusters")
	public List<OpsClusterVO> listCluster() {
		return clusterManager.getAllOpsCluster();
	}

	/**
	 * database
	 */
	@GetMapping("/clusters/{clusterId}/instances")
	public List<String> datebaseList(@PathVariable String clusterId) {
		return clusterManager.databaseList(clusterId);
	}

	@GetMapping("/node-settings")
	public Map<String, Object> configList() {
		return dictionaryConfigServiceImpl.listFromCache();
	}

	@PutMapping("/node-settings/{nodeid}")
	public List<DictionaryConfigDO> configSet(@PathVariable("nodeid") String nodeId, @RequestBody List<DictionaryConfigDO> configs) {
		configs.forEach(config -> dictionaryConfigServiceImpl.set(config.setNodeId(nodeId)));
		return configs;
	}
}
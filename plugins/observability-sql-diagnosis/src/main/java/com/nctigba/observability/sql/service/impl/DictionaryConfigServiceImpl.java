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
 *  DictionaryConfigServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/DictionaryConfigServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.observability.sql.mapper.DictionaryConfigMapper;
import com.nctigba.observability.sql.model.entity.DictionaryConfigDO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DictionaryConfigServiceImpl extends ServiceImpl<DictionaryConfigMapper, DictionaryConfigDO> {
	private static final Map<String, Map<String, String>> CONFIGS = new HashMap<>();
	private final ClusterManager clusterManager;

	public Map<String, Object> listFromCache() {
		var list = new ArrayList<>();
		var columns = JSONObject.parseArray(CONFIGS.get("0").get("COLUMNS"));
		for (Entry<String, Map<String, String>> e : CONFIGS.entrySet()) {
			var config = new HashMap<String, String>();
			config.putAll(e.getValue());
			columns.forEach(col->{
				var key = ((JSONObject)col).getString("key");
				if(!config.containsKey(key))
					config.put(key, "");
			});
			list.add(config);
		}
		return Map.of("columns", columns, "data", list);
	}

	public void set(DictionaryConfigDO config) {
		if (!CONFIGS.containsKey(config.getNodeId()))
			CONFIGS.put(config.getNodeId(), init(config));
		var c = CONFIGS.get(config.getNodeId());
		if (c.containsKey(config.getKey())) {
			if (c.get(config.getKey()).equals(config.getValue()))
				return;
			c.put(config.getKey(), config.getValue());
			baseMapper.updateById(config);
		} else {
			c.put(config.getKey(), config.getValue());
			baseMapper.insert(config);
		}
	}

	@PostConstruct
	public void refresh() {
		baseMapper.selectList(Wrappers.emptyWrapper()).forEach(config -> {
			if (!CONFIGS.containsKey(config.getNodeId()))
				CONFIGS.put(config.getNodeId(), init(config));
			CONFIGS.get(config.getNodeId()).put(config.getKey(), config.getValue());
		});
	}

	private HashMap<String, String> init(DictionaryConfigDO config) {
		var value = new HashMap<String, String>();
		value.put("clusterId", clusterManager.getOpsClusterIdByNodeId(config.getNodeId()));
		value.put("nodeId", config.getNodeId());
		return value;
	}

	public String getConfig(String nodeId, String key) {
		if (CONFIGS.containsKey(nodeId))
			if (CONFIGS.get(nodeId).containsKey(key))
				return CONFIGS.get(nodeId).get(key);
		if (CONFIGS.get("0").containsKey(key))
			return CONFIGS.get("0").get(key);
		return null;
	}
}
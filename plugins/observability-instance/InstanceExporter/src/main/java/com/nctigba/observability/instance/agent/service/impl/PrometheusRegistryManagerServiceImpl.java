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
 *  PrometheusRegistryManagerServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/impl/PrometheusRegistryManagerServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service.impl;

import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.service.PrometheusRegistryManagerService;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * CollectService Implement
 *
 * @since 2023/12/1
 */
@Service
public class PrometheusRegistryManagerServiceImpl implements PrometheusRegistryManagerService {
    private static Map<String, PrometheusRegistry> registryCache = new HashMap<>();

    /**
     * @inheritDoc
     */
    @Override
    public void storeRegistry(TargetConfig config, PrometheusRegistry registry) {
        registryCache.put(getRegistryKey(config), registry);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isTargetRegistryStored(TargetConfig config) {
        return registryCache.containsKey(getRegistryKey(config));
    }

    /**
     * @inheritDoc
     */
    @Override
    public PrometheusRegistry getCollector(TargetConfig config) {
        return registryCache.get(getRegistryKey(config));
    }

    String getRegistryKey(TargetConfig config) {
        return config.getNodeId();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void clear() {
        registryCache = new HashMap<>();
    }
}

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
 *  PrometheusRegistryManagerService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/PrometheusRegistryManagerService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service;

import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

/**
 * Prometheus registry manager
 *
 * @since 2023/12/1
 */
public interface PrometheusRegistryManagerService {
    /**
     * Store prometheus registry in manager
     *
     * @param config   Target config
     * @param registry Prometheus Registry
     * @since 2023/12/1
     */
    void storeRegistry(TargetConfig config, PrometheusRegistry registry);

    /**
     * Check this target is stored in manager or not
     *
     * @param config Target config
     * @return Boolean
     * @since 2023/12/1
     */
    boolean isTargetRegistryStored(TargetConfig config);

    /**
     * Get stored PrometheusRegistry by target config
     *
     * @param config Target config
     * @return PrometheusRegistry
     * @since 2023/12/1
     */
    PrometheusRegistry getCollector(TargetConfig config);

    /**
     * Clear all PrometheusRegistry in manager
     *
     * @since 2023/12/1
     */
    void clear();
}

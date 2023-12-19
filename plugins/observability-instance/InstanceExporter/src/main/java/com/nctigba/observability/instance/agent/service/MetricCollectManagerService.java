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
 *  MetricCollectManagerService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/MetricCollectManagerService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service;

import com.nctigba.observability.instance.agent.collector.AgentCollector;
import com.nctigba.observability.instance.agent.metric.MetricResult;

import java.util.List;

/**
 * Metric collector manager
 *
 * @since 2023/12/1
 */
public interface MetricCollectManagerService {
    /**
     * Store a collector in manager
     *
     * @param key       Key for collector
     * @param collector Agent Collector
     * @since 2023/12/1
     */
    void storeCollector(String key, AgentCollector collector);

    /**
     * Get collector from manager
     *
     * @param key Key for collector
     * @return Agent Collector
     * @since 2023/12/1
     */
    AgentCollector getCollector(String key);


    /**
     * Store group collect time in manager
     *
     * @param key Metric group name
     * @since 2023/12/1
     */
    void storeGroupCollectTime(String key);

    /**
     * Get the gap time for this collect action from last one
     *
     * @param key Key for collector
     * @return Key value
     * @since 2023/12/1
     */
    long getGroupCollectGapTime(String key);


    /**
     * Get the cached value for metric group
     *
     * @param groupCollectKey Metric group key
     * @return Metric result
     * @since 2023/12/1
     */
    List<List<MetricResult>> getCachedGroupCollectData(String groupCollectKey);

    /**
     * Cache metric group collect result
     *
     * @param groupCollectKey Metric group key
     * @param result          metric group collect result
     * @since 2023/12/1
     */
    void cacheGroupCollectData(String groupCollectKey, List<List<MetricResult>> result);


    /**
     * clear all collector, data, time in manager
     *
     * @since 2023/12/1
     */
    void clear();
}

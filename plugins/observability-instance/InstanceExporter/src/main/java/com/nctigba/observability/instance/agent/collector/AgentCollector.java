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
 *  AgentCollector.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/collector/AgentCollector.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.collector;

/**
 * Interface for metric collector, base on prometheus java client Collector
 *
 * @since 2023/12/1
 */
public interface AgentCollector {
    /**
     * Prometheus Collector don't have a methods to clean all label values,
     * so add this method to clean label values
     *
     * @since 2023/12/1
     */
    void cleanAllLabelValuesData();
}

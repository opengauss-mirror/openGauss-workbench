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
 *  Metric.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/Metric.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric;

import com.nctigba.observability.instance.agent.constant.CollectConstants;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;

import java.util.List;

/**
 * Metric interface, when you want to add a metric to collect, implements this interface
 *
 * @since 2023/12/1
 */
public interface Metric {
    /**
     * Get type of metric
     *
     * @return com.nctigba.observability.instance.agent.metric.MetricType
     * @since 2023/12/1
     */
    MetricType getType();

    /**
     * Get metric group name
     *
     * @return Group name
     * @since 2023/12/1
     */
    String getGroupName();

    /**
     * Get metric names in this group
     *
     * @return Names
     * @since 2023/12/1
     */
    String[] getNames();

    /**
     * Get helps for metrics
     *
     * @return Helps
     * @since 2023/12/1
     */
    String[] getHelps();

    /**
     * Get label names for metrics
     *
     * @return Label names
     * @since 2023/12/1
     */
    String[] getLabelNames();

    /**
     * Real collect metric data action
     *
     * @param target Target info
     * @param param  Collect params
     * @return Collect result
     * @throws CollectException Collect data error
     * @since 2023/12/1
     */
    List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) throws CollectException;

    /**
     * Get collect group key from target config
     *
     * @param targetConfig Target config DTO
     * @return Key value
     */
    default String getCollectGroupKey(TargetConfig targetConfig) {
        return targetConfig.getNodeId() + CollectConstants.SEPARATOR + getGroupName();
    }

    /**
     * key for Collector which register into Prometheus Register
     *
     * @param targetConfig    Config of target
     * @param metricNameIndex Metric name index of Metric names
     * @return Key of target and name
     * @since 2023/12/1
     */
    default String getCollectorKey(TargetConfig targetConfig, int metricNameIndex) {
        return targetConfig.getNodeId() + CollectConstants.SEPARATOR + getNames()[metricNameIndex];
    }
}
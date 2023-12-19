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
 *  AgentGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/collector/AgentGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.collector;

import io.prometheus.metrics.core.datapoints.GaugeDataPoint;
import io.prometheus.metrics.core.metrics.Gauge;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent Gauge metrics, base on Prometheus java client Counter
 *
 * @since 2023/12/1
 */
public class AgentGauge implements AgentCollector {
    private Gauge gauge;
    private List<String[]> labelValues = new ArrayList<>();

    /**
     * Constructor
     *
     * @param gauge Prometheus Gauge
     * @since 2023/12/1
     */
    public AgentGauge(Gauge gauge) {
        this.gauge = gauge;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void cleanAllLabelValuesData() {
        labelValues.forEach(z -> {
            gauge.remove(z);
        });
        labelValues = new ArrayList<>();
    }


    /**
     * Set prometheus label values and cache
     *
     * @param labelValuesSetting set Gauge label values
     * @return Prometheus GaugeDataPoint
     * @since 2023/12/1
     */
    public GaugeDataPoint labelValues(String... labelValuesSetting) {
        labelValues.add(labelValuesSetting);
        return gauge.labelValues(labelValuesSetting);
    }
}

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
 *  AgentCounter.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/collector/AgentCounter.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.collector;

import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent Counter metrics, base on Prometheus java client Counter
 *
 * @since 2023/12/1
 */
@Data
public class AgentCounter implements AgentCollector {
    private Counter counter;
    private List<String[]> labelValues = new ArrayList<>();

    /**
     * Constructor
     *
     * @param counter Prometheus Counter
     * @since 2023/12/1
     */
    public AgentCounter(Counter counter) {
        this.counter = counter;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void cleanAllLabelValuesData() {
        labelValues.forEach(z -> {
            counter.remove(z);
        });
        labelValues = new ArrayList<>();
    }

    /**
     * Set prometheus label values and cache
     *
     * @param labelValuesSetting set Counter label values
     * @return Prometheus CounterDataPoint
     * @since 2023/12/1
     */
    public CounterDataPoint labelValues(String... labelValuesSetting) {
        labelValues.add(labelValuesSetting);
        return counter.labelValues(labelValuesSetting);
    }
}

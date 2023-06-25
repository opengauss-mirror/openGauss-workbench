/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import io.prometheus.client.Collector;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Metric {
    Collector.Type type;
    String help;
    List<String> labelNames = new ArrayList<>();
    List<Metric.values> values = new ArrayList<>();

    public Metric addValue(List<String> labelValues, String value) {
        return addValue(labelValues, StrUtil.isBlank(value) ? 0 : Double.parseDouble(value));
    }

    public Metric addValue(List<String> labelValues, double value) {
        values.add(new values(labelValues, value));
        return this;
    }

    public Metric(Collector.Type type, List<String> labelNames) {
        this.type = type;
        this.labelNames = labelNames;
    }

    public Metric(Collector.Type type, String help, List<String> labelNames) {
        this.type = type;
        this.help = help;
        this.labelNames = labelNames;
    }

    public List<String> getLabelNames() {
        return labelNames == null ? Collections.emptyList() : labelNames;
    }

    @Data
    @AllArgsConstructor
    public static class values {
        private List<String> labelValues;
        private Double value;

        public List<String> getLabelValues() {
            return labelValues == null ? Collections.emptyList() : labelValues;
        }
    }
}
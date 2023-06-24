/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric.system.cpu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.util.FileUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.stereotype.Service;

import org.opengauss.plugin.agent.metric.OSmetric;

import io.prometheus.client.Collector.Type;

@Service("load")
public class LoadAvg implements OSmetric {
    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        var load1 = new Metric(Type.GAUGE, "1m load average.", null);
        var load5 = new Metric(Type.GAUGE, "5m load average.", null);
        var load15 = new Metric(Type.GAUGE, "15m load average.", null);
        FileUtil.readFileLine("/proc/loadavg", s -> {
            var part = StringUtil.splitByBlank(s);
            load1.addValue(null, part[0]);
            load5.addValue(null, part[1]);
            load15.addValue(null, part[2]);
        });
        return Map.of("1", load1, "5", load5, "15", load15);
    }
}
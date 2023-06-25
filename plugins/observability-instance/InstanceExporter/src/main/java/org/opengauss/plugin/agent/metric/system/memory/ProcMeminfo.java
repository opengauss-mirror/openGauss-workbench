/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric.system.memory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.util.FileUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.stereotype.Service;

import org.opengauss.plugin.agent.metric.OSmetric;

import io.prometheus.client.Collector.Type;

/**
 * Linux file <code>/proc/meminfo</code>
 */
@Service("memory")
public class ProcMeminfo implements OSmetric {
    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        var map = new HashMap<String, Metric>();
        FileUtil.readFileLine("/proc/meminfo", line -> {
            var part = StringUtil.splitByBlank(line);
            double value = Double.parseDouble(part[1]);
            var key = StringUtil.replaceParenthesis(line);
            if (part.length == 3) {
                value *= 1024;
                key += "_bytes";
            }
            map.put("_" + key, new Metric(Type.GAUGE, null).addValue(null, value));
        });
        return map;
    }

}
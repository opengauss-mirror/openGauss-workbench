/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric.system.network;

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

@Service("sockstat_")
public class SockStat implements OSmetric {
    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        Map<String, Metric> map = new HashMap<>();
        FileUtil.readFileLine("/proc/net/sockstat", line -> {
            var part = StringUtil.splitByBlank(line);
            var key = StringUtil.replaceParenthesis(part[0]) + "_";
            for (int i = 1; i * 2 <= part.length; i++) {
                map.put(key + part[2 * i - 1], new Metric(Type.GAUGE, null).addValue(null, part[2 * i]));
            }
        });
        return map;
    }
}
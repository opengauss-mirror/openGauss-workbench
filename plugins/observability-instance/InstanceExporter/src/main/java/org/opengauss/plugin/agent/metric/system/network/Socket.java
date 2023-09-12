/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.metric.system.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.metric.OSmetric;
import org.opengauss.plugin.agent.util.CmdUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.stereotype.Service;

import io.prometheus.client.Collector.Type;

@Service("network_socket")
public class Socket implements OSmetric {
    private static final String SPLIT = ",";

    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        Map<String, Integer> counter = new HashMap<>();
        CmdUtil.readFromCmd("netstat", (index, line) -> {
            if (index < 2)
                return;
            var part = StringUtil.splitByBlank(line);
            if (part.length < 5)
                return;
            var proto = part[0];
            if (proto.startsWith("tcp") || proto.startsWith("udp")) {
                var key = proto + SPLIT + part[5];
                counter.put(key, counter.getOrDefault(key, 0) + 1);
            }
        });
        var metric = new Metric(Type.GAUGE, Arrays.asList("proto", "state"));
        counter.forEach((key, value) -> {
            var part = key.split(SPLIT);
            metric.addValue(Arrays.asList(part), value);
        });
        return Map.of("", metric);
    }
}
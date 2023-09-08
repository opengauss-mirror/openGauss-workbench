/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.metric.system.memory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.metric.OSmetric;
import org.opengauss.plugin.agent.util.CmdUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.stereotype.Service;

import io.prometheus.client.Collector.Type;

@Service("free_")
public class Free implements OSmetric {
    private static final String[] KEYS = {
            "type",
            "total",
            "used",
            "free",
            "shared",
            "cache",
            "available"
    };

    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        Map<String, Metric> map = new HashMap<>();
        CmdUtil.readFromCmd("free", (index, line) -> {
            if (index == 0) {
                // total used free shared buff/cache available
                return;
            }
            var part = StringUtil.splitByBlank(line);
            // Mem: 15896504 9520972 496816 1156408 5878716 4745916
            // Swap: 8278012 209664 8068348
            for (int i = 1; i < part.length; i++) {
                map.put(StringUtil.removeLast(part[0]) + "_" + KEYS[i] + "_bytes",
                        new Metric(Type.GAUGE, null).addValue(null, Double.parseDouble(part[i]) * 1024));
            }
            return;
        });
        return map;
    }
}
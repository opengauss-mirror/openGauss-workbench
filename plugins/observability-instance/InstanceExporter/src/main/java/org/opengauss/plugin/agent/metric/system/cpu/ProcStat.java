/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric.system.cpu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.util.FileUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.stereotype.Service;

import org.opengauss.plugin.agent.metric.OSmetric;

import io.prometheus.client.Collector.Type;

/**
 * Linux file <code>/proc/stat</code>
 */
@Service("cpu")
public class ProcStat implements OSmetric {
    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        var seconds_total = new Metric(Type.COUNTER, Arrays.asList("cpu", "mode"));
        FileUtil.readFileLine("/proc/stat", s -> {
            var part = StringUtil.splitByBlank(s);
            if (part[0].equalsIgnoreCase("cpu"))
                return;
            if (part[0].startsWith("cpu")) {
                String cpuIndex = part[0].replaceAll("cpu", "");
                for (cpuStat stat : cpuStat.values()) {
                    int index = stat.ordinal() + 1;
                    if (index < part.length) {
                        var value = Double.parseDouble(part[index]);
                        value /= 100;
                        seconds_total.addValue(Arrays.asList(cpuIndex, stat.name()), value);
                    }
                }
            }
        });
        return Map.of("_seconds_total", seconds_total);
    }

    public enum cpuStat {
        user,
        nice,
        system,
        idle,
        iowait,
        irq,
        softirq,
        steal,
    }
}
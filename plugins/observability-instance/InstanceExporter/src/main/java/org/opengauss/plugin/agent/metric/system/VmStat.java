/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric.system;

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

/**
 * @see <a href=
 *      "https://zhuanlan.zhihu.com/p/364383421">zhuanlan.zhihu.com/p/364383421</a>
 */
@Service("vmstat_")
public class VmStat implements OSmetric {
    private static final String[] KEYS = {
            "r",
            "b",
            "swpd",
            "free",
            "buff",
            "cache",
            "si",
            "so",
            "bi",
            "bo",
            "in",
            "cs",
            "us",
            "sy",
            "wa",
            "st" };

    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        Map<String, Metric> map = new HashMap<>();
        CmdUtil.readFromCmd(CmdUtil.cmd("vmstat"), (index, line) -> {
            if (index < 2)
                return;
            var part = StringUtil.splitByBlank(line);
            for (int i = 0; i < part.length && i < KEYS.length; i++) {
                map.put(KEYS[i], new Metric(Type.GAUGE, null).addValue(null, part[i]));
            }
        });
        return map;
    }
}
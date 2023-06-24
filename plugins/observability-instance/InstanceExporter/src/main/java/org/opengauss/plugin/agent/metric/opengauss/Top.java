/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric.opengauss;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.util.CmdUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.stereotype.Service;

import org.opengauss.plugin.agent.metric.DBmetric;

import io.prometheus.client.Collector.Type;

@Service
public class Top implements DBmetric {
    private static final String COMMAND_PORT_TO_PID = "netstat -nap|grep :'|grep LISTEN";
    private static final String COMMAND_TOP_ = "top -b -n 1|grep ";

    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        if (dbPort == null)
            return Collections.emptyMap();
        Set<String> pids = new HashSet<>();
        CmdUtil.readFromCmd(COMMAND_PORT_TO_PID.replaceAll("'", dbPort.toString()), line -> {
            var part = StringUtil.splitByBlank(line);
            var pid = part[part.length - 1];
            pid = pid.split("/")[0];
            pids.add(pid);
        });
        Map<String, Metric> map = new HashMap<String, Metric>();
        if (pids.size() == 0)
            return map;
        CmdUtil.readFromCmd(COMMAND_TOP_ + pids.iterator().next(), line -> {
            // PID USER PR NI VIRT RES SHR S %CPU %MEM TIME+ COMMAND
            // 163616 omm 20 0 3133820 525420 171716 S 0.0 3.3 507:05.43 gaussdb
            var part = StringUtil.splitByBlank(line);
            map.put("top_db_cpu", new Metric(Type.GAUGE, null).addValue(null, part[8]));
            map.put("top_db_mem", new Metric(Type.GAUGE, null).addValue(null, part[9]));
        });
        return map;
    }
}
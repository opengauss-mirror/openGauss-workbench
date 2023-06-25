/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric.system.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.util.FileUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.stereotype.Service;

import org.opengauss.plugin.agent.metric.OSmetric;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import io.prometheus.client.Collector;
import lombok.extern.log4j.Log4j2;

@Service("netstat_")
@Log4j2
public class Netstat implements OSmetric {
    private static final Pattern PATTERN = Pattern.compile(
            "^(.*_(InErrors|InErrs)|Ip_Forwarding|Ip(6|Ext)_(InOctets|OutOctets)|Icmp6?_(InMsgs|OutMsgs)|TcpExt_(Listen.*|Syncookies.*|TCPSynRetrans|TCPTimeouts)|Tcp_(ActiveOpens|InSegs|OutSegs|OutRsts|PassiveOpens|RetransSegs|CurrEstab)|Udp6?_(InDatagrams|OutDatagrams|NoPorts|RcvbufErrors|SndbufErrors))$");

    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        var netStats = getNetStats("/proc/net/netstat");
        var snmp = getNetStats("/proc/net/snmp");
        var snmp6Stats = getSNMP6Stats("/proc/net/snmp6");
        netStats.putAll(snmp);
        netStats.putAll(snmp6Stats);
        var result = new HashMap<String, Metric>();
        netStats.forEach((key, map) -> {
            map.forEach((name, value) -> {
                String metricName = key + "_" + name;
                if (ReUtil.contains(PATTERN, metricName))
                    result.put(metricName, new Metric(Collector.Type.UNKNOWN, null).addValue(null, value));
            });
        });
        return result;
    }

    private Map<String, Map<String, Long>> getNetStats(String fileName) throws FileNotFoundException, IOException {
        Map<String, Map<String, Long>> values = new HashMap<String, Map<String, Long>>();
        List<String> keys = new ArrayList<String>();
        FileUtil.readFileLine(fileName, (i, line) -> {
            var part = StringUtil.splitByBlank(line);
            if (keys.isEmpty()) {
                Collections.addAll(keys, part);
                return;
            }
            try {
                var map = new HashMap<String, Long>();
                for (int j = 1; j < part.length; j++) {
                    map.put(keys.get(j), NumberUtil.parseLong(part[j]));
                }
                String key = keys.get(0);
                values.put(key.substring(0, key.length() - 1), map);
            } catch (Exception e) {
                log.error("{} error:{}", fileName, i);
            } finally {
                keys.clear();
            }
        });
        return values;
    }

    private Map<String, Map<String, Long>> getSNMP6Stats(String fileName) throws FileNotFoundException, IOException {
        Map<String, Map<String, Long>> values = new HashMap<String, Map<String, Long>>();
        FileUtil.readFileLine(fileName, line -> {
            // Expect to have "6" in metric name, skip line otherwise
            if (!line.contains("6"))
                return;
            var part = StringUtil.splitByBlank(line);
            if (part.length < 2)
                return;
            var keys = part[0].split("6");
            var protocol = keys[0] + 6;
            if (!values.containsKey(protocol))
                values.put(protocol, new HashMap<String, Long>());
            values.get(protocol).put(keys[1], NumberUtil.parseLong(part[1]));
        });
        return values;
    }
}
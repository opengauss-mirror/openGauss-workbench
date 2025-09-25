/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * HostMonitorCacheService.java
 *
 * IDENTIFICATION
 * openGauss-datakit/visualtool-service/src/main/java/org/opengauss/admin/system/service/HostMonitorCacheService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.entity.agent.MetricRealTime;
import org.opengauss.admin.common.utils.MathUtils;
import org.opengauss.agent.repository.MetricRealTimeStorageService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

/**
 * HostMonitorCacheService
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Slf4j
@Service
public class HostMonitorAgentService {
    @Resource
    private MetricRealTimeStorageService metricRealTimeStorageService;
    @Resource
    private HostBasicService hostBasicService;

    /**
     * start agent host monitor scheduled
     *
     * @param cacheMap cache map
     * @return agent id set
     */
    public Set<String> startAgentHostMonitorScheduled(Map<String, Map<String, String>> cacheMap) {
        // host dynamic info,support seconds level data refresh
        List<MetricRealTime> list = metricRealTimeStorageService.list();
        Map<String, List<MetricRealTime>> agentMetricsMap = list.stream()
            .collect(Collectors.groupingBy(metric -> String.valueOf(metric.getAgentId()), Collectors.toList()));
        agentMetricsMap.forEach((hostId, metrics) -> {
            cacheMap.compute(hostId, (k, v) -> {
                if (Objects.isNull(v)) {
                    v = new HashMap<>();
                }
                // cache host basic info from local db cache ops_host
                v.putAll(hostBasicService.getHostBasicInfoMap(k));
                // if agent metrics has current host id, update cache
                if (agentMetricsMap.containsKey(hostId)) {
                    v.putAll(refreshHostMonitorCache(agentMetricsMap.get(hostId)));
                }
                return v;
            });
        });
        return agentMetricsMap.keySet();
    }

    private Map<String, String> refreshHostMonitorCache(List<MetricRealTime> metrics) {
        Map<String, String> cache = new HashMap<>();
        for (MetricRealTime metric : metrics) {
            switch (metric.getMetric()) {
                case AgentConstants.HostMetric.SYSTEM_CPU_USAGE, AgentConstants.HostMetric.SYSTEM_MEMORY_USAGE,
                    AgentConstants.HostMetric.SYSTEM_MEMORY_AVAILABLE, AgentConstants.HostMetric.SYSTEM_MEMORY_TOTAL ->
                    cache.put(metric.getMetric(), MathUtils.formatScaleTwo(metric.getValue()));
                case AgentConstants.HostMetric.SYSTEM_NETWORK_RECEIVED ->
                    accMergesMetricsValue(cache, AgentConstants.HostMetric.SYSTEM_NETWORK_RECEIVED, metric.getValue());
                case AgentConstants.HostMetric.SYSTEM_NETWORK_SENT ->
                    accMergesMetricsValue(cache, AgentConstants.HostMetric.SYSTEM_NETWORK_SENT, metric.getValue());
                case AgentConstants.HostMetric.SYSTEM_DISK_TOTAL ->
                    accMergesMetricsValue(cache, AgentConstants.HostMetric.SYSTEM_DISK_TOTAL, metric.getValue());
                case AgentConstants.HostMetric.SYSTEM_DISK_USED ->
                    accMergesMetricsValue(cache, AgentConstants.HostMetric.SYSTEM_DISK_USED, metric.getValue());
                case AgentConstants.HostMetric.SYSTEM_DISK_FREE ->
                    accMergesMetricsValue(cache, AgentConstants.HostMetric.SYSTEM_DISK_FREE, metric.getValue());
                default -> {
                    log.debug("unknown metric:{}", metric.getMetric());
                }
            }
        }
        String networkName = extractNetworkInterfaceName(metrics);
        refreshHostNetWork(cache, networkName);
        refreshHostDiskUsage(cache);
        return cache;
    }

    private String extractNetworkInterfaceName(List<MetricRealTime> metrics) {
        return metrics.stream()
            .filter(metric -> metric.getMetric().equalsIgnoreCase(AgentConstants.HostMetric.SYSTEM_NETWORK_SENT))
            .map(MetricRealTime::getProperty)
            .filter(prop -> StrUtil.isNotEmpty(prop) && !StrUtil.equalsIgnoreCase(prop, "none"))
            .distinct()
            .collect(Collectors.joining(":"));
    }

    private void refreshHostNetWork(Map<String, String> cache, String networkName) {
        if (cache.containsKey(AgentConstants.HostMetric.SYSTEM_NETWORK_RECEIVED) && cache.containsKey(
            AgentConstants.HostMetric.SYSTEM_NETWORK_SENT)) {
            String netMonitor = networkName + "|" + cache.get(AgentConstants.HostMetric.SYSTEM_NETWORK_SENT) + "|"
                + cache.get(AgentConstants.HostMetric.SYSTEM_NETWORK_RECEIVED);
            cache.put(AgentConstants.HostMetric.NET_MONITOR, netMonitor);
        }
    }

    /**
     * get agent host info
     *
     * @param hostId host id
     * @return map
     */
    public Map<String, String> getAgentHostInfo(String hostId) {
        List<MetricRealTime> list = metricRealTimeStorageService.listAgentHostInfo(hostId);
        if (CollUtil.isEmpty(list)) {
            return new HashMap<>();
        }
        return refreshHostMonitorCache(list);
    }

    private static String refreshHostDiskUsage(Map<String, String> cache) {
        return cache.compute(AgentConstants.HostMetric.SYSTEM_DISK_USAGE,
            (k, v) -> MathUtils.percent(cache.get(AgentConstants.HostMetric.SYSTEM_DISK_USED),
                cache.get(AgentConstants.HostMetric.SYSTEM_DISK_TOTAL)));
    }

    private static void accMergesMetricsValue(Map<String, String> cache, String key, String metricValue) {
        cache.compute(key, (k, v) -> {
            if (v == null) {
                return metricValue;
            } else {
                return MathUtils.add(v, metricValue);
            }
        });
    }
}

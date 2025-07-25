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
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.agent.HostBaseInfo;
import org.opengauss.admin.common.utils.MathUtils;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.agent.repository.MetricRealTimeStorageService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * HostMonitorCacheService
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Slf4j
@Service
public class HostMonitorAgentService {
    // cacheMap  : hostId : item : value
    private final Map<String, HostBaseInfo> hostBasicMap = new ConcurrentHashMap<>();
    @Resource
    private IHostService hostService;
    @Resource
    private MetricRealTimeStorageService metricRealTimeStorageService;

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
                if (v == null) {
                    v = new HashMap<>();
                }
                cacheHostBasicInfo(k, v);
                if (agentMetricsMap.containsKey(hostId)) {
                    v = refreshHostMonitorCache(agentMetricsMap.get(hostId));
                }
                return v;
            });
        });
        return agentMetricsMap.keySet();
    }

    private void cacheHostBasicInfo(String hostId, Map<String, String> hostInfoMap) {
        HostBaseInfo hostBaseInfo = hostBasicMap.compute(hostId, (k, v) -> {
            if (v == null) {
                OpsHostEntity host = hostService.getById(hostId);
                return HostBaseInfo.builder()
                    .hostName(host.getHostname())
                    .agentId(host.getHostId())
                    .cpuArchitecture(host.getCpuArch())
                    .cpuModel(host.getCpuModel())
                    .cpuFreq(host.getCpuFreq())
                    .physicalCores(host.getPhysicalCores())
                    .logicalCores(host.getLogicalCores())
                    .osName(host.getOs())
                    .osVersion(host.getOsVersion())
                    .osBuild(host.getOsBuild())
                    .build();
            } else {
                return v;
            }
        });
        if (Objects.isNull(hostBaseInfo)) {
            return;
        }
        hostBasicMap.put(hostBaseInfo.getAgentId(), hostBaseInfo);
        updateHostMonitorCache(hostInfoMap, hostBaseInfo);
    }

    /**
     * update HostMonitorCacheService
     *
     * @param hostInfoMap host info map
     * @param hostBaseInfo hostBaseInfo
     */
    public void updateHostMonitorCache(Map<String, String> hostInfoMap, HostBaseInfo hostBaseInfo) {
        // 1. 使用辅助方法处理键值对的插入
        putIfValid(hostInfoMap, AgentConstants.HostMetric.CPU_ARCH, hostBaseInfo.getCpuArchitecture());
        putIfValid(hostInfoMap, AgentConstants.HostMetric.CPU_CORE_NUM, String.valueOf(hostBaseInfo.getLogicalCores()));
        // 2. 特殊处理 CPU_FREQUENCY
        if (hostBaseInfo.getCpuFreq() > 0) {
            String freqValue = hostBaseInfo.getCpuFreq() + "GHz";
            if (StrUtil.isNotEmpty(freqValue)) {
                hostInfoMap.put(AgentConstants.HostMetric.CPU_FREQUENCY, freqValue);
            }
        } else {
            hostInfoMap.put(AgentConstants.HostMetric.CPU_FREQUENCY, "Unknown Frequency Of lscpu cmd");
        }
        // 3. 统一处理其他字段
        putIfValid(hostInfoMap, AgentConstants.HostMetric.OS_NAME, hostBaseInfo.getOsName());
        putIfValid(hostInfoMap, AgentConstants.HostMetric.OS_VERSION, hostBaseInfo.getOsVersion());
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
        refreshHostDiskUsage(cache);
        return cache;
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

    private void putIfValid(Map<String, String> map, String key, String value) {
        if (StrUtil.isNotEmpty(value)) {
            map.put(key, value);
        }
    }

    /**
     * delete host cache
     *
     * @param hostId host id
     */
    public void deleteHostCache(String hostId) {
        hostBasicMap.remove(hostId);
    }
}

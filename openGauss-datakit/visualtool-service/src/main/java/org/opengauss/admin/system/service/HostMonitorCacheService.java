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

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.common.core.domain.model.agent.HostBaseInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

/**
 * HostMonitorCacheService
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Slf4j
@Service
public class HostMonitorCacheService {
    // cacheMap  : hostId : item : value
    private final Map<String, Map<String, String>> cacheMap = new ConcurrentHashMap<>();
    private volatile AtomicLong lastedFetch = new AtomicLong(0);
    @Resource
    private HostMonitorSshService hostMonitorSshService;
    @Resource
    private HostMonitorAgentService hostMonitorAgentService;
    @Resource
    private ScheduledExecutorService scheduledExecutorService;
    private final List<TaskMetricsDefinition> metricsDefinitionList = new LinkedList<>();

    /**
     * init host monitor cache environment
     *
     * @param metricsDefinitionList metricsDefinitionList
     */
    public void initHostMonitorCacheEnvironment(List<TaskMetricsDefinition> metricsDefinitionList) {
        hostMonitorSshService.initHostMonitorCacheEnvironment();
        if (ArrayUtil.isEmpty(metricsDefinitionList)) {
            return;
        }
        this.metricsDefinitionList.addAll(metricsDefinitionList);
    }

    /**
     * init host cache scheduled
     */
    public void startHostMonitorScheduled() {
        // host dynamic info,support seconds level data refresh
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            Thread.currentThread().setName("host-realtime-monitor");
            if (noUsing()) {
                return;
            }
            Set<String> agentSet = hostMonitorAgentService.startAgentHostMonitorScheduled(cacheMap);
            hostMonitorSshService.startHostMonitorScheduled(cacheMap, agentSet);
        }, 1, 5, TimeUnit.SECONDS);
    }

    private synchronized String getCacheValue(String hostId, String key) {
        lastedFetch.set(System.currentTimeMillis() / 1000);
        // cache does not have host info, try to get from agent
        if (!cacheMap.containsKey(hostId)) {
            refreshCache(hostId, hostMonitorAgentService.getAgentHostInfo(hostId));
        }
        // cache does not have host info, try to get from ssh
        if (!cacheMap.containsKey(hostId)) {
            refreshCache(hostId, hostMonitorSshService.getHostBasicInfo(hostId));
        }
        // cache does not have host info, return empty
        if (!cacheMap.containsKey(hostId)) {
            return "";
        }
        // cache has host info, try to get value
        Map<String, String> hostInfoMap = cacheMap.get(hostId);
        if (!hostInfoMap.containsKey(key)) {
            synchronized (hostInfoMap) {
                if (!hostInfoMap.containsKey(key)) {
                    refreshCache(hostId, key, hostMonitorSshService.getSshHostSingleInfo(hostId, key));
                    refreshCache(hostId, hostMonitorSshService.getSshHostInfo(hostId, key));
                }
            }
        }
        return cacheMap.get(hostId).getOrDefault(key, "");
    }

    private void refreshCache(String hostId, Map<String, String> hostInfoMap) {
        cacheMap.compute(hostId, (k, v) -> {
            if (v == null) {
                v = new HashMap<>();
            }
            v.putAll(hostInfoMap);
            return v;
        });
    }

    private void refreshCache(String hostId, String key, String value) {
        cacheMap.compute(hostId, (k, v) -> {
            if (v == null) {
                v = new HashMap<>();
            }
            if (StrUtil.isNotEmpty(value)) {
                v.put(key, value);
            }
            return v;
        });
    }

    private boolean noUsing() {
        long currentSecond = System.currentTimeMillis() / 1000;
        return (currentSecond - lastedFetch.get()) > 60;
    }

    /**
     * delete host cache
     *
     * @param hostId host id
     */
    public void deleteHostCache(String hostId) {
        cacheMap.remove(hostId);
        hostMonitorSshService.deleteHostCache(hostId);
        hostMonitorAgentService.deleteHostCache(hostId);
    }

    /**
     * get host os information
     *
     * @param hostId host id
     * @return host os information
     */
    public String getOs(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.OS_NAME);
    }

    private boolean isNotCache(String hostId) {
        return !cacheMap.containsKey(hostId);
    }

    /**
     * get host os version information
     *
     * @param hostId host id
     * @return host os version information
     */
    public String getOsVersion(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.OS_VERSION);
    }

    /**
     * get host cpu core number
     *
     * @param hostId host id
     * @return host cpu core number
     */
    public String getCpuCoreNum(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.CPU_CORE_NUM);
    }

    /**
     * get host cpu architecture information
     *
     * @param hostId host id
     * @return host cpu architecture information
     */
    public String getCpuArch(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.CPU_ARCH);
    }

    /**
     * get host cpu using
     *
     * @param hostId host id
     * @return host cpu using
     */
    public String getCpuUsing(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.CPU_USING);
    }

    /**
     * get host cpu frequency
     *
     * @param hostId host id
     * @return host cpu frequency
     */
    public String getCpuFrequency(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.CPU_FREQUENCY);
    }

    /**
     * get host network information tx and rx
     *
     * @param hostId host id
     * @param hasNetName if has net name
     * @return host network information tx and rx
     */
    public String[] getNetMonitor(String hostId, boolean hasNetName) {
        String[] res = hasNetName ? new String[3] : new String[2];
        Arrays.fill(res, "-1");
        String netMonitor = getCacheValue(hostId, AgentConstants.HostMetric.NET_MONITOR);
        String[] netRes = netMonitor.split(CacheConstants.SPL);
        if (netRes.length == 3) {
            return hasNetName ? netRes : new String[] {netRes[1], netRes[2]};
        } else {
            return res;
        }
    }

    /**
     * get host memory using percent
     *
     * @param hostId host id
     * @return host memory using
     */
    public String getMemoryUsing(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.SYSTEM_MEMORY_USAGE);
    }

    /**
     * get host memory remaining unit G
     *
     * @param hostId host id
     * @return host memory remaining
     */
    public String getRemainingMemory(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.SYSTEM_MEMORY_AVAILABLE);
    }

    /**
     * get host memory total unit G
     *
     * @param hostId host id
     * @return host memory total
     */
    public String getMemoryTotal(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.SYSTEM_MEMORY_TOTAL);
    }

    /**
     * get migration host info cpu core num ,remaining memory, available disk space
     *
     * @param hostId hostId
     * @return migration host info
     */
    public String getMigrationHostInfo(String hostId) {
        return getCacheValue(hostId, CacheConstants.MIGRATION_HOST).replaceAll("##", "\n");
    }

    /**
     * get host disk available unit GB
     *
     * @param hostId host id
     * @return host disk total
     */
    public String getAvailableDiskSpace(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.SYSTEM_DISK_FREE);
    }

    /**
     * get host disk use percent
     *
     * @param hostId host id
     * @return host disk percent
     */
    public String getDiskUse(String hostId) {
        return getCacheValue(hostId, AgentConstants.HostMetric.SYSTEM_DISK_USAGE);
    }

    /**
     * update host monitor cache
     *
     * @param hostBaseInfo hostBaseInfo
     */
    public void updateHostMonitorCache(HostBaseInfo hostBaseInfo) {
        String agentId = hostBaseInfo.getAgentId();
        cacheMap.compute(agentId, (k, v) -> {
            if (v == null) {
                v = new HashMap<>();
            }
            hostMonitorAgentService.updateHostMonitorCache(v, hostBaseInfo);
            return v;
        });
    }

    /**
     * host cache item
     */
    interface CacheConstants {
        /**
         * migration Host
         */
        String MIGRATION_HOST = "migrationHost";

        /**
         * host network tx | rx
         */
        String NET_MONITOR = "netMonitor";

        /**
         * empty string
         */
        String EMPTY = "";

        /**
         * spliter   |
         */
        String SPL = "\\|";

        /**
         * spliter  \n
         */
        String SPL_N = "\n";

        /**
         * host disk monitor
         */
        String DISK_MONITOR = "diskMonitor";
    }
}

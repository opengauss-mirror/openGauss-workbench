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

import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.entity.agent.MetricRealTime;
import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.agent.HostBaseInfo;
import org.opengauss.admin.common.core.vo.HostRealtimeStatistics;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.MathUtils;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.agent.repository.MetricRealTimeStorageService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
public class HostMonitorCacheService {
    // cacheMap  : hostId : item : value
    private final Map<String, Map<String, String>> cacheMap = new ConcurrentHashMap<>();
    private final Map<String, SshLogin> sshHostMap = new ConcurrentHashMap<>();
    private final Set<String> hostAgentSet = Collections.synchronizedSet(new HashSet<>());
    private volatile AtomicLong lastedFetch = new AtomicLong(0);
    private volatile AtomicInteger scheduledExecuteCounter = new AtomicInteger(0);
    @Resource
    private IHostService hostService;
    @Resource
    private IHostUserService hostUserService;
    @Resource
    private EncryptionUtils encryptionUtils;
    @Resource
    private JschExecutorService jschExecutorService;
    @Resource
    private MetricRealTimeStorageService metricRealTimeStorageService;
    @Resource
    private ScheduledExecutorService scheduledExecutorService;
    private final List<TaskMetricsDefinition> metricsDefinitionList = new LinkedList<>();

    /**
     * init host monitor cache environment
     *
     * @param metricsDefinitionList metricsDefinitionList
     */
    public void initHostMonitorCacheEnvironment(List<TaskMetricsDefinition> metricsDefinitionList) {
        lastedFetch.set(System.currentTimeMillis() / 1000);
        initHostSshInfoCache();
        if (ArrayUtil.isEmpty(metricsDefinitionList)) {
            return;
        }
        this.metricsDefinitionList.addAll(metricsDefinitionList);
    }

    private void initHostSshInfoCache() {
        hostService.list().forEach(host -> {
            sshHostMap.compute(host.getHostId(), (k, v) -> {
                if (v == null) {
                    OpsHostUserEntity user = getHostUserRootOrNormal(host.getHostId());
                    v = new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
                        encryptionUtils.decrypt(user.getPassword()));
                }
                return v;
            });
        });
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
            List<MetricRealTime> list = metricRealTimeStorageService.list();
            Map<String, List<MetricRealTime>> agentMetricsMap = list.stream()
                .collect(Collectors.groupingBy(metric -> String.valueOf(metric.getAgentId()), Collectors.toList()));
            // refresh hostAgentSet
            resetHostInstallAgent(agentMetricsMap.keySet());
            // refresh host monitor
            sshHostMap.forEach((hostId, sshLogin) -> {
                cacheMap.compute(hostId, (k, v) -> {
                    if (agentMetricsMap.containsKey(hostId)) {
                        v = refreshHostMonitorCache(agentMetricsMap.get(hostId));
                    } else {
                        v = executeHostMonitorBySshCommand(sshLogin);
                    }
                    return v;
                });
            });
            // schedule execute counter ,it is a circle counter,when it is 100,reset it to 0
            if (scheduledExecuteCounter.incrementAndGet() == 100) {
                scheduledExecuteCounter.set(0);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    private Map<String, String> executeHostMonitorBySshCommand(SshLogin sshLogin) {
        Map<String, String> cache = new HashMap<>();
        try {
            if (scheduledExecuteCounter.get() % 10 == 0) {
                cacheHostFixedInfo(cache, sshLogin);
            }
            cacheHostRealtimeInfo(cache, sshLogin);
        } catch (OpsException ex) {
            log.error("executeHostMonitorBySshCommand error,hostId:{},error:{}", sshLogin.getHost(), ex.getMessage());
        } catch (Exception ex) {
            log.error("execute unknown error,hostId:{},error:{}", sshLogin.getHost(), ex.getMessage());
        }
        return cache;
    }

    private Map<String, String> forceRefreshHostMonitorBySshCommand(SshLogin sshLogin) {
        Map<String, String> cache = new HashMap<>();
        cacheHostFixedInfo(cache, sshLogin);
        cacheHostRealtimeInfo(cache, sshLogin);
        return cache;
    }

    private void resetHostInstallAgent(Set<String> hostSet) {
        synchronized (hostAgentSet) {
            hostAgentSet.clear();
            hostAgentSet.addAll(hostSet);
        }
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

    /**
     * update HostMonitorCacheService
     *
     * @param hostBaseInfo hostBaseInfo
     */
    public void updateHostMonitorCache(HostBaseInfo hostBaseInfo) {
        Map<String, String> hostInfoMap = cacheMap.getOrDefault(hostBaseInfo.getAgentId(), new HashMap<>());
        hostInfoMap.put(AgentConstants.HostMetric.CPU_ARCH, hostBaseInfo.getCpuArchitecture());
        hostInfoMap.put(AgentConstants.HostMetric.CPU_CORE_NUM, String.valueOf(hostBaseInfo.getLogicalCores()));
        if (hostBaseInfo.getCpuFreq() > 0) {
            hostInfoMap.put(AgentConstants.HostMetric.CPU_FREQUENCY, hostBaseInfo.getCpuFreq() + "GHz");
        } else {
            hostInfoMap.put(AgentConstants.HostMetric.CPU_FREQUENCY, "Unknown Frequency Of lscpu cmd");
        }
        hostInfoMap.put(AgentConstants.HostMetric.OS_NAME, hostBaseInfo.getOsName());
        hostInfoMap.put(AgentConstants.HostMetric.OS_VERSION, hostBaseInfo.getOsVersion());
    }

    private synchronized String getCacheValue(String hostId, String key) {
        lastedFetch.set(System.currentTimeMillis() / 1000);
        Map<String, String> hostInfoMap = cacheMap.computeIfAbsent(hostId, k -> {
            SshLogin hostLogin = getOpsHostSsh(hostId);
            if (Objects.isNull(hostLogin)) {
                throw new OpsException("host not found,hostId:" + hostId);
            }
            return forceRefreshHostMonitorBySshCommand(hostLogin);
        });
        if (!hostInfoMap.containsKey(key)) {
            synchronized (hostInfoMap) {
                if (!hostInfoMap.containsKey(key)) {
                    SshLogin hostLogin = getOpsHostSsh(hostId);
                    Map<String, String> temp = new HashMap<>();
                    switch (key) {
                        case AgentConstants.HostMetric.SYSTEM_DISK_FREE, AgentConstants.HostMetric.SYSTEM_DISK_USED,
                            AgentConstants.HostMetric.SYSTEM_DISK_USAGE, AgentConstants.HostMetric.SYSTEM_DISK_TOTAL ->
                            disk(hostLogin, temp);
                        case AgentConstants.HostMetric.SYSTEM_MEMORY_AVAILABLE,
                            AgentConstants.HostMetric.SYSTEM_MEMORY_TOTAL,
                            AgentConstants.HostMetric.SYSTEM_MEMORY_USAGE ->
                            memory(hostLogin, temp);
                        case AgentConstants.HostMetric.NET_MONITOR -> netMonitor(hostLogin, temp);
                        case AgentConstants.HostMetric.MIGRATION_HOST -> hostForMigration(hostLogin, temp);
                    }
                    hostInfoMap.putAll(temp);
                }
            }
        }
        return hostInfoMap.getOrDefault(key, "");
    }

    private SshLogin getOpsHostSsh(String hostId) {
        return sshHostMap.compute(hostId, (k, v) -> {
            if (v == null) {
                OpsHostEntity host = hostService.getById(hostId);
                if (host == null) {
                    throw new OpsException("host not found,hostId:" + hostId);
                }
                OpsHostUserEntity user = getHostUserRootOrNormal(host.getHostId());
                if (user == null) {
                    throw new OpsException("host user not found or invalid,hostId:" + hostId);
                }
                v = new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
                    encryptionUtils.decrypt(user.getPassword()));
            }
            return v;
        });
    }

    private void cacheHostFixedInfo(Map<String, String> hostInfoMap, SshLogin sshLogin) {
        os(sshLogin, hostInfoMap);
        osVersion(sshLogin, hostInfoMap);
        cpu(sshLogin, hostInfoMap);
        disk(sshLogin, hostInfoMap);
    }

    private void cacheHostRealtimeInfo(Map<String, String> hostInfoMap, SshLogin sshLogin) {
        // cpu using
        cpuUsing(sshLogin, hostInfoMap);
        // memory using
        memory(sshLogin, hostInfoMap);
        // host base info for migration
        hostForMigration(sshLogin, hostInfoMap);
        // net monitor
        netMonitor(sshLogin, hostInfoMap);
    }

    private boolean noUsing() {
        long currentSecond = System.currentTimeMillis() / 1000;
        return (currentSecond - lastedFetch.get()) > 60;
    }

    private void cpuUsing(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String cpuUsing = jschExecutorService.getCpuUsing(sshLogin);
            if (StrUtil.isNotEmpty(cpuUsing)) {
                hostInfoMap.put(AgentConstants.HostMetric.CPU_USING, cpuUsing);
            }
        } catch (OpsException ex) {
            throw new OpsException("get cpu using " + ex.getMessage());
        }
    }

    private void netMonitor(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String[] netMonitor2 = jschExecutorService.getNetMonitor(sshLogin, true);
            if (ArrayUtil.isNotEmpty(netMonitor2)) {
                hostInfoMap.put(AgentConstants.HostMetric.NET_MONITOR, ArrayUtil.join(netMonitor2, "|"));
            }
        } catch (OpsException ex) {
            throw new OpsException("get net " + ex.getMessage());
        }
    }

    private void hostForMigration(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String baseInfo = jschExecutorService.getHostBaseInfo(sshLogin);
            if (StrUtil.isNotEmpty(baseInfo)) {
                hostInfoMap.put(AgentConstants.HostMetric.MIGRATION_HOST, baseInfo.replaceAll("\n", "##"));
            }
        } catch (OpsException ex) {
            throw new OpsException("get migration host info " + ex.getMessage());
        }
    }

    private void memory(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String memory = jschExecutorService.getMemory(sshLogin);
            if (StrUtil.isEmpty(memory)) {
                return;
            }
            HostRealtimeStatistics ofMemory = HostRealtimeStatistics.ofMemory(memory);
            hostInfoMap.put(AgentConstants.HostMetric.SYSTEM_MEMORY_TOTAL, ofMemory.getTotal() + "");
            hostInfoMap.put(AgentConstants.HostMetric.SYSTEM_MEMORY_AVAILABLE, ofMemory.getAvailable() + "");
            hostInfoMap.put(AgentConstants.HostMetric.SYSTEM_MEMORY_USAGE, ofMemory.getUse());
        } catch (OpsException ex) {
            throw new OpsException("get memory " + ex.getMessage());
        }
    }

    private void disk(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String res = jschExecutorService.getDiskMonitor(sshLogin);
            if (StrUtil.isNotEmpty(res)) {
                HostRealtimeStatistics ofDisk = HostRealtimeStatistics.ofDisk(res);
                hostInfoMap.put(CacheConstants.DISK_MONITOR, JSONObject.toJSONString(ofDisk));
                hostInfoMap.put(AgentConstants.HostMetric.SYSTEM_DISK_FREE, ofDisk.getAvailable() + "");
                hostInfoMap.put(AgentConstants.HostMetric.SYSTEM_DISK_USED, ofDisk.getUsed() + "");
                hostInfoMap.put(AgentConstants.HostMetric.SYSTEM_DISK_USAGE, ofDisk.getUse());
                hostInfoMap.put(AgentConstants.HostMetric.SYSTEM_DISK_TOTAL, ofDisk.getTotal() + "");
            }
        } catch (OpsException ex) {
            throw new OpsException("get disk " + ex.getMessage());
        }
    }

    private void cpu(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String ofCpu = jschExecutorService.getCpu(sshLogin);
            if (StrUtil.isEmpty(ofCpu)) {
                return;
            }
            String[] split = ofCpu.replaceAll(" ", "").split("\n");
            for (String line : split) {
                if (line.contains("Architecture")) {
                    String[] arch = line.split(":");
                    hostInfoMap.put(AgentConstants.HostMetric.CPU_ARCH, arch[1].trim());
                    continue;
                }
                if (line.contains("CPU(s):") && !line.contains("node")) {
                    String[] arch = line.split(":");
                    hostInfoMap.put(AgentConstants.HostMetric.CPU_CORE_NUM, arch[1].trim());
                    continue;
                }
                // CPUmaxMHz arm frequency ;   CPUMHz x86 frequency
                if (line.contains("CPUmaxMHz:") || line.contains("CPUMHz:")) {
                    String[] arch = line.split(":");
                    hostInfoMap.put(AgentConstants.HostMetric.CPU_FREQUENCY, toCpuGHz(arch[1].trim()));
                }
            }
            if ((!ofCpu.contains("CPU max MHz:")) && (!ofCpu.contains("CPU MHz:"))) {
                hostInfoMap.put(AgentConstants.HostMetric.CPU_FREQUENCY, "Unknown Frequency Of lscpu cmd");
            }
        } catch (OpsException ex) {
            throw new OpsException("get cpu " + ex.getMessage());
        }
    }

    private String toCpuGHz(String cpuMHz) {
        return String.format("%.2fGHz", Float.parseFloat(cpuMHz) / 1000);
    }

    private void osVersion(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String res = jschExecutorService.getOsVersion(sshLogin);
            if (StrUtil.isNotEmpty(res)) {
                hostInfoMap.put(AgentConstants.HostMetric.OS_VERSION, res);
            }
        } catch (OpsException ex) {
            throw new OpsException("get os version " + ex.getMessage());
        }
    }

    private void os(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String res = jschExecutorService.getOs(sshLogin);
            if (StrUtil.isNotEmpty(res)) {
                hostInfoMap.put(AgentConstants.HostMetric.OS_NAME, res);
            }
        } catch (OpsException ex) {
            throw new OpsException("get os " + ex.getMessage());
        }
    }

    /**
     * delete host cache
     *
     * @param hostId host id
     */
    public void deleteHostCache(String hostId) {
        cacheMap.remove(hostId);
        sshHostMap.remove(hostId);
        hostAgentSet.remove(hostId);
    }

    /**
     * get host user,when has root ,return root user,otherwise return normal user
     *
     * @param hostId host id
     * @return host user
     * @throws OpsException host user not found
     */
    private OpsHostUserEntity getHostUserRootOrNormal(String hostId) {
        List<OpsHostUserEntity> userList = hostUserService.listHostUserByHostId(hostId);
        OpsHostUserEntity hostUser = userList.stream()
            .filter(user -> StrUtil.isNotEmpty(user.getPassword()))
            .filter(user -> StrUtil.equalsIgnoreCase(user.getUsername(), "root"))
            .findFirst()
            .orElse(null);
        if (Objects.isNull(hostUser)) {
            hostUser = userList.stream()
                .filter(user -> StrUtil.isNotEmpty(user.getPassword()))
                .findAny()
                .orElseThrow(() -> new OpsException("host user does not exist"));
        }
        return hostUser;
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

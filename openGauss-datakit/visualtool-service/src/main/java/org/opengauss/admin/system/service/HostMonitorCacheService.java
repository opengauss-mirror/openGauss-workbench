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

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.vo.HostRealtimeStatistics;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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
    private final Map<String, OpsHostEntity> cacheHostMap = new ConcurrentHashMap<>();
    private volatile AtomicLong lastedFetch = new AtomicLong(0);
    @Resource
    private IHostService hostService;
    @Resource
    private IHostUserService hostUserService;
    @Resource
    private EncryptionUtils encryptionUtils;
    @Resource
    private JschExecutorService jschExecutorService;
    @Resource
    private ScheduledThreadPoolExecutor scheduledExecutorService;

    /**
     * init host cache scheduled
     */
    public void initHostMonitorCacheService() {
        lastedFetch.set(System.currentTimeMillis() / 1000);
        List<OpsHostEntity> list = hostService.list();
        for (OpsHostEntity host : list) {
            cacheHostMap.put(host.getHostId(), host);
        }
        initCache();
        // host static info, support refresh data per minute
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            Thread.currentThread().setName("host-monitor");
            if (noUsing()) {
                return;
            }
            cacheHostMap.values().forEach(host -> {
                try {
                    OpsHostUserEntity user = getHostUserRootOrNormal(host.getHostId());
                    SshLogin sshLogin = new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
                        encryptionUtils.decrypt(user.getPassword()));
                    cacheHostFixedInfo(host.getHostId(), sshLogin);
                } catch (OpsException ex) {
                    log.warn("monitor host cache {} error : ", host.getPublicIp(), ex);
                }
            });
        }, 0, 10 * 60, TimeUnit.SECONDS);
        // host dynamic info,support seconds level data refresh
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            Thread.currentThread().setName("host-realtime-monitor");
            if (noUsing()) {
                return;
            }
            cacheHostMap.values().forEach(host -> {
                try {
                    OpsHostUserEntity user = getHostUserRootOrNormal(host.getHostId());
                    SshLogin sshLogin = new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
                        encryptionUtils.decrypt(user.getPassword()));
                    cacheHostRealtimeInfo(host.getHostId(), sshLogin);
                } catch (OpsException ex) {
                    log.warn("monitor host cache {} error : {}", host.getPublicIp(), ex.getMessage());
                }
            });
        }, 1, 30, TimeUnit.SECONDS);
    }

    private void initCache() {
        cacheHostMap.values().forEach(host -> {
            try {
                OpsHostUserEntity user = getHostUserRootOrNormal(host.getHostId());
                SshLogin sshLogin = new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
                    encryptionUtils.decrypt(user.getPassword()));
                cacheHostFixedInfo(host.getHostId(), sshLogin);
                cacheHostRealtimeInfo(host.getHostId(), sshLogin);
            } catch (OpsException ex) {
                log.warn("monitor host cache {} error : ", host.getPublicIp(), ex);
            }
        });
    }

    private synchronized String getCacheValue(String hostId, String key) {
        lastedFetch.set(System.currentTimeMillis() / 1000);
        OpsHostEntity host = getOpsHost(hostId);
        if (Objects.isNull(host)) {
            throw new OpsException("host not found,hostId:" + hostId);
        }
        if (isNotCache(hostId)) {
            OpsHostUserEntity user = getHostUserRootOrNormal(host.getHostId());
            SshLogin sshLogin = new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
                encryptionUtils.decrypt(user.getPassword()));
            cacheHostFixedInfo(hostId, sshLogin);
            cacheHostRealtimeInfo(hostId, sshLogin);
        }
        Map<String, String> hostCache = cacheMap.getOrDefault(hostId, new HashMap<>());
        if (!hostCache.containsKey(key)) {
            cachingHostInfoByKey(host, key);
        }
        return hostCache.getOrDefault(key, CacheConstants.EMPTY);
    }

    private void cachingHostInfoByKey(OpsHostEntity host, String key) {
        OpsHostUserEntity user = getHostUserRootOrNormal(host.getHostId());
        SshLogin sshLogin = new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
            encryptionUtils.decrypt(user.getPassword()));
        Map<String, String> hostInfoMap = cacheMap.getOrDefault(host.getHostId(), new HashMap<>());
        switch (key) {
            case CacheConstants.DISK_MONITOR:
                disk(sshLogin, hostInfoMap);
                break;
            case CacheConstants.CPU_ARCH:
            case CacheConstants.CPU_FREQUENCY:
            case CacheConstants.CPU_CORE_NUM:
                cpu(sshLogin, hostInfoMap);
                break;
            case CacheConstants.CPU_USING:
                cpuUsing(sshLogin, hostInfoMap);
                break;
            case CacheConstants.OS:
                os(sshLogin, hostInfoMap);
                break;
            case CacheConstants.OS_VERSION:
                osVersion(sshLogin, hostInfoMap);
                break;
            case CacheConstants.MEMORY:
                memory(sshLogin, hostInfoMap);
                break;
            case CacheConstants.NET_MONITOR:
                netMonitor(sshLogin, hostInfoMap);
                break;
            default:
                ;
        }
    }

    private OpsHostEntity getOpsHost(String hostId) {
        OpsHostEntity host;
        if (!cacheHostMap.containsKey(hostId)) {
            host = hostService.getById(hostId);
            if (Objects.nonNull(host)) {
                cacheHostMap.put(hostId, host);
            }
        }
        return cacheHostMap.get(hostId);
    }

    private void cacheHostFixedInfo(String hostId, SshLogin sshLogin) {
        Map<String, String> hostInfoMap = cacheMap.getOrDefault(hostId, new HashMap<>());
        os(sshLogin, hostInfoMap);
        osVersion(sshLogin, hostInfoMap);
        cpu(sshLogin, hostInfoMap);
        disk(sshLogin, hostInfoMap);
        log.info("monitor host cache {} success {}", sshLogin.getHost(), hostInfoMap);
        cacheMap.put(hostId, hostInfoMap);
    }

    private void cacheHostRealtimeInfo(String hostId, SshLogin sshLogin) {
        Map<String, String> hostInfoMap = cacheMap.getOrDefault(hostId, new HashMap<>());
        // cpu using
        cpuUsing(sshLogin, hostInfoMap);
        // memory using
        memory(sshLogin, hostInfoMap);
        // host base info for migration
        hostForMigration(sshLogin, hostInfoMap);
        // net monitor
        netMonitor(sshLogin, hostInfoMap);
        log.info("monitor host cache {} success {}", sshLogin.getHost(), hostInfoMap);
        cacheMap.put(hostId, hostInfoMap);
    }

    private boolean noUsing() {
        long currentSecond = System.currentTimeMillis() / 1000;
        return (currentSecond - lastedFetch.get()) > 60;
    }

    private void cpuUsing(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        String cpuUsing = jschExecutorService.getCpuUsing(sshLogin);
        if (StrUtil.isNotEmpty(cpuUsing)) {
            hostInfoMap.put(CacheConstants.CPU_USING, cpuUsing);
        }
    }

    private void netMonitor(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        String[] netMonitor2 = jschExecutorService.getNetMonitor(sshLogin, true);
        if (ArrayUtil.isNotEmpty(netMonitor2)) {
            hostInfoMap.put(CacheConstants.NET_MONITOR, ArrayUtil.join(netMonitor2, "|"));
        }
    }

    private void hostForMigration(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        String baseInfo = jschExecutorService.getHostBaseInfo(sshLogin);
        if (StrUtil.isNotEmpty(baseInfo)) {
            hostInfoMap.put(CacheConstants.MIGRATION_HOST, baseInfo.replaceAll("\n", "##"));
        }
    }

    private void memory(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        String memory = jschExecutorService.getMemory(sshLogin);
        if (StrUtil.isEmpty(memory)) {
            return;
        }
        HostRealtimeStatistics ofMemory = HostRealtimeStatistics.ofMemory(memory);
        hostInfoMap.put(CacheConstants.MEMORY, JSONObject.toJSONString(ofMemory));
    }

    private void disk(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String res = jschExecutorService.getDiskMonitor(sshLogin);
            if (StrUtil.isNotEmpty(res)) {
                HostRealtimeStatistics ofDisk = HostRealtimeStatistics.ofDisk(res);
                hostInfoMap.put(CacheConstants.DISK_MONITOR, JSONObject.toJSONString(ofDisk));
            }
        } catch (OpsException ex) {
            log.error("error occurred while getting disk monitor {}", ex.getMessage());
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
                    hostInfoMap.put(CacheConstants.CPU_ARCH, arch[1].trim());
                    continue;
                }
                if (line.contains("CPU(s):") && !line.contains("node")) {
                    String[] arch = line.split(":");
                    hostInfoMap.put(CacheConstants.CPU_CORE_NUM, arch[1].trim());
                    continue;
                }
                // CPUmaxMHz arm frequency ;   CPUMHz x86 frequency
                if (line.contains("CPUmaxMHz:") || line.contains("CPUMHz:")) {
                    String[] arch = line.split(":");
                    hostInfoMap.put(CacheConstants.CPU_FREQUENCY, toCpuGHz(arch[1].trim()));
                }
            }
            if ((!ofCpu.contains("CPU max MHz:")) && (!ofCpu.contains("CPU MHz:"))) {
                hostInfoMap.put(CacheConstants.CPU_FREQUENCY, "Unknown Frequency Of lscpu cmd");
            }
        } catch (OpsException ex) {
            log.error("error occurred while getting cpu monitor {}", ex.getMessage());
        }
    }

    private String toCpuGHz(String cpuMHz) {
        return String.format("%.2fGHz", Float.parseFloat(cpuMHz) / 1000);
    }

    private void osVersion(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String res = jschExecutorService.getOsVersion(sshLogin);
            if (StrUtil.isNotEmpty(res)) {
                hostInfoMap.put(CacheConstants.OS_VERSION, res);
            }
        } catch (OpsException ex) {
            log.error("error occurred while get os version {}", ex.getMessage());
        }
    }

    private void os(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String res = jschExecutorService.getOs(sshLogin);
            if (StrUtil.isNotEmpty(res)) {
                hostInfoMap.put(CacheConstants.OS, res);
            }
        } catch (OpsException ex) {
            log.error("error occurred while get os {}", ex.getMessage());
        }
    }

    /**
     * delete host cache
     *
     * @param hostId host id
     */
    public void deleteHostCache(String hostId) {
        cacheMap.remove(hostId);
        cacheHostMap.remove(hostId);
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
        return getCacheValue(hostId, CacheConstants.OS);
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
        return getCacheValue(hostId, CacheConstants.OS_VERSION);
    }

    /**
     * get host cpu core number
     *
     * @param hostId host id
     * @return host cpu core number
     */
    public String getCpuCoreNum(String hostId) {
        return getCacheValue(hostId, CacheConstants.CPU_CORE_NUM);
    }

    /**
     * get host cpu architecture information
     *
     * @param hostId host id
     * @return host cpu architecture information
     */
    public String getCpuArch(String hostId) {
        return getCacheValue(hostId, CacheConstants.CPU_ARCH);
    }

    /**
     * get host cpu using
     *
     * @param hostId host id
     * @return host cpu using
     */
    public String getCpuUsing(String hostId) {
        return getCacheValue(hostId, CacheConstants.CPU_USING);
    }

    /**
     * get host cpu frequency
     *
     * @param hostId host id
     * @return host cpu frequency
     */
    public String getCpuFrequency(String hostId) {
        return getCacheValue(hostId, CacheConstants.CPU_FREQUENCY);
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
        String netMonitor = getCacheValue(hostId, CacheConstants.NET_MONITOR);
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
        String ofMemory = getCacheValue(hostId, CacheConstants.MEMORY);
        if (StrUtil.isEmpty(ofMemory)) {
            return "";
        }
        HostRealtimeStatistics memory = JSONObject.parseObject(ofMemory, HostRealtimeStatistics.class);
        return memory.getUse();
    }

    /**
     * get host memory remaining unit G
     *
     * @param hostId host id
     * @return host memory remaining
     */
    public String getRemainingMemory(String hostId) {
        String ofMemory = getCacheValue(hostId, CacheConstants.MEMORY);
        if (StrUtil.isEmpty(ofMemory)) {
            return "";
        }
        HostRealtimeStatistics memory = JSONObject.parseObject(ofMemory, HostRealtimeStatistics.class);
        return HostRealtimeStatistics.toString(memory.getAvailable());
    }

    /**
     * get host memory total unit G
     *
     * @param hostId host id
     * @return host memory total
     */
    public String getMemoryTotal(String hostId) {
        String ofMemory = getCacheValue(hostId, CacheConstants.MEMORY);
        if (StrUtil.isEmpty(ofMemory)) {
            return "";
        }
        HostRealtimeStatistics memory = JSONObject.parseObject(ofMemory, HostRealtimeStatistics.class);
        return HostRealtimeStatistics.toString(memory.getTotal());
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
        String diskMonitor = getCacheValue(hostId, CacheConstants.DISK_MONITOR);
        if (StrUtil.isEmpty(diskMonitor)) {
            return "";
        }
        HostRealtimeStatistics disk = JSONObject.parseObject(diskMonitor, HostRealtimeStatistics.class);
        return HostRealtimeStatistics.toString(disk.getAvailable());
    }

    /**
     * get host disk total unit GB
     *
     * @param hostId host id
     * @return host disk total
     */
    public String getDiskTotalSpace(String hostId) {
        String diskMonitor = getCacheValue(hostId, CacheConstants.DISK_MONITOR);
        if (StrUtil.isEmpty(diskMonitor)) {
            return "";
        }
        HostRealtimeStatistics disk = JSONObject.parseObject(diskMonitor, HostRealtimeStatistics.class);
        return HostRealtimeStatistics.toString(disk.getTotal());
    }

    /**
     * get host disk used unit GB
     *
     * @param hostId host id
     * @return host disk used
     */
    public String getDiskUsedSpace(String hostId) {
        String diskMonitor = getCacheValue(hostId, CacheConstants.DISK_MONITOR);
        if (StrUtil.isEmpty(diskMonitor)) {
            return "";
        }
        HostRealtimeStatistics disk = JSONObject.parseObject(diskMonitor, HostRealtimeStatistics.class);
        return HostRealtimeStatistics.toString(disk.getUsed());
    }

    /**
     * get host disk use percent
     *
     * @param hostId host id
     * @return host disk percent
     */
    public String getDiskUse(String hostId) {
        String diskMonitor = getCacheValue(hostId, CacheConstants.DISK_MONITOR);
        HostRealtimeStatistics disk = JSONObject.parseObject(diskMonitor, HostRealtimeStatistics.class);
        return disk.getUse();
    }

    /**
     * host cache item
     */
    interface CacheConstants {
        /**
         * host os
         */
        String OS = "os";

        /**
         * host os version
         */
        String OS_VERSION = "osVersion";

        /**
         * host cpu arch
         */
        String CPU_ARCH = "cpuArch";

        /**
         * host cpu core num
         */
        String CPU_CORE_NUM = "cpuCoreNum";

        /**
         * host cpu using
         */
        String CPU_USING = "cpuUsing";

        /**
         * host momory
         */
        String MEMORY = "memory";

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

        /**
         * cpu frequency
         */
        String CPU_FREQUENCY = "cpuFrequency";
    }
}

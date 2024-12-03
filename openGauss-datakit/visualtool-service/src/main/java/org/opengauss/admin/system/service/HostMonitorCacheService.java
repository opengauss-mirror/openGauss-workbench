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

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
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

import javax.annotation.PostConstruct;
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
    @PostConstruct
    private void init() {
        // host static info, support refresh data per minute
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            List<OpsHostEntity> list = hostService.list();
            list.forEach(host -> {
                try {
                    OpsHostUserEntity user = getHostUserRootOrNormal(host.getHostId());
                    SshLogin sshLogin = new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
                        encryptionUtils.decrypt(user.getPassword()));
                    String os = jschExecutorService.getOs(sshLogin);
                    String osVersion = jschExecutorService.getOsVersion(sshLogin);
                    String cpuArch = jschExecutorService.getCpuArch(sshLogin);
                    String cpuCoreNum = jschExecutorService.getCpuCoreNum(sshLogin);
                    String memoryTotal = jschExecutorService.getMemoryTotal(sshLogin);
                    Map<String, String> hostInfoMap = cacheMap.getOrDefault(host.getHostId(), new HashMap<>());
                    hostInfoMap.put(CacheConstants.OS, os);
                    hostInfoMap.put(CacheConstants.OS_VERSION, osVersion);
                    hostInfoMap.put(CacheConstants.CPU_ARCH, cpuArch);
                    hostInfoMap.put(CacheConstants.CPU_CORE_NUM, cpuCoreNum);
                    hostInfoMap.put(CacheConstants.MEMORY_TOTAL, memoryTotal);
                    cacheMap.put(host.getHostId(), hostInfoMap);
                } catch (Exception ex) {
                    log.warn("monitor host cache {} error : {}", host.getPublicIp(), ex.getMessage());
                }
            });
        }, 5, 5 * 60, TimeUnit.SECONDS);
        // host dynamic info,support seconds level data refresh
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            List<OpsHostEntity> list = hostService.list();
            list.forEach(host -> {
                try {
                    OpsHostUserEntity user = getHostUserRootOrNormal(host.getHostId());
                    SshLogin sshLogin = new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
                        encryptionUtils.decrypt(user.getPassword()));
                    Map<String, String> hostInfoMap = cacheMap.getOrDefault(host.getHostId(), new HashMap<>());
                    // cpu using
                    String cpuUsing = jschExecutorService.getCpuUsing(sshLogin);
                    hostInfoMap.put(CacheConstants.CPU_USING, cpuUsing);
                    // memory using
                    String memoryUsing = jschExecutorService.getMemoryUsing(sshLogin);
                    hostInfoMap.put(CacheConstants.MEMORY_USING, memoryUsing);
                    // disk using
                    String diskMonitor = jschExecutorService.diskMonitor(sshLogin);
                    hostInfoMap.put(CacheConstants.DISK_MONITOR, diskMonitor);
                    // available disk space and memory remaining
                    String baseInfo = jschExecutorService.getHostBaseInfo(sshLogin);
                    String[] split = baseInfo.split(CacheConstants.SPL_N);
                    hostInfoMap.put(CacheConstants.MEMORY_REMAINING, split[1]);
                    hostInfoMap.put(CacheConstants.AVAILABLE_DISK_SPACE, split[2]);
                    // net monitor
                    String[] netMonitor2 = jschExecutorService.getNetMonitor(sshLogin, true);
                    hostInfoMap.put(CacheConstants.NET_MONITOR, ArrayUtil.join(netMonitor2, "|"));
                    cacheMap.put(host.getHostId(), hostInfoMap);
                } catch (Exception ex) {
                    log.warn("monitor host cache {} error : {}", host.getPublicIp(), ex.getMessage());
                }
            });
        }, 5, 6, TimeUnit.SECONDS);
    }

    /**
     * delete host cache
     *
     * @param hostId host id
     */
    public void deleteHostCache(String hostId) {
        cacheMap.remove(hostId);
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
     * get host cpu architecture information
     *
     * @param hostId host id
     * @return host cpu architecture information
     */
    public String getCpuArch(String hostId) {
        return getCacheValue(hostId, CacheConstants.CPU_ARCH);
    }

    private String getCacheValue(String hostId, String key) {
        if (isNotCache(hostId)) {
            return CacheConstants.EMPTY;
        }
        Map<String, String> hostCache = cacheMap.get(hostId);
        return hostCache.getOrDefault(key, CacheConstants.EMPTY);
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
     * get host cpu using
     *
     * @param hostId host id
     * @return host cpu using
     */
    public String getCpuUsing(String hostId) {
        return getCacheValue(hostId, CacheConstants.CPU_USING);
    }

    /**
     * get host memory using
     *
     * @param hostId host id
     * @return host memory using
     */
    public String getMemoryUsing(String hostId) {
        return getCacheValue(hostId, CacheConstants.MEMORY_USING);
    }

    /**
     * get host memory remaining unit MB
     *
     * @param hostId host id
     * @return host memory remaining
     */
    public String getRemainingMemory(String hostId) {
        return getCacheValue(hostId, CacheConstants.MEMORY_REMAINING);
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
     * get host memory total unit MB
     *
     * @param hostId host id
     * @return host memory total
     */
    public String getMemoryTotal(String hostId) {
        return getCacheValue(hostId, CacheConstants.MEMORY_TOTAL);
    }

    /**
     * get host disk total unit GB
     *
     * @param hostId host id
     * @return host disk total
     */
    public String getAvailableDiskSpace(String hostId) {
        return getCacheValue(hostId, CacheConstants.AVAILABLE_DISK_SPACE);
    }

    /**
     * get host disk using
     *
     * @param hostId host id
     * @return host disk using
     */
    public String diskMonitor(String hostId) {
        return getCacheValue(hostId, CacheConstants.DISK_MONITOR);
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
         * host memory total
         */
        String MEMORY_TOTAL = "memoryTotal";

        /**
         * host cpu using
         */
        String CPU_USING = "cpuUsing";

        /**
         * host memory using
         */
        String MEMORY_USING = "memoryUsing";

        /**
         * host memory remaining MB
         */
        String MEMORY_REMAINING = "memoryRemaining";

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
         * host disk available space GB
         */
        String AVAILABLE_DISK_SPACE = "AvailableDiskSpace";

        /**
         * host disk using
         */
        String DISK_MONITOR = "diskMonitor";
    }
}

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

import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.CPU_ARCH;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.CPU_CORE_NUM;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.CPU_FREQUENCY;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.MIGRATION_HOST;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.OS_NAME;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.OS_VERSION;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.SYSTEM_DISK_FREE;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.SYSTEM_DISK_TOTAL;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.SYSTEM_DISK_USAGE;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.SYSTEM_DISK_USED;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.SYSTEM_MEMORY_AVAILABLE;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.SYSTEM_MEMORY_TOTAL;
import static org.opengauss.admin.common.constant.AgentConstants.HostMetric.SYSTEM_MEMORY_USAGE;
import static org.opengauss.admin.system.service.HostMonitorSshService.CacheConstants.DISK_MONITOR;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.agent.HostBaseInfo;
import org.opengauss.admin.common.core.vo.HostRealtimeStatistics;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

/**
 * HostMonitorCacheService
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Slf4j
@Service
public class HostMonitorSshService {
    // cacheMap  : hostId : item : value
    private final Map<String, SshLogin> sshHostMap = new ConcurrentHashMap<>();
    private final Map<String, HostBaseInfo> hostBasicMap = new ConcurrentHashMap<>();
    @Resource
    private IHostService hostService;
    @Resource
    private IHostUserService hostUserService;
    @Resource
    private EncryptionUtils encryptionUtils;
    @Resource
    private JschExecutorService jschExecutorService;

    /**
     * init host monitor cache environment
     */
    public void initHostMonitorCacheEnvironment() {
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
     * start host monitor scheduled
     *
     * @param cacheMap host cache map
     * @param agentSet agent set
     */
    public void startHostMonitorScheduled(Map<String, Map<String, String>> cacheMap, Set<String> agentSet) {
        for (Map.Entry<String, Map<String, String>> cacheEntry : cacheMap.entrySet()) {
            if (agentSet.contains(cacheEntry.getKey())) {
                continue;
            }
            SshLogin sshLogin = getOpsHostSsh(cacheEntry.getKey());
            Map<String, String> value = cacheEntry.getValue();
            cacheHostBasicInfo(cacheEntry.getKey(), value);
            value.putAll(executeHostMonitorBySshCommand(sshLogin));
            cacheMap.put(cacheEntry.getKey(), value);
        }
    }

    private Map<String, String> executeHostMonitorBySshCommand(SshLogin sshLogin) {
        Map<String, String> cache = new HashMap<>();
        try {
            cacheHostFixedInfo(cache, sshLogin);
            cacheHostRealtimeInfo(cache, sshLogin);
        } catch (OpsException ex) {
            log.error("executeHostMonitorBySshCommand error,hostId:{},error:{}", sshLogin.getHost(), ex.getMessage());
        } catch (Exception ex) {
            log.error("execute unknown error,hostId:{},error:{}", sshLogin.getHost(), ex.getMessage());
        }
        return cache;
    }

    /**
     * update HostMonitorCacheService
     *
     * @param hostInfoMap host info map
     * @param hostBaseInfo hostBaseInfo
     */
    private void updateHostMonitorCache(Map<String, String> hostInfoMap, HostBaseInfo hostBaseInfo) {
        // 1. 使用辅助方法处理键值对的插入
        putIfValid(hostInfoMap, CPU_ARCH, hostBaseInfo.getCpuArchitecture());
        if (hostBaseInfo.getLogicalCores() > 0) {
            putIfValid(hostInfoMap, CPU_CORE_NUM, String.valueOf(hostBaseInfo.getLogicalCores()));
        }
        // 2. 特殊处理 CPU_FREQUENCY
        if (hostBaseInfo.getCpuFreq() > 0) {
            String freqValue = hostBaseInfo.getCpuFreq() + "GHz";
            hostInfoMap.put(CPU_FREQUENCY, freqValue);
            putIfValid(hostInfoMap, CPU_FREQUENCY, freqValue);
        }
        // 3. 统一处理其他字段
        putIfValid(hostInfoMap, OS_NAME, hostBaseInfo.getOsName());
        putIfValid(hostInfoMap, OS_VERSION, hostBaseInfo.getOsVersion());
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

    /**
     * get host basic info
     *
     * @param hostId host id
     * @return host basic info
     */
    public Map<String, String> getHostBasicInfo(String hostId) {
        Map<String, String> hostInfoMap = new HashMap<>();
        cacheHostBasicInfo(hostId, hostInfoMap);
        return hostInfoMap;
    }

    /**
     * cache host os name, os version, migration info
     *
     * @param hostId host id
     * @param key key
     * @return value
     */
    public String getSshHostSingleInfo(String hostId, String key) {
        Map<String, String> hostInfoMap = new HashMap<>();
        cacheHostBasicInfo(hostId, hostInfoMap);
        SshLogin sshLogin = getOpsHostSsh(hostId);
        switch (key) {
            case OS_NAME -> os(sshLogin, hostInfoMap);
            case OS_VERSION -> osVersion(sshLogin, hostInfoMap);
            case MIGRATION_HOST -> hostForMigration(sshLogin, hostInfoMap);
            default -> ignoreEmpty("getSshHostSingleInfo not found " + sshLogin.toString() + " key " + key);
        }
        return hostInfoMap.getOrDefault(key, "");
    }

    private void ignoreEmpty(String msg) {
        log.debug("fetch host info {} ignore empty", msg);
    }

    /**
     * get host info , contains cpu, memory, disk
     *
     * @param hostId host id
     * @param key key
     * @return host info
     */
    public Map<String, String> getSshHostInfo(String hostId, String key) {
        Map<String, String> hostInfoMap = new HashMap<>();
        cacheHostBasicInfo(hostId, hostInfoMap);
        SshLogin sshLogin = getOpsHostSsh(hostId);
        switch (key) {
            case CPU_ARCH, CPU_CORE_NUM, CPU_FREQUENCY -> cpu(sshLogin, hostInfoMap);
            case DISK_MONITOR, SYSTEM_DISK_TOTAL, SYSTEM_DISK_USED, SYSTEM_DISK_FREE, SYSTEM_DISK_USAGE ->
                disk(sshLogin, hostInfoMap);
            case SYSTEM_MEMORY_AVAILABLE, SYSTEM_MEMORY_TOTAL, SYSTEM_MEMORY_USAGE -> memory(sshLogin, hostInfoMap);
            default -> ignoreEmpty("getSshHostInfo not found " + sshLogin.toString() + " key " + key);
        }
        return hostInfoMap;
    }

    private void cpuUsing(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            // 获取CPU使用率
            String cpuUsing = jschExecutorService.getCpuUsing(sshLogin);
            // 如果CPU使用率不为空，则将其放入hostInfoMap中
            if (StrUtil.isNotEmpty(cpuUsing)) {
                hostInfoMap.put(AgentConstants.HostMetric.CPU_USING, cpuUsing);
            }
        } catch (OpsException ex) {
            // 如果发生OpsException异常，则抛出新的OpsException异常，并附带异常信息
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
                hostInfoMap.put(MIGRATION_HOST, baseInfo.replaceAll("\n", "##"));
            }
        } catch (OpsException ex) {
            throw new OpsException("get migration host info " + ex.getMessage());
        }
    }

    /**
     * get memory info : SYSTEM_MEMORY_AVAILABLE, SYSTEM_MEMORY_TOTAL, SYSTEM_MEMORY_USAGE
     *
     * @param sshLogin ssh login
     * @param hostInfoMap host info map
     */
    private void memory(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String memory = jschExecutorService.getMemory(sshLogin);
            if (StrUtil.isEmpty(memory)) {
                return;
            }
            HostRealtimeStatistics ofMemory = HostRealtimeStatistics.ofMemory(memory);
            hostInfoMap.put(SYSTEM_MEMORY_TOTAL, ofMemory.getTotal() + "");
            hostInfoMap.put(SYSTEM_MEMORY_AVAILABLE, ofMemory.getAvailable() + "");
            hostInfoMap.put(SYSTEM_MEMORY_USAGE, ofMemory.getUse());
        } catch (OpsException ex) {
            throw new OpsException("get memory " + ex.getMessage());
        }
    }

    /**
     * get disk info : DISK_MONITOR,SYSTEM_DISK_TOTAL, SYSTEM_DISK_USED, SYSTEM_DISK_FREE, SYSTEM_DISK_USAGE
     *
     * @param sshLogin ssh login
     * @param hostInfoMap host info map
     */
    private void disk(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        try {
            String res = jschExecutorService.getDiskMonitor(sshLogin);
            if (StrUtil.isNotEmpty(res)) {
                HostRealtimeStatistics ofDisk = HostRealtimeStatistics.ofDisk(res);
                hostInfoMap.put(DISK_MONITOR, JSONObject.toJSONString(ofDisk));
                hostInfoMap.put(SYSTEM_DISK_FREE, ofDisk.getAvailable() + "");
                hostInfoMap.put(SYSTEM_DISK_USED, ofDisk.getUsed() + "");
                hostInfoMap.put(SYSTEM_DISK_USAGE, ofDisk.getUse());
                hostInfoMap.put(SYSTEM_DISK_TOTAL, ofDisk.getTotal() + "");
            }
        } catch (OpsException ex) {
            throw new OpsException("get disk " + ex.getMessage());
        }
    }

    /**
     * get cpu info : CPU_ARCH, CPU_CORE_NUM, CPU_FREQUENCY
     *
     * @param sshLogin ssh login
     * @param hostInfoMap host info map
     */
    private void cpu(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        if (hostInfoMap.containsKey(CPU_ARCH) && hostInfoMap.containsKey(CPU_CORE_NUM) && hostInfoMap.containsKey(
            CPU_FREQUENCY)) {
            return;
        }
        try {
            String ofCpu = jschExecutorService.getCpu(sshLogin);
            if (StrUtil.isEmpty(ofCpu)) {
                return;
            }
            String[] split = ofCpu.replaceAll(" ", "").split("\n");
            for (String line : split) {
                if (line.contains("Architecture")) {
                    String[] arch = line.split(":");
                    hostInfoMap.put(CPU_ARCH, arch[1].trim());
                    continue;
                }
                if (line.contains("CPU(s):") && !line.contains("node")) {
                    String[] arch = line.split(":");
                    hostInfoMap.put(CPU_CORE_NUM, arch[1].trim());
                    continue;
                }
                // CPUmaxMHz arm frequency ;   CPUMHz x86 frequency
                if (line.contains("CPUmaxMHz:") || line.contains("CPUMHz:")) {
                    String[] arch = line.split(":");
                    hostInfoMap.put(CPU_FREQUENCY, toCpuGHz(arch[1].trim()));
                }
            }
            hostService.updateHostCpu(sshLogin.getHost(), hostInfoMap.get(CPU_ARCH), hostInfoMap.get(CPU_CORE_NUM),
                hostInfoMap.get(CPU_FREQUENCY));
        } catch (OpsException ex) {
            throw new OpsException("get cpu " + ex.getMessage());
        }
    }

    private String toCpuGHz(String cpuMHz) {
        return String.format("%.2fGHz", Float.parseFloat(cpuMHz) / 1000);
    }

    private void osVersion(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        if (hostInfoMap.containsKey(OS_VERSION)) {
            return;
        }
        try {
            String res = jschExecutorService.getOsVersion(sshLogin);
            if (StrUtil.isNotEmpty(res)) {
                hostInfoMap.put(OS_VERSION, res);
            }
            hostService.updateHostOsVersion(sshLogin.getHost(), res);
        } catch (OpsException ex) {
            throw new OpsException("get os version " + ex.getMessage());
        }
    }

    private void os(SshLogin sshLogin, Map<String, String> hostInfoMap) {
        if (hostInfoMap.containsKey(OS_NAME)) {
            return;
        }
        try {
            String res = jschExecutorService.getOs(sshLogin);
            if (StrUtil.isNotEmpty(res)) {
                hostInfoMap.put(OS_NAME, res);
            }
            hostService.updateHostOsName(sshLogin.getHost(), res);
        } catch (OpsException ex) {
            throw new OpsException("get os " + ex.getMessage());
        }
    }

    /**
     * cache host basic info
     *
     * @param hostId host id
     * @param hostInfoMap host info map
     */
    public void cacheHostBasicInfo(String hostId, Map<String, String> hostInfoMap) {
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
        hostBasicMap.put(hostBaseInfo.getAgentId(), hostBaseInfo);
        updateHostMonitorCache(hostInfoMap, hostBaseInfo);
    }

    /**
     * delete host cache
     *
     * @param hostId host id
     */
    public void deleteHostCache(String hostId) {
        sshHostMap.remove(hostId);
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

    private void putIfValid(Map<String, String> map, String key, String value) {
        if (StrUtil.isNotEmpty(value)) {
            map.put(key, value);
        }
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

    /**
     * host cache item
     */
    interface CacheConstants {
        /**
         * host disk monitor
         */
        String DISK_MONITOR = "diskMonitor";
    }
}

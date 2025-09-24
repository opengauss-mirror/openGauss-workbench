/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.admin.system.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.model.agent.HostBaseInfo;
import org.opengauss.admin.system.service.ops.IHostService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Resource;

/**
 * HostMonitorCacheService
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Slf4j
@Service
public class HostBasicService {
    private final Map<String, HostBaseInfo> hostBasicMap = new ConcurrentHashMap<>();
    private final CheckValueValid valueChecker = (key, value) -> switch (key) {
        case AgentConstants.HostMetric.CPU_CORE_NUM -> value.matches("\\d+") && Integer.parseInt(value) > 0;
        case AgentConstants.HostMetric.CPU_FREQUENCY -> value.matches("\\d+") && Long.parseLong(value) > 0;
        default -> true;
    };

    @Resource
    private IHostService hostService;

    /**
     * get host basic info map by host id
     *
     * @param hostId host id
     * @return host basic info map
     */
    public Map<String, String> getHostBasicInfoMap(String hostId) {
        HostBaseInfo hostBasicInfo = getHostBasicInfo(hostId);
        Map<String, String> hostInfoMap = new HashMap<>();
        if (Objects.isNull(hostBasicInfo)) {
            return hostInfoMap;
        }
        updateHostMonitorCache(hostInfoMap, hostBasicInfo);
        return hostInfoMap;
    }

    /**
     * get host basic info by host id
     *
     * @param hostId host id
     * @return host basic info
     */
    public HostBaseInfo getHostBasicInfo(String hostId) {
        return hostBasicMap.compute(hostId, (k, v) -> {
            if (v == null) {
                return convertHostBasicInfo(hostId).orElse(null);
            } else {
                return v;
            }
        });
    }

    /**
     * refresh host basic info by host id
     *
     * @param hostId host id
     */
    public void refreshCacheOfHostBasicInfo(String hostId) {
        hostBasicMap.compute(hostId, (k, v) -> convertHostBasicInfo(hostId).orElse(null));
    }

    private Optional<HostBaseInfo> convertHostBasicInfo(String hostId) {
        return Optional.ofNullable(hostService.getById(hostId))
            .map(host -> HostBaseInfo.builder()
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
                .build());
    }

    /**
     * update HostMonitorCacheService
     *
     * @param hostInfoMap host info map
     * @param hostBaseInfo hostBaseInfo
     */
    public void updateHostMonitorCache(Map<String, String> hostInfoMap, HostBaseInfo hostBaseInfo) {
        putIfValid(hostInfoMap, AgentConstants.HostMetric.CPU_ARCH, hostBaseInfo.getCpuArchitecture());
        putIfValid(hostInfoMap, AgentConstants.HostMetric.CPU_CORE_NUM, String.valueOf(hostBaseInfo.getLogicalCores()));
        String cpuFreq = hostBaseInfo.getCpuFreq();
        if (StrUtil.isNotEmpty(cpuFreq) && cpuFreq.matches(AgentConstants.HostMetric.CPU_FREQ_REGEX)) {
            String freqValue = hostBaseInfo.getCpuFreq() + "GHz";
            if (StrUtil.isNotEmpty(freqValue)) {
                hostInfoMap.put(AgentConstants.HostMetric.CPU_FREQUENCY, freqValue);
            }
        } else {
            hostInfoMap.put(AgentConstants.HostMetric.CPU_FREQUENCY, "Unknown Frequency Of lscpu cmd");
        }
        putIfValid(hostInfoMap, AgentConstants.HostMetric.OS_NAME, hostBaseInfo.getOsName());
        putIfValid(hostInfoMap, AgentConstants.HostMetric.OS_VERSION, hostBaseInfo.getOsVersion());
    }

    private void putIfValid(Map<String, String> map, String key, String value) {
        if (StrUtil.isNotEmpty(value) && valueChecker.checked(key, value)) {
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

    @FunctionalInterface
    private interface CheckValueValid {
        /**
         * check value is valid
         *
         * @param key key
         * @param value value
         * @return true if valid
         */
        boolean checked(String key, String value);
    }
}

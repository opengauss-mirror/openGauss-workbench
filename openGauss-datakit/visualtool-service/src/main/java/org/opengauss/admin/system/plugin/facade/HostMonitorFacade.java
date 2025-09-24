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
 * HostMonitorFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool
 * /visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/HostMonitorFacade.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.system.service.HostMonitorCacheService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * Host Monitor Cache Facade
 *
 * @author wangchao
 * @since 2024/10/30 9:41
 **/
@Service
public class HostMonitorFacade {
    @Resource
    private HostMonitorCacheService hostMonitorCacheService;

    /**
     * Get the os of the remote machine
     *
     * @param hostId host id
     * @return os
     */
    public String getOs(String hostId) {
        return hostMonitorCacheService.getOs(hostId);
    }

    /**
     * Get the os version of the remote machine
     *
     * @param hostId host id
     * @return os version
     */
    public String getOsVersion(String hostId) {
        return hostMonitorCacheService.getOsVersion(hostId);
    }

    /**
     * Get the cpu architecture of the remote machine
     *
     * @param hostId host id
     * @return cpu architecture
     */
    public String getCpuArch(String hostId) {
        return hostMonitorCacheService.getCpuArch(hostId);
    }

    /**
     * Get the cpu info of the remote machine
     *
     * @param hostId host id
     * @param hasNetName true 包含网卡名称，false 不包含网卡名称
     * @return net info
     */
    public String[] getNetMonitor(String hostId, boolean hasNetName) {
        return hostMonitorCacheService.getNetMonitor(hostId, hasNetName);
    }

    /**
     * 获取cpu使用率
     *
     * @param hostId host id
     * @return cpu使用率
     */
    public String getCpuUsing(String hostId) {
        return hostMonitorCacheService.getCpuUsing(hostId);
    }

    /**
     * 获取内存使用率
     *
     * @param hostId host id
     * @return 内存使用率
     */
    public String getMemoryUsing(String hostId) {
        return hostMonitorCacheService.getMemoryUsing(hostId);
    }

    /**
     * get host cpu core number
     *
     * @param hostId host id
     * @return cpu core num
     */
    public String getCpuCoreNum(String hostId) {
        return hostMonitorCacheService.getCpuCoreNum(hostId);
    }

    /**
     * get host cpu frequency  eg. 2.4GHz
     *
     * @param hostId host id
     * @return cpu frequency
     */
    public String getCpuFrequency(String hostId) {
        return hostMonitorCacheService.getCpuFrequency(hostId);
    }

    /**
     * 获取内存总量
     *
     * @param hostId host id
     * @return 内存总量
     */
    public String getMemoryTotal(String hostId) {
        return hostMonitorCacheService.getMemoryTotal(hostId);
    }

    /**
     * 获取剩余内存 单位MB
     *
     * @param hostId hostId
     * @return 剩余内存
     */
    public String getRemainingMemory(String hostId) {
        return hostMonitorCacheService.getRemainingMemory(hostId);
    }

    /**
     * 获取可用磁盘总量 单位 GB
     *
     * @param hostId hostId
     * @return 可用磁盘总量
     */
    public String getAvailableDiskSpace(String hostId) {
        return hostMonitorCacheService.getAvailableDiskSpace(hostId);
    }

    /**
     * get migration host info cpu core num ,remaining memory, available disk space
     *
     * @param hostId host info
     * @return migration host info
     */
    public String getMigrationHostInfo(String hostId) {
        return hostMonitorCacheService.getMigrationHostInfo(hostId);
    }
}

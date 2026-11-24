/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.ebpf.service;

/**
 * MonitorService
 *
 * @author luomeng
 * @since 2024/2/22
 */
public interface MonitorService {
    /**
     * Start monitor
     *
     * @param tid         String
     * @param taskId      String
     * @param monitorType String
     */
    void startMonitor(String tid, String taskId, String monitorType);

    /**
     * Stop monitor
     *
     * @param taskId String
     * @return boolean
     */
    boolean stopMonitor(String taskId);

    /**
     * Status monitor
     *
     * @param taskId String
     * @return boolean
     */
    boolean statusMonitor(String taskId);

    /**
     * Record taskId
     *
     * @param taskId String
     */
    void record(String taskId);
}

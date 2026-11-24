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

package com.tools.monitor.service;

import lombok.extern.slf4j.Slf4j;

/**
 * MonitorFlake
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class MonitorFlake {
    private static final long START_STAMP = 1480166465631L;

    private static final long MONITOR_BIT = 12L;

    private static final long MECHANICAL_BIT = 5L;

    private static final long SNOW_FLAKE_CENTER_BIT = 5L;

    private static final long MAX_SNOW_FLAKE_NUM = ~(-1L << SNOW_FLAKE_CENTER_BIT);

    private static final long MAX_SNOW_FLAKE_MACHINE_NUM = ~(-1L << MECHANICAL_BIT);

    private static final long MAX_ESPECIALLY  = ~(-1L << MONITOR_BIT);

    private static final long MONITOR_LEFT = MONITOR_BIT;

    private static final long FLAKE_MONITOR_LEFT = MONITOR_BIT + MECHANICAL_BIT;

    private static final long SNOW_LEFT = FLAKE_MONITOR_LEFT + SNOW_FLAKE_CENTER_BIT;

    private final long monitorCenterId;

    private final long monitorId;

    private long queue = 0L;

    private long lastGeneratorTime = -1L;

    /**
     * MonitorFlake
     *
     * @param monitorCenterId monitorCenterId
     * @param monitorMachineId    monitorMachineId
     */
    public MonitorFlake(long monitorCenterId, long monitorMachineId) {
        if (monitorCenterId > MAX_SNOW_FLAKE_NUM || monitorCenterId < 0) {
            throw new IllegalArgumentException("monitorCenterId can't be greater than MAX_SNOW_FLAKE_MACHINE_NUM or less than 0");
        }
        if (monitorMachineId > MAX_SNOW_FLAKE_MACHINE_NUM || monitorMachineId < 0) {
            throw new IllegalArgumentException("monitorMachineId can't be greater than MAX_SNOW_FLAKE_MACHINE_NUM or less than 0");
        }
        this.monitorCenterId = monitorCenterId;
        this.monitorId = monitorMachineId;
    }

    /**
     * nextId
     *
     * @return long
     */
    public synchronized long nextId() {
        long time = getMonitorewStamp();
        if (time < lastGeneratorTime) {
            log.error("The clock is moved backward. refuse to generate id");
        }
        if (time == lastGeneratorTime) {
            queue = (queue + 1) & MAX_ESPECIALLY;
            if (queue == 0L) {
                time = getNextFactory();
            }
        } else {
            queue = 0L;
        }
        lastGeneratorTime = time;
        return (time - START_STAMP) << SNOW_LEFT
                | monitorCenterId << FLAKE_MONITOR_LEFT
                | monitorId << MONITOR_LEFT
                | queue;
    }

    /**
     * getNextFactory
     *
     * @return long
     */
    private long getNextFactory() {
        long factory = getMonitorewStamp();
        while (factory <= lastGeneratorTime) {
            factory = getMonitorewStamp();
        }
        return factory;
    }

    /**
     * getNewStamp
     *
     * @return long
     */
    private long getMonitorewStamp() {
        return System.currentTimeMillis();
    }
}


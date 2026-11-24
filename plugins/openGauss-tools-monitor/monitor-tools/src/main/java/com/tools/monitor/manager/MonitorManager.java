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

package com.tools.monitor.manager;

import com.tools.monitor.quartz.util.spring.MonitSpringUtils;
import com.tools.monitor.util.MonitorThreads;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MonitorManager
 *
 * @author liu
 * @since 2022-10-01
 */
public class MonitorManager {
    private static MonitorManager manager = new MonitorManager();

    /**
     * Operation delay of 10 ms
     */
    private static final int OPERATION_DELAY = 10;

    /**
     * Asynchronous operation task scheduling thread pool
     */
    private ScheduledExecutorService monitorExecutor = MonitSpringUtils.getStr("monitorExecutorService");

    /**
     * Singleton mode
     */
    private MonitorManager() {
    }

    /**
     * me
     *
     * @return AsyncManager
     */
    public static MonitorManager mine() {
        return manager;
    }

    /**
     * Perform tasks
     *
     * @param task  task
     */
    public void work(TimerTask task) {
        monitorExecutor.schedule(task, OPERATION_DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * stopTask
     */
    public void stopTask() {
        MonitorThreads.monitorShutdown(monitorExecutor);
    }
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package org.opengauss.tun.manager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import org.opengauss.tun.utils.MonitSpringUtils;
import org.opengauss.tun.utils.MonitorThreads;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * MonitorManager
 *
 * @author liu
 * @since 2022-10-01
 */
public class AsyncManager {
    private static AsyncManager manager = new AsyncManager();

    /**
     * 异步操作任务调度线程池
     */
    private ScheduledExecutorService scheduledExecutor = MonitSpringUtils.getStr("monitorExecutorService");

    /**
     * 异步操作任务调度线程池
     */
    private ThreadPoolTaskExecutor threadPoolExecutor = MonitSpringUtils.getStr("threadPoolTaskExecutor");

    /**
     * 单例模式
     */
    private AsyncManager() {
    }

    /**
     * me
     *
     * @return AsyncManager
     */
    public static AsyncManager mine() {
        return manager;
    }


    /**
     * workExecute
     *
     * @param callableTask callableTask
     * @return Future Future
     */
    public Future<Boolean> workTuning(Callable<Boolean> callableTask) {
        return threadPoolExecutor.submit(callableTask);
    }

    /**
     * stopTask
     */
    public void stopTask() {
        MonitorThreads.scheduledShutdown(scheduledExecutor);
        MonitorThreads.executeShutdown(threadPoolExecutor);
    }
}

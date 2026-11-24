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

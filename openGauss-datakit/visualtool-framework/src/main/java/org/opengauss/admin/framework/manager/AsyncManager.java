/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * AsyncManager.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/manager/AsyncManager.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.framework.manager;

import org.opengauss.admin.common.utils.Threads;
import org.opengauss.admin.common.utils.spring.SpringUtils;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous task manager
 *
 * @author xielibo
 */
public class AsyncManager {

    /**
     * Operation delay time. (millisecond)
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * Asynchronous operation task scheduling thread pool
     */
    private ScheduledExecutorService executor = SpringUtils.getBean("scheduledExecutorService");

    private AsyncManager() {
    }

    private static AsyncManager me = new AsyncManager();

    public static AsyncManager me() {
        return me;
    }

    /**
     * Execute Task
     *
     * @param task task
     */
    public void execute(TimerTask task) {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * shutdown
     */
    public void shutdown() {
        Threads.shutdownAndAwaitTermination(executor);
    }
}

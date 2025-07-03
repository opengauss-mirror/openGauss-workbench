/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.common;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import lombok.Data;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * MutinyExecutor
 *
 * @author: wangchao
 * @Date: 2025/5/21 11:14
 * @since 7.0.0-RC2
 **/
@Singleton
@Data
@Unremovable
public class MutinyExecutor {
    @ConfigProperty(name = "agent.thread-pool.max-threads")
    int poolSize;
    private ExecutorService workerPool;
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * init
     */
    @PostConstruct
    void init() {
        workerPool = Executors.newFixedThreadPool(poolSize);
        scheduledExecutorService = Executors.newScheduledThreadPool(poolSize);
    }

    /**
     * execute
     *
     * @param command command
     */
    public void execute(Runnable command) {
        workerPool.execute(command);
    }

    /**
     * schedule
     *
     * @param command command
     * @param delay delay
     * @param unit unit
     * @return ScheduledFuture
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledExecutorService.scheduleWithFixedDelay(command, delay, delay, unit);
    }

    void onStop(@Observes ShutdownEvent event) {
        if (workerPool != null) {
            workerPool.shutdown();
        }
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }
}

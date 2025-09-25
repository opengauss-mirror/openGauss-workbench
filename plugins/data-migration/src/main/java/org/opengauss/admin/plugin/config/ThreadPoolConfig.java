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
 * ThreadPoolConfig.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/config/ThreadPoolConfig.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.config;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.opengauss.admin.common.utils.Threads;
import org.opengauss.jsch.pool.JschSessionPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import jakarta.annotation.PreDestroy;

/**
 * ThreadPool Configuration
 *
 * @author xielibo
 **/
@Configuration
public class ThreadPoolConfig {
    private ThreadPoolTaskExecutor executor;
    private ScheduledExecutorService scheduledExecutor;

    private int corePoolSize = 10;

    private int maxPoolSize = 20;

    private int queueCapacity = 2000;

    private int keepAliveSeconds = 30;

    /**
     * thread pool task executor
     *
     * @return thread pool task executor
     */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * scheduled executor service
     *
     * @return scheduled executor service
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        scheduledExecutor = new ScheduledThreadPoolExecutor(corePoolSize,
            new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
        return scheduledExecutor;
    }

    @PreDestroy
    private void destroyExecutor() {
        if (executor != null) {
            executor.shutdown();
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        JschSessionPool.closeAllPools();
    }
}

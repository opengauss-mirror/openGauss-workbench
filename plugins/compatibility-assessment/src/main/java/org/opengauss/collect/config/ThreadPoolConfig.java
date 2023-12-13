/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.config;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * ThreadPoolConfig
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {
    private final int coreThreadPoolSize = 40;

    private final int maxThreadPoolSize = 100;

    private final int maxQueueCapacity = 1200;

    private final int monitorMaxThreadAliveSeconds = 400;

    /**
     * threadPoolTaskExecutor
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor monitor = new ThreadPoolTaskExecutor();
        monitor.setMaxPoolSize(maxThreadPoolSize);
        monitor.setCorePoolSize(coreThreadPoolSize);
        monitor.setQueueCapacity(maxQueueCapacity);
        monitor.setKeepAliveSeconds(monitorMaxThreadAliveSeconds);
        // Thread pool processing strategy for rejected tasks (wireless programs are available)
        monitor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return monitor;
    }

    /**
     * Scheduled
     *
     * @return  ScheduledExecutorService
     */
    @Bean(name = "monitorExecutorService")
    protected ScheduledExecutorService monitorExecutorService() {
        return new ScheduledThreadPoolExecutor(coreThreadPoolSize,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable runnable, Throwable throwable) {
                log.info("ThreadPoolConfig runnable throwable");
            }
        };
    }
}

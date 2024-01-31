/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package org.opengauss.tun.config;

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
    private int coreThreadPoolSize = 40;

    private int maxThreadPoolSize = 100;

    private int maxQueueCapacity = 1200;

    private int monitorMaxThreadAliveSeconds = 400;

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

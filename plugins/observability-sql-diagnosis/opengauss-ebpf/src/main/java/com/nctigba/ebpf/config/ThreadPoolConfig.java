/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ThreadPoolConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/config/ThreadPoolConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@EnableAsync
@Configuration
public class ThreadPoolConfig {
    private static final int CORE_POOL_SIZE = 15;
    private static final int MAX_POOL_SIZE = 2 * CORE_POOL_SIZE;
    private static final int KEEPALIVE_SECONDS = 60;
    private static final int QUERY_CAPACITY = 128;
    private static final int AVAIL_TERMINATION_MILLIS = 60;
    private static final String THREAD_NAME_PREFIX = "ebpf-";

    @Bean("ebpfPool")
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //Number of core threads
        executor.setCorePoolSize(CORE_POOL_SIZE);
        //Maximum number of threads
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        //Allow thread idle time
        executor.setKeepAliveSeconds(KEEPALIVE_SECONDS);
        //Buffer queue
        executor.setQueueCapacity(QUERY_CAPACITY);
        //Prefix of thread
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        //Set the waiting time of tasks in the thread
        executor.setAwaitTerminationMillis(AVAIL_TERMINATION_MILLIS);
        //Processing strategy of thread to reject task
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //When the thread is closed, wait until all tasks are completed before continuing to destroy other beans
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}

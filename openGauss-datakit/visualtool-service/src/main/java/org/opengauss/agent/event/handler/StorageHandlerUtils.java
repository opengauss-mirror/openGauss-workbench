/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.event.handler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * StorageHandlerUtils
 *
 * @author: wangchao
 * @Date: 2025/6/10 08:58
 * @since 7.0.0-RC2
 **/
public class StorageHandlerUtils {
    private static final ScheduledExecutorService PERIOD_EXECUTOR = Executors.newScheduledThreadPool(4,
        new ThreadFactoryBuilder().setNameFormat("his-cleaner-%d").build());

    /**
     * get period schedule executor
     *
     * @return period executor
     */
    public static ScheduledExecutorService getPeriodExecutor() {
        return PERIOD_EXECUTOR;
    }

    /**
     * calculate initial delay ： after half of keep period (avoid execute immediately)
     *
     * @param keepPeriod keep period
     * @return initial delay in seconds
     */
    public static long calculateInitialDelay(String keepPeriod) {
        return Duration.parse(keepPeriod).toSeconds() / 2;
    }

    /**
     * calculate clean interval :
     * <pre>
     *     1. calculate base interval : 0.1 * keep period
     *     2. keep period <= 1h , the tiniest interval is 60s
     *     3. keep period > 1h , the tiniest interval is 3600s
     *     4. calculate clean interval : take the max of base interval and tiniest interval
     * </pre>
     *
     * @param keepPeriod keep period
     * @return clean interval in seconds
     */
    public static long calculateCleanInterval(String keepPeriod) {
        long durationSeconds = Duration.parse(keepPeriod).toSeconds();
        long interval = durationSeconds / 10;
        long tiniestInterval = durationSeconds <= 3600 ? 60 : 3600;
        return Math.max(interval, tiniestInterval);
    }

    /**
     * shutdown period executor
     */
    public static void shutdownNow() {
        PERIOD_EXECUTOR.shutdownNow();
    }
}

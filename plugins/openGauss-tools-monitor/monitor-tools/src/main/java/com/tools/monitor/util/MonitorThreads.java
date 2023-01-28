/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * MonitorThreads
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class MonitorThreads {
    /**
     * monitorShutdown
     *
     * @param executorService
     */
    public static void monitorShutdown(ExecutorService executorService) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(100, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(100, TimeUnit.SECONDS)) {
                        log.info("executorService not stop");
                    }
                }
            } catch (InterruptedException exception) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

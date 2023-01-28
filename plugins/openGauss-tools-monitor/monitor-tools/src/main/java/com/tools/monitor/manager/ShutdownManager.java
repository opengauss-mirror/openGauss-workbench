/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * ShutdownManager
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Component
public class ShutdownManager {
    /**
     * destroy
     */
    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    /**
     * Stop asynchronous task execution
     */
    private void shutdownAsyncManager() {
        log.info("====stop Executior====");
        MonitorManager.mine().stopTask();
    }
}

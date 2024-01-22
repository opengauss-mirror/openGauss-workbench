/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package org.opengauss.tun.manager;

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
     * 停止异步执行任务
     */
    private void shutdownAsyncManager() {
        log.info("====stop Executior====");
        AsyncManager.mine().stopTask();
    }
}

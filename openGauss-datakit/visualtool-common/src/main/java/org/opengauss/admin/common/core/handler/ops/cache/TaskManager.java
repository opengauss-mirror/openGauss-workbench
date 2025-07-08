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
 * TaskManager.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/handler/ops/cache/TaskManager
 * .java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.common.core.handler.ops.cache;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.exception.ops.OpsException;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author lhf
 * @date 2022/8/11 20:37
 **/
@Slf4j
public class TaskManager {
    private static final int MAX_RETRIES = 10;
    private static final ConcurrentHashMap<String, Future<?>> TASK_CONTEXT = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Runnable> TASK_WORKER_CONTEXT = new ConcurrentHashMap<>();

    /**
     * registry task to context
     *
     * @param businessId businessId
     * @param future future
     */
    public static void registry(String businessId, Future<?> future) {
        TASK_CONTEXT.put(businessId, future);
    }

    /**
     * remove task from context, if the task is not finished, it will be cancelled.
     * if the task is finished or cancelled, it will be removed from context.
     *
     * @param businessId businessId
     * @return Optional<Future < ?>> future
     */
    public static Optional<Future<?>> remove(String businessId) {
        Future<?> future = TASK_CONTEXT.get(businessId);
        if (future == null) {
            log.info("task {} not found in context", businessId);
            return Optional.empty();
        }
        if (future.isDone() || future.isCancelled()) {
            log.info("task {} already completed or cancelled", businessId);
            TASK_WORKER_CONTEXT.remove(businessId);
            return Optional.of(TASK_CONTEXT.remove(businessId));
        }
        int retryCount = 0;
        while (!future.isCancelled() && !future.isDone() && retryCount < MAX_RETRIES) {
            future.cancel(true);
            log.warn("attempting to cancel task: {}", businessId);
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                log.debug("thread interrupted while waiting for task cancellation", e);
                Thread.currentThread().interrupt();
                break;
            }
            retryCount++;
        }
        if (future.isCancelled() || future.isDone()) {
            log.info("task {} successfully cancelled or completed", businessId);
            TASK_WORKER_CONTEXT.remove(businessId);
            return Optional.of(TASK_CONTEXT.remove(businessId));
        } else {
            log.error("failed to cancel task {} after {} attempts", businessId, MAX_RETRIES);
            throw new OpsException("Failed to cancel task " + businessId);
        }
    }

    /**
     * registry worker to context
     *
     * @param businessId businessId
     * @param worker worker
     */
    public static void registryWorker(String businessId, Runnable worker) {
        TASK_WORKER_CONTEXT.put(businessId, worker);
    }

    /**
     * get worker from context
     *
     * @param businessId businessId
     * @return Runnable
     */
    public static Runnable getRegistryWorker(String businessId) {
        return TASK_WORKER_CONTEXT.get(businessId);
    }
}

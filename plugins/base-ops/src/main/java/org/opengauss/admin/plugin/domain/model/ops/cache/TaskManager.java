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
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/cache/TaskManager.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
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
    private static final ConcurrentHashMap<String, Future<?>> TASK_CONTEXT = new ConcurrentHashMap<>();

    public static void registry(String sessionId, Future<?> future) {
        TASK_CONTEXT.put(sessionId, future);
    }

    public static int executeCount(String businessPrefix) {
        return (int) TASK_CONTEXT.keySet().stream().filter(s -> s.startsWith(businessPrefix)).count();
    }

    /**
     * cancel task
     *
     * @param sessionId session id
     * @return task future
     */
    public static Optional<Future<?>> remove(String sessionId) {
        Future<?> future = TASK_CONTEXT.get(sessionId);
        while (Objects.nonNull(future) && !future.isCancelled()) {
            future.cancel(true);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.warn("remove task {} interrupted", sessionId);
            }
        }
        return Optional.ofNullable(TASK_CONTEXT.remove(sessionId));
    }
}

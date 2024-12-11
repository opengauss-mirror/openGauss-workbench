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
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/handler/ops/cache/TaskManager.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.handler.ops.cache;

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

    public static void registry(String businessId, Future<?> future) {
        TASK_CONTEXT.put(businessId, future);
    }

    public static Optional<Future<?>> remove(String businessId) {
        Future<?> future = TASK_CONTEXT.get(businessId);
        while (Objects.nonNull(future) && !future.isCancelled()) {
            future.cancel(true);
            log.warn("remove task message: {}", businessId);
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException ignore) {
                log.debug("thread interrupted while waiting for task {}cancellation", businessId);
            }
        }
        return Optional.ofNullable(TASK_CONTEXT.remove(businessId));
    }
}

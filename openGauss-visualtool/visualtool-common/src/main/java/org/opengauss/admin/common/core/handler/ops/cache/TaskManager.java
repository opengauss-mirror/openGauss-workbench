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
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.error("remove task error, message: {}", e.getMessage());
            }
        }

        return Optional.ofNullable(TASK_CONTEXT.remove(businessId));
    }
}

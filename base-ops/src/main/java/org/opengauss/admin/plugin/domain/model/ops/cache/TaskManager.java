package org.opengauss.admin.plugin.domain.model.ops.cache;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author lhf
 * @date 2022/8/11 20:37
 **/
public class TaskManager {
    private static final ConcurrentHashMap<String, Future<?>> TASK_CONTEXT = new ConcurrentHashMap<>();

    public static void registry(String sessionId, Future<?> future) {
        TASK_CONTEXT.put(sessionId, future);
    }

    public static Optional<Future<?>> remove(String sessionId) {
        Future<?> future = TASK_CONTEXT.get(sessionId);
        while (Objects.nonNull(future) && !future.isCancelled()) {
            future.cancel(true);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return Optional.ofNullable(TASK_CONTEXT.remove(sessionId));
    }
}

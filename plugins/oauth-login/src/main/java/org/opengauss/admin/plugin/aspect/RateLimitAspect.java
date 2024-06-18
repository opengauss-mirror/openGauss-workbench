/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * RateLimitAspect.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/aspect/RateLimitAspect.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.opengauss.admin.plugin.annotation.RateLimit;
import org.opengauss.admin.plugin.exception.OauthLoginException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @date 2024/6/14 10:33
 * @since 0.0
 */
@Aspect
@Component
public class RateLimitAspect {
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> timestamps = new ConcurrentHashMap<>();

    /**
     * aspect of rate limit
     *
     * @param joinPoint join point
     * @param rateLimit rate limit annotation
     * @return Object
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        try {
            String key = generateKey(joinPoint);
            // Current timestamp (minutes)
            long currentTimestamp = System.currentTimeMillis() / 60000;
            synchronized (this) {
                if (!timestamps.containsKey(key) || timestamps.get(key) != currentTimestamp) {
                    // If the current interface does not have a corresponding timestamp record or the record has expired.
                    // The counter and timestamp are reset.
                    counters.put(key, new AtomicInteger(1));
                    timestamps.put(key, currentTimestamp);
                    // continuation method
                    return joinPoint.proceed();
                } else if (counters.get(key).incrementAndGet() <= rateLimit.maxRequests()) {
                    // If the number of requests for the current interface in the current time window has not reached the upper limit.
                    // The counter plus continues to execute the method.
                    return joinPoint.proceed();
                } else {
                    // An exception is thrown when the number of requests reaches the upper limit.
                    throw new OauthLoginException(rateLimit.message());
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * generate key by join point
     *
     * @param joinPoint join point
     * @return String key
     */
    private String generateKey(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature().toString();
    }
}

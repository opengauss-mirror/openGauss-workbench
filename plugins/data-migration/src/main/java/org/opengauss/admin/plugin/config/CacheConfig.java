/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.opengauss.admin.plugin.constants.TaskAlertConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * cache config
 *
 * @since 2024/12/16
 */
@Configuration
public class CacheConfig {
    /**
     * get a caffeine cache instance to store the number of parsed objects
     *
     * @return Cache<Integer, Integer>
     */
    @Bean
    public Cache<Integer, Integer> resolvedObjectsNumberCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(TaskAlertConstants.CACHE_EXPIRE_TIME, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .build();
    }

    /**
     * get a caffeine cache instance to store the last modified time of a file
     *
     * @return Cache<String, Long>
     */
    @Bean
    public Cache<String, Long> fileLastModifiedCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(10000)
                .build();
    }
}

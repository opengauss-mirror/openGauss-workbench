/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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

    /**
     * Set a caffeine cache instance to store the restart incremental or reverse task cache
     *
     * @return Cache<Integer, String>
     */
    @Bean
    public Cache<Integer, String> restartIncrementalOrReverseTaskCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(100)
                .build();
    }

    /**
     * Set a caffeine cache instance to store the portal process exited cache
     *
     * @return Cache<Integer, Long>
     */
    @Bean(name = "portalProcessExitedCache")
    public Cache<Integer, Long> portalProcessExitedCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(100)
                .build();
    }
}

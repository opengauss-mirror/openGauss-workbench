/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.opengauss.collect.domain.Assessment;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.domain.LinuxConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CacheConfig
 *
 * @author liu
 * @since 2023-10-15
 */
@Configuration
public class CacheConfig {
    /**
     * collectCache
     *
     * @return Cache Cache
     */
    @Bean
    public Cache<Long, CollectPeriod> collectCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Long.MAX_VALUE, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(300)
                .build();
    }

    /**
     * linuxConfigCache
     *
     * @return Cache
     */
    @Bean
    public Cache<Long, LinuxConfig> linuxConfigCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Long.MAX_VALUE, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(120)
                .build();
    }

    /**
     * linuxConfigCache
     *
     * @return Cache Cache
     */
    @Bean
    public Cache<String, Assessment> assessmentCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Long.MAX_VALUE, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(120)
                .build();
    }
}

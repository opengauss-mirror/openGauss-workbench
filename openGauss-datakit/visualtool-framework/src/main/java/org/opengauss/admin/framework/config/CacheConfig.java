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
 * CacheConfig.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/config/CacheConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.framework.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.opengauss.admin.common.constant.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @className: CacheConfig
 * @description: CacheConfig
 * @author: xielibo
 * @date: 2022-12-17 12:50
 **/
@Configuration
public class CacheConfig {
    private int expireTime = Constants.TOKEN_EXPIRE_TIME;

    @Bean
    public Cache<String, Object> caffeineCache() {
        return Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .initialCapacity(100)
            .maximumSize(1000)
            .build();
    }

    @Bean
    public Cache<String, Object> loginCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(expireTime, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .build();
    }
}

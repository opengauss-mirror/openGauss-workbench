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
 * CacheConfig.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/config/CacheConfig.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.opengauss.admin.plugin.constants.MainConstants;
import org.opengauss.admin.plugin.domain.PkceBody;
import org.opengauss.admin.plugin.domain.SsoInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @date 2024/5/30 10:53
 * @since 0.0
 */
@Configuration
public class CacheConfig {
    private int initialCapacity = 100;
    private int maximumSize = 1000;
    private int expiresTime = 60;

    /**
     * cache for storing sso information
     *
     * @return Cache ssoInfoCache
     */
    @Bean
    public Cache<String, SsoInfo> ssoInfoCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(MainConstants.TOKEN_EXPIRES_TIME, TimeUnit.MINUTES)
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .build();
    }

    /**
     * cache for login tokens of sso mapping users
     *
     * @return Set tokenCacheSet
     */
    @Bean
    public Set<String> tokenCacheSet() {
        return new HashSet<>();
    }

    /**
     * Cache for pkce information
     *
     * @return Cache statePkceCache
     */
    @Bean
    public Cache<String, PkceBody> statePkceCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(expiresTime, TimeUnit.SECONDS)
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .build();
    }
}

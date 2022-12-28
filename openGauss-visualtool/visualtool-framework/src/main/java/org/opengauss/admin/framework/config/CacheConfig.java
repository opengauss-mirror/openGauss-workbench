package org.opengauss.admin.framework.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${token.expireTime}")
    private int expireTime;

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

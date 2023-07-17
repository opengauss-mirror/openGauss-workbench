/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio;

import com.gitee.starblues.bootstrap.EmptyMainApplicationContext;
import com.gitee.starblues.spring.MainApplicationContext;
import lombok.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * DataStudioPluginBootApplication
 *
 * @since 2023-6-26
 */
@Generated
@Profile({"dev", "gba"})
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class})
@EnableAsync
public class DataStudioPluginBootApplication {
    /**
     * main
     *
     * @param args args
     */
    public static void main(String[] args) {
        SpringApplication.run(DataStudioPluginBootApplication.class, args);
    }

    /**
     * test
     *
     * @return MainApplicationContext
     */
    @Bean
    @Primary
    public MainApplicationContext test() {
        return new EmptyMainApplicationContext();
    }

}

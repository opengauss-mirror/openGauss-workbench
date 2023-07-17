/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.gitee.starblues.bootstrap.SpringPluginBootstrap;
import lombok.Generated;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * DataStudioPluginApplication
 *
 * @since 2023-6-26
 */
@Generated
@EnableAsync
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        DruidDataSourceAutoConfigure.class,
        RedisRepositoriesAutoConfiguration.class
})
public class DataStudioPluginApplication extends SpringPluginBootstrap {
    /**
     * main
     *
     * @param args args
     */
    public static void main(String[] args) {
        new DataStudioPluginApplication().run(args);
    }
}

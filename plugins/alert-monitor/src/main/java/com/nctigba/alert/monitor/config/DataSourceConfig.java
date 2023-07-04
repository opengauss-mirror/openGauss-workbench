/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * @Author wuyuebin
 * @Date 2023/4/13 09:39
 * @Description
 */
@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    @Profile("!dev")
    public DataSource dataSource() {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        // read config from dataKit platform
        String url = environmentProvider.getString("spring.datasource.url");
        String username = environmentProvider.getString("spring.datasource.username");
        String password = environmentProvider.getString("spring.datasource.password");
        String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");

        DataSource primary = DataSourceBuilder.create().driverClassName(driverClassName).url(url).username(username)
                .password(password).build();
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.addDataSource("primary", primary);
        dataSource.setPrimary("primary");
        return dataSource;
    }
}

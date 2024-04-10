/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  DataSourceConfig.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/config/DataSourceConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import com.nctigba.alert.monitor.enums.DbDataLocationEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author wuyuebin
 * @Date 2023/4/13 09:39
 * @Description
 */
@Configuration
public class DataSourceConfig {
    @Autowired
    DruidDataSourceCreator druidDataSourceCreator;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    @Profile("!dev")
    public DataSource dataSource() {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        // read config from dataKit platform
        DataSourceProperty primaryProperty = new DataSourceProperty();
        primaryProperty.setUrl(environmentProvider.getString("spring.datasource.url"));
        primaryProperty.setUsername(environmentProvider.getString("spring.datasource.username"));
        primaryProperty.setPassword(environmentProvider.getString("spring.datasource.password"));
        primaryProperty.setDriverClassName(environmentProvider.getString("spring.datasource.driver-class-name"));
        DataSource primary = druidDataSourceCreator.doCreateDataSource(primaryProperty);
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.addDataSource("primary", primary);
        dataSource.setPrimary("primary");
        return dataSource;
    }

    @Bean
    @Profile("!dev")
    DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource) {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");
        Optional<DbDataLocationEnum> optional = DbDataLocationEnum.of(driverClassName);
        DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;
        DataSource primary = drds.getDataSource("primary");
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(true);
        settings.setSeparator(";");
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        if (optional.isEmpty()) {
            return new DataSourceScriptDatabaseInitializer(primary, new DatabaseInitializationSettings());
        }
        settings.setSchemaLocations(scriptLocations(null, "schema", "all"));
        DbDataLocationEnum dataLocationEnum = optional.get();
        settings.setDataLocations(dataLocationEnum.getLocations());
        return new DataSourceScriptDatabaseInitializer(primary, settings);
    }
    private List<String> scriptLocations(List<String> locations, String fallback, String platform) {
        if (locations != null) {
            return locations;
        } else {
            List<String> fallbackLocations = new ArrayList();
            fallbackLocations.add("optional:classpath*:" + fallback + "-" + platform + ".sql");
            fallbackLocations.add("optional:classpath*:" + fallback + ".sql");
            return fallbackLocations;
        }
    }
}

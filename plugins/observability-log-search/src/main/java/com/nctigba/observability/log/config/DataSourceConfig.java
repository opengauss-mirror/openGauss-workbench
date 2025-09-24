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
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/config/DataSourceConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.druid.DruidDataSourceCreator;
import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import com.nctigba.observability.log.enums.DbDataLocationEnum;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author LZW
 * @date 2023/1/5
 */
@Configuration
public class DataSourceConfig {
    @Autowired
    DruidDataSourceCreator druidDataSourceCreator;

    /**
     * dataSource
     *
     * @return DataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        // read config from dataKit platform
        DataSourceProperty primaryProperty = new DataSourceProperty();
        primaryProperty.setUrl(environmentProvider.getString("spring.datasource.url"));
        primaryProperty.setUsername(environmentProvider.getString("spring.datasource.username"));
        primaryProperty.setPassword(environmentProvider.getString("spring.datasource.password"));
        primaryProperty.setDriverClassName(environmentProvider.getString("spring.datasource.driver-class-name"));
        DataSource primary = druidDataSourceCreator.createDataSource(primaryProperty);
        DynamicRoutingDataSource d = new DynamicRoutingDataSource(new ArrayList<>());
        d.addDataSource("primary", primary);
        d.setPrimary("primary");
        return d;
    }

    /**
     * DataSourceScriptDatabaseInitializer
     *
     * @param dataSource DataSource
     * @return DataSourceScriptDatabaseInitializer
     */
    @Bean("logSearchDataSourceScriptDatabaseInitializer")
    @Profile("!dev")
    public DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource) {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");
        Optional<DbDataLocationEnum> optional = DbDataLocationEnum.of(driverClassName);
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            throw new CustomException("datasource is not DynamicRoutingDataSource");
        }
        DynamicRoutingDataSource drds = (DynamicRoutingDataSource) dataSource;
        DataSource primary = drds.getDataSource("primary");
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(true);
        settings.setSeparator(";");
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        if (optional.isEmpty()) {
            return new DataSourceScriptDatabaseInitializer(primary, new DatabaseInitializationSettings());
        }
        DbDataLocationEnum dataLocationEnum = optional.get();
        settings.setDataLocations(dataLocationEnum.getLocations());
        return new DataSourceScriptDatabaseInitializer(primary, settings);
    }
}
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
 * DataSourceConfig.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/config/DataSourceConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.config;

import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import org.opengauss.admin.plugin.enums.DbDataLocationEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * @date 2024/5/28 9:58
 * @since 0.0
 */
@Configuration
public class DataSourceConfig {

    /**
     * get DataSource bean
     *
     * @return DataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        // read config from dataKit platform
        String url = environmentProvider.getString("spring.datasource.url");
        String username = environmentProvider.getString("spring.datasource.username");
        String password = environmentProvider.getString("spring.datasource.password");
        String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");

        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    /**
     * get DataSourceScriptDatabaseInitializer bean
     *
     * @param dataSource data source
     * @return DataSourceScriptDatabaseInitializer
     */
    @Bean
    DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource) {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");
        Optional<DbDataLocationEnum> dbDataLocationEnum = DbDataLocationEnum.of(driverClassName);
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(false);
        settings.setSeparator(";");
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        if (dbDataLocationEnum.isEmpty()) {
            return new DataSourceScriptDatabaseInitializer(dataSource, new DatabaseInitializationSettings());
        }
        settings.setDataLocations(dbDataLocationEnum.get().getLocations());
        return new DataSourceScriptDatabaseInitializer(dataSource, settings);
    }
}

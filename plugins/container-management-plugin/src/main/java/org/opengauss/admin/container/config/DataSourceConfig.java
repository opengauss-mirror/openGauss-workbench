/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/config/DataSourceConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.config;

import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.List;

/**
 * note：当需要初始化数据库的时候，启用此配置、或者配置文件名配置:myProps.initdb.enable=true
 *
 * @since 2024-08-29
 */
@Slf4j
@ConditionalOnProperty(prefix = "myProps.initdb", name = "enable", havingValue = "true")
@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
        // read config from dataKit platform
        String url = environmentProvider.getString("spring.datasource.url");
        String username = environmentProvider.getString("spring.datasource.username");
        String password = environmentProvider.getString("spring.datasource.password");
        String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");

        return DataSourceBuilder.create().driverClassName(driverClassName).url(url).username(username)
                .password(password).build();
    }

    @Bean
    @Profile("!test")
    DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource) {
        log.info("Enable database initialization option for application");
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(true);
        settings.setSeparator(";");
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        settings.setSchemaLocations(List.of("classpath:db/openGauss-container-schema.sql"));
        return new DataSourceScriptDatabaseInitializer(dataSource, settings);
    }
}

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
 * DataSourceConfig.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/config/DataSourceConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.web.core.config;

import org.opengauss.admin.common.enums.DbDataLocationEnum;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * DataSourceConfig
 *
 * @since 2024/1/16
 */
@Configuration
public class DataSourceConfig {
    @Bean
    @Profile("!dev")
    DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSourceProperties properties,
                                                                            DataSource dataSource) {
        String driverClassName = properties.getDriverClassName();
        Optional<DbDataLocationEnum> dbDataLocationEnum = DbDataLocationEnum.of(driverClassName);
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setContinueOnError(true);
        settings.setSeparator(";");
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        if (dbDataLocationEnum.isEmpty()) {
            return new DataSourceScriptDatabaseInitializer(dataSource, new DatabaseInitializationSettings());
        }
        settings.setDataLocations(dbDataLocationEnum.get().getLocations());
        return new DataSourceScriptDatabaseInitializer(dataSource, settings);
    }
}

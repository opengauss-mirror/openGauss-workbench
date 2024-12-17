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

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.enums.DbDataLocationEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * DataSourceConfig
 *
 * @since 2024/1/16
 */
@Slf4j
@Configuration
public class DataSourceConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @Profile("!dev")
    DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSourceProperties properties,
                                                                            DataSource dataSource) {
        checkConnection(properties);
        checkPermission(properties);
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

    private void checkConnection(DataSourceProperties properties) {
        try (Connection connection = DriverManager.getConnection(
                properties.getUrl(), properties.getUsername(), properties.getPassword())) {
            if (connection == null) {
                log.error("Failed to get connection to the database: connection is null.");
                System.exit(SpringApplication.exit(applicationContext, () -> 1));
            }
        } catch (SQLException e) {
            log.error("Failed to get connection to the database: ", e);
            System.exit(SpringApplication.exit(applicationContext, () -> 1));
        }
    }

    private void checkPermission(DataSourceProperties properties) {
        String permissionSql = String.format(
                "select rolsystemadmin from pg_roles where rolname= '%s';", properties.getUsername());

        try (Connection connection = DriverManager.getConnection(
                properties.getUrl(), properties.getUsername(), properties.getPassword());
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(permissionSql)) {
            String permissionResult = "f";
            if (resultSet.next()) {
                permissionResult = resultSet.getString(1);
            }

            if (permissionResult == null || !permissionResult.equals("t")) {
                log.error("The database user permission does not meet requirements. "
                        + "Please set the user as the sysadmin.");
                System.exit(SpringApplication.exit(applicationContext, () -> 1));
            }
        } catch (SQLException e) {
            log.error("Failed to check the permission of the database user: ", e);
            System.exit(SpringApplication.exit(applicationContext, () -> 1));
        }
    }
}

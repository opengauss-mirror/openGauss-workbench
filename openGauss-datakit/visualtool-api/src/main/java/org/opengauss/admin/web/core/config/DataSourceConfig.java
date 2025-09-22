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
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.system.base.DbDataLocation;
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
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Bean("customDataSourceInitializer")
    @Profile("!dev")
    DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSourceProperties properties,
        DataSource dataSource) {
        String driverClassName = properties.getDriverClassName();
        if (DbDataLocationEnum.OPENGAUSS.getDriverClass().equals(driverClassName)) {
            checkDatabaseAvailability(properties);
        }
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

    private void checkDatabaseAvailability(DataSourceProperties properties) {
        try (Connection connection = createConnection(properties)) {
            checkSqlCompatibility(connection);
            checkPermission(connection, properties.getUsername());
        } catch (SQLException e) {
            log.error("Failed to check the database availability.", e);
            System.exit(SpringApplication.exit(applicationContext, () -> 1));
        }
    }

    private Connection createConnection(DataSourceProperties properties) throws SQLException {
        Connection connection = DriverManager.getConnection(properties.getUrl(), properties.getUsername(),
            properties.getPassword());
        if (connection == null) {
            log.error("Failed to get connection to the database: connection is null.");
            System.exit(SpringApplication.exit(applicationContext, () -> 1));
        }
        return connection;
    }

    /**
     * release the database location according to the database type
     *
     * @param properties properties
     * @return DbDataLocation
     */
    @Bean
    public DbDataLocation getDbDataLocation(DataSourceProperties properties) {
        String driverClassName = properties.getDriverClassName();
        return DbDataLocationEnum.of(driverClassName).map(DbDataLocation::new).orElseThrow(() -> {
            String supportDrivers = Arrays.stream(DbDataLocationEnum.values())
                .map(DbDataLocationEnum::getDriverClass)
                .collect(Collectors.joining());
            return new OpsException(
                "Invalid driver class name: " + driverClassName + ", support drivers: " + supportDrivers);
        });
    }

    private void checkSqlCompatibility(Connection connection) throws SQLException {
        String showCompatibilitySql = "show sql_compatibility;";
        try (Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(showCompatibilitySql)) {
            if (resultSet.next()) {
                String sqlCompatibility = resultSet.getString(1);
                if (sqlCompatibility == null || !sqlCompatibility.equals("A")) {
                    log.error("The sql_compatibility type of the database does not meet requirements. "
                        + "The required type is 'A', and the current type is '{}'.", sqlCompatibility);
                    System.exit(SpringApplication.exit(applicationContext, () -> 1));
                }
            }
        }
    }

    private void checkPermission(Connection connection, String username) throws SQLException {
        String permissionSql = String.format("select rolsystemadmin from pg_roles where rolname= '%s';", username);
        try (Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(permissionSql)) {
            if (resultSet.next()) {
                String permissionResult = resultSet.getString(1);
                if (permissionResult == null || !permissionResult.equals("t")) {
                    log.error("The database user permission does not meet requirements. "
                        + "Please set the user as the sysadmin.");
                    System.exit(SpringApplication.exit(applicationContext, () -> 1));
                }
            }
        }
    }
}

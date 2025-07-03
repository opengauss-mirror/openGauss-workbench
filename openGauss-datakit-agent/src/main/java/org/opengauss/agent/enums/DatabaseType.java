/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.enums;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

/**
 * DatabaseType
 *
 * @author: wangchao
 * @Date: 2025/5/6 17:31
 * @Description: DatabaseType
 * @since 7.0.0-RC2
 **/
@Getter
public enum DatabaseType implements ConnectionContainer {
    MYSQL("mysql", "com.mysql.cj.jdbc.Driver") {
        @Override
        public void configureDataSource(HikariConfig config, String url, String user, String password) {
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        }
    },
    OPENGAUSS("opengauss", "org.opengauss.Driver") {
        @Override
        public void configureDataSource(HikariConfig config, String url, String user, String password) {
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.addDataSourceProperty("ssl", "false");
        }
    },
    POSTGRESQL("postgresql", "org.postgresql.Driver") {
        @Override
        public void configureDataSource(HikariConfig config, String url, String user, String password) {
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
        }
    },
    ORACLE("oracle", "oracle.jdbc.driver.OracleDriver") {
        @Override
        public void configureDataSource(HikariConfig config, String url, String user, String password) {
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.addDataSourceProperty("implicitCachingEnabled", "true");
        }
    },
    SQL_SERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver") {
        @Override
        public void configureDataSource(HikariConfig config, String url, String user, String password) {
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.addDataSourceProperty("encrypt", "false");
        }
    },
    UNKNOWN("", null) {
        @Override
        public void configureDataSource(HikariConfig config, String url, String user, String password) {
            throw new UnsupportedOperationException("Unknown database type");
        }

        @Override
        public Connection getConnection(String url, String user, String password) throws SQLException {
            throw new UnsupportedOperationException();
        }
    };

    private final String value;
    private final String driverClass;
    private final ConcurrentMap<String, DataSource> dataSources = new ConcurrentHashMap<>();

    DatabaseType(String value, String driverClass) {
        this.value = value.trim().toLowerCase(Locale.ENGLISH);
        this.driverClass = driverClass;
    }

    @Override
    public Connection getConnection(String url, String user, String password) throws SQLException {
        if (this == UNKNOWN) {
            throw new SQLException("Unsupported database type");
        }
        String poolKey = generatePoolKey(url, user);
        DataSource dataSource = dataSources.computeIfAbsent(poolKey, k -> {
            HikariConfig hikariConfig = createHikariConfig(url, user, password);
            return new HikariDataSource(hikariConfig);
        });
        return dataSource.getConnection();
    }

    private HikariConfig createHikariConfig(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClass);
        config.setPoolName("hikariPool-" + this.value + "_" + url.hashCode());
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        configureDataSource(config, url, user, password);
        return config;
    }

    private String generatePoolKey(String url, String user) {
        return url + "|" + user;
    }

    /**
     * getByValue
     *
     * @param value String
     * @return Matching DatabaseType instance
     */
    public static DatabaseType getByValue(String value) {
        String normalizedValue = value == null ? "" : value.trim().toLowerCase(Locale.ENGLISH);
        for (DatabaseType type : values()) {
            if (type.value.equals(normalizedValue)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * current database type is support
     *
     * @param value String
     * @return Boolean
     */
    public static boolean isSupport(String value) {
        return getByValue(value) != UNKNOWN;
    }

    /**
     * shutdownAll
     * Shutdown all data sources in all database types.
     */
    public static void shutdownAll() {
        for (DatabaseType type : values()) {
            type.dataSources.values().forEach(ds -> {
                if (ds instanceof HikariDataSource) {
                    ((HikariDataSource) ds).close();
                }
            });
            type.dataSources.clear();
        }
    }
}


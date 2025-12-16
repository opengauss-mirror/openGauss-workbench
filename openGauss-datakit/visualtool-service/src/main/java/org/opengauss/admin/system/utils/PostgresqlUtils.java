/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PostgresqlUtils
 *
 * @since 2025/11/14
 */
public class PostgresqlUtils {
    private static final Pattern PG_VERSION_PATTERN = Pattern.compile("PostgreSQL\\s((?:\\d+\\.)+[^\\s]*) ");

    /**
     * Get PostgreSQL version
     *
     * @param connection PostgreSQL connection
     * @return PostgreSQL version
     * @throws SQLException SQL exception
     */
    public static String getVersion(Connection connection) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null.");
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT VERSION()")) {
            if (resultSet.next()) {
                String rsString = resultSet.getString("version");
                Matcher matcher = PG_VERSION_PATTERN.matcher(rsString);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }

        throw new SQLException("Not found PostgreSQL version.");
    }

    /**
     * List databases
     *
     * @param connection PostgreSQL connection
     * @return databases
     * @throws SQLException SQL exception
     */
    public static List<String> listDatabases(Connection connection) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null.");
        }

        String sql = "SELECT datname FROM pg_database WHERE datistemplate = false";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            List<String> databases = new ArrayList<>();
            while (resultSet.next()) {
                databases.add(resultSet.getString("datname"));
            }
            return databases;
        }
    }

    /**
     * List schemas
     *
     * @param connection PostgreSQL connection
     * @return schemas list
     * @throws SQLException SQL exception
     */
    public static List<String> listSchemas(Connection connection) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null.");
        }

        String sql = "SELECT schema_name FROM information_schema.schemata";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            List<String> schemas = new ArrayList<>();
            while (resultSet.next()) {
                schemas.add(resultSet.getString("schema_name"));
            }
            return schemas;
        }
    }
}

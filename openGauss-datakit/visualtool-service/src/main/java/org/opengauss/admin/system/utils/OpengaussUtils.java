/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * openGauss utils
 *
 * @since 2025/11/14
 */
public class OpengaussUtils {
    private static final Pattern GS_VERSION_PATTERN = Pattern
            .compile("\\(openGauss(?:-[a-zA-Z]+)?\\s+(\\d+\\.\\d+\\.\\d+(?:-\\S+)?)");

    /**
     * Get the openGauss version
     *
     * @param connection connection
     * @return openGauss version
     * @throws SQLException if a database access error occurs
     */
    public static String getVersion(Connection connection) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null.");
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT VERSION()")) {
            if (resultSet.next()) {
                String rsString = resultSet.getString("version");
                Matcher matcher = GS_VERSION_PATTERN.matcher(rsString);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }

        throw new SQLException("Not found OpenGauss version");
    }

    /**
     * Get the openGauss databases with SQL compatibility
     *
     * @param connection connection
     * @return Map<String, String> openGauss databases with SQL compatibility
     * @throws SQLException if a database access error occurs
     */
    public static Map<String, String> getDatabasesWithSqlCompatibility(Connection connection) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null.");
        }

        String sql = "select datname, datcompatibility from pg_database WHERE datistemplate = false;";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            Map<String, String> databases = new HashMap<>();
            while (resultSet.next()) {
                databases.put(resultSet.getString("datname"), resultSet.getString("datcompatibility"));
            }
            return databases;
        }
    }
}

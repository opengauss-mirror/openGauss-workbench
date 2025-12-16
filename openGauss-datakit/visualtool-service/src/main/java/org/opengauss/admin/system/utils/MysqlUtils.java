/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * MysqlUtils
 *
 * @since 2025/11/14
 */
public class MysqlUtils {
    /**
     * Get mysql version
     *
     * @param connection mysql connection
     * @return mysql version
     * @throws SQLException SQL exception
     */
    public static String getVersion(Connection connection) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null.");
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT VERSION()")) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        throw new SQLException("Not found MySQL version.");
    }

    /**
     * List databases
     *
     * @param connection mysql connection
     * @return database list
     * @throws SQLException SQL exception
     */
    public static List<String> listDatabases(Connection connection) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null.");
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW DATABASES")) {
            List<String> databases = new ArrayList<>();
            while (resultSet.next()) {
                databases.add(resultSet.getString(1));
            }
            return databases;
        }
    }

    /**
     * List tables in a database with pagination
     *
     * @param connection mysql connection
     * @param dbName database name
     * @param pageNum page number
     * @param pageSize page size
     * @return tables
     * @throws SQLException SQL exception
     */
    public static List<String> getDatabaseTablesPage(Connection connection, String dbName, long pageNum, long pageSize)
            throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null.");
        }
        if (dbName == null || dbName.isEmpty()) {
            throw new IllegalArgumentException("Database name is null or empty.");
        }
        if (pageNum <= 0) {
            throw new IllegalArgumentException("Page number must be a positive integer.");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be a positive integer.");
        }

        String sql = "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = ? AND Table_type = 'BASE TABLE' "
                + "LIMIT ? OFFSET ?";

        long offset = (pageNum - 1) * pageSize;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, dbName);
            statement.setLong(2, pageSize);
            statement.setLong(3, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> tables = new ArrayList<>();
                while (resultSet.next()) {
                    tables.add(resultSet.getString("table_name"));
                }
                return tables;
            }
        }
    }

    /**
     * Count tables in a database
     *
     * @param connection mysql connection
     * @param dbName database name
     * @return table count
     * @throws SQLException SQL exception
     */
    public static long countDatabaseTables(Connection connection, String dbName) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection is null.");
        }
        if (dbName == null || dbName.isEmpty()) {
            throw new IllegalArgumentException("Database name is null or empty.");
        }

        String sql = "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? "
                + "and Table_type = 'BASE TABLE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, dbName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        }
        throw new SQLException("Not found tables count in database " + dbName + ".");
    }
}

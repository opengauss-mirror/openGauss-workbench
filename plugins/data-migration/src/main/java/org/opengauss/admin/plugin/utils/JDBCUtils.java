/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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

package org.opengauss.admin.plugin.utils;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.TranscribeReplayNodeInfo;
import org.opengauss.admin.plugin.domain.TranscribeReplayTask;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JDBCUtils
 *
 * @since 2025-01-17
 */
@Slf4j
public class JDBCUtils {
    private static Connection connection = null;
    private static PreparedStatement preparedStatement = null;
    private static ResultSet resultSet = null;

    /**
     * Connect to the database.
     *
     * @param transcribeReplayTask transcribeReplayTask
     * @param nodeInfo nodeInfo
     * @return Connection
     */
    public static Connection getConnection(TranscribeReplayTask transcribeReplayTask,
        TranscribeReplayNodeInfo nodeInfo) {
        List<String> dbName = Arrays.stream(transcribeReplayTask.getDbName().split(";")).collect(Collectors.toList());
        if (dbName.isEmpty()) {
            throw new OpsException("Database name is empty.");
        }
        String[] schemas = dbName.get(0).split(":");
        String url = String.format("jdbc:opengauss://%s:%s/%s?currentSchema=%s", nodeInfo.getIp(), nodeInfo.getPort(),
            schemas[1], schemas[0]);
        try {
            Class.forName("org.opengauss.Driver");
            connection = DriverManager.getConnection(url, nodeInfo.getUsername(),
                nodeInfo.getPassword());
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Get JDBC connection is failed, {}", e.getMessage());
        }
        return connection;
    }

    /**
     * Close the resources.
     */
    public static void close() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
            if (preparedStatement != null) {
                preparedStatement.close();
                preparedStatement = null;
            }
            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Translate the result set into entity class objects.
     *
     * @param rs resultSet
     * @param cc class
     * @return list
     * @throws SQLException SQLException
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     */
    public static <T> List<T> populate(ResultSet rs, Class<T> cc)
        throws SQLException, InstantiationException, IllegalAccessException {
        ResultSetMetaData rsm = rs.getMetaData();
        int colNumber = rsm.getColumnCount();
        List<T> list = new ArrayList<>();
        Field[] fields = cc.getDeclaredFields();

        while (rs.next()) {
            T obj = cc.newInstance();
            for (int i = 1; i <= colNumber; i++) {
                String camelCaseColumnName = toCamelCase(rsm.getColumnName(i));
                setFieldValue(obj, fields, camelCaseColumnName, rs.getObject(i));
            }
            list.add(obj);
        }
        return list;
    }

    private static void setFieldValue(Object obj, Field[] fields, String fieldName, Object value)
        throws IllegalAccessException {
        for (Field filed : fields) {
            if (filed.getName().equals(fieldName)) {
                boolean isAccessible = filed.isAccessible();
                filed.setAccessible(true);
                filed.set(obj, value);
                filed.setAccessible(isAccessible);
                break;
            }
        }
    }

    private static String toCamelCase(String s) {
        StringBuilder result = new StringBuilder();
        boolean isUpper = false;
        for (char c : s.toCharArray()) {
            if (c == '_') {
                isUpper = true;
            } else {
                if (isUpper) {
                    result.append(Character.toUpperCase(c));
                    isUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
}

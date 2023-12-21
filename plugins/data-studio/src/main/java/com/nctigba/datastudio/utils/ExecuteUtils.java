/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ExecuteUtils.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/utils/ExecuteUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.utils;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExecuteUtil
 *
 * @since 2023-12-11
 */
@Slf4j
public class ExecuteUtils {
    /**
     * executeGetMap
     *
     * @param conn conn
     * @param sql sql
     * @param keyList keyList
     * @return List
     * @throws SQLException SQLException
     */
    public static List<Map<String, Object>> executeGetMap(
            Connection conn, String sql, List<String> keyList) throws SQLException {
        log.info("ExecuteUtils executeGetMap sql: " + sql);
        List<Map<String, Object>> list = new ArrayList<>();
        try (
                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();
                for (String s : keyList) {
                    map.put(buildConvert(s), resultSet.getString(s));
                }
                list.add(map);
            }
        }
        log.info("ExecuteUtils executeGetMap list: " + list);
        return list;
    }

    /**
     * executeGetOne
     *
     * @param conn conn
     * @param sql sql
     * @return Object
     * @throws SQLException SQLException
     */
    public static Object executeGetOne(Connection conn, String sql) throws SQLException {
        try (
                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            if (resultSet.next()) {
                return resultSet.getObject(1);
            }
        }
        return null;
    }

    /**
     * execute
     *
     * @param conn conn
     * @param sql sql
     * @throws SQLException SQLException
     */
    public static void execute(Connection conn, String sql) throws SQLException {
        log.info("ExecuteUtils execute sql: " + sql);
        try (
                Statement statement = conn.createStatement()
        ) {
            statement.execute(sql);
            log.info("ExecuteUtils execute end: ");
        }
    }

    /**
     * buildConvert
     *
     * @param str str
     * @return String
     */
    public static String buildConvert(String str) {
        String[] words = str.split("_");
        for (int i = 1; i < words.length; i++) {
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(word);
        }
        return result.toString();
    }
}
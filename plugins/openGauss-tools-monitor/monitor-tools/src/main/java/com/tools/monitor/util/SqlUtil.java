/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util;

import cn.hutool.core.util.ObjectUtil;
import com.tools.monitor.exception.ParamsException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

/**
 * SqlUtil
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class SqlUtil {
    public static final String SQL_REGEX =
            "insert|alter|delete|update|drop|exec|chr|mid|master|truncate|char|declare|create|into";

    /**
     * DQL_SQL
     */
    public static final String DQL_SQL = "select";

    public static final String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    /**
     * escapeOrderBySql
     *
     * @param value value
     * @return String
     */
    public static String escapeOrderBySql(String value) {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            throw new ParamsException("参数不符合规范，不能进行查询");
        }
        return value;
    }

    /**
     * contain
     *
     * @param key key
     * @return Boolean
     */
    public static Boolean contain(String key) {
        if (key.equalsIgnoreCase("query_start") || key.equalsIgnoreCase("ckpt_redo_point")) {
            return true;
        }
        if (key.equalsIgnoreCase("last_updated")) {
            return true;
        }
        if (key.equalsIgnoreCase("xact_start")) {
            return true;
        }
        if (key.equalsIgnoreCase("query")) {
            return true;
        }
        return false;
    }

    /**
     * isValidOrderBySql
     *
     * @param value value
     * @return boolean
     */
    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }

    /**
     * filterKeyword
     *
     * @param value value
     */
    public static void filterKeyword(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords) {
            if (StringUtils.indexOfIgnoreCase(value, sqlKeyword) > -1) {
                throw new ParamsException("参数存在SQL注入风险");
            }
        }
    }

    /**
     * checkDql
     *
     * @param value value
     * @return String
     */
    public static String checkDql(String value) {
        String result = value.toLowerCase(Locale.ROOT);
        if (result.contains("；")) {
            return "非法语句";
        }
        String[] strings = result.split(";");
        if (strings.length > 1) {
            return "非法语句";
        }
        List<String> list = Arrays.asList(result.split("\\s"));
        String select = list.stream().filter(item -> item.equals(DQL_SQL)).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(select)) {
            return "非查询语句";
        }
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords) {
            for (String str : list) {
                if (sqlKeyword.equals(str)) {
                    return "只支持查询语句";
                }
            }
        }
        return "";
    }

    /**
     * execute
     *
     * @param list       list
     * @param connection connection
     */
    private static void execute(List<String> list, Connection connection) {
        try {
            Statement statement = connection.createStatement();
            for (String str : list) {
                statement.addBatch(str);
            }
            statement.executeBatch();
        } catch (SQLException exception) {
            log.error("execute-->{}", exception.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException exception) {
                    log.error("execute-->{}", exception.getMessage());
                }
            }
        }
    }
}

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
 * JdbcUtil.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/utils/ops/JdbcUtil.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.utils.ops;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcInfo;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.springframework.util.ObjectUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lhf
 * @date 2023/1/13 13:43
 **/
@Slf4j
public class JdbcUtil {
    private static Pattern p = Pattern.compile("jdbc:(?<dbType>\\w+):.*((//)|@)(?<ip>.+):(?<port>\\d+)");

    public static JdbcInfo parseUrl(String url) {
        if (StrUtil.isEmpty(url)) {
            throw new OpsException("JDBC URL information does not exist");
        }

        JdbcInfo jdbcInfo = new JdbcInfo();

        Matcher m = p.matcher(url);
        if (m.find()) {
            String dbType = m.group("dbType");
            if (StrUtil.isEmpty(dbType)) {
                throw new OpsException("Error parsing JDBC URL, database type not found");
            }
            jdbcInfo.setDbType(DbTypeEnum.typeOf(dbType));

            String ip = m.group("ip");
            if (StrUtil.isEmpty(ip)) {
                throw new OpsException("Error parsing JDBC URL, database ip not found");
            }
            jdbcInfo.setIp(ip);

            String port = m.group("port");
            if (StrUtil.isEmpty(port)) {
                throw new OpsException("Error parsing JDBC URL, database port not found");
            }
            jdbcInfo.setPort(port);
        }

        return jdbcInfo;
    }

    public static Connection getConnection(String url, String username, String password) {
        Connection connection = null;
        try {
            JdbcInfo jdbcInfo = parseUrl(url);
            Class.forName(jdbcInfo.getDbType().getDriverClass());

            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            log.error("jdbc failed to get connection", e);
            throw new OpsException("jdbc failed to get connection");
        }
        return connection;
    }

    /**
     * Select string value string.
     *
     * @param connection the connection
     * @param selectSql  the select sql
     * @param key        the key
     * @return the string
     * @throws SQLException the sql exception
     */
    public static String selectStringValue(Connection connection, String selectSql, String key) throws SQLException {
        String value = "";
        if (connection != null) {
            try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(selectSql)) {
                if (rs.next()) {
                    value = rs.getString(key);
                }
            } catch (SQLException e) {
                throw e;
            }
        }
        return value;
    }

    /**
     * judge database user is admin
     *
     * @param ip ip
     * @param port port
     * @param user user
     * @param password password
     * @return result boolean
     */
    public static boolean judgeSystemAdmin(String ip, String port, String user, String password) {
        boolean isAdmin = false;
        Connection pgConnection = null;
        try {
            String opengaussUrl = "jdbc:opengauss://" + ip + ":" + port + "/postgres";
            pgConnection = JdbcUtil.getConnection(opengaussUrl, user, password);
            String permissionStr = JdbcUtil.selectStringValue(pgConnection,
                "select rolsystemadmin from pg_roles where rolname= '" + user + "'", "rolsystemadmin");
            isAdmin = permissionStr.equals("t") || permissionStr.equals("1");
        } catch (OpsException | SQLException e) {
            log.error("get connection or sql execute failed.");
        } finally {
            closeConnection(pgConnection);
        }
        return isAdmin;
    }

    /**
     * get openGauss jdbc url
     *
     * @param host host ip
     * @param port port
     * @param database database name
     * @param schema schema name
     * @return String url
     */
    public static String getOpengaussJdbcUrl(String host, String port, String database, String schema) {
        StringBuilder stringBuilder = new StringBuilder("jdbc:opengauss://");
        stringBuilder.append(host).append(":").append(port).append("/").append(database);

        if (!ObjectUtils.isEmpty(schema)) {
            stringBuilder.append("?currentSchema=").append(schema);
        }

        return stringBuilder.toString();
    }

    /**
     * determine whether execute query has result
     *
     * @param url jdbc url
     * @param sql sql
     * @param username database user name
     * @param password database user password
     * @return boolean
     */
    public static boolean hasResultSetByExecuteQuery(String url, String sql, String username, String password) {
        try (Connection connection = getConnection(url, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next();
        } catch (SQLException e) {
            log.error("Failed to execute the query. SQL: {}. ERROR: {}", sql, e.getMessage());
        }

        return false;
    }

    /**
     * close connection
     *
     * @param connection connection
     */
    public static void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("close connection fail.");
        }
    }
}

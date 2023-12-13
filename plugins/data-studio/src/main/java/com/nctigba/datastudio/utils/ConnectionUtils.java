/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.nctigba.datastudio.utils.SecretUtils.desEncrypt;

/**
 * ConnectionUtils
 *
 * @since 2023-6-26
 */
public class ConnectionUtils {
    /**
     * get connect
     *
     * @param jdbcUrl  jdbcUrl
     * @param username username
     * @param password password
     * @return Connection
     * @throws SQLException SQLException
     */
    public static Connection connectGet(String jdbcUrl, String username, String password)
            throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, desEncrypt(password));
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.util;

import java.sql.Connection;
import java.sql.DriverManager;

import static com.nctigba.datastudio.util.SecretUtil.desEncrypt;

public final class ConnectionUtils {
    public static Connection connectGet(String jdbcUrl, String username, String password) throws Exception {
        return DriverManager.getConnection(jdbcUrl, username, desEncrypt(password));
    }
}

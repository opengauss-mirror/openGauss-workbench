package com.nctigba.datastudio.util;

import java.sql.Connection;
import java.sql.DriverManager;

import static com.nctigba.datastudio.util.SecretUtil.desEncrypt;

public final class ConnectionUtils {
    public static Connection connectGet(String jdbcUrl, String username, String password) throws Exception {
        String driver = "com.huawei.opengauss.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(jdbcUrl, username, desEncrypt(password));
    }
}

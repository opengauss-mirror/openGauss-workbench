/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.utils;

import cn.hutool.core.util.StrUtil;

import org.opengauss.exception.ApiTestException;
import org.opengauss.global.AppConfig;
import org.opengauss.global.AppConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;

/**
 * openGauss JDBC utility used by API tests to set up and clean up test data.
 * All connection parameters are read from application.yml (database.jdbc.opengauss).
 *
 * @since 2026/08/18
 */
public final class OpenGaussJdbcUtils {
    private static final String DRIVER_CLASS_NAME = "org.opengauss.Driver";
    private static final String URL_TEMPLATE = "jdbc:opengauss://%s:%d/%s?currentSchema=public";

    static {
        try {
            Class.forName(DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new ApiTestException("openGauss JDBC driver is not available.", e);
        }
    }

    private OpenGaussJdbcUtils() {
    }

    /**
     * build a JDBC connection from the database.jdbc.opengauss configuration
     *
     * @return openGauss connection
     */
    public static Connection getConnection() {
        AppConfig.Database.Jdbc.OpenGauss openGauss =
                AppConfigLoader.getAppConfig().getDatabase().getJdbc().getOpengauss();
        if (openGauss == null || StrUtil.isEmpty(openGauss.getHostIp())) {
            throw new ApiTestException("database.jdbc.opengauss is not configured in application.yml.");
        }
        String url = String.format(Locale.ROOT, URL_TEMPLATE,
                openGauss.getHostIp(), openGauss.getPort(), openGauss.getDatabase());
        try {
            return DriverManager.getConnection(url, openGauss.getUsername(), openGauss.getPassword());
        } catch (SQLException e) {
            throw new ApiTestException("Failed to connect to openGauss database.", e);
        }
    }
}

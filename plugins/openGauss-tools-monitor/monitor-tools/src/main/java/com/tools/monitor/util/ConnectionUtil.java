/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util;

import cn.hutool.core.util.StrUtil;
import com.tools.monitor.config.ZabbixConfig;
import com.tools.monitor.entity.SysConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;

/**
 * ConnectionUtil
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class ConnectionUtil {
    /**
     * getConnection
     *
     * @param sysConfig sysConfig
     * @return String
     */
    public static String getConnection(SysConfig sysConfig) {
        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    Class.forName(sysConfig.getDriver());
                    DriverManager.getConnection(sysConfig.getUrl(), sysConfig.getUserName(), sysConfig.getPassword());
                } catch (SQLException | ClassNotFoundException exception) {
                    return "Please check the data source information";
                }
                return "";
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        try {
            Future<String> future = threadPoolExecutor.submit(task);
            String obj = future.get(500 * ZabbixConfig.getTimeout(), TimeUnit.MILLISECONDS);
            if (StrUtil.isNotBlank(obj)) {
                return obj;
            }
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            return "Incorrect database connection information!";
        } finally {
            threadPoolExecutor.shutdown();
        }
        return "";
    }

    /**
     * gainConnenction
     *
     * @param sysConfig sysConfig
     * @return Connection
     */
    public static Connection gainConnenction(SysConfig sysConfig) {
        Connection connection = null;
        try {
            Class.forName(sysConfig.getDriver());
            connection =
                    DriverManager.getConnection(sysConfig.getUrl(), sysConfig.getUserName(), sysConfig.getPassword());
        } catch (ClassNotFoundException | SQLException exception) {
            log.error("gainConnection fail{}", exception.getMessage());
        }
        return connection;
    }
}
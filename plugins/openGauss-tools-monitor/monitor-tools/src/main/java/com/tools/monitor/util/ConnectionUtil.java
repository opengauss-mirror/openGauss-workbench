package com.tools.monitor.util;

import cn.hutool.core.util.StrUtil;
import com.tools.monitor.config.ZabbixConfig;
import com.tools.monitor.entity.SysConfig;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;

/**
 * ConnectionUtil
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class ConnectionUtil {
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

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<String> future = executorService.submit(task);
            String obj = future.get(500 * ZabbixConfig.getTimeout(), TimeUnit.MILLISECONDS);
            if (StrUtil.isNotBlank(obj)) {
                return obj;
            }
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            return "Incorrect database connection information!";
        } finally {
            executorService.shutdown();
        }
        return "";
    }

    public static Connection gainConnection(SysConfig sysConfig) {
        Connection connection = null;
        try {
            Class.forName(sysConfig.getDriver());
            connection = DriverManager.getConnection(sysConfig.getUrl(), sysConfig.getUserName(), sysConfig.getPassword());
        } catch (ClassNotFoundException | SQLException exception) {
            log.error("gainConnection fail{}", exception.getMessage());
        }
        return connection;
    }
}
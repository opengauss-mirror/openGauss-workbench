/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * ConnectionUtils
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class ConnectionUtils {
    private static final int CONNECTION_TIMEOUT = 2; // 连接超时时间（秒）
    private static ThreadPoolTaskExecutor threadPoolExecutor = MonitSpringUtils.getStr("threadPoolTaskExecutor");

    /**
     * getConnection
     *
     * @param driver driver
     * @param url url
     * @param user user
     * @param passWord passWord
     * @return Connection
     */
    public static Connection getConnection(String driver, String url, String user, String passWord) {
        CompletableFuture<Connection> future = CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            try {
                Class.forName(driver);
                connection = DriverManager.getConnection(url, user, passWord);
                log.info("Successfully obtained connection information");
            } catch (SQLException | ClassNotFoundException exception) {
                throw new ServiceException("Please check the database connection information");
            }
            return connection;
        }, threadPoolExecutor);
        try {
            return future.get(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException exception) {
            throw new ServiceException("Connection timeout or incorrect database connection information");
        }
    }

    /**
     * executeWithTimeout
     *
     * @param task    task
     * @param timeout timeout
     * @param unit    unit
     * @throws TimeoutException TimeoutException
     */
    public static void executeWithTimeout(Runnable task, long timeout, TimeUnit unit) throws TimeoutException {
        Future<?> future = threadPoolExecutor.submit(task);
        try {
            future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException ex) {
            log.error(ex.getMessage());
        } finally {
            future.cancel(true); // Cancel the task if it hasn't completed within the given timeout
        }
    }

    /**
     * executeWithTimeout
     *
     * @param task    task
     * @param timeout timeout
     * @param unit    unit
     * @param <T>     t
     * @return t
     * @throws TimeoutException TimeoutException
     */
    public static <T> T executeWithTimeout(Callable<T> task, long timeout, TimeUnit unit) throws TimeoutException {
        Future<T> future = threadPoolExecutor.submit(task);
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException ex) {
            log.error(ex.getMessage());
        } finally {
            future.cancel(true); // Cancel the task if it hasn't completed within the given timeout
        }
        throw new ServiceException("Task execution timed out");
    }
}

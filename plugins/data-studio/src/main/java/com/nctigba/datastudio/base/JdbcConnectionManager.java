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
 *  JdbcConnectionManager.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/base/JdbcConnectionManager.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.base;

import com.alibaba.druid.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JdbcConnectionManager
 */
@Component
public class JdbcConnectionManager extends Thread {

    private static final Logger log = LoggerFactory.getLogger(JdbcConnectionManager.class);
    private static final long CONN_CHECK_TIME = 5 * 60 * 1000L;
    public static final long CONN_TIMEOUT = 3 * 60 * 1000L;
    private static ConcurrentHashMap<String, LinkedBlockingQueue<ConnectionWrapper>> JDBC_POOL =
            new ConcurrentHashMap<>();
    private static Map<String, Integer> CONNECTION_NUMLIMIT = new HashMap<>();
    private static final Integer MAX_CONNECTION_NUM = 30;

    /**
     * A timed task to detect connections in the connection pool that have been unused for a long time
     */
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(CONN_CHECK_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("sleep error", e);
            }
            int size = 0;
            for (String key : JDBC_POOL.keySet()) {
                LinkedBlockingQueue<ConnectionWrapper> queue = JDBC_POOL.get(key);
                log.info("Start checking for expired connections in the connection pool, size:[{}] key:[{}]",
                        queue.size(), key);
                synchronized (JdbcConnectionManager.class) {
                    Integer num = 0;
                    while (queue.peek() != null && queue.peek().checkTimeOut()) {
                        ConnectionWrapper ConnectionWrapper = queue.poll();
                        try {
                            log.info("Close idle JDBC connections :{}", key);
                            ConnectionWrapper.closeConnection();
                        } catch (SQLException e) {
                            log.error("Failed to close the connection", e);
                        }
                        num--;
                    }
                    CONNECTION_NUMLIMIT.put(key, CONNECTION_NUMLIMIT.get(key) + num);
                }
                size += queue.size();
            }
            log.info("The current verification is complete, and the total number of connections in the connection "
                    + "pool is [{}]", size);
        }
    }

    /**
     * get jdbc connection
     *
     * @param jdbcUrl jdbcUrl
     * @param username username
     * @param password password
     * @return ConnectionWrapper
     */
    public static ConnectionWrapper getConnection(String jdbcUrl, String username, String password) {
        try {
            ConnectionWrapper connectionWrapper = null;
            String key = username + "-" + jdbcUrl;
            LinkedBlockingQueue<ConnectionWrapper> queue = JDBC_POOL.get(key);
            synchronized (JdbcConnectionManager.class) {
                if (queue == null) {
                    queue = new LinkedBlockingQueue<>();
                    JDBC_POOL.put(key, queue);
                    CONNECTION_NUMLIMIT.put(key, 0);
                }
                Integer limitNum = CONNECTION_NUMLIMIT.get(key);
                if (!queue.isEmpty()) {
                    ConnectionWrapper conn = queue.take();
                    return conn.isValid(3) ? conn : createConnnection(jdbcUrl, username, password);
                }
                if (limitNum >= MAX_CONNECTION_NUM) {
                    ConnectionWrapper conn = JDBC_POOL.get(key).take();
                    connectionWrapper = conn.isValid(3) ? conn : createConnnection(jdbcUrl, username, password);
                } else {
                    connectionWrapper = createConnnection(jdbcUrl, username, password);
                    CONNECTION_NUMLIMIT.put(key, CONNECTION_NUMLIMIT.get(key) + 1);
                }
            }
            return connectionWrapper;
        } catch (Exception e) {
            log.error("getConnection error", e);
        }
        return null;
    }

    /**
     * create jdbc connnection
     *
     * @param jdbcUrl jdbcUrl
     * @param username username
     * @param password password
     * @return ConnectionWrapper
     * @throws SQLException e
     * @throws ClassNotFoundException e
     */
    private static ConnectionWrapper createConnnection(String jdbcUrl, String username,
                                                       String password) throws SQLException, ClassNotFoundException {
        String key = username + "-" + jdbcUrl;
        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        Connection conn = DriverManager.getConnection(jdbcUrl, properties);
        return new ConnectionWrapper(conn, JDBC_POOL.get(key));
    }
}


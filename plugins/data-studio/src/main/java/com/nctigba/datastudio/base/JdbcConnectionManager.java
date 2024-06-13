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

    // 5分钟检测一次连接
    private static final long CONN_CHECK_TIME = 5 * 60 * 1000L;

    // jdbc连接超时时间(3分钟未使用中断)
    public static final long CONN_TIMEOUT = 3 * 60 * 1000L;

    //总的jdbc连接池
    private static ConcurrentHashMap<String, LinkedBlockingQueue<ConnectionWrapper>> JDBC_POOL =
            new ConcurrentHashMap<>();

    //一个连接最大线程数
    private static Map<String, Integer> CONNECTION_NUMLIMIT = new HashMap<>();

    //最大连接数
    private static final Integer MAX_CONNECTION_NUM = 30;

    /**
     * 定时任务检测连接池里面长时间未使用的连接
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
                log.info("---------------------开始校验连接池中是否有过期连接，size:【{}】 key:【{}】 ---------------------",
                        queue.size(), key);
                synchronized (JdbcConnectionManager.class) {
                    Integer num = 0;
                    // 头部的肯定是时间最早的，只需要判断头部的时间是否超时即可
                    while (queue.peek() != null && queue.peek().checkTimeOut()) {
                        ConnectionWrapper ConnectionWrapper = queue.poll();
                        try {
                            log.info("关闭空闲jdbc连接:{}", key);
                            ConnectionWrapper.closeConnection();
                        } catch (SQLException e) {
                            log.error("关闭连接失败", e);
                        }
                        num--;
                    }
                    CONNECTION_NUMLIMIT.put(key, CONNECTION_NUMLIMIT.get(key) + num);
                }
                size += queue.size();
            }
            log.info("----------------------本次校验完毕，连接池总连接数量为【{}】----------------------", size);
        }
    }

    /**
     * 获取jdbc连接
     *
     * @param jdbcUrl
     * @param username
     * @param password
     * @return
     */
    public static ConnectionWrapper getConnection(String jdbcUrl, String username, String password) {
        try {
            ConnectionWrapper connectionWrapper = null;
            String key = username + "-" + jdbcUrl;
            // 从总的连接池获取
            LinkedBlockingQueue<ConnectionWrapper> queue = JDBC_POOL.get(key);
            synchronized (JdbcConnectionManager.class) {
                if (queue == null) {
                    log.info("初始化key为【{}】的连接队列", key);
                    queue = new LinkedBlockingQueue<>();
                    JDBC_POOL.put(key, queue);
                    CONNECTION_NUMLIMIT.put(key, 0);
                }
                Integer limitNum = CONNECTION_NUMLIMIT.get(key);
                log.info("key为【{}】对应的limitNum = 【{}】", key, limitNum);
                if (!queue.isEmpty()) {
                    log.info("key为【{}】的连接池中有可用的连接，直接获取", key);
                    ConnectionWrapper conn = queue.take();
                    return conn.isValid(3) ? conn : createConnnection(jdbcUrl, username, password);
                }
                if (limitNum >= MAX_CONNECTION_NUM) {
                    log.info("key为【{}】超过了最大连接数，进入等待队列", key);
                    ConnectionWrapper conn = JDBC_POOL.get(key).take();
                    connectionWrapper = conn.isValid(3) ? conn : createConnnection(jdbcUrl, username, password);
                } else {
                    log.info("key为【{}】无可用的连接，开始创建连接", key);
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
     * 创建jdbc连接
     *
     * @param jdbcUrl
     * @param username
     * @param password
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
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


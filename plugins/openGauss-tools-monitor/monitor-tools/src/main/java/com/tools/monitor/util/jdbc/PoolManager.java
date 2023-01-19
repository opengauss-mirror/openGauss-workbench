/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.tools.monitor.entity.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

/**
 * PoolManager
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class PoolManager {
    /**
     * map
     */
    static Map<String, DruidDataSource> zabbixMap = new HashMap<>();

    private static Lock poolLock = new ReentrantLock();

    private static Lock pooldelLock = new ReentrantLock();

    /**
     * getJdbcConnectionPool
     *
     * @param source source
     * @return DruidDataSource
     */
    public static DruidDataSource getMonitorPool(DataSource source) {
        if (zabbixMap.containsKey(source.getId())) {
            return zabbixMap.get(source.getId());
        } else {
            poolLock.lock();
            try {
                log.info(Thread.currentThread().getName() + "get key");
                if (!zabbixMap.containsKey(source.getId())) {
                    DruidDataSource monitorSource = new DruidDataSource();
                    monitorSource.setInitialSize(5);
                    monitorSource.setMaxActive(20);
                    monitorSource.setMaxWait(60000);
                    monitorSource.setTimeBetweenEvictionRunsMillis(60000);
                    monitorSource.setMinEvictableIdleTimeMillis(300000);
                    monitorSource.setMaxEvictableIdleTimeMillis(900000);
                    monitorSource.setName("zabbix");
                    monitorSource.setUrl(source.getUrl());
                    monitorSource.setUsername(source.getUsername());
                    monitorSource.setPassword(source.getPassword());
                    monitorSource.setDriverClassName(source.getDriver());
                    monitorSource.setConnectionErrorRetryAttempts(3);
                    monitorSource.setBreakAfterAcquireFailure(true);
                    zabbixMap.put(source.getId(), monitorSource);
                    log.info("Druid connection monitorPool :{}", source.getName());
                }
                return zabbixMap.get(source.getId());
            } finally {
                poolLock.unlock();
            }
        }
    }

    /**
     * removeJdbcConnectionPool
     *
     * @param id id
     */
    public static void removeJdbcConnectionPool(String id) {
        pooldelLock.lock();
        try {
            DruidDataSource druidDataSource = zabbixMap.get(id);
            if (druidDataSource != null) {
                druidDataSource.close();
                zabbixMap.remove(id);
            }
        } finally {
            pooldelLock.unlock();
        }
    }

    /**
     * getMonitorConnection
     *
     * @param source source
     * @return DruidPooledConnection
     */
    public static DruidPooledConnection getMonitorConnection(DataSource source) {
        DruidDataSource dataSource = PoolManager.getMonitorPool(source);
        DruidPooledConnection druidPooledConnection = null;
        try {
            druidPooledConnection = dataSource.getConnection();
        } catch (SQLException exception) {
            log.error("getMonitorConnection fail-->{}", exception.getMessage());
        }
        return druidPooledConnection;
    }
}

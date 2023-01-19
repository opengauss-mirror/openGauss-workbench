package com.tools.monitor.util.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.tools.monitor.entity.DataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PoolManager
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class PoolManager {

    private static Lock lock = new ReentrantLock();

    private static Lock deleteLock = new ReentrantLock();

    static Map<String, DruidDataSource> map = new HashMap<>();

    public static DruidDataSource getJdbcConnectionPool(DataSource ds) {
        if (map.containsKey(ds.getId())) {
            return map.get(ds.getId());
        } else {
            lock.lock();
            try {
                log.info(Thread.currentThread().getName() + "获取锁");
                if (!map.containsKey(ds.getId())) {
                    DruidDataSource druidDataSource = new DruidDataSource();
                    druidDataSource.setInitialSize(5);
                    druidDataSource.setMaxActive(20);
                    druidDataSource.setMaxWait(60000);
                    druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
                    druidDataSource.setMinEvictableIdleTimeMillis(300000);
                    druidDataSource.setMaxEvictableIdleTimeMillis(900000);
                    druidDataSource.setName("zabbix");
                    druidDataSource.setUrl(ds.getUrl());
                    druidDataSource.setUsername(ds.getUsername());
                    druidDataSource.setPassword(ds.getPassword());
                    druidDataSource.setDriverClassName(ds.getDriver());
                    druidDataSource.setConnectionErrorRetryAttempts(3);       //失败后重连次数
                    druidDataSource.setBreakAfterAcquireFailure(true);

                    map.put(ds.getId(), druidDataSource);
                    log.info("create Druid pool success：{}", ds.getName());
                }
                return map.get(ds.getId());
            } catch (Exception e) {
                return null;
            } finally {
                lock.unlock();
            }
        }
    }

    public static void removeJdbcConnectionPool(String id) {
        deleteLock.lock();
        try {
            DruidDataSource druidDataSource = map.get(id);
            if (druidDataSource != null) {
                druidDataSource.close();
                map.remove(id);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            deleteLock.unlock();
        }

    }

    public static DruidPooledConnection getPooledConnection(DataSource ds) throws SQLException {
        DruidDataSource pool = PoolManager.getJdbcConnectionPool(ds);
        DruidPooledConnection connection = pool.getConnection();
        return connection;
    }
}

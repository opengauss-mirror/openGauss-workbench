/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 */

package org.opengauss.jsch.pool.utils;

import com.jcraft.jsch.Session;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.opengauss.jsch.pool.ProgressCallback;
import org.opengauss.jsch.pool.config.SessionConfig;

import java.text.DecimalFormat;

/**
 * JschUtils
 *
 * @author: wangchao
 * @Date: 2025/8/4 14:10
 * @since 7.0.0-RC2
 **/
@Slf4j
public class JschUtils {
    /**
     * create default session pool config
     *
     * @return session pool config
     */
    public static GenericObjectPoolConfig<Session> createDefaultPoolConfig() {
        GenericObjectPoolConfig<Session> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(8);
        config.setMaxIdle(4);
        config.setMinIdle(2);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setBlockWhenExhausted(true);
        config.setMaxWaitMillis(5000);
        return config;
    }

    /**
     * create high performance session pool config
     *
     * @return session pool config
     */
    public static GenericObjectPoolConfig<Session> createHighPerfPoolConfig() {
        GenericObjectPoolConfig<Session> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(20);
        config.setMaxIdle(10);
        config.setMinIdle(5);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setTimeBetweenEvictionRunsMillis(30000);
        config.setMinEvictableIdleTimeMillis(600000);
        return config;
    }

    /**
     * create console progress callback
     *
     * @param operationName operation name
     * @return progress callback
     */
    public static ProgressCallback createConsoleProgressCallback(String operationName) {
        return new ProgressCallback() {
            private static final ThreadLocal<DecimalFormat> DF = ThreadLocal.withInitial(
                () -> new DecimalFormat("#.00"));
            private long lastLogTime = 0L;
            private String percent = "0.00";

            @Override
            public String progress(long current, long total) {
                long now = System.currentTimeMillis();
                if (now - lastLogTime > 1000) {
                    percent = DF.get().format(((double) current) / ((double) total) * 100.0);
                    log.info("{} progress: {}%", operationName, percent);
                    lastLogTime = now;
                }
                return percent;
            }

            @Override
            public void completed() {
                log.info("{} completed", operationName);
            }

            @Override
            public void failed(Throwable t) {
                log.error("{} failed: {}", operationName, t.getMessage());
            }
        };
    }

    /**
     * create basic session config
     *
     * @param host host
     * @param username username
     * @param password password
     * @return session config
     */
    public static SessionConfig createBasicConfig(String host, String username, String password) {
        return new SessionConfig(host, username, password);
    }

    /**
     * create key based session config
     *
     * @param host host
     * @param username username
     * @param privateKey private key
     * @return session config
     */
    public static SessionConfig createKeyBasedConfig(String host, String username, String privateKey) {
        return new SessionConfig(host, username, null).withPrivateKey(privateKey);
    }
}

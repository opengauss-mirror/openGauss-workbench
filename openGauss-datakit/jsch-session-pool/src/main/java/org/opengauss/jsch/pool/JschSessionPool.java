/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 */

package org.opengauss.jsch.pool;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.JSch;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.opengauss.jsch.pool.config.SessionConfig;
import org.opengauss.jsch.pool.exception.JschPoolException;
import org.opengauss.jsch.pool.records.HostUserKey;
import org.opengauss.jsch.pool.utils.JschUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JschSessionPool session pool ,support multi-host ,multi-user multi session
 *
 * @author: wangchao
 * @Date: 2025/8/4 12:21
 * @since 7.0.0-RC2
 **/
public class JschSessionPool {
    private static final Logger logger = LoggerFactory.getLogger(JschSessionPool.class);
    private static final Map<HostUserKey, GenericObjectPool<Session>> POOL_MAP = new ConcurrentHashMap<>();
    private static final GenericObjectPoolConfig<Session> DEFAULT_POOL_CONFIG = JschUtils.createDefaultPoolConfig();

    /**
     * create session pool
     *
     * @param config session config
     * @return session pool
     */
    public static synchronized GenericObjectPool<Session> getOrCreatePool(SessionConfig config) {
        HostUserKey key = config.getKey();
        GenericObjectPool<Session> pool = POOL_MAP.get(key);
        if (pool == null) {
            GenericObjectPoolConfig<Session> poolConfig = config.getPoolConfig() != null
                ? config.getPoolConfig()
                : DEFAULT_POOL_CONFIG;
            pool = new GenericObjectPool<>(new SessionFactory(config), poolConfig);
            POOL_MAP.put(key, pool);
            preloadPool(pool, poolConfig.getMinIdle());
        }
        return pool;
    }

    /**
     * preload pool
     *
     * @param pool session pool
     * @param minIdle min idle
     */
    private static void preloadPool(GenericObjectPool<Session> pool, int minIdle) {
        Session[] sessions = new Session[minIdle];
        try {
            for (int i = 0; i < minIdle; i++) {
                sessions[i] = pool.borrowObject();
            }
        } catch (Exception e) {
            logger.warn("Preloading connection pool failed", e);
        } finally {
            for (Session session : sessions) {
                if (session != null) {
                    try {
                        pool.returnObject(session);
                    } catch (Exception e) {
                        logger.warn("Preloading connection pool,when returnObject failed {}", e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * close all pools
     */
    public static synchronized void closeAllPools() {
        for (Map.Entry<HostUserKey, GenericObjectPool<Session>> entry : POOL_MAP.entrySet()) {
            entry.getValue().close();
        }
        POOL_MAP.clear();
    }

    /**
     * close pool
     *
     * @param key host user key
     */
    public static synchronized void closePool(HostUserKey key) {
        GenericObjectPool<Session> pool = POOL_MAP.remove(key);
        if (pool != null) {
            pool.close();
        }
    }

    /**
     * borrow session
     *
     * @param config session config
     * @return session
     * @throws JschPoolException borrow exception
     */
    public static Session borrowSession(SessionConfig config) throws JschPoolException {
        GenericObjectPool<Session> pool = getOrCreatePool(config);
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            pool.clear();
            throw new JschPoolException(
                "Failed to borrow session from pool [" + config.getHost() + ":" + config.getUsername() + "] caused by "
                    + e.getMessage(), e);
        }
    }

    /**
     * return session
     *
     * @param config session config
     * @param session session
     */
    public static void returnSession(SessionConfig config, Session session) {
        if (session == null) {
            return;
        }
        try {
            GenericObjectPool<Session> pool = POOL_MAP.get(config.getKey());
            if (pool != null) {
                pool.returnObject(session);
            } else {
                destroySession(session);
            }
        } catch (Exception e) {
            logger.error("Failed to return session to pool", e);
            destroySession(session);
        }
    }

    /**
     * destroy session
     *
     * @param session session
     */
    private static void destroySession(Session session) {
        try {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        } catch (Exception e) {
            logger.debug("Error destroying SFTP channel", e);
        }
    }

    /**
     * get pool stats
     *
     * @return pool stats
     */
    public static String getPoolStats() {
        StringBuilder sb = new StringBuilder("SSH Session Pool Statistics:\n");
        sb.append("========================================\n");
        for (Map.Entry<HostUserKey, GenericObjectPool<Session>> entry : POOL_MAP.entrySet()) {
            HostUserKey key = entry.getKey();
            GenericObjectPool<Session> pool = entry.getValue();
            sb.append(
                String.format(Locale.getDefault(), "Host: %s:%d, User: %s, AuthType: %s, Channel: %s\n", key.host(),
                    key.port(), key.username(), key.authType(), key.channelType()));
            sb.append(String.format(Locale.getDefault(), "  Active: %d, Idle: %d, Waiters: %d\n", pool.getNumActive(),
                pool.getNumIdle(), pool.getNumWaiters()));
            sb.append(String.format(Locale.getDefault(), "  Created: %d, Destroyed: %d\n", pool.getCreatedCount(),
                pool.getDestroyedCount()));
            sb.append("----------------------------------------\n");
        }
        return sb.toString();
    }

    /**
     * session factory
     */
    private static class SessionFactory extends BasePooledObjectFactory<Session> {
        private final SessionConfig config;

        /**
         * session factory
         *
         * @param config session config
         */
        private SessionFactory(SessionConfig config) {
            this.config = config;
        }

        @Override
        public Session create() throws JSchException {
            JSch jsch = new JSch();
            if (config.getPrivateKey() != null) {
                jsch.addIdentity(config.getPrivateKey());
            }
            if (config.getKnownHosts() != null) {
                jsch.setKnownHosts(config.getKnownHosts());
            }
            Session session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
            session.setPassword(config.getPassword());
            Properties sessionConfig = new Properties();
            sessionConfig.put("StrictHostKeyChecking", config.getKnownHosts() != null ? "yes" : "no");
            session.setConfig(sessionConfig);
            session.setTimeout(config.getTimeout());
            session.connect();
            return session;
        }

        @Override
        public void destroyObject(PooledObject<Session> p, DestroyMode destroyMode) throws Exception {
            super.destroyObject(p, destroyMode);
        }

        @Override
        public boolean validateObject(PooledObject<Session> p) {
            Session session = p.getObject();
            return session.isConnected();
        }

        @Override
        public PooledObject<Session> wrap(Session session) {
            return new DefaultPooledObject<>(session);
        }
    }
}

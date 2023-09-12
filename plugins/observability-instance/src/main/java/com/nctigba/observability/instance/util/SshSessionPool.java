/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.util;

import static com.nctigba.observability.instance.util.SshSession.connect;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opengauss.admin.common.exception.CustomException;

import cn.hutool.core.util.StrUtil;

/**
 * SshSessionPool
 *
 * @author liupengfei
 * @since 2023/8/11
 */
public enum SshSessionPool {
    INSTANCE;

    private Map<String, SshSession> sessionPool = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();

    /**
     * get a session by key
     *
     * @param key key
     * @return SshSession
     */
    public SshSession get(String key) {
        return sessionPool.get(key);
    }

    /**
     * put a session
     *
     * @param key key
     * @param session SshSession
     */
    public void put(String key, SshSession session) {
        this.sessionPool.put(key, session);
    }

    /**
     * get an SSH session and reuse a previously used session.
     *
     * @param sshHost host
     * @param sshPort port
     * @param sshUser username
     * @param sshPass password
     * @return an SSH session
     * @throws IOException IOException
     */
    public SshSession getSession(String sshHost, int sshPort, String sshUser, String sshPass) throws IOException {
        final String key = StrUtil.format("{}@{}:{}", sshUser, sshHost, sshPort);
        SshSession session = get(key);
        if (session == null || !session.isConnected()) {
            synchronized (LOCK) {
                session = get(key);
                if (session == null || !session.isConnected()) {
                    session = connect(sshHost, sshPort, sshUser, sshPass);
                    put(key, session);
                }
            }
        }
        return session;
    }

    /**
     * remove a session
     *
     * @param session session
     */
    public void remove(SshSession session) {
        if (session != null) {
            final Iterator<Map.Entry<String, SshSession>> iterator = this.sessionPool.entrySet().iterator();
            Map.Entry<String, SshSession> entry;
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (session.equals(entry.getValue())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    /**
     * close and clear all session
     */
    public void closeAll() {
        Collection<SshSession> sessions = sessionPool.values();
        for (SshSession session : sessions) {
            if (session.isConnected()) {
                try {
                    session.close();
                } catch (IOException e) {
                    throw new CustomException(null, e);
                }
            }
        }
        sessionPool.clear();
    }
}

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
 *  SshSessionPool.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/enums/SshSessionPool.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.enums;

import static com.nctigba.observability.instance.util.SshSessionUtils.connect;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nctigba.observability.instance.util.SshSessionUtils;
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

    private Map<String, SshSessionUtils> sessionPool = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();

    /**
     * get a session by key
     *
     * @param key key
     * @return SshSession
     */
    public SshSessionUtils get(String key) {
        return sessionPool.get(key);
    }

    /**
     * put a session
     *
     * @param key key
     * @param session SshSession
     */
    public void put(String key, SshSessionUtils session) {
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
    public SshSessionUtils getSession(String sshHost, int sshPort, String sshUser, String sshPass) throws IOException {
        final String key = StrUtil.format("{}@{}:{}", sshUser, sshHost, sshPort);
        SshSessionUtils session = get(key);
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
    public void remove(SshSessionUtils session) {
        if (session != null) {
            final Iterator<Map.Entry<String, SshSessionUtils>> iterator = this.sessionPool.entrySet().iterator();
            Map.Entry<String, SshSessionUtils> entry;
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
        Collection<SshSessionUtils> sessions = sessionPool.values();
        for (SshSessionUtils session : sessions) {
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

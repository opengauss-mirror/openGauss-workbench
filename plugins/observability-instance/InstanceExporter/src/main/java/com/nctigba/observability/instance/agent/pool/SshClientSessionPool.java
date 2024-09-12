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
 *  SshClientSessionPool.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/pool/SshClientSessionPool.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.pool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Pool for SSH client session
 *
 * @since 2023/12/28
 */
@Slf4j
public class SshClientSessionPool {
    private GenericObjectPool<ClientSession> pool;

    /**
     * Constructor
     *
     * @param sshClientConfig SSH client config entity
     */
    public SshClientSessionPool(SshClientConfig sshClientConfig) {
        init(sshClientConfig);
    }

    /**
     * Pool init method
     *
     * @param sshClientConfig SSH Client config entity
     */
    public void init(SshClientConfig sshClientConfig) {
        SshClientSessionFactory factory = new SshClientSessionFactory(sshClientConfig);
        pool = new GenericObjectPool<>(factory, sshClientConfig.getObjectPoolConfig());
    }

    /**
     * Get a session from pool
     *
     * @return SSH session
     */
    public Optional<ClientSession> getSession() {
        try {
            return Optional.ofNullable(pool.borrowObject());
        } catch (Exception e) {
            log.error("{} Get ssh client session error!", Thread.currentThread().getName());
            try (StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                log.error(sw.toString());
            } catch (IOException ioe) {
                log.error("StringWriter error!");
            }
        }
        return Optional.empty();
    }

    /**
     * Release session
     *
     * @param session SSH session
     */
    public void releaseSession(ClientSession session) {
        try {
            if (session != null) {
                pool.returnObject(session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
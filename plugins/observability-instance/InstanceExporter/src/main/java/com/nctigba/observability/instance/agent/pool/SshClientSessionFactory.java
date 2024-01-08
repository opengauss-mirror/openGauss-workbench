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
 *  SshClientFactory.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/pool/SshClientSessionFactory.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.pool;

import com.nctigba.observability.instance.agent.exception.SshInitException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;

/**
 * Factory for SSH client session
 *
 * @since 2023/12/28
 */
@Slf4j
public class SshClientSessionFactory implements PooledObjectFactory<ClientSession> {
    private static final int SESSION_TIMEOUT = 10000;

    private SshClientConfig config;
    private SshClient client;

    /**
     * Constructor
     *
     * @param sshClientConfig SSH client config entity
     */
    public SshClientSessionFactory(SshClientConfig sshClientConfig) {
        this.config = sshClientConfig;

        String ip = config.getMachineIP();
        int port = Integer.valueOf(config.getMachinePort());
        String user = config.getMachineUser();
        SshClient newClient = SshClient.setUpDefaultClient();
        newClient.start();
        client = newClient;

        try {
            newClient.connect(user, ip, port).verify();
        } catch (IOException e) {
            log.error("Init sshd client error for " + ip);
            throw new SshInitException(e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public PooledObject<ClientSession> makeObject() throws Exception {
        String ip = config.getMachineIP();
        int port = Integer.valueOf(config.getMachinePort());
        String user = config.getMachineUser();
        String pass = config.getMachinePassword();
        ClientSession session = client.connect(user, ip, port).verify().getSession();
        session.addPasswordIdentity(pass);
        session.auth().verify(SESSION_TIMEOUT);
        return new DefaultPooledObject<>(session);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void destroyObject(PooledObject<ClientSession> pooledObject) throws Exception {
        if (pooledObject.getObject() != null) {
            pooledObject.getObject().close();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean validateObject(PooledObject<ClientSession> pooledObject) {
        return pooledObject.getObject().isOpen();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void activateObject(PooledObject<ClientSession> pooledObject) {
    }

    /**
     * @inheritDoc
     */
    @Override
    public void passivateObject(PooledObject<ClientSession> pooledObject) {
    }
}
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

import com.nctigba.observability.instance.agent.exception.CMDException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories;
import org.apache.sshd.common.io.IoServiceFactoryFactory;
import org.apache.sshd.common.session.SessionHeartbeatController.HeartbeatType;
import org.apache.sshd.common.util.threads.CloseableExecutorService;
import org.apache.sshd.common.util.threads.SshThreadPoolExecutor;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Factory for SSH client session
 *
 * @since 2023/12/28
 */
@Slf4j
public class SshClientSessionFactory implements PooledObjectFactory<ClientSession> {
    private static final int CONNECT_TIMEOUT = 3000;
    private static final int AUTH_TIMEOUT = 5000;

    private final SshClientConfig config;

    /**
     * Constructor
     *
     * @param sshClientConfig SSH client config entity
     */
    public SshClientSessionFactory(SshClientConfig sshClientConfig) {
        this.config = sshClientConfig;
    }

    private SshClient getClient() {
        return SshClientHolder.INSTANCE;
    }

    /**
     * @inheritDoc
     */
    @Override
    public PooledObject<ClientSession> makeObject() throws Exception {
        String ip = config.getMachineIP();
        int port = config.getMachinePort();
        String user = config.getMachineUser();
        String pass = config.getMachinePassword();
        ClientSession session = null;
        try {
            session = getClient()
                    .connect(user, ip, port)
                    .verify(CONNECT_TIMEOUT)
                    .getSession();

            session.setSessionHeartbeat(HeartbeatType.IGNORE, TimeUnit.MILLISECONDS,3000L);
            session.addPasswordIdentity(pass);
            session.auth().verify(AUTH_TIMEOUT);

            log.debug("Session created for {}@{}:{}", user, ip, port);
            return new DefaultPooledObject<>(session);
        } catch (Exception e) {
            if (session != null) {
                session.close();
            }
            log.warn("Failed to create SSH session for {}@{}:{}", user, ip, port, e);
            throw new CMDException("Failed to create SSH session for " + user + " @" + ip + ":" + port, e);
        }
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
        ClientSession session = pooledObject.getObject();
        return session.isOpen();
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

    private static class SshClientHolder {
        private static final int MAX_NIO_WORKERS = 32;
        private static final int IO_QUEUE_CAP = 128;

        private static final SshClient INSTANCE;
        private static final CloseableExecutorService NIO_EXECUTOR;
        private static final IoServiceFactoryFactory IO_FACTORY_FACTORY;

        static {
            int cpuCore = Runtime.getRuntime().availableProcessors();
            int realIoThreads = Math.max(1, Math.min(cpuCore / 2, MAX_NIO_WORKERS));
            log.info("Detect cpu core:{}, realIoThreads:{}", cpuCore, realIoThreads);

            NIO_EXECUTOR = new SshThreadPoolExecutor(
                    realIoThreads, realIoThreads,
                    0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(IO_QUEUE_CAP),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

            IO_FACTORY_FACTORY = BuiltinIoServiceFactoryFactories.NIO2.create();
            IO_FACTORY_FACTORY.setExecutorServiceFactory(() -> NIO_EXECUTOR);

            INSTANCE = SshClient.setUpDefaultClient();
            INSTANCE.setIoServiceFactoryFactory(IO_FACTORY_FACTORY);

            INSTANCE.start();
            log.info("Global SshClient started successfully, nio worker limit:{}", realIoThreads);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!INSTANCE.isClosed()) {
                    try {
                        INSTANCE.stop();
                        INSTANCE.close();
                    } catch (IOException e) {
                        log.error("SshClient close error", e);
                    }
                }
                if (NIO_EXECUTOR != null && !NIO_EXECUTOR.isShutdown()) {
                    NIO_EXECUTOR.shutdownNow();
                    log.info("NIO executor shutdown complete");
                }
            }));
        }
    }
}

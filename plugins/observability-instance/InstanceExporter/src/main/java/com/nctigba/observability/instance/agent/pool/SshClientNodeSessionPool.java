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
 *  SshClientHostPool.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/pool/SshClientNodeSessionPool.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.pool;

import org.opengauss.tool.cipher.RsaUtils;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.sshd.client.session.ClientSession;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Different node has own pool
 *
 * @since 2023/12/28
 */
@Slf4j
public class SshClientNodeSessionPool {
    /**
     * Max parallel SSH channels per host: 40 metrics in batches,
     * with 5 concurrent connections sufficient for peak shaving
     */
    private static final int MAX_SESSION_FOR_ONE_CLIENT = 8;
    private static final int MAX_IDLE_FOR_ONE_NODE = 3;
    private static final int MIN_IDLE_FOR_ONE_NODE = 0;

    /**
     * Extend wait time to 4s to buffer queued tasks
     */
    private static final long MAX_GET_CONNECTION_WAIT_MILLIS = 4000L;

    /**
     * Reduce the GenericObjectPoolConfig eviction scan interval to 10s to quickly clean up bad connections.
     */
    private static final Duration EVICT_INTERVAL = Duration.ofSeconds(10);

    /**
     * Destroy connections idle for 25s without access to prevent long-term connection hoarding
     */
    private static final Duration IDLE_TIMEOUT = Duration.ofSeconds(25);

    /**
     * Evict more connections per scan to accelerate the cleanup of invalid sessions.
     */
    private static final int EVICTION_TEST_NUM = 20;
    private static final Map<String, SshClientSessionPool> POOL_MAP = new ConcurrentHashMap<>();

    /**
     * Get pool for one node
     *
     * @param targetConfig Target info entity
     * @return SSH client session pool
     */
    public static SshClientSessionPool getNodePool(TargetConfig targetConfig) {
        String nodeId = targetConfig.getNodeId();
        if (!POOL_MAP.containsKey(nodeId)) {
            synchronized (POOL_MAP) {
                if (!POOL_MAP.containsKey(nodeId)) {
                    GenericObjectPoolConfig<ClientSession> objectPoolConfig = new GenericObjectPoolConfig<>();

                    objectPoolConfig.setMaxTotal(MAX_SESSION_FOR_ONE_CLIENT);
                    objectPoolConfig.setMaxIdle(MAX_IDLE_FOR_ONE_NODE);
                    objectPoolConfig.setMinIdle(MIN_IDLE_FOR_ONE_NODE);
                    objectPoolConfig.setMaxWaitMillis(MAX_GET_CONNECTION_WAIT_MILLIS);

                    objectPoolConfig.setTestOnCreate(false);
                    objectPoolConfig.setTestOnBorrow(true);
                    objectPoolConfig.setTestOnReturn(true);
                    objectPoolConfig.setTestWhileIdle(true);
                    objectPoolConfig.setBlockWhenExhausted(true);

                    objectPoolConfig.setMinEvictableIdleTime(IDLE_TIMEOUT);
                    objectPoolConfig.setTimeBetweenEvictionRuns(EVICT_INTERVAL);
                    objectPoolConfig.setNumTestsPerEvictionRun(EVICTION_TEST_NUM);

                    SshClientConfig sshClientConfig = new SshClientConfig();
                    sshClientConfig.setMachineIP(targetConfig.getMachineIP());
                    sshClientConfig.setMachinePort(Integer.parseInt(targetConfig.getMachinePort()));
                    sshClientConfig.setMachineUser(targetConfig.getMachineUser());
                    String machinePassword = targetConfig.getMachinePassword();
                    sshClientConfig.setMachinePassword(RsaUtils.getInstance().decrypt(machinePassword));
                    sshClientConfig.setObjectPoolConfig(objectPoolConfig);
                    POOL_MAP.put(nodeId, new SshClientSessionPool(sshClientConfig));
                    log.info("Create ssh session pool for node:{}, maxTotal:{}", nodeId, MAX_SESSION_FOR_ONE_CLIENT);
                }
            }
        }
        return POOL_MAP.get(nodeId);
    }

    /**
     * Clear pool
     */
    public static void clear() {
        synchronized (POOL_MAP) {
            for (Map.Entry<String, SshClientSessionPool> entry : POOL_MAP.entrySet()) {
                String nodeId = entry.getKey();
                SshClientSessionPool pool = entry.getValue();
                try {
                    pool.close();
                    log.info("SSH session pool for node {} closed successfully.", nodeId);
                } catch (Exception e) {
                    log.error("Failed to close SSH session pool for node: {}", nodeId, e);
                }
            }
            POOL_MAP.clear();
        }
    }
}
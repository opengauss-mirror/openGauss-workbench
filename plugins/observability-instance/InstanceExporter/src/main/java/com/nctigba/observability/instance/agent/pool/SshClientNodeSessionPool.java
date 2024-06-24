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

import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Different node has own pool
 *
 * @since 2023/12/28
 */
public class SshClientNodeSessionPool {
    private static final int MAX_SESSION_FOR_ONE_CLIENT = 12;
    private static final int MAX_IDLE_FOR_ONE_NODE = 12;
    private static final int MIN_IDLE_FOR_ONE_NODE = 0;
    private static final int MAX_GET_CONNECTION_WAIT_SECONDS = 3;
    private static Map<String, SshClientSessionPool> poolMap = new HashMap<>();

    /**
     * Get pool for one node
     *
     * @param targetConfig Target info entity
     * @return SSH client session pool
     */
    public static SshClientSessionPool getNodePool(TargetConfig targetConfig) {
        String nodeId = targetConfig.getNodeId();
        if (!poolMap.containsKey(nodeId)) {
            synchronized (poolMap) {
                if (!poolMap.containsKey(nodeId)) {
                    SshClientConfig sshClientConfig = new SshClientConfig();
                    GenericObjectPoolConfig objectPoolConfig = new GenericObjectPoolConfig();
                    objectPoolConfig.setMaxTotal(MAX_SESSION_FOR_ONE_CLIENT);
                    objectPoolConfig.setMaxIdle(MAX_IDLE_FOR_ONE_NODE);
                    objectPoolConfig.setMinIdle(MIN_IDLE_FOR_ONE_NODE);
                    objectPoolConfig.setMaxWait(Duration.ofSeconds(MAX_GET_CONNECTION_WAIT_SECONDS));
                    sshClientConfig.setObjectPoolConfig(objectPoolConfig);

                    sshClientConfig.setMachineIP(targetConfig.getMachineIP());
                    sshClientConfig.setMachinePort(Integer.valueOf(targetConfig.getMachinePort()));
                    sshClientConfig.setMachineUser(targetConfig.getMachineUser());
                    sshClientConfig.setMachinePassword(targetConfig.getMachinePassword());
                    poolMap.put(nodeId, new SshClientSessionPool(sshClientConfig));
                }
            }
        }
        return poolMap.get(nodeId);
    }

    /**
     * Clear pool
     */
    public static void clear() {
        poolMap = new HashMap<>();
    }
}
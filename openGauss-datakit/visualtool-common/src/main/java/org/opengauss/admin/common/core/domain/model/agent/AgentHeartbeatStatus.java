/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.common.core.domain.model.agent;

import java.time.Duration;
import java.time.Instant;

/**
 * AgentHeartbeatStatus
 *
 * @author: wangchao
 * @Date: 2025/3/31 11:48
 * @Description: AgentHeartbeatStatus
 * @since 7.0.0-RC2
 **/
public class AgentHeartbeatStatus {
    private static final long TIMEOUT_SEC = 3L;

    private volatile Instant lastHeartbeat = Instant.now();
    private volatile boolean isOnline;
    private volatile boolean isStatusChanged;

    /**
     * update the last heartbeat time
     * and mark the agent as online
     *
     * @param heartbeatTime the time of the last heartbeat
     */
    public void updateFromHeartbeat(Instant heartbeatTime) {
        this.lastHeartbeat = heartbeatTime;
        if (!isOnline) {
            isOnline = true;
            isStatusChanged = true;
        }
    }

    @Override
    public String toString() {
        return "[isOnline=" + isOnline + ", isChanged=" + isStatusChanged + " lastHeartbeat=" + lastHeartbeat + ']';
    }

    /**
     * get the last heartbeat time
     *
     * @return the last heartbeat time
     */
    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    /**
     * get the status duration
     *
     * @return the status duration
     */
    public Duration getStatusDuration() {
        if (lastHeartbeat == null) {
            return Duration.ZERO;
        }
        return Duration.between(lastHeartbeat, Instant.now());
    }

    /**
     * check if the status has changed
     *
     * @return true if the status has changed, false otherwise
     */
    public boolean checkAndResetStatusChanged() {
        if (isStatusChanged) {
            isStatusChanged = false;
            return true;
        }
        return false;
    }

    /**
     * update the last heartbeat time
     * and mark the agent as online
     *
     * @param time the time of the last heartbeat
     */
    public void updateLastHeartbeatTime(Instant time) {
        this.lastHeartbeat = time;
        this.isOnline = true; // 收到心跳即标记为Up
    }

    /**
     * mark the agent as down
     */
    public void markAsDown() {
        this.isOnline = false;
    }

    /**
     * check if the agent is online (i.e. last heartbeat received within the timeout period)
     *
     * @return true if online, false otherwise
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * check if the agent is expired (i.e. no heartbeat received for a while)
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        if (lastHeartbeat == null) {
            return true;
        }
        return Duration.between(lastHeartbeat, Instant.now()).getSeconds() > TIMEOUT_SEC;
    }

    /**
     * update the online status based on the last heartbeat time
     *
     * @return true if the status changed, false otherwise
     */
    public boolean updateOnlineStatus() {
        boolean shouldBeOnline = !isExpired();
        if (isOnline != shouldBeOnline) {
            isOnline = shouldBeOnline;
            isStatusChanged = true;
            return true;
        }
        return false;
    }
}

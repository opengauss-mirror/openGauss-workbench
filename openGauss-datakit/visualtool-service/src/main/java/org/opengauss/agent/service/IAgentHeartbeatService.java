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

package org.opengauss.agent.service;

import org.opengauss.admin.common.core.domain.model.agent.HeartbeatReport;

/**
 * IAgentHeartbeatService
 *
 * @author: wangchao
 * @Date: 2025/4/14 11:01
 * @Description: IAgentHeartbeatService
 * @since 7.0.0-RC2
 **/
public interface IAgentHeartbeatService {
    /**
     * Start the heartbeat service
     */
    void startHeartbeatService();

    /**
     * Check if the agent is online
     *
     * @param agentId agent id
     * @return true if the agent is online, otherwise false
     */
    boolean isAgentOnline(String agentId);

    /**
     * Agent heartbeat
     *
     * @param heart heartbeat information
     */

    void receiveHeartbeat(HeartbeatReport heart);

    /**
     * Agent deregister
     *
     * @param requestDown deregister information
     */
    void deregister(HeartbeatReport requestDown);
}

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

package org.opengauss.agent.event;

import lombok.Getter;

import org.springframework.context.ApplicationEvent;

/**
 * AgentStatusEvent
 *
 * @author: wangchao
 * @Date: 2025/4/14 16:29
 * @Description: AgentStatusEvent
 * @since 7.0.0-RC2
 **/
@Getter
public class AgentStatusEvent extends ApplicationEvent {
    private final String agentId;
    private final boolean isOnline;

    /**
     * Constructor Agent status event
     *
     * @param source source
     * @param agentId agentId
     * @param isOnline isOnline
     */
    public AgentStatusEvent(Object source, String agentId, boolean isOnline) {
        super(source);
        this.agentId = agentId;
        this.isOnline = isOnline;
    }
}

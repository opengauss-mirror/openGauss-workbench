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

package org.opengauss.agent.impl;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.model.agent.AgentHeartbeatStatus;
import org.opengauss.admin.common.core.domain.model.agent.HeartbeatReport;
import org.opengauss.agent.event.AgentStatusEvent;
import org.opengauss.agent.service.IAgentHeartbeatService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * IAgentHeartbeatService
 *
 * @author: wangchao
 * @Date: 2025/4/14 11:01
 * @Description: IAgentHeartbeatService
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class AgentHeartbeatServiceImpl implements IAgentHeartbeatService {
    private final ConcurrentMap<String, AgentHeartbeatStatus> agents = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * init heartbeat service
     */
    @PostConstruct
    public void init() {
        startHeartbeatService();
    }

    /**
     * start heartbeat schedule service
     */
    @Override
    public void startHeartbeatService() {
        scheduler.scheduleAtFixedRate(this::checkAgentsStatus, 0, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * check host agent status
     */
    public void checkAgentsStatus() {
        Instant now = Instant.now();
        agents.forEach((agentId, status) -> {
            status.updateOnlineStatus();
            if (status.checkAndResetStatusChanged()) {
                triggerStatusEvent(agentId, status.isOnline());
            }
            if (!status.isOnline() && Duration.between(status.getLastHeartbeat(), now).getSeconds() > 600) {
                agents.remove(agentId);
                log.info("Removed offline agent: {}", agentId);
            }
        });
    }

    @Override
    public boolean isAgentOnline(String agentId) {
        AgentHeartbeatStatus status = agents.get(agentId);
        return status != null && status.isOnline();
    }

    @Override
    public void receiveHeartbeat(HeartbeatReport heart) {
        String agentId = heart.getAgentId();
        if (agentId == null || agentId.isBlank()) {
            log.warn("Invalid heartbeat: missing agentId");
            return;
        }
        agents.compute(agentId, (id, status) -> {
            if (status == null) {
                status = new AgentHeartbeatStatus();
                log.info("New agent registered: {}", agentId);
            }
            status.updateFromHeartbeat(heart.getTimestamps());
            return status;
        });
    }

    /**
     * Trigger an event if the status of an agent has changed.
     *
     * @param agentId agentId
     * @param isOnline isOnline
     */
    private void triggerStatusEvent(String agentId, boolean isOnline) {
        eventPublisher.publishEvent(new AgentStatusEvent(this, agentId, isOnline));
        log.info("Agent status changed: {} -> {}", agentId, isOnline ? "ONLINE" : "OFFLINE");
    }

    @Override
    public void deregister(HeartbeatReport requestDown) {
        String agentId = requestDown.getAgentId();
        if (agentId != null) {
            agents.remove(agentId);
            triggerStatusEvent(agentId, false);
            log.info("Agent deregistered: {}", agentId);
        }
    }
}

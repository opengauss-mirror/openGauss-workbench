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

import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskConfig;
import org.opengauss.admin.common.enums.agent.AgentStatus;
import org.opengauss.agent.event.AgentStatusEvent;
import org.opengauss.agent.service.AgentTaskManager;
import org.opengauss.agent.service.IAgentHeartbeatService;
import org.opengauss.agent.service.IAgentInstallService;
import org.opengauss.agent.service.IAgentServerService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.annotation.Resource;

/**
 * IAgentServerService
 *
 * @author: wangchao
 * @Date: 2025/4/12 16:38
 * @Description: AgentServerService
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class AgentServerServiceImpl implements IAgentServerService {
    private static final ConcurrentMap<String, Boolean> AGENT_STATUS_MAP = new ConcurrentHashMap<>();

    @Resource
    private IAgentInstallService agentInstallService;
    @Resource
    private AgentEnvironmentService agentEnvironmentService;
    @Resource
    private AgentTaskManager agentTaskManager;

    @Override
    public Map<String, String> anomalyChecking(String agentId) {
        AgentInstallEntity agent = agentInstallService.getByAgentId(agentId);
        Map<String, String> result = new HashMap<>();
        boolean hasInstallRecord = Objects.nonNull(agent);
        result.put("server_install_record", String.valueOf(hasInstallRecord));
        if (!hasInstallRecord) {
            return result;
        }
        result.put("server_status", agent.getStatus().getValue());
        Map<String, String> execEnvCheckRes = agentEnvironmentService.checkEnvironment(agent);
        result.putAll(execEnvCheckRes);
        return result;
    }

    @Override
    public void taskCallbackStarted(String agentId) {
        log.info("agent {} task callback started", agentId);
        AgentInstallEntity agent = agentInstallService.getByAgentId(agentId);
        int size = agentTaskManager.startAgentTask(agent);
        log.info("agent {} task callback : {}", agent.getAgentId(), size);
    }

    @Override
    public void startAgentTaskByTaskId(Long id) {
        AgentTaskConfig taskConfig = agentTaskManager.queryAgentTaskInstance(id);
        AgentInstallEntity agent = agentInstallService.getByAgentId(taskConfig.getAgentId());
        agentTaskManager.startAgentTask(agent, taskConfig);
    }

    @Override
    public boolean checkClusterNodeAgentInstallAndOnline(String hostId) {
        AgentInstallEntity agent = agentInstallService.getByAgentId(hostId);
        if (Objects.isNull(agent)) {
            return false;
        }
        return AGENT_STATUS_MAP.getOrDefault(hostId, false);
    }

    /**
     * listen to agent status change event
     * when agent up, send task notification to agent
     *
     * @param event event
     */
    @EventListener
    @Async("agentTaskExecutor")
    public void handleStatusChange(AgentStatusEvent event) {
        if (event.getSource() instanceof IAgentHeartbeatService) {
            if (event.isOnline()) {
                AgentInstallEntity agent = agentInstallService.getByAgentId(event.getAgentId());
                int size = agentTaskManager.startAgentTask(agent);
                log.info("agent {} online, start agent task: {}", agent.getAgentId(), size);
                agentInstallService.updateAgentStatus(event.getAgentId(), AgentStatus.RUNNING);
            } else {
                agentInstallService.updateAgentStatus(event.getAgentId(), AgentStatus.STOP);
            }
            AGENT_STATUS_MAP.put(event.getAgentId(), event.isOnline());
        }
        logStateTransition(event);
    }

    /**
     * log state transition
     *
     * @param event event
     */
    private void logStateTransition(AgentStatusEvent event) {
        log.info("Agent Status Event: " + event.getAgentId() + " -> " + (event.isOnline() ? "online" : "offline"));
    }
}

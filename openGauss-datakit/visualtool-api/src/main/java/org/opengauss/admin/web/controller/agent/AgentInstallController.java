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

package org.opengauss.admin.web.controller.agent;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.core.domain.model.agent.AgentStartResult;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskParam;
import org.opengauss.agent.event.AgentStatusEvent;
import org.opengauss.agent.service.AgentTaskManager;
import org.opengauss.agent.service.IAgentInstallService;
import org.opengauss.agent.utils.AgentTaskUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * AgentInstallController
 *
 * @author: wangchao
 * @Date: 2025/4/10 09:27
 * @Description: AgentInstallController
 * @since 7.0.0-RC2
 **/
@RestController
@RequestMapping("/agent")
public class AgentInstallController {
    @Resource
    private IAgentInstallService agentInstallService;
    @Resource
    private AgentTaskManager agentTaskManager;

    /**
     * Install agent
     * <pre>
     *     After installation, create a Host task, but do not start it directly.
     *     Wait until the Agent goes online, and then trigger the task to start through the online notification event.
     *     {@link org.opengauss.agent.impl.AgentServerServiceImpl#handleStatusChange(AgentStatusEvent)}
     * </pre>
     *
     * @param install install information
     * @return result
     */
    @PostMapping("/install")
    public AjaxResult install(@RequestBody AgentInstallEntity install) {
        agentInstallService.installAgent(install);
        AgentTaskParam hostTask = AgentTaskParam.builder()
            .agentId(install.getAgentId())
            .taskTemplateId(AgentConstants.TaskTemplate.HOST_DYNAMIC_METRICS)
            .taskName(AgentTaskUtils.generateTaskName(AgentConstants.TaskTemplate.HOST_DYNAMIC_METRICS))
            .build();
        agentTaskManager.createTask(hostTask);
        return AjaxResult.success();
    }

    /**
     * Uninstall agent
     *
     * @param agentId agent id
     * @return result
     */
    @PostMapping("/uninstall")
    public AjaxResult unInstall(@RequestParam("agentId") String agentId) {
        agentInstallService.uninstallAgent(agentId);
        agentTaskManager.cleanAgentTaskInstance(agentId);
        return AjaxResult.success();
    }

    /**
     * List agent
     *
     * @return agent list
     */
    @GetMapping("/list")
    public AjaxResult list() {
        return AjaxResult.success(agentInstallService.list());
    }

    /**
     * Start agent
     *
     * @param agentId agent id
     * @return result
     */
    @PostMapping("/start")
    public AjaxResult start(@RequestParam("agentId") String agentId) {
        AgentStartResult agentStartStatus = agentInstallService.startAgent(agentId);
        if (agentStartStatus.isSuccess()) {
            return AjaxResult.success(agentStartStatus.message());
        } else {
            return AjaxResult.error(agentStartStatus.message());
        }
    }

    /**
     * Stop agent
     *
     * @param agentId agent id
     * @return result
     */
    @PostMapping("/stop")
    public AjaxResult stop(@RequestParam("agentId") String agentId) {
        agentInstallService.stopAgent(agentId);
        return AjaxResult.success();
    }

    /**
     * Upgrade agent
     *
     * @param agentId agent id
     * @return result
     */
    @PostMapping("/upgrade")
    public AjaxResult upgrade(@RequestParam("agentId") String agentId) {
        agentInstallService.updateAgent(agentId);
        return AjaxResult.success();
    }

    /**
     * Update agent port
     *
     * @param agentId agent id
     * @param agentPort agent port
     * @return result
     */
    @PostMapping("/updateAgentPort")
    public AjaxResult updateAgentPort(@RequestParam("agentId") String agentId,
        @RequestParam("agentPort") String agentPort) {
        agentInstallService.updateAgentPort(agentId, Integer.valueOf(agentPort));
        return AjaxResult.success();
    }
}

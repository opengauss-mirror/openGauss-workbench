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

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.common.core.domain.entity.agent.TaskSchemaDefinition;
import org.opengauss.admin.common.core.domain.entity.agent.TaskTemplateDefinition;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskParam;
import org.opengauss.admin.common.utils.ChainAssert;
import org.opengauss.agent.repository.TaskMetricsDefinitionService;
import org.opengauss.agent.repository.TaskSchemaDefinitionService;
import org.opengauss.agent.repository.TaskTemplateDefinitionService;
import org.opengauss.agent.service.AgentHttpProxy;
import org.opengauss.agent.service.AgentTaskManager;
import org.opengauss.agent.service.IAgentInstallService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.annotation.Resource;

/**
 * AgentTaskDefinitionController
 *
 * @author: wangchao
 * @Date: 2025/4/10 09:27
 * @Description: AgentInstallController
 * @since 7.0.0-RC2
 **/
@RestController
@RequestMapping("/agent")
public class AgentTaskDefinitionController {
    @Resource
    private TaskTemplateDefinitionService taskTemplateDefinitionService;
    @Resource
    private TaskSchemaDefinitionService taskSchemaDefinitionService;
    @Resource
    private TaskMetricsDefinitionService taskMetricsDefinitionService;
    @Resource
    private AgentTaskManager agentTaskManager;
    @Resource
    private IAgentInstallService agentInstallService;
    @Resource
    private AgentHttpProxy agentHttpProxy;

    /**
     * save task template definition
     *
     * @param taskTemplateDefinitions taskTemplateDefinitions
     * @return result
     */
    @PostMapping("/taskTemplateDefinition/save")
    public AjaxResult taskTemplateDefinition(@RequestBody List<TaskTemplateDefinition> taskTemplateDefinitions) {
        taskTemplateDefinitionService.saveBatch(taskTemplateDefinitions);
        return AjaxResult.success();
    }

    /**
     * save task schema definition
     *
     * @param taskSchemaDefinitions taskSchemaDefinitions
     * @return result
     */
    @PostMapping("/taskSchemaDefinition/save")
    public AjaxResult taskSchemaDefinition(@RequestBody List<TaskSchemaDefinition> taskSchemaDefinitions) {
        taskSchemaDefinitionService.saveBatch(taskSchemaDefinitions);
        return AjaxResult.success();
    }

    /**
     * save task metrics definition
     *
     * @param taskMetricsDefinitions taskMetricsDefinitions
     * @return result
     */
    @PostMapping("/taskMetricsDefinition/save")
    public AjaxResult taskMetricsDefinition(@RequestBody List<TaskMetricsDefinition> taskMetricsDefinitions) {
        taskMetricsDefinitionService.saveBatch(taskMetricsDefinitions);
        return AjaxResult.success();
    }

    /**
     * save task instance
     *
     * @param agentTaskParam agentTaskParam
     * @return result
     */
    @PostMapping("/taskInstance/save")
    public AjaxResult taskInstance(@RequestBody AgentTaskParam agentTaskParam) {
        ChainAssert.of(agentInstallService.getByAgentId(agentTaskParam.getAgentId())).nonNull("Agent not found");
        return AjaxResult.success(agentTaskManager.createTask(agentTaskParam));
    }

    /**
     * get task list
     *
     * @return result
     */
    @GetMapping("/task/list")
    public AjaxResult taskList() {
        List<AgentInstallEntity> list = agentInstallService.list();
        return AjaxResult.success(agentTaskManager.queryAllOfAgentTask(list));
    }

    /**
     * get task metrics definition list
     *
     * @return result
     */
    @GetMapping("/taskMetricsDefinition/list")
    public AjaxResult taskMetricsDefinitionList() {
        return AjaxResult.success(taskMetricsDefinitionService.list());
    }

    /**
     * start task
     *
     * @param agentId agent id
     * @param id task id
     * @return result
     */
    @GetMapping("/start/task")
    public AjaxResult startTask(@RequestParam("agentId") String agentId, @RequestParam("id") Long id) {
        AgentInstallEntity agent = agentInstallService.getByAgentId(agentId);
        agentHttpProxy.fetchAgentPubKey(agent);
        agentTaskManager.startAgentTaskByTaskId(agent, id);
        return AjaxResult.success();
    }

    /**
     * get task schema definition list
     *
     * @return result
     */
    @GetMapping("/taskSchemaDefinition/list")
    public AjaxResult taskSchemaDefinitionList() {
        return AjaxResult.success(taskSchemaDefinitionService.list());
    }

    /**
     * get task template definition list
     *
     * @return result
     */
    @GetMapping("/taskTemplateDefinition/list")
    public AjaxResult list() {
        return AjaxResult.success(taskTemplateDefinitionService.list());
    }
}

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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.core.domain.entity.agent.TaskInstanceEntity;
import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.common.core.domain.entity.agent.TaskTemplateDefinition;
import org.opengauss.admin.common.core.domain.model.agent.AgentClusterVo;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskConfig;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskParam;
import org.opengauss.admin.common.enums.agent.ObjectType;
import org.opengauss.admin.common.exception.ops.AgentTaskException;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ChainAssert;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.common.utils.RsaUtils;
import org.opengauss.agent.data.ClusterOriginal;
import org.opengauss.agent.repository.DynamicTableService;
import org.opengauss.agent.repository.TaskInstanceService;
import org.opengauss.agent.repository.TaskMetricsDefinitionService;
import org.opengauss.agent.repository.TaskSchemaDefinitionService;
import org.opengauss.agent.repository.TaskTemplateDefinitionService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * AgentTaskManager
 *
 * @author: wangchao
 * @Date: 2025/4/18 15:42
 * @Description: AgentTaskManager
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class AgentTaskManager {
    private static final String AGENT_TASK_START_URL = "http://%s:%d/agent/task/start";

    @Resource
    private TaskInstanceService taskInstanceService;
    @Resource
    private TaskSchemaDefinitionService taskSchemaDefinitionService;
    @Resource
    private TaskMetricsDefinitionService taskMetricsDefinitionService;
    @Resource
    private TaskTemplateDefinitionService taskTemplateDefinitionService;
    @Resource
    private IAgentHeartbeatService agentHeartbeatService;
    @Resource
    private DynamicTableService dynamicTableService;
    @Resource
    private WebClient webClient;
    @Resource
    private AgentHttpProxy agentHttpProxy;

    /**
     * create task
     *
     * @param agentTaskParam agent task param
     * @return task id
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(AgentTaskParam agentTaskParam) {
        validateInputParameters(agentTaskParam);
        TaskTemplateDefinition taskTemplateDefinition = fetchTaskTemplate(agentTaskParam.getTaskTemplateId());
        validateBusinessRules(agentTaskParam, taskTemplateDefinition);
        TaskInstanceEntity taskInstanceEntity = agentTaskParam.toTaskInstanceEntity();
        taskInstanceService.save(taskInstanceEntity);
        log.info("create agent task success, task id: {}", taskInstanceEntity.getId());
        return taskInstanceEntity.getId();
    }

    /**
     * query agent task instance
     *
     * @param agentId agent id
     */
    public void cleanAgentTaskInstance(String agentId) {
        int count = taskInstanceService.removeAgentAllTask(agentId);
        log.info("clean agent task instance success, agent id: {}, count: {}", agentId, count);
    }

    /**
     * query agent task instance config
     *
     * @param agentTaskId agent task instance id
     * @return agent task instance config
     */
    public AgentTaskConfig queryAgentTaskInstance(Long agentTaskId) {
        TaskInstanceEntity agentTask = Optional.of(taskInstanceService.getById(agentTaskId))
            .orElseThrow(() -> new OpsException("task id not found"));
        return buildAgentTaskConfig(agentTask);
    }

    /**
     * <pre>
     * build agent task instance
     * 1. transfer task entity to agent task instance
     * 2. fetch task template definition
     * 3. fetch cluster config, if the task template definition is DB type. this contains cluster configuration
     * information.
     * 4. fetch schema definition list,this contains collect metric list
     * 5. fetch metrics definition list, this contains collect metric definition list
     * </pre>
     *
     * @param agentTask agent task entity
     * @return agent task instance
     */
    private AgentTaskConfig buildAgentTaskConfig(TaskInstanceEntity agentTask) {
        AgentTaskConfig config = new AgentTaskConfig(fetchTaskTemplate(agentTask.getTaskTemplateId()));
        config.setTaskId(agentTask.getId());
        config.setTaskName(agentTask.getTaskName());
        config.setAgentId(agentTask.getAgentId());
        if (config.isDbTemplate()) {
            config.setClusterConfig(fetchClusterConfig(agentTask));
        }
        List<String> collectorMetricDetails = fetchSchemaDefinitionList(config.getCollectMetric());
        config.setCollectorMetricDetails(collectorMetricDetails);
        config.setMetricsDefinitionList(fetchMetricsDefinitionList(collectorMetricDetails));
        return config;
    }

    private AgentClusterVo fetchClusterConfig(TaskInstanceEntity task) {
        ClusterOriginal clusterOriginal = safeParseClusterOriginal(task.getClusterOriginal());
        ChainAssert.of(clusterOriginal).nonNull("Invalid cluster original: " + task.getClusterOriginal());
        ChainAssert.of(task.getClusterNodeId()).nonNull("Cluster ID cannot be empty");
        return clusterOriginal.load(task.getClusterNodeId());
    }

    private void refreshTaskStatusToError(List<Long> taskIds, String errorMessage) {
        taskInstanceService.batchUpdateTaskFailedStatus(taskIds, errorMessage);
    }

    private boolean checkAgentStatus(String agentId) {
        return agentHeartbeatService.isAgentOnline(agentId);
    }

    private void refreshTaskStatusToRunning(Long taskId) {
        taskInstanceService.startTask(taskId);
    }

    private void sendStartTaskRequest(AgentInstallEntity agent, AgentTaskConfig taskConfig) {
        String url = String.format(Locale.getDefault(), AGENT_TASK_START_URL, agent.getAgentIp(), agent.getAgentPort());
        webClient.post()
            .uri(url)
            .accept(MediaType.ALL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(taskConfig)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> {
                refreshTaskStatusToRunning(taskConfig.getTaskId());
                log.info("task request  {}:{} send successfully", taskConfig.getTaskName(), taskConfig.getTaskId());
            })
            .onErrorResume(WebClientResponseException.class, ex -> {
                log.error("API error (status {}): {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                refreshTaskStatusToError(Collections.singletonList(taskConfig.getTaskId()), ex.getMessage());
                return Mono.error(new AgentTaskException("task request send failed: " + ex.getStatusCode()));
            })
            .onErrorResume(WebClientRequestException.class, ex -> {
                log.error("Network error: {}", ex.getMessage(), ex);
                refreshTaskStatusToError(Collections.singletonList(taskConfig.getTaskId()), ex.getMessage());
                return Mono.error(new AgentTaskException("Network connection failed: " + ex.getMessage()));
            })
            .onErrorResume(TimeoutException.class, ex -> {
                log.error("Request timed out", ex);
                refreshTaskStatusToError(Collections.singletonList(taskConfig.getTaskId()), ex.getMessage());
                return Mono.error(new AgentTaskException("Request timed out"));
            })
            .onErrorResume(ex -> {
                log.error("Unexpected error: {}", ex.getMessage(), ex);
                refreshTaskStatusToError(Collections.singletonList(taskConfig.getTaskId()), ex.getMessage());
                return Mono.error(new AgentTaskException("Unexpected error: " + ex.getMessage()));
            })
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .filter(throwable -> throwable instanceof WebClientRequestException
                    || throwable instanceof TimeoutException))
            .subscribe(success -> {
                refreshTaskStatusToRunning(taskConfig.getTaskId());
                log.info("task request  {}:{} send successfully", taskConfig.getTaskName(), taskConfig.getTaskId());
            }, error -> {
                log.error("Unexpected error: {}", error.getMessage(), error);
                Mono.error(new AgentTaskException("task request send failed"));
            }, () -> log.debug("Request completed successfully"));
    }

    private List<String> fetchSchemaDefinitionList(String collectMetric) {
        return taskSchemaDefinitionService.getMetricsByName(collectMetric);
    }

    private List<TaskMetricsDefinition> fetchMetricsDefinitionList(List<String> collectMetric) {
        return taskMetricsDefinitionService.findByNameIn(collectMetric);
    }

    private void validateInputParameters(AgentTaskParam agentTaskParam) {
        ChainAssert.of(agentTaskParam)
            .nonNull("Agent task parameter cannot be null")
            .validateFieldNoneNull(AgentTaskParam::getAgentId, "Agent ID cannot be empty")
            .validateField(AgentTaskParam::getTaskTemplateId, "Task template ID cannot be empty");
    }

    private TaskTemplateDefinition fetchTaskTemplate(String taskTemplateId) {
        return Optional.of(taskTemplateDefinitionService.getById(taskTemplateId))
            .orElseThrow(() -> new OpsException("Task template not found for ID: " + taskTemplateId));
    }

    private void validateBusinessRules(AgentTaskParam agentTaskParam, TaskTemplateDefinition template) {
        if (template.getOperateObjType() == ObjectType.DB) {
            ChainAssert.of(agentTaskParam)
                .validateField(AgentTaskParam::getClusterNodeId, "cluster node id cannot be empty")
                .validateField(AgentTaskParam::getClusterOriginal, "cluster original cannot be empty");
            ClusterOriginal original = safeParseClusterOriginal(agentTaskParam.getClusterOriginal());
            ChainAssert.of(original).nonNull("Invalid cluster original: " + agentTaskParam.getClusterOriginal());
            AgentClusterVo agentClusterVo = original.load(agentTaskParam.getClusterNodeId());
            ChainAssert.of(agentClusterVo).nonNull("Cluster not found");
        }
    }

    private ClusterOriginal safeParseClusterOriginal(String value) {
        try {
            return ClusterOriginal.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new AgentTaskException("invalid cluster original: " + value);
        }
    }

    /**
     * start agent task
     *
     * @param agent agent
     * @param id task id
     */
    public void startAgentTaskByTaskId(AgentInstallEntity agent, Long id) {
        TaskInstanceEntity task = taskInstanceService.getById(id);
        AgentTaskConfig taskConfig = buildAgentTaskConfig(task);
        if (StrUtil.isNotEmpty(agent.getPublicKey())) {
            AgentClusterVo clusterConfig = taskConfig.getClusterConfig();
            clusterConfig.setDbPassword(RsaUtils.encrypt(clusterConfig.getDbPassword(), agent.getPublicKey()));
            taskConfig.setClusterConfig(clusterConfig);
        }
        sendStartTaskRequest(agent, taskConfig);
        refreshTaskStatusToRunning(taskConfig.getTaskId());
    }

    /**
     * start agent task
     *
     * @param agent agent
     * @return start task count
     */
    public int startAgentTask(AgentInstallEntity agent) {
        agentHttpProxy.fetchAgentPubKey(agent);
        List<TaskInstanceEntity> list = taskInstanceService.queryAgentTaskInstance(agent.getAgentId());
        for (TaskInstanceEntity task : list) {
            try {
                AgentTaskConfig taskConfig = buildAgentTaskConfig(task);
                startAgentTask(agent, taskConfig);
            } catch (OpsException e) {
                log.error("start agent task failed, task id: {}", task.getId(), e);
            }
        }
        return list.size();
    }

    /**
     * start agent task
     *
     * @param agent agent
     * @param taskConfig task config
     */
    public void startAgentTask(AgentInstallEntity agent, AgentTaskConfig taskConfig) {
        sendStartTaskRequest(agent, taskConfig);
        refreshTaskStatusToRunning(taskConfig.getTaskId());
    }

    /**
     * query all of agent task
     *
     * @param list list
     * @return agent task map
     */
    public Map<String, List<AgentTaskConfig>> queryAllOfAgentTask(List<AgentInstallEntity> list) {
        return list.stream()
            .flatMap(agent -> taskInstanceService.queryAgentTaskInstance(agent.getAgentId())
                .stream()
                .map(task -> buildAgentTaskConfig(task)))
            .collect(Collectors.groupingBy(AgentTaskConfig::getAgentId));
    }

    /**
     * initialize pipe task data storage table
     */
    @PostConstruct
    public void initializedPipeDataStorageTable() {
        List<TaskTemplateDefinition> pipeTemplateList = taskTemplateDefinitionService.queryPipeTaskTemplateDefinition();
        for (TaskTemplateDefinition template : pipeTemplateList) {
            if (dynamicTableService.checkTemplateTableExist(template.getName(), template.getStoragePolicy())) {
                continue;
            } else {
                List<String> collectorMetricDetails = fetchSchemaDefinitionList(template.getCollectMetric());
                if (CollUtil.isEmpty(collectorMetricDetails)) {
                    log.warn("template {} has no collector metric details", template.getName());
                    continue;
                }
                List<TaskMetricsDefinition> columnList = fetchMetricsDefinitionList(collectorMetricDetails);
                dynamicTableService.createPipeDataStorageTable(template, columnList);
            }
        }
    }

    /**
     * query host metrics definition
     *
     * @return host metrics definition
     */
    public List<TaskMetricsDefinition> queryHostMetricsDefinition() {
        List<String> collectorMetricDetails = fetchSchemaDefinitionList(
            AgentConstants.TaskTemplate.HOST_DYNAMIC_METRICS);
        OpsAssert.isTrue(CollUtil.isNotEmpty(collectorMetricDetails), "metric definition is not found");
        return fetchMetricsDefinitionList(collectorMetricDetails);
    }

    /**
     * delete agent task by cluster node id
     *
     * @param clusterNodeId cluster node id
     */
    public void delAgentTaskByClusterId(String clusterNodeId) {
        taskInstanceService.removeAgentTaskByClusterNodeId(clusterNodeId);
    }
}

/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.service.task;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.entity.TaskDefinition;
import org.opengauss.agent.entity.TaskDefinitionBuilder;
import org.opengauss.agent.entity.TaskExecution;
import org.opengauss.agent.entity.task.AgentTaskVo;
import org.opengauss.agent.enums.TaskType;
import org.opengauss.agent.exception.AgentException;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TaskManager
 *
 * @author: wangchao
 * @Date: 2025/5/21 11:18
 * @since 7.0.0-RC2
 **/
@Slf4j
@Unremovable
@ApplicationScoped
public class TaskManager {
    @Inject
    TaskExecutorFactory executorFactory;

    private final ConcurrentMap<Long, TaskExecution> taskExecMap = new ConcurrentHashMap<>();

    /**
     * startTask
     *
     * @param taskConfig taskConfig
     * @return TaskExecution
     */
    public TaskExecution startTask(AgentTaskVo taskConfig) {
        TaskDefinition taskDef = TaskDefinitionBuilder.builder(taskConfig);
        TaskExecution execution = new TaskExecution(taskDef);
        if (taskExecMap.containsKey(taskDef.getTaskId())) {
            log.info("task {} is already running", taskDef.getTaskId());
            execution.setStatus("already_running");
            execution.setErrorMessage("task " + taskDef.getTaskId() + " is already running");
            return execution;
        }
        log.info("task {} is running ", taskDef.getTaskName());
        try {
            TaskExecutor executor = executorFactory.getExecutor(taskDef.getTaskType());
            executor.initialize(taskDef);
            executor.execute();
            execution.setStatus("running");
            execution.setStartTime(Instant.now());
            taskExecMap.put(taskDef.getTaskId(), execution);
        } catch (AgentException e) {
            log.error("task {} execute failed", taskDef.getTaskName(), e);
            execution.setStatus("no_start");
            execution.setErrorMessage(e.getMessage());
        }
        return execution;
    }

    /**
     * stop task
     *
     * @param taskId taskId
     * @return TaskExecution
     */
    public Optional<TaskExecution> stop(Long taskId) {
        if (!taskExecMap.containsKey(taskId)) {
            log.warn("task {} is not running", taskId);
            return Optional.empty();
        }
        TaskExecution execution = taskExecMap.get(taskId);
        TaskType taskType = execution.getTaskType();
        if (taskType == null) {
            log.warn("task {} type is null", taskId);
            return Optional.empty();
        }
        TaskExecutor executor = executorFactory.getExecutor(taskType);
        if (executor == null) {
            log.warn("task {} does not have executor for task type {}", taskId, taskType);
            return Optional.empty();
        }
        executor.cancel(taskId);
        log.info("stop task {}", taskId);
        if (!executor.hasTaskRunning()) {
            taskExecMap.remove(taskId);
            log.info("task {} does not have executor {} running, remove it", taskId, taskType);
        }
        execution.setStatus("stop");
        execution.setEndTime(Instant.now());
        return Optional.of(execution);
    }

    /**
     * check task is empty
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return taskExecMap.isEmpty();
    }
}

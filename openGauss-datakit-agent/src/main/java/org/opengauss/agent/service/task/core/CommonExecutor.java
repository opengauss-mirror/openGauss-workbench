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

package org.opengauss.agent.service.task.core;

import lombok.Getter;

import org.opengauss.agent.config.AppConfig;
import org.opengauss.agent.entity.TaskDefinition;
import org.opengauss.agent.exception.TaskExecutionException;
import org.opengauss.agent.service.task.group.GroupKey;
import org.opengauss.agent.service.task.group.TaskGroup;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * CommonExecutor
 *
 * @author: wangchao
 * @Date: 2025/4/9 10:35
 * @Description: CommonExecutor
 * @since 7.0.0-RC2
 **/
public class CommonExecutor {
    private final Logger log;
    private final AppConfig appConfig;
    private final Map<Long, GroupKey> taskIdToGroupKey = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentMap<GroupKey, TaskGroup> targetGroups = new ConcurrentHashMap<>();

    /**
     * CommonExecutor
     *
     * @param appConfig appConfig
     * @param log log
     */
    public CommonExecutor(AppConfig appConfig, Logger log) {
        this.log = log;
        this.appConfig = appConfig;
    }

    /**
     * Initialize task
     *
     * @param task task
     * @throws TaskExecutionException TaskExecutionException
     */
    public void initialize(TaskDefinition task) throws TaskExecutionException {
        GroupKey key = new GroupKey(task);
        taskIdToGroupKey.put(task.getTaskId(), key);
        TaskGroup otelTaskGroup = targetGroups.compute(key, (k, group) -> {
            if (group == null) {
                TaskGroup newGroup = new TaskGroup(key);
                newGroup.addTask(task);
                newGroup.createContext(appConfig);
                return newGroup;
            } else {
                synchronized (this) {
                    if (!group.validateTaskConsistency(task)) {
                        throw new TaskExecutionException("Task conflicts with existing group configuration");
                    }
                    group.addTask(task);
                    group.refreshContext();
                }
                return group;
            }
        });
        log.info("Initialized task execute environment {} :tasks:{}", key, otelTaskGroup.getTaskIds());
    }

    /**
     * Check if there is a task running
     *
     * @return boolean
     */
    public boolean hasTaskRunning() {
        return targetGroups.values().stream().anyMatch(TaskGroup::hasTask);
    }

    /**
     * Remove task
     *
     * @param taskId Task ID
     */
    public void cancal(Long taskId) {
        GroupKey groupKey = taskIdToGroupKey.remove(taskId);
        if (groupKey == null) {
            log.warn("Task {} not found in the executor", taskId);
            return;
        }
        TaskGroup group = targetGroups.get(groupKey);
        if (group == null) {
            log.warn("Group for task {} not found in the executor", taskId);
            return;
        }
        synchronized (group) {
            group.removeTask(taskId);
            if (group.isEmpty()) {
                targetGroups.remove(groupKey);
                log.info("Removed group {} due to no tasks", groupKey);
            }
        }
    }

    /**
     * Stop all tasks
     */
    public void stop() {
        targetGroups.forEach((target, group) -> {
            group.stopPeriodicCollection();
        });
        targetGroups.clear();
        taskIdToGroupKey.clear();
    }
}

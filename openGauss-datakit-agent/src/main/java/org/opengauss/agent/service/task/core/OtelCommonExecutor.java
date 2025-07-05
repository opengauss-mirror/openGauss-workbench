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
import org.opengauss.agent.service.task.group.OtelTaskGroup;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * OtelCommonExecutor
 *
 * @author: wangchao
 * @Date: 2025/4/9 10:35
 * @Description: OtelCommonExecutor
 * @since 7.0.0-RC2
 **/
public class OtelCommonExecutor {
    private final Logger log;
    private final Map<Long, GroupKey> taskIdToGroupKey = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentMap<GroupKey, OtelTaskGroup> targetGroups = new ConcurrentHashMap<>();
    private final AppConfig appConfig;

    /**
     * Constructor
     *
     * @param appConfig AppConfig
     * @param log Logger
     */
    public OtelCommonExecutor(AppConfig appConfig, Logger log) {
        this.log = log;
        this.appConfig = appConfig;
    }

    /**
     * Initialize
     *
     * @param task TaskDefinition
     * @throws TaskExecutionException TaskExecutionException
     */
    public void initialize(TaskDefinition task) throws TaskExecutionException {
        GroupKey key = new GroupKey(task);
        taskIdToGroupKey.put(task.getTaskId(), key);
        OtelTaskGroup otelTaskGroup = targetGroups.compute(key, (k, group) -> {
            if (group == null) {
                OtelTaskGroup newGroup = new OtelTaskGroup(key);
                newGroup.addTask(task);
                newGroup.createContext(appConfig);
                return newGroup;
            } else {
                synchronized (this) {
                    if (!group.validateTaskConsistency(task)) {
                        throw new TaskExecutionException("Task conflicts with existing group configuration");
                    }
                    group.addTask(task);
                    // re-create context
                    group.recreateContext(appConfig);
                }
                return group;
            }
        });
        log.info("Initialized task execute environment {} :tasks:{}", key, otelTaskGroup.getTaskIds());
    }

    /**
     * Has task running
     *
     * @return boolean
     */
    public boolean hasTaskRunning() {
        return targetGroups.values().stream().anyMatch(OtelTaskGroup::hasTask);
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
        OtelTaskGroup group = targetGroups.get(groupKey);
        if (group == null) {
            log.warn("Group for task {} not found in the executor", taskId);
            return;
        }
        synchronized (group) {
            group.removeTask(taskId);
            if (group.isEmpty()) {
                group.getMeterProviderContext().shutdown();
                targetGroups.remove(groupKey);
                log.info("Removed group {} due to no tasks", groupKey);
            } else {
                try {
                    group.recreateContext(appConfig); // re-create the context with the remaining tasks
                    log.info("Recreated context for group {} after task removal", groupKey);
                } catch (TaskExecutionException e) {
                    log.error("Failed to update context after task removal", e);
                }
            }
        }
    }

    /**
     * Stop
     */
    public void stop() {
        targetGroups.forEach((target, group) -> {
            group.stopPeriodicCollection();
        });
        targetGroups.clear();
        taskIdToGroupKey.clear();
    }
}

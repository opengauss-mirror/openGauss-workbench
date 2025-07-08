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

package org.opengauss.agent.service.task;

import io.quarkus.arc.Unremovable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.opengauss.agent.enums.TaskType;
import org.opengauss.agent.exception.TaskExecutionException;
import org.opengauss.agent.service.task.core.DbMetricExecutor;
import org.opengauss.agent.service.task.core.DbPipeExecutor;
import org.opengauss.agent.service.task.core.HostDynamicExecutor;
import org.opengauss.agent.service.task.core.HostStaticExecutor;
import org.opengauss.agent.service.task.core.OsMetricExecutor;
import org.opengauss.agent.service.task.core.OsPipeExecutor;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * TaskExecutorFactory
 *
 * @author: wangchao
 * @Date: 2025/4/9 09:34
 * @Description: TaskExecutorFactory
 * @since 7.0.0-RC2
 **/
@Unremovable
@ApplicationScoped
public class TaskExecutorFactory {
    @Inject
    HostStaticExecutor hostStaticExecutor;
    @Inject
    HostDynamicExecutor hostDynamicExecutor;
    @Inject
    OsMetricExecutor osMetricExecutor;
    @Inject
    DbMetricExecutor dbMetricExecutor;
    @Inject
    OsPipeExecutor osPipeExecutor;
    @Inject
    DbPipeExecutor dbPipeExecutor;

    private final Map<TaskType, TaskExecutor> executors = new EnumMap<>(TaskType.class);

    /**
     * Initialize the task executor factory.
     */
    @PostConstruct
    public void init() {
        executors.put(TaskType.OSHI_FIXED_METRIC, hostStaticExecutor);
        executors.put(TaskType.OSHI_DYNAMIC_METRIC, hostDynamicExecutor);
        executors.put(TaskType.OS_METRIC, osMetricExecutor);
        executors.put(TaskType.DB_METRIC, dbMetricExecutor);
        executors.put(TaskType.OS_PIPE, osPipeExecutor);
        executors.put(TaskType.DB_PIPE, dbPipeExecutor);
    }

    /**
     * Get the task executor by task type.
     *
     * @param taskType task type
     * @return task executor
     */
    public TaskExecutor getExecutor(TaskType taskType) {
        TaskExecutor executor = executors.get(taskType);
        if (Objects.isNull(executor)) {
            throw new TaskExecutionException("task executor" + taskType + " not found");
        }
        return executor;
    }
}

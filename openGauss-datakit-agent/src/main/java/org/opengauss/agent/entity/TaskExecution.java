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

package org.opengauss.agent.entity;

import cn.hutool.core.util.IdUtil;
import lombok.Data;

import org.opengauss.agent.enums.TaskType;
import org.opengauss.agent.exception.AgentException;
import org.opengauss.agent.service.task.TaskExecutor;

import java.time.Instant;
import java.util.Optional;

/**
 * TaskExecution
 *
 * @author: wangchao
 * @Date: 2025/4/9 09:28
 * @Description: TaskExecution
 * @since 7.0.0-RC2
 **/
@Data
public class TaskExecution {
    private String executionId;
    private Long taskId;
    private TaskDefinition taskDefinition;
    private TaskExecutor executor;
    private Instant startTime;
    private Instant endTime;
    private String status;
    private String errorMessage;

    /**
     * Constructor for TaskExecution
     *
     * @param taskDefinition taskDefinition
     */
    public TaskExecution(TaskDefinition taskDefinition) {
        this.executionId = IdUtil.getSnowflakeNextIdStr();
        this.taskId = taskDefinition.getTaskId();
        this.taskDefinition = taskDefinition;
        this.startTime = Instant.now();
        this.status = "running";
    }

    /**
     * getTaskType
     *
     * @return TaskType
     */
    public TaskType getTaskType() {
        return Optional.ofNullable(taskDefinition)
            .orElseThrow(() -> new AgentException("TaskExecution [" + executionId + "] taskDefinition is null"))
            .getTaskType();
    }
}

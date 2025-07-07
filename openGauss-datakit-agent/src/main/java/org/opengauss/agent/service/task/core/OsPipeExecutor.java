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

import jakarta.enterprise.context.ApplicationScoped;

import org.opengauss.agent.entity.TaskDefinition;
import org.opengauss.agent.exception.TaskExecutionException;
import org.opengauss.agent.service.task.TaskExecutor;

/**
 * OsPipeExecutor
 *
 * @author: wangchao
 * @Date: 2025/4/9 10:44
 * @Description: OsPipeExecutor
 * @since 7.0.0-RC2
 **/
@ApplicationScoped
public class OsPipeExecutor implements TaskExecutor {
    @Override
    public void initialize(TaskDefinition taskDefinition) throws TaskExecutionException {
    }

    @Override
    public void execute() throws TaskExecutionException {
    }

    @Override
    public void cancel(Long taskId) {
    }

    @Override
    public boolean hasTaskRunning() {
        return false;
    }
}

/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.common.core.domain.model.agent;

import lombok.Builder;
import lombok.Data;

import org.opengauss.admin.common.core.domain.entity.agent.TaskInstanceEntity;
import org.springframework.beans.BeanUtils;

import java.time.Instant;

/**
 * AgentTaskParam
 *
 * @author: wangchao
 * @Date: 2025/4/17 16:01
 * @Description: AgentTaskParam
 * @since 7.0.0-RC2
 **/
@Builder
@Data
public class AgentTaskParam {
    private String taskName;
    private String taskTemplateId;
    private String agentId;
    private String clusterNodeId;
    private String clusterOriginal;

    /**
     * Convert to TaskInstanceEntity
     *
     * @return TaskInstanceEntity
     */
    public TaskInstanceEntity toTaskInstanceEntity() {
        TaskInstanceEntity taskInstanceEntity = new TaskInstanceEntity();
        BeanUtils.copyProperties(this, taskInstanceEntity);
        taskInstanceEntity.setTaskStatus(AgentTaskStatus.NO_START);
        taskInstanceEntity.setCreateTime(Instant.now());
        taskInstanceEntity.setUpdateTime(Instant.now());
        return taskInstanceEntity;
    }
}

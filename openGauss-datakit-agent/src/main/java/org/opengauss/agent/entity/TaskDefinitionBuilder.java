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

import org.opengauss.agent.entity.task.AgentTaskVo;
import org.opengauss.agent.entity.task.TaskTemplateDefinitionVo;
import org.opengauss.agent.enums.ObjectType;
import org.opengauss.agent.utils.DurationUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * TaskDefinitionBuilder
 *
 * @author: wangchao
 * @Date: 2025/4/28 18:34
 * @Description: TaskDefinitionBuilder
 * @since 7.0.0-RC2
 **/
public class TaskDefinitionBuilder {
    /**
     * builder
     *
     * @param taskConfig taskConfig
     * @return TaskDefinition
     */
    public static TaskDefinition builder(AgentTaskVo taskConfig) {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTaskId(taskConfig.getTaskId());
        taskDefinition.setTaskName(taskConfig.getTaskName());
        taskDefinition.setAgentId(taskConfig.getAgentId());
        TaskTemplateDefinitionVo definition = taskConfig.getTemplateDefinition();
        taskDefinition.setTaskType(definition.getType());
        taskDefinition.setStoragePolicy(definition.getStoragePolicy());
        taskDefinition.setGroupTag(definition.getGroupTag());
        taskDefinition.setPluginsTag(definition.getPluginsTag());
        taskDefinition.setOperateObjType(definition.getOperateObjType());
        taskDefinition.setOperateObj(definition.getOperateObj());
        taskDefinition.setReceiveApi(definition.getReceiveApi());
        taskDefinition.setPeriod(DurationUtils.parseToMillis(definition.getPeriod()));
        taskDefinition.setEstimatedExecutionTime(Math.max(definition.getEstimatedExecutionTime(), 5));
        taskDefinition.setUnit(TimeUnit.MILLISECONDS);
        taskDefinition.setDataSendTarget(definition.getReceiveApi());
        taskDefinition.setDbTemplate(isDbTemplate(definition));
        taskDefinition.setClusterConfig(taskConfig.getClusterConfig());
        taskDefinition.setMetricsDefinitionList(taskConfig.getMetricsDefinitionList());
        taskDefinition.setCollectorList(taskConfig.getCollectorMetricDetails());
        return taskDefinition;
    }

    /**
     * isDbTemplate
     *
     * @param definition definition
     * @return boolean
     */
    public static boolean isDbTemplate(TaskTemplateDefinitionVo definition) {
        return Objects.equals(definition.getOperateObjType(), ObjectType.DB);
    }
}

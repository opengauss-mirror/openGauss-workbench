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

package org.opengauss.admin.common.core.domain.model.agent;

import lombok.Data;

import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.common.core.domain.entity.agent.TaskTemplateDefinition;
import org.opengauss.admin.common.enums.agent.ObjectType;

import java.util.List;
import java.util.Objects;

/**
 * AgentTaskConfig
 *
 * @author: wangchao
 * @Date: 2025/4/21 16:34
 * @Description: AgentTaskConfig
 * @since 7.0.0-RC2
 **/
@Data
public class AgentTaskConfig {
    private Long taskId;
    private String taskName;
    private String agentId;
    private TaskTemplateDefinition templateDefinition;
    private List<String> collectorMetricDetails;
    private List<TaskMetricsDefinition> metricsDefinitionList;
    private AgentClusterVo clusterConfig;

    /**
     * AgentTaskConfig
     *
     * @param templateDefinition templateDefinition
     */
    public AgentTaskConfig(TaskTemplateDefinition templateDefinition) {
        this.templateDefinition = templateDefinition;
    }

    /**
     * check if the task template is db template
     *
     * @return boolean
     */
    public boolean isDbTemplate() {
        return Objects.equals(templateDefinition.getOperateObjType(), ObjectType.DB);
    }

    /**
     * get collect metric name
     *
     * @return collect metric name
     */
    public String getCollectMetric() {
        return templateDefinition.getCollectMetric();
    }
}

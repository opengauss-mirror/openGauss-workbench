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

package org.opengauss.agent.data;

import lombok.Builder;
import lombok.Data;

import org.opengauss.admin.common.core.domain.model.agent.AgentTaskConfig;

/**
 * CustomEventConfig
 *
 * @author: wangchao
 * @Date: 2025/5/6 17:31
 * @Description: CustomMetricData
 * @since 7.0.0-RC2
 **/
@Data
@Builder
public class CustomEventConfig {
    private Long taskId;
    private Long agentId;
    private AgentTaskConfig taskConfig;

    /**
     * simple info of task config
     *
     * @return String
     */
    public String info() {
        return "[taskId=" + taskId + ", agentId=" + agentId + ", metric=" + taskConfig.getCollectorMetricDetails()
            .size() + ":" + taskConfig.getMetricsDefinitionList().size() + "]";
    }

    /**
     * detail info of task config
     *
     * @return String
     */
    public String detail() {
        return "[taskId=" + taskId + ", agentId=" + agentId + ", metrics=" + taskConfig.getCollectorMetricDetails()
            + "]";
    }
}

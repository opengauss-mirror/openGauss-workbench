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

package org.opengauss.agent.service;

import org.opengauss.admin.common.core.domain.model.agent.HostBaseInfo;
import org.opengauss.agent.data.CustomMetricData;

import java.util.List;
import java.util.Map;

/**
 * IAgentServerReceiveService
 *
 * @author: wangchao
 * @Date: 2025/4/10 09:30
 * @Description: IAgentInstallService
 * @since 7.0.0-RC2
 **/
public interface IAgentServerReceiveService {
    /**
     * refreshBaseHostFixedInfo
     *
     * @param hostBaseInfo hostBaseInfo
     */
    void refreshBaseHostFixedInfo(HostBaseInfo hostBaseInfo);

    /**
     * receiveMetricData
     *
     * @param taskIds task ids
     * @param agentId agent id
     * @param metricDataList metric data list
     */
    void receiveMetricData(List<Long> taskIds, Long agentId, List<CustomMetricData> metricDataList);

    /**
     * receivePipelineData
     *
     * @param taskId taskId
     * @param agentId agentId
     * @param dataList dataList
     */
    void receivePipelineData(Long taskId, Long agentId, List<Map<String, Object>> dataList);
}

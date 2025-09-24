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

package org.opengauss.admin.web.controller.agent;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.agent.HostBaseInfo;
import org.opengauss.agent.data.CustomMetricData;
import org.opengauss.agent.event.producer.BackpressureStatus;
import org.opengauss.agent.event.producer.BackpressureStrategy;
import org.opengauss.agent.service.IAgentServerReceiveService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

/**
 * AgentServerReceiveController
 *
 * @author: wangchao
 * @Date: 2025/4/10 09:27
 * @Description: AgentInstallController
 * @since 7.0.0-RC2
 **/
@RestController
@RequestMapping("/receive")
public class AgentServerReceiveController {
    @Resource
    private IAgentServerReceiveService agentServerReceiveService;
    @Resource
    private BackpressureStrategy strategy;

    /**
     * Get the custom metric data of the host
     *
     * @param hostBaseInfo hostBaseInfo
     * @return result
     */
    @PostMapping("/fixed/host/info")
    public AjaxResult fixedHostBaseInfo(HostBaseInfo hostBaseInfo) {
        agentServerReceiveService.refreshBaseHostFixedInfo(hostBaseInfo);
        return AjaxResult.success();
    }

    /**
     * get custom metric data of the host
     *
     * @param taskIds taskIds
     * @param agentId agentId
     * @param metricDataList metricDataList
     * @return result
     */
    @PostMapping("/metrics")
    public AjaxResult metrics(@RequestParam(value = "taskIds") List<Long> taskIds,
        @RequestParam(value = "agentId") Long agentId, @RequestBody List<CustomMetricData> metricDataList) {
        agentServerReceiveService.receiveMetricData(taskIds, agentId, metricDataList);
        return AjaxResult.success();
    }

    /**
     * get custom metric data of the host
     *
     * @param taskId taskId
     * @param agentId agentId
     * @param dataList dataList
     * @return result
     */
    @PostMapping("/pipeline")
    public AjaxResult pipeline(@RequestParam(value = "taskId") Long taskId,
        @RequestParam(value = "agentId") Long agentId, @RequestBody List<Map<String, Object>> dataList) {
        agentServerReceiveService.receivePipelineData(taskId, agentId, dataList);
        return AjaxResult.success();
    }

    /**
     * Adjust the threshold
     *
     * @param percent percent
     */
    @PostMapping("/backpressure/threshold")
    public void adjustThreshold(@RequestParam int percent) {
        strategy.setThresholdPercent(percent);
    }

    /**
     * Get the current status of the backpressure strategy
     *
     * @return status
     */
    @GetMapping("/backpressure/status")
    public BackpressureStatus getStatus() {
        return strategy.getBackpressureStatus();
    }
}

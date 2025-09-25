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

package org.opengauss.agent.impl;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskConfig;
import org.opengauss.admin.common.core.domain.model.agent.HostBaseInfo;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.system.service.HostBasicService;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.agent.data.CustomEventConfig;
import org.opengauss.agent.data.CustomMetricData;
import org.opengauss.agent.event.producer.DynamicMetricEventProducer;
import org.opengauss.agent.event.producer.PipelineEventProducer;
import org.opengauss.agent.service.IAgentServerReceiveService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

/**
 * IAgentServerReceiveService
 *
 * @author: wangchao
 * @Date: 2025/4/12 16:38
 * @Description: IAgentServerReceiveService
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class AgentServerReceiveServiceImpl implements IAgentServerReceiveService {
    @Resource
    private IHostService hostService;
    @Resource
    private HostBasicService hostBasicService;
    @Resource
    private AgentTaskConfigService agentTaskConfigService;
    @Resource
    private PipelineEventProducer pipelineEventProducer;
    @Resource
    private DynamicMetricEventProducer dynamicMetricEventProducer;

    @Override
    public void refreshBaseHostFixedInfo(HostBaseInfo hostBaseInfo) {
        OpsAssert.nonNull(hostBaseInfo, "hostBaseInfo is null");
        String agentId = hostBaseInfo.getAgentId();
        OpsHostEntity host = hostService.getById(agentId);
        OpsAssert.nonNull(host, "agent id is not existed, agentId: " + agentId + " cannot find host info");
        host.setHostname(hostBaseInfo.getHostName());
        host.setOs(hostBaseInfo.getOsName());
        host.setOsVersion(hostBaseInfo.getOsVersion());
        host.setOsBuild(hostBaseInfo.getOsBuild());

        host.setCpuArch(hostBaseInfo.getCpuArchitecture());
        host.setCpuModel(hostBaseInfo.getCpuModel());
        host.setCpuFreq(hostBaseInfo.getCpuFreq());
        host.setPhysicalCores(hostBaseInfo.getPhysicalCores());
        host.setLogicalCores(hostBaseInfo.getLogicalCores());

        hostService.updateById(host);
        hostBasicService.getHostBasicInfo(agentId);
        log.info("refresh host info success,hostId:{} : {}", agentId, hostBaseInfo);
    }

    @Override
    public void receiveMetricData(List<Long> taskIds, Long agentId, List<CustomMetricData> metricDataList) {
        log.debug("receive metric: taskId={},agentId={} data: {}", taskIds, agentId, metricDataList.size());
        taskIds.forEach(taskId -> {
            AgentTaskConfig taskConfig = agentTaskConfigService.getAgentTaskConfig(taskId);
            if (taskConfig == null) {
                log.error("Failed to load config for taskId={}", taskId);
                return;
            }
            dynamicMetricEventProducer.onDataTranslate(
                CustomEventConfig.builder().taskId(taskId).agentId(agentId).taskConfig(taskConfig).build(),
                Collections.unmodifiableList(metricDataList));
        });
    }

    @Override
    public void receivePipelineData(Long taskId, Long agentId, List<Map<String, Object>> dataList) {
        AgentTaskConfig taskConfig = agentTaskConfigService.getAgentTaskConfig(taskId);
        if (taskConfig == null) {
            log.error("Failed to load config for taskId={}", taskId);
            return;
        }
        pipelineEventProducer.onDataTranslate(
            CustomEventConfig.builder().taskId(taskId).agentId(agentId).taskConfig(taskConfig).build(),
            Collections.unmodifiableList(dataList));
    }
}

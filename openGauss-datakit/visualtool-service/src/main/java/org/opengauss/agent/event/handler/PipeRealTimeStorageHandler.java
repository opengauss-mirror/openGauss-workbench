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

package org.opengauss.agent.event.handler;

import com.lmax.disruptor.EventHandler;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.agent.TaskTemplateDefinition;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskConfig;
import org.opengauss.admin.common.enums.agent.StoragePolicy;
import org.opengauss.admin.common.utils.spring.SpringUtils;
import org.opengauss.agent.event.PipelineEvent;
import org.opengauss.agent.impl.AgentTaskConfigService;
import org.opengauss.agent.repository.DynamicTableService;
import org.opengauss.agent.repository.SqlTemplate;
import org.opengauss.agent.utils.AgentTaskUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PipeRealTimeStorageHandler
 *
 * @author: wangchao
 * @Date: 2025/3/18 10:21
 * @Description: PipeRealTimeStorageHandler
 * @since 7.0.0-RC2
 **/
@Slf4j
public class PipeRealTimeStorageHandler implements EventHandler<PipelineEvent> {
    private DynamicTableService dynamicTableService;
    private AgentTaskConfigService agentTaskConfigService;

    public PipeRealTimeStorageHandler() {
    }

    @Override
    public void onEvent(PipelineEvent event, long sequence, boolean isEndOfBatch) {
        if (!Objects.equals(StoragePolicy.REAL_TIME, event.getStoragePolicy())) {
            return;
        }
        String clusterNodeId = event.getClusterNodeId();
        AgentTaskConfig taskConfig = agentTaskConfigService.getAgentTaskConfig(event.getTaskId());
        List<String> fieldNameList = agentTaskConfigService.getAgentTaskFields(taskConfig.getTaskId());
        // 设置commonProperty的属性值
        Map<String, Object> commonProperty = new HashMap<>();
        commonProperty.put("task_id", taskConfig.getTaskId());
        commonProperty.put("agent_id", taskConfig.getAgentId());
        commonProperty.put("cluster_node_id", clusterNodeId);
        commonProperty.put("create_time", Instant.now());
        List<String> nameList = agentTaskConfigService.getAgentTaskNames(taskConfig.getTaskId());
        TaskTemplateDefinition templateDefinition = taskConfig.getTemplateDefinition();
        dynamicTableService.deleteRealtimeExpiredData(SqlTemplate.buildRealTimeDeleteSql(templateDefinition.getName()),
            commonProperty);
        final AtomicLong rowIdx = new AtomicLong(0);
        String insertSql = SqlTemplate.buildRealTimeTableInsertSql(templateDefinition.getName(), fieldNameList);
        for (Map<String, Object> rowData : event.getDataList()) {
            commonProperty.put("row_id", rowIdx.incrementAndGet());
            commonProperty.put("id",
                AgentTaskUtils.calcRowPrimaryId(taskConfig.getTaskId(), taskConfig.getAgentId(), clusterNodeId,
                    rowIdx.get()));
            dynamicTableService.saveRealtimeRowData(insertSql, nameList, commonProperty, rowData);
        }
        log.info("pipe realtime onEvent with {} event {}, rowSize={}", sequence, isEndOfBatch,
            event.getDataList().size());
    }

    @Override
    public void onStart() {
        dynamicTableService = SpringUtils.getBean(DynamicTableService.class);
        agentTaskConfigService = SpringUtils.getBean(AgentTaskConfigService.class);
    }

    @Override
    public void onShutdown() {
    }
}

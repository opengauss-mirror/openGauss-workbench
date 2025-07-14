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

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.model.agent.AgentTaskConfig;
import org.opengauss.admin.common.enums.agent.StoragePolicy;
import org.opengauss.admin.common.exception.ops.AgentTaskException;
import org.opengauss.admin.common.utils.spring.SpringUtils;
import org.opengauss.agent.event.PipelineEvent;
import org.opengauss.agent.impl.AgentTaskConfigService;
import org.opengauss.agent.repository.DynamicTableService;
import org.opengauss.agent.repository.SqlTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * PipeHistoricalStorageHandler
 *
 * @author: wangchao
 * @Date: 2025/3/18 10:21
 * @Description: PipeHistoricalStorageHandler
 * @since 7.0.0-RC2
 **/
@Slf4j
public class PipeHistoricalStorageHandler implements EventHandler<PipelineEvent> {
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(4, 5);

    private DynamicTableService dynamicTableService;
    private AgentTaskConfigService agentTaskConfigService;

    public PipeHistoricalStorageHandler() {
    }

    @Override
    public void onEvent(PipelineEvent event, long sequence, boolean isEndOfBatch) {
        if (!Objects.equals(StoragePolicy.HISTORY, event.getStoragePolicy())) {
            return;
        }
        long lastRowId = saveMetricData(event);
        log.info("pipe history onEvent with {} event {}, rowSize={}, endOfId={}", sequence, isEndOfBatch,
            event.getDataList().size(), lastRowId);
        registerCleanScheduledFuture(event);
    }

    private long saveMetricData(PipelineEvent event) {
        String clusterNodeId = event.getClusterNodeId();
        AgentTaskConfig taskConfig = agentTaskConfigService.getAgentTaskConfig(event.getTaskId());
        // 设置commonProperty的属性值
        Map<String, Object> commonProperty = new HashMap<>();
        commonProperty.put("batch_id", SNOWFLAKE.nextId());
        commonProperty.put("task_id", taskConfig.getTaskId());
        commonProperty.put("agent_id", taskConfig.getAgentId());
        commonProperty.put("cluster_node_id", clusterNodeId);
        commonProperty.put("create_time", Instant.now());
        String taskTemplateName = agentTaskConfigService.getAgentTaskTemplateName(event.getTaskId());
        List<String> fieldNameList = agentTaskConfigService.getAgentTaskFields(taskConfig.getTaskId());
        String insertSql = SqlTemplate.buildHistoryTableInsertSql(taskTemplateName, fieldNameList);
        long lastRowId = 0L;
        List<String> nameList = agentTaskConfigService.getAgentTaskNames(taskConfig.getTaskId());
        for (Map<String, Object> rowData : event.getDataList()) {
            lastRowId = dynamicTableService.saveHistoryRowData(insertSql, nameList, commonProperty, rowData);
        }
        return lastRowId;
    }

    private void registerCleanScheduledFuture(PipelineEvent event) {
        try {
            String keepPeriod = event.getKeepPeriod();
            Long taskId = event.getTaskId();
            String taskTemplateName = agentTaskConfigService.getAgentTaskTemplateName(event.getTaskId());
            HistoricalStorageCleanerManager.registerCleanScheduledFuture(keepPeriod, (k, cleaner) -> {
                if (cleaner != null) {
                    cleaner.addTask(taskId);
                    return cleaner;
                }
                cleaner = new PipOldHistoryDataCleaner(keepPeriod, taskTemplateName, dynamicTableService);
                long interval = StorageHandlerUtils.calculateCleanInterval(keepPeriod);
                long delay = StorageHandlerUtils.calculateInitialDelay(keepPeriod);
                cleaner.setFuture(StorageHandlerUtils.getPeriodExecutor()
                    .scheduleAtFixedRate(cleaner, delay, interval, TimeUnit.SECONDS));
                cleaner.addTask(taskId);
                return cleaner;
            });
        } catch (AgentTaskException ex) {
            log.error("register old history data cleaner error", ex);
        }
    }

    @Override
    public void onStart() {
        dynamicTableService = SpringUtils.getBean(DynamicTableService.class);
        agentTaskConfigService = SpringUtils.getBean(AgentTaskConfigService.class);
    }

    static class PipOldHistoryDataCleaner extends HistoryDataCleaner {
        private final String taskTemplateName;
        private final DynamicTableService dynamicTableService;

        /**
         * PipOldHistoryDataCleaner
         *
         * @param keepPeriod keep period
         * @param taskTemplateName taskTemplateName
         * @param dynamicTableService dynamicTableService
         */
        public PipOldHistoryDataCleaner(String keepPeriod, String taskTemplateName,
            DynamicTableService dynamicTableService) {
            super(keepPeriod);
            this.taskTemplateName = taskTemplateName;
            this.dynamicTableService = dynamicTableService;
        }

        @Override
        public int cleanOldHistoryData(List<Long> taskIds, Instant cutOffTime) {
            String deleteSql = SqlTemplate.buildHistoryTableDeleteSql(taskTemplateName);
            Map<String, Object> commonProperty = new HashMap<>();
            commonProperty.put("create_time", Timestamp.from(cutOffTime));
            int cleanRowSize = 0;
            for (Long taskId : taskIds) {
                commonProperty.put("taskId", taskId);
                cleanRowSize += dynamicTableService.deleteHistoryExpiredData(deleteSql, commonProperty);
            }
            return cleanRowSize;
        }
    }
}

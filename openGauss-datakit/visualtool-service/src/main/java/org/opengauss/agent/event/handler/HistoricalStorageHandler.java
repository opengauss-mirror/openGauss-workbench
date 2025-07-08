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

import static org.opengauss.admin.common.constant.AgentConstants.Default.NONE;

import com.lmax.disruptor.EventHandler;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.agent.MetricHistorical;
import org.opengauss.admin.common.enums.agent.StoragePolicy;
import org.opengauss.admin.common.exception.ops.AgentTaskException;
import org.opengauss.admin.common.utils.spring.SpringUtils;
import org.opengauss.agent.data.CustomMetricData;
import org.opengauss.agent.data.CustomMetricDataPoint;
import org.opengauss.agent.event.MetricEvent;
import org.opengauss.agent.repository.MetricHistoricalStorageService;
import org.opengauss.agent.utils.AgentTaskUtils;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * HistoricalStorageHandler
 *
 * @author: wangchao
 * @Date: 2025/3/18 10:21
 * @Description: HistoricalStorageHandler
 * @since 7.0.0-RC2
 **/
@Slf4j
public class HistoricalStorageHandler implements EventHandler<MetricEvent> {
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(3, 2);

    private MetricHistoricalStorageService metricHistoricalStorageService;

    public HistoricalStorageHandler() {
    }

    /**
     * Initialize the handler.
     *
     * @param event published to the  RingBuffer
     * @param sequence of the event being processed
     * @param isEndOfBatch flag to indicate if this is the last event in a batch from the  RingBuffer
     */
    @Override
    public void onEvent(MetricEvent event, long sequence, boolean isEndOfBatch) {
        if (Objects.equals(StoragePolicy.HISTORY, event.getStoragePolicy())) {
            log.info("historical metric event with {} event {} info  {}", sequence, isEndOfBatch, event.info());
            // save metric data, processing the data and save it to the historical storage
            saveMetricData(event);
            // and then register a scheduled task to clean up the historical data after the retention time
            registerCleanScheduledFuture(event);
        }
    }

    private void saveMetricData(MetricEvent event) {
        List<CustomMetricData> data = event.getMetricData();
        List<String> collectorMetrics = event.getCollectorMetrics();
        List<MetricHistorical> metricHistoricalList = new LinkedList<>();
        Map<String, String> propertyMap = event.getProperty();
        data.stream().filter(metric -> collectorMetrics.contains(metric.getName())).forEach(metric -> {
            List<CustomMetricDataPoint> points = metric.getPoints();
            points.forEach(point -> {
                metricHistoricalList.add(convertMetricHistorical(event, metric, point, propertyMap));
            });
        });
        metricHistoricalStorageService.saveBatch(metricHistoricalList);
    }

    private void registerCleanScheduledFuture(MetricEvent event) {
        try {
            String keepPeriod = event.getKeepPeriod();
            Long taskId = event.getTaskId();
            HistoricalStorageCleanerManager.registerCleanScheduledFuture(keepPeriod, (k, cleaner) -> {
                if (cleaner != null) {
                    cleaner.addTask(taskId);
                    return cleaner;
                }
                cleaner = new OldHistoryDataCleaner(keepPeriod);
                long interval = StorageHandlerUtils.calculateCleanInterval(keepPeriod);
                long delay = StorageHandlerUtils.calculateInitialDelay(keepPeriod);
                ScheduledFuture<?> scheduledFuture = StorageHandlerUtils.getPeriodExecutor()
                    .scheduleAtFixedRate(cleaner, delay, interval, TimeUnit.SECONDS);
                cleaner.setFuture(scheduledFuture);
                cleaner.addTask(taskId);
                return cleaner;
            });
        } catch (AgentTaskException ex) {
            log.error("register old history data cleaner error", ex);
        }
    }

    static class OldHistoryDataCleaner extends HistoryDataCleaner {
        private final MetricHistoricalStorageService storageService;

        /**
         * OldHistoryDataCleaner
         *
         * @param keepPeriod keep period
         */
        public OldHistoryDataCleaner(String keepPeriod) {
            super(keepPeriod);
            this.storageService = SpringUtils.getBean(MetricHistoricalStorageService.class);
        }

        @Override
        public int cleanOldHistoryData(List<Long> taskIds, Instant cutoffTime) {
            if (storageService != null) {
                return storageService.deleteOldHistoryData(getTaskIds(), cutoffTime);
            }
            return 0;
        }
    }

    private MetricHistorical convertMetricHistorical(MetricEvent event, CustomMetricData metric,
        CustomMetricDataPoint point, Map<String, String> propertyMap) {
        return MetricHistorical.builder()
            .id(SNOWFLAKE.nextId())
            .taskId(event.getTaskId())
            .metric(metric.getName())
            .agentId(event.getAgentId())
            .clusterNodeId(StrUtil.isEmpty(event.getClusterNodeId()) ? NONE : event.getClusterNodeId())
            .property(AgentTaskUtils.getProperty(metric.getName(), point.getAttributes(), propertyMap))
            .value(point.getValue())
            .startTime(point.getStartEpochNanos())
            .endTime(point.getEpochNanos())
            .createTime(Instant.now())
            .build();
    }

    @Override
    public void onStart() {
        log.info("Real time metric handler onStart");
        metricHistoricalStorageService = SpringUtils.getBean(MetricHistoricalStorageService.class);
        log.info("MetricHistoricalStorageService is get successfully");
    }

    @Override
    public void onShutdown() {
    }
}

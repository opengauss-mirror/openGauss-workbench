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

package org.opengauss.agent.event.producer;

import com.lmax.disruptor.EventTranslatorTwoArg;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.common.core.domain.entity.agent.TaskTemplateDefinition;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskConfig;
import org.opengauss.admin.common.exception.ops.AgentTaskException;
import org.opengauss.agent.data.CustomEventConfig;
import org.opengauss.agent.data.CustomMetricData;
import org.opengauss.agent.event.MetricEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import jakarta.annotation.PreDestroy;

/**
 * DynamicMetricEventProducer
 *
 * @author: wangchao
 * @Date: 2025/3/18 15:59
 * @Description: DynamicMetricEventProducer
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class DynamicMetricEventProducer {
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(3, 3);
    private static final Map<Long, Map<String, String>> taskMetricPropMap = new ConcurrentHashMap<>();
    private static volatile AtomicInteger requestCounter = new AtomicInteger(0);

    private final BlockingQueue<Pair<CustomEventConfig, List<CustomMetricData>>> overflowQueue
        = new LinkedBlockingQueue<>(10000);
    private final BackpressureStrategy backpressureStrategy;

    /**
     * <pre>
     *   translator function definition:
     *      the function parameters { MetricEvent, sequence , CustomEventConfig }
     *   otel data structure will be converted to Disruptor data Event
     * </pre>
     */
    private final EventTranslatorTwoArg<MetricEvent, CustomEventConfig, List<CustomMetricData>> translator
        = (event, sequence, config, data) -> {
        event.setEventId(SNOWFLAKE.nextId());
        AgentTaskConfig taskConfig = config.getTaskConfig();
        event.setProperty(taskMetricPropMap.get(config.getTaskId()));
        event.setCollectorMetrics(taskConfig.getCollectorMetricDetails());
        TaskTemplateDefinition templateDefinition = taskConfig.getTemplateDefinition();
        event.setKeepPeriod(templateDefinition.getKeepPeriod());
        event.setClusterNodeId(taskConfig.isDbTemplate()
            ? taskConfig.getClusterConfig().getClusterNodeId()
            : AgentConstants.Default.EMPTY);
        event.setStoragePolicy(templateDefinition.getStoragePolicy());
        event.setAgentId(config.getAgentId());
        event.setTaskId(config.getTaskId());
        event.setMetricData(data);
    };

    /**
     * DynamicMetricEventProducer
     *
     * @param backpressureStrategy BackpressureStrategy
     */
    public DynamicMetricEventProducer(BackpressureStrategy backpressureStrategy) {
        this.backpressureStrategy = backpressureStrategy;
    }

    /**
     * onDataTranslate
     *
     * @param metric CustomEventConfig
     * @param dataList dataList
     */
    public void onDataTranslate(CustomEventConfig metric, List<CustomMetricData> dataList) {
        requestCounter.incrementAndGet();
        Pair<CustomEventConfig, List<CustomMetricData>> pair = Pair.of(metric, dataList);
        updateTaskMetric(metric);
        log.debug("metric={}, requestCounter={}", metric.info(), requestCounter.get());
        if (tryFastPublish(pair)) {
            return;
        }
        if (backpressureStrategy.shouldApplyBackpressure()) {
            backpressureStrategy.applyBackpressure();
            if (tryFastPublish(pair)) {
                log.debug("Backpressure tryFastPublish");
                return;
            }
        }
        if (!overflowQueue.offer(pair)) {
            handleCriticalOverflow(metric);
        }
        if (!overflowQueue.isEmpty()) {
            log.debug("Metric overflowQueue={}", overflowQueue.size());
        }
    }

    private void updateTaskMetric(CustomEventConfig metric) {
        if (taskMetricPropMap.containsKey(metric.getTaskId())) {
            return;
        }
        taskMetricPropMap.put(metric.getTaskId(), metric.getTaskConfig()
            .getMetricsDefinitionList()
            .stream()
            .filter(definition -> definition.getProp() != null)
            .collect(Collectors.toMap(TaskMetricsDefinition::getName, TaskMetricsDefinition::getProp)));
    }

    private boolean tryFastPublish(Pair<CustomEventConfig, List<CustomMetricData>> data) {
        try {
            return backpressureStrategy.tryPublishEvent(translator, data);
        } catch (AgentTaskException e) {
            handlePublishError(data.getKey(), e);
            return false;
        }
    }

    private void handlePublishError(CustomEventConfig metric, Exception e) {
        log.error("CRITICAL: Publish failed {}", metric, e);
    }

    @Scheduled(fixedRate = 10)
    private void drainOverflowQueue() {
        Pair<CustomEventConfig, List<CustomMetricData>> pair;
        log.debug("Metric requestCounter={}", requestCounter.get());
        while ((pair = overflowQueue.poll()) != null) {
            if (!tryFastPublish(pair)) {
                overflowQueue.offer(pair);
                break;
            }
        }
    }

    private void handleCriticalOverflow(CustomEventConfig metric) {
        log.error("Critical overflow persistence failed {}", metric);
    }

    /**
     * shutdown
     */
    @PreDestroy
    public void shutdown() {
        Pair<CustomEventConfig, List<CustomMetricData>> pair;
        while ((pair = overflowQueue.poll()) != null) {
            handleCriticalOverflow(pair.getKey());
        }
    }
}
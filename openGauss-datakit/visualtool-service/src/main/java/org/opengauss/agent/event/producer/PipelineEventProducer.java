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
import com.lmax.disruptor.RingBuffer;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskConfig;
import org.opengauss.agent.data.CustomEventConfig;
import org.opengauss.agent.event.PipelineEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import jakarta.annotation.PreDestroy;

/**
 * PipelineEventProducer
 *
 * @author: wangchao
 * @Date: 2025/3/18 10:28
 * @Description: PipelineEventProducer
 * @since 7.0.0-RC2
 **/
@Slf4j
public class PipelineEventProducer {
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 2);
    private static volatile AtomicInteger requestCounter = new AtomicInteger(0);

    private final RingBuffer<PipelineEvent> ringBuffer;
    private final BlockingQueue<Pair<CustomEventConfig, List<Map<String, Object>>>> backpressureQueue
        = new LinkedBlockingQueue<>(10000);
    private final ScheduledExecutorService retryScheduler = Executors.newSingleThreadScheduledExecutor();
    private final EventTranslatorTwoArg<PipelineEvent, CustomEventConfig, List<Map<String, Object>>> translator
        = (event, sequence, config, data) -> {
        event.setEventId(SNOWFLAKE.nextId());
        AgentTaskConfig taskConfig = config.getTaskConfig();
        event.setProperty(taskConfig.getMetricsDefinitionList()
            .stream()
            .filter(definition -> definition.getProp() != null)
            .collect(Collectors.toMap(TaskMetricsDefinition::getName, TaskMetricsDefinition::getProp)));
        event.setClusterNodeId(taskConfig.isDbTemplate()
            ? taskConfig.getClusterConfig().getClusterNodeId()
            : AgentConstants.Default.EMPTY);
        event.setStoragePolicy(taskConfig.getTemplateDefinition().getStoragePolicy());
        event.setAgentId(config.getAgentId());
        event.setTaskId(config.getTaskId());
        event.setDataList(data);
    };

    /**
     * PipelineEventProducer
     *
     * @param ringBuffer ringBuffer
     */
    public PipelineEventProducer(RingBuffer<PipelineEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
        retryScheduler.scheduleAtFixedRate(this::retryFailedEvents, 10, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * onDataTranslate
     *
     * @param metric metric
     * @param dataList dataList
     */
    public void onDataTranslate(CustomEventConfig metric, List<Map<String, Object>> dataList) {
        requestCounter.incrementAndGet();
        if (!ringBuffer.tryPublishEvent(translator, metric, dataList)) {
            if (!backpressureQueue.offer(Pair.of(metric, dataList))) {
                handleOverflow(metric);
            }
            log.info("onDataTranslate backpressureQueue={} ", backpressureQueue.size());
            ThreadUtil.safeSleep(200);
        }
    }

    private void retryFailedEvents() {
        Pair<CustomEventConfig, List<Map<String, Object>>> pair;
        while ((pair = backpressureQueue.poll()) != null) {
            if (!ringBuffer.tryPublishEvent(translator, pair.getKey(), pair.getValue())) {
                backpressureQueue.offer(pair);
                break;
            }
        }
    }

    private void handleOverflow(CustomEventConfig metric) {
        log.error("CRITICAL: Backpressure queue overflow! {}", metric);
    }

    /**
     * shutdown
     */
    @PreDestroy
    public void shutdown() {
        Pair<CustomEventConfig, List<Map<String, Object>>> pair;
        while ((pair = backpressureQueue.poll()) != null) {
            handleOverflow(pair.getKey());
        }
        retryScheduler.shutdown();
    }
}

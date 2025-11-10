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

package org.opengauss.agent.event.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.event.producer.DynamicMetricEventProducer;
import org.opengauss.agent.event.producer.PipelineEventProducer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Locale;

/**
 * MonitorDisruptorEventQueue
 *
 * @author: wangchao
 * @Date: 2025/11/10 10:24
 * @Description: MonitorDisruptorEventQueue
 * @since 7.0.0-RC3
 **/
@Slf4j
@Configuration
public class MonitorDisruptorEventQueue {
    @Resource
    private PipelineEventProducer pipelineEventProducer;
    @Resource
    private DynamicMetricEventProducer dynamicMetricEventProducer;

    /**
     * start PipelineEvent and MetricEvent queue monitor task
     */
    @Scheduled(fixedRate = 10000)
    public void monitorEventQueue() {
        long currentSize = pipelineEventProducer.getRingBuffer().getCursor();
        long capacity = pipelineEventProducer.getRingBuffer().getBufferSize();
        monitorEventQueue("PipelineEvent", currentSize, capacity);
        currentSize = dynamicMetricEventProducer.getRingBuffer().getCursor();
        capacity = dynamicMetricEventProducer.getRingBuffer().getBufferSize();
        monitorEventQueue("MetricEvent", currentSize, capacity);
    }

    private void monitorEventQueue(String eventName, long currentSize, long capacity) {
        double utilization = (double) (currentSize / capacity);
        String percentage = String.format(Locale.getDefault(), "%.1f%%", utilization * 100);
        log.debug("{} Queue currentSize/capacity : {}/{} ({})", eventName, currentSize, capacity, percentage);
    }
}

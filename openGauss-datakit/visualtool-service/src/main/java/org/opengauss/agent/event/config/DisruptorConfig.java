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

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.event.PipelineEvent;
import org.opengauss.agent.event.handler.HistoricalStorageCleanerManager;
import org.opengauss.agent.event.handler.MetricExceptionHandler;
import org.opengauss.agent.event.handler.PipeFingerprintStorageHandler;
import org.opengauss.agent.event.handler.PipeHistoricalStorageHandler;
import org.opengauss.agent.event.handler.PipeRealTimeStorageHandler;
import org.opengauss.agent.event.handler.PipeTreeStorageHandler;
import org.opengauss.agent.event.handler.StorageHandlerUtils;
import org.opengauss.agent.event.producer.PipelineEventProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

import jakarta.annotation.PreDestroy;

/**
 * DisruptorConfig
 *
 * @author: wangchao
 * @Date: 2025/3/18 10:24
 * @Description: DisruptorConfig
 * @since 7.0.0-RC2
 **/
@Slf4j
@Configuration
public class DisruptorConfig {
    @Value("${disruptor.bufferSize:16}")
    private int bufferSize;

    /**
     * create a bean for the order event producer
     * <pre>
     *     config disruptor
     *     1. buffer size: 1024 * 16
     *     2. producer type: multi
     *     3. wait strategy: YieldingWaitStrategy
     *     4. event handler: PipeTreeStorageHandler, PipeFingerprintStorageHandler,
     *        RealTimeStorageHandler, HistoricalStorageHandler
     *     5. exception handler: MetricExceptionHandler
     *     6. start disruptor
     *     7. log disruptor started with buffer size
     * </pre>
     *
     * @return PipelineEventProducer
     */
    @Bean
    public PipelineEventProducer orderEventProducer() {
        Disruptor<PipelineEvent> disruptor = new Disruptor<>(PipelineEvent::new, bufferSize * 1024,
            Executors.defaultThreadFactory(), ProducerType.SINGLE, new SleepingWaitStrategy(100, 1000));
        // bind event handlers to the disruptor:
        // all of the event handlers will be broadcast to all of the event messages.
        disruptor.handleEventsWith(new PipeRealTimeStorageHandler());
        disruptor.handleEventsWith(new PipeHistoricalStorageHandler());
        disruptor.handleEventsWith(new PipeTreeStorageHandler());
        disruptor.handleEventsWith(new PipeFingerprintStorageHandler());
        disruptor.setDefaultExceptionHandler(new MetricExceptionHandler());
        disruptor.start();
        log.info("disruptor started with buffer size: {}", bufferSize);
        return new PipelineEventProducer(disruptor.getRingBuffer());
    }

    /**
     * system shutdown, release resources
     */
    @PreDestroy
    public void destroy() {
        // system shutdown, release resources
        HistoricalStorageCleanerManager.unregister();
        StorageHandlerUtils.shutdownNow();
    }
}

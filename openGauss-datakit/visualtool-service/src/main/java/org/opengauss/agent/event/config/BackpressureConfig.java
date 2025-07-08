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

import org.opengauss.agent.event.MetricEvent;
import org.opengauss.agent.event.handler.HistoricalStorageHandler;
import org.opengauss.agent.event.handler.MetricExceptionHandler;
import org.opengauss.agent.event.handler.RealTimeStorageHandler;
import org.opengauss.agent.event.producer.BackpressureStrategy;
import org.opengauss.agent.event.producer.DynamicBackpressureStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * BackpressureConfig
 *
 * @author: wangchao
 * @Date: 2025/3/18 15:57
 * @Description: BackpressureConfig
 * @since 7.0.0-RC2
 **/
@Configuration
public class BackpressureConfig {
    /**
     * capacity Threshold Percent
     */
    @Value("${disruptor.backpressure.threshold:10}")
    private int capacityThresholdPercent;

    /**
     * init Delay Ms
     */
    @Value("${disruptor.backpressure.initDelay:1}")
    private long initDelayMs;

    /**
     * max Delay Ms
     */
    @Value("${disruptor.backpressure.maxDelay:100}")
    private long maxDelayMs;

    /**
     * delay Factor
     */
    @Value("${disruptor.backpressure.factor:2}")
    private double delayFactor;
    @Value("${disruptor.bufferSize:16}")
    private int bufferSize;

    /**
     * backpressure Strategy
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
     * @return BackpressureStrategy
     */
    @Bean
    public BackpressureStrategy backpressureStrategy() {
        Disruptor<MetricEvent> disruptor = new Disruptor<>(MetricEvent::new, bufferSize * 1024,
            Executors.defaultThreadFactory(), ProducerType.SINGLE,  // 多生产者模式
            new SleepingWaitStrategy(100, 1000));
        // bind event handlers to the disruptor:
        // all of the event handlers will be broadcast to all of the event messages.
        disruptor.handleEventsWith(new RealTimeStorageHandler()).then(new HistoricalStorageHandler());
        disruptor.setDefaultExceptionHandler(new MetricExceptionHandler());
        disruptor.start();
        return new DynamicBackpressureStrategy(disruptor.getRingBuffer(), capacityThresholdPercent, initDelayMs,
            maxDelayMs, delayFactor);
    }
}

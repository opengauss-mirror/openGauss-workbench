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

import org.opengauss.agent.data.CustomEventConfig;
import org.opengauss.agent.data.CustomMetricData;
import org.opengauss.agent.event.MetricEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * DynamicBackpressureStrategy
 *
 * @author: wangchao
 * @Date: 2025/3/18 15:58
 * @Description: DynamicBackpressureStrategy
 * @since 7.0.0-RC2
 **/
public class DynamicBackpressureStrategy implements BackpressureStrategy {
    private final RingBuffer<MetricEvent> ringBuffer;
    private final long initDelayNanos;
    private final long maxDelayNanos;
    private final double factor;
    private volatile long currentDelayNanos;
    private volatile int threshold;
    private final AtomicLong pressureCounter = new AtomicLong();

    /**
     * Constructor dynamic backpressure strategy
     *
     * @param ringBuffer RingBuffer<MetricEvent> ring buffer
     * @param thresholdPercent threshold percent
     * @param initDelayMs init delay ms
     * @param maxDelayMs max delay ms
     * @param factor factor
     */
    public DynamicBackpressureStrategy(RingBuffer<MetricEvent> ringBuffer, int thresholdPercent, long initDelayMs,
        long maxDelayMs, double factor) {
        this.ringBuffer = ringBuffer;
        this.threshold = (int) (ringBuffer.getBufferSize() * thresholdPercent / 100.0);
        this.initDelayNanos = TimeUnit.MILLISECONDS.toNanos(initDelayMs);
        this.maxDelayNanos = TimeUnit.MILLISECONDS.toNanos(maxDelayMs);
        this.factor = factor;
        this.currentDelayNanos = initDelayNanos;
    }

    /**
     * Should apply backpressure
     *
     * @return boolean
     */
    public boolean shouldApplyBackpressure() {
        return ringBuffer.remainingCapacity() < threshold;
    }

    @Override
    public boolean tryPublishEvent(
        EventTranslatorTwoArg<MetricEvent, CustomEventConfig, List<CustomMetricData>> translator,
        Pair<CustomEventConfig, List<CustomMetricData>> data) {
        return ringBuffer.tryPublishEvent(translator, data.getKey(), data.getValue());
    }

    @Override
    public void setThresholdPercent(int percent) {
        this.threshold = percent;
    }

    @Override
    public BackpressureStatus getBackpressureStatus() {
        return new BackpressureStatus(currentDelayNanos / 1000, pressureCounter.get(), ringBuffer.remainingCapacity());
    }

    @Override
    public void applyBackpressure() {
        long delay = currentDelayNanos;
        int consecutivePressure = 0;
        while (shouldApplyBackpressure()) {
            if (consecutivePressure > 5) {
                delay = Math.min(delay * 2, maxDelayNanos);
            } else {
                delay = (long) (delay * factor);
            }
            LockSupport.parkNanos(delay);
            pressureCounter.incrementAndGet();
            consecutivePressure++;
            if (consecutivePressure % 10 == 0) {
                threshold = (int) (ringBuffer.getBufferSize() * Math.min(0.3,
                    0.1 + consecutivePressure * 0.02));
            }
        }
        currentDelayNanos = Math.max(delay / 2, initDelayNanos);
    }

    /**
     * Reset delay
     */
    public void resetDelay() {
        currentDelayNanos = initDelayNanos;
    }
}

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

import com.lmax.disruptor.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.event.BaseEvent;
import org.opengauss.agent.event.MetricEvent;
import org.opengauss.agent.event.PipelineEvent;

/**
 * MetricExceptionHandler
 *
 * @author: wangchao
 * @Date: 2025/3/13 10:13
 * @Description: EventExceptionHandler
 * @since 7.0.0-RC2
 **/
@Slf4j
public class MetricExceptionHandler implements ExceptionHandler<BaseEvent> {
    @Override
    public void handleEventException(Throwable ex, long sequence, BaseEvent event) {
        if (event instanceof MetricEvent) {
            log.error("Error processing metric event: {}", ((MetricEvent) event).info(), ex);
        } else if (event instanceof PipelineEvent) {
            log.error("Error processing pipeline event: {}", ((PipelineEvent) event).info(), ex);
        } else {
            log.error("Error processing event: {}", event.toString(), ex);
        }
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("Error starting metric event processor", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("Error shutting down metric event processor", ex);
    }
}

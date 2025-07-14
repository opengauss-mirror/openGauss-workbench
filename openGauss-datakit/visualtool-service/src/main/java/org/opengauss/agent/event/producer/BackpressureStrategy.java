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

import org.opengauss.agent.data.CustomEventConfig;
import org.opengauss.agent.data.CustomMetricData;
import org.opengauss.agent.event.MetricEvent;

import java.util.List;

/**
 * BackpressureStrategy
 *
 * @author: wangchao
 * @Date: 2025/3/18 16:17
 * @Description: BackpressureStrategy
 * @since 7.0.0-RC2
 **/
public interface BackpressureStrategy {
    /**
     * shouldApplyBackpressure
     *
     * @return boolean
     */
    boolean shouldApplyBackpressure();

    /**
     * applyBackpressure
     */
    void applyBackpressure();

    /**
     * tryPublishEvent
     *
     * @param translator EventTranslatorTwoArg
     * @param data Pair<CustomEventConfig, List<CustomMetricData>>
     * @return boolean
     */
    boolean tryPublishEvent(EventTranslatorTwoArg<MetricEvent, CustomEventConfig, List<CustomMetricData>> translator,
        Pair<CustomEventConfig, List<CustomMetricData>> data);

    /**
     * setThresholdPercent
     *
     * @param percent int
     */
    void setThresholdPercent(int percent);

    /**
     * getBackpressureStatus
     *
     * @return BackpressureStatus
     */
    BackpressureStatus getBackpressureStatus();
}

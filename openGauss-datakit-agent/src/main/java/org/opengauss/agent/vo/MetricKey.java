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

package org.opengauss.agent.vo;

import lombok.Data;

/**
 * MetricKey
 *
 * @author: wangchao
 * @Date: 2025/5/6 09:53
 * @Description: MetricKey
 * @since 7.0.0-RC2
 **/
@Data
public class MetricKey {
    private final String metricName;
    private final String propValue;

    /**
     * Constructor
     *
     * @param metricName metric name
     * @param propValue prop value
     */
    private MetricKey(String metricName, String propValue) {
        this.metricName = metricName;
        this.propValue = propValue;
    }

    /**
     * Create MetricKey
     *
     * @param metricName metric name
     * @param propValue prop value
     * @return MetricKey
     */
    public static MetricKey of(String metricName, String propValue) {
        return new MetricKey(metricName, propValue);
    }

    /**
     * Create MetricKey
     *
     * @param metricName metric name
     * @return MetricKey
     */
    public static MetricKey of(String metricName) {
        return new MetricKey(metricName, null);
    }
}

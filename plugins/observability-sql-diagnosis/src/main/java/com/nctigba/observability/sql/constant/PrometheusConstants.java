/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.constant;

/**
 * PrometheusConstants
 *
 * @author luomeng
 * @since 2023/6/11
 */
public class PrometheusConstants {
    // prometheus query ?query={query}
    public static final String PROMETHEUS_QUERY_POINT = "/api/v1/query";

    // prometheus range query   ?query={query}&start={start}&end={end}&step={step}
    public static final String PROMETHEUS_QUERY_RANGE = "/api/v1/query_range";
    public static final String STEP = "15";
    public static final int MINUTE = 60;
    public static final long MS = 1000L;
    public static final int MAX_STEP_NUM = 10000;
    public static final int MIN_STEP_NUM = 120;
}

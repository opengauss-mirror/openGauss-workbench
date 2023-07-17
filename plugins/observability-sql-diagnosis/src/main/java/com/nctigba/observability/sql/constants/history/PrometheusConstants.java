/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.constants.history;

import lombok.Generated;

/**
 * PrometheusConstants
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Generated
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

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.constants;

public class MonitoringConstants {
    // prometheus query ?query={query}
    public static final String PROMETHEUS_QUERY_POINT = "/api/v1/query";

    // prometheus range query ?query={query}&start={start}&end={end}&step={step}
    public static final String PROMETHEUS_QUERY_RANGE = "/api/v1/query_range";
}

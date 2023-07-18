/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.constants.history;

/**
 * MetricCommon
 *
 * @author luomeng
 * @since 2023/6/11
 */
public class MetricCommon {
    public static final String AVG_CPU_USAGE_RATE =
            "(avg(sum(irate(agent_cpu_seconds_total{mode!='idle',"
                    + "host=\"history_diagnosis_hostId\"}[5m]))by (cpu))) * 100";
    public static final String DB_AVG_CPU_USAGE_RATE = "top_db_cpu{instanceId=\"history_diagnosis_nodeId\"}/8";
    public static final String ACTIVITY_NUM =
            "sum(gauss_thread_wait_status_count{instanceId=\"history_diagnosis_nodeId\"})";
    public static final String THREAD_POOL_USAGE_RATE =
            "local_threadpool_status_pool_utilization_rate{instanceId=\"history_diagnosis_nodeId\"}";
    public static final String WAIT_EVENT = "gauss_thread_wait_status_count{instanceId=\"history_diagnosis_nodeId\"}";

    /**
     * Business connection number constant value
     */
    public static final String BUSINESS_CONN_COUNT =
            "sum(pg_stat_activity_count{instanceId=\"history_diagnosis_nodeId\"})";
}

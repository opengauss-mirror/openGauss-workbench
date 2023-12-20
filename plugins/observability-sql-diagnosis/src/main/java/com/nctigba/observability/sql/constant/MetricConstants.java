/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  MetricConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/constant/MetricConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.constant;

/**
 * MetricConstants
 *
 * @author luomeng
 * @since 2023/6/11
 */
public class MetricConstants {
    /**
     * Os avg cpu usage rate value
     */
    public static final String AVG_CPU_USAGE_RATE =
            "(avg(sum(irate(agent_cpu_seconds_total{mode!='idle',"
                    + "host=\"history_diagnosis_hostId\"}[5m]))by (cpu))) * 100";

    /**
     * Db avg cpu usage rate value
     */
    public static final String DB_AVG_CPU_USAGE_RATE = "top_db_cpu{instanceId=\"history_diagnosis_nodeId\"}"
            + "/label_join((count(agent_cpu_seconds_total{mode='system',host=\"history_diagnosis_hostId\"}) "
            + "by (host,instance,type,job)), \"instanceId\", "
            + "\"history_diagnosis_nodeId\", \"tag1\", \"tag2\")";

    /**
     * Db activity num
     */
    public static final String ACTIVITY_NUM =
            "sum(gauss_thread_wait_status_count{instanceId=\"history_diagnosis_nodeId\"})";

    /**
     * Db thread pool usage rate
     */
    public static final String THREAD_POOL_USAGE_RATE =
            "local_threadpool_status_pool_utilization_rate{instanceId=\"history_diagnosis_nodeId\"}";

    /**
     * Db wait event
     */
    public static final String WAIT_EVENT = "gauss_thread_wait_status_count{instanceId=\"history_diagnosis_nodeId\"}";

    /**
     * Business connection number constant value
     */
    public static final String BUSINESS_CONN_COUNT =
            "sum(pg_stat_activity_count{instanceId=\"history_diagnosis_nodeId\"})";

    /**
     * Cpu core num
     */
    public static final String CPU_CORE_NUM =
            "count(agent_cpu_seconds_total{mode='system',host=\"history_diagnosis_hostId\"}) by (host,instance)";

    /**
     * Swap in
     */
    public static final String SWAP_IN = "agent_vmstat_si{host=\"history_diagnosis_hostId\"}";

    /**
     * Swap out
     */
    public static final String SWAP_OUT = "agent_vmstat_so{host=\"history_diagnosis_hostId\"}";
}

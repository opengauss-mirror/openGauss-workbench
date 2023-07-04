/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.constants.history;

/**
 * ThresholdCommon
 *
 * @author luomeng
 * @since 2023/6/11
 */
public class ThresholdCommon {
    public static final String CPU_USAGE_RATE = "cpuUsageRate";
    public static final String DB_CPU_USAGE_RATE = "dbCpuUsageRate";
    public static final String PRO_CPU_USAGE_RATE = "proCpuUsageRate";
    public static final String ACTIVITY_NUM = "activityNum";
    public static final String CONNECTION_NUM = "connectionNum";
    public static final String THREAD_POOL_USAGE_RATE = "threadPoolUsageRate";
    public static final String WAIT_EVENT_NUM = "waitEventNum";
    public static final String DURING = "duration";
    public static final String SQL_NUM = "sqlNum";
    public static final String INSERT =
            "insert into his_diagnosis_threshold_info(threshold_type,threshold,threshold_name,"
                    + "threshold_value,threshold_unit,threshold_detail,sort_no) ";
}

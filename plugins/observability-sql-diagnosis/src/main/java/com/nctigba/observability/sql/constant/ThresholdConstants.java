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
 *  ThresholdConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/constant/ThresholdConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.constant;

/**
 * ThresholdConstants
 *
 * @author luomeng
 * @since 2023/6/11
 */
public class ThresholdConstants {
    public static final String CPU_USAGE_RATE = "cpuUsageRate";
    public static final String DB_CPU_USAGE_RATE = "dbCpuUsageRate";
    public static final String PRO_CPU_USAGE_RATE = "proCpuUsageRate";
    public static final String ACTIVITY_NUM = "activityNum";

    /**
     * Business connection number threshold constant value
     */
    public static final String CONNECTION_NUM = "connectionNum";
    public static final String THREAD_POOL_USAGE_RATE = "threadPoolUsageRate";
    public static final String WAIT_EVENT_NUM = "waitEventNum";
    public static final String DURING = "duration";
    public static final String SQL_NUM = "sqlNum";
    public static final String INSERT =
            "insert into his_diagnosis_threshold_info(id,threshold_type,threshold,threshold_name,"
                    + "threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type) ";

    /**
     * Swap in/out num
     */
    public static final String SWAP_NUM = "swapNum";

    /**
     * Random Page Cost
     */
    public static final String RANDOM_PAGE_COST = "randomPageCost";

    /**
     * Db memory usage rate
     */
    public static final String DB_MEM_USAGE_RATE = "dbMemUsageRate";

    /**
     * Db dynamic memory usage rate
     */
    public static final String SESSION_MEM_USAGE_RATE = "sessionMemUsageRate";

    /**
     * Db share memory usage rate
     */
    public static final String SHARE_MEM_USAGE_RATE = "shareMemUsageRate";
}

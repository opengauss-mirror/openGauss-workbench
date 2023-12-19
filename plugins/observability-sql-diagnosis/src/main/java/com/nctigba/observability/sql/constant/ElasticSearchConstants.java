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
 *  ElasticSearchConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/constant/ElasticSearchConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.constant;

/**
 * ElasticSearchConstants
 *
 * @author luomeng
 * @since 2023/6/11
 */
public class ElasticSearchConstants {
    public static final String LOCK_TIMEOUT = "Lock wait timeout";
    public static final String DEADLOCK = "deadlock detected";
    public static final String HYPHEN = "-";
    public static final String FIELDS = "fields";
    public static final String LOG_TYPE = "log_type";
    public static final String LOG_LEVEL = "log_level";
    public static final String MESSAGE = "message";
    public static final String CLUSTER_ID = "clusterId";
    public static final String NODE_ID = "nodeId";
    public static final String TIMESTAMP = "@timestamp";
}

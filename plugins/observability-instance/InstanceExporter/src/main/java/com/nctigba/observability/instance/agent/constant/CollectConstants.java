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
 *  CollectConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/constant/CollectConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.constant;

import java.util.Arrays;
import java.util.List;

/**
 * Constants strings
 *
 * @since 2023/12/1
 */
public class CollectConstants {
    /**
     * Separator to build cache key
     *
     * @since 2023/12/1
     */
    public static final String SEPARATOR = "_";

    /**
     * When call local, use this ip
     *
     * @since 2023/12/1
     */
    public static final String LOCAL_IP = "127.0.0.1";

    /**
     * SSH port
     *
     * @since 2023/12/1
     */
    public static final String SSH_PORT = "22";

    /**
     * default database name
     *
     * @since 2023/12/1
     */
    public static final String DEFAULT_DATABASE = "postgres";

    /**
     * Metric cache effective time
     *
     * @since 2023/12/1
     */
    public static final long CACHE_TIME_OUT = 3000L;

    /**
     * Min collect interval supported
     *
     * @since 2023/12/1
     */
    public static final long MIN_COLLECT_INTERVAL = 5000L;

    /**
     * Database metric default labels
     *
     * @since 2023/12/1
     */
    public static final List<String> DB_DEFAULT_METRIC_LABELS = Arrays.asList("host", "instanceId");

    /**
     * PRIMARY_ROLE
     */
    public static final String PRIMARY_ROLE = "primary";

    /**
     * THREAD_TIMEOUT
     */
    public static final int THREAD_TIMEOUT = 1;

    /**
     * THREAD_TIMEOUT
     */
    public static final int COLLECT_TIMEOUT = 1;
}

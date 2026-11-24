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

package com.tools.monitor.common.contant;

/**
 * ConmmonShare
 *
 * @author liu
 * @since 2022-10-01
 */
public class ConmmonShare {
    /**
     * PROM
     */
    public static final String PROM = "Prometheus";

    /**
     * ZABBIX
     */
    public static final String ZABBIX = "Zabbix";

    /**
     * NAGIOS
     */
    public static final String NAGIOS = "Nagios";

    /**
     * SYSTEMTARGET
     */
    public static final String SYSTEMTARGET = "system_default";

    /**
     * DEFAULTERNUM
     */
    public static final long DEFAULTERNUM = 1669780925627L;

    /**
     * Http request
     */
    public static final String HTTP = "http://";

    /**
     * Https request
     */
    public static final String HTTPS = "https://";

    /**
     * Universal Success Marker
     */
    public static final String SUCCESS = "0";

    /**
     * Universal failure flag
     */
    public static final String FAIL = "1";

    /**
     * Scheduled task whitelist configuration
     */
    public static final String[] JOB_WHITELIST_STR = {"com.tools.monitor"};

    /**
     * SECOND
     */
    public static final String SECOND = "second";

    /**
     * MINUTE
     */
    public static final String MINUTE = "minute";

    /**
     * HOUR
     */
    public static final String HOUR = "hour";

    /**
     * DAY
     */
    public static final String DAY = "day";

    /**
     * WEEK
     */
    public static final String WEEK = "week";

    /**
     * MONTH
     */
    public static final String MONTH = "month";

    /**
     * YEAR
     */
    public static final String YEAR = "year";
}

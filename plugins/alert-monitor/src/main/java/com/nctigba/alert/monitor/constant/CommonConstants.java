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
 *  CommonConstants.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/constant/CommonConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.constant;

/**
 * @author wuyuebin
 * @date 2023/4/27 17:49
 * @description
 */
public class CommonConstants {
    public static final String PROMETHEUS_YML = "prometheus.yml";
    public static final Integer IS_DELETE = 1;
    public static final Integer IS_NOT_DELETE = 0;
    public static final Integer UNREAD_STATUS = 0;
    public static final Integer READ_STATUS = 1;
    public static final Integer FIRING_STATUS = 0;
    public static final Integer RECOVER_STATUS = 1;
    public static final String SERIOUS = "serious";
    public static final String WARN = "warn";
    public static final String INFO = "info";
    public static final String DELIMITER = ",";
    public static final Integer IS_NOT_REPEAT = 0;
    public static final Integer IS_REPEAT = 1;
    public static final Integer IS_NOT_SILENCE = 0;
    public static final Integer IS_SILENCE = 1;
    public static final String EMAIL = "email";
    public static final String WE_COM = "WeCom";
    public static final String DING_TALK = "DingTalk";

    /**
     * webhook
     */
    public static final String WEBHOOK = "webhook";

    /**
     * SNMP
     */
    public static final String SNMP = "SNMP";
    public static final Integer ENABLE = 1;
    public static final Integer DISABLE = 0;
    //dev use "/"
    public static final String SLASH = "/";

    /**
     * \n
     */
    public static final String LINE_SEPARATOR = String.valueOf((char) 10);

    /**
     * ruleType: index
     */
    public static final String INDEX_RULE = "index";

    /**
     * ruleType: log
     */
    public static final String LOG_RULE = "log";

    /**
     * schedule name prefix
     */
    public static final String THREAD_NAME_PREFIX = "alertRule-";

    /**
     * schedule type: fixedDelay
     */
    public static final String FIXED_DELAY = "fixedDelay";

    /**
     * schedule type: fixedRate
     */
    public static final String FIXED_RATE = "fixedRate";

    /**
     * day
     */
    public static final String DAY = "d";

    /**
     * hour
     */
    public static final String HOUR = "h";

    /**
     * minute
     */
    public static final String MINUTE = "m";

    /**
     * second
     */
    public static final String SECOND = "s";

    /**
     * second to millisecond
     */
    public static final Long SEC_TO_MILL = 1000L;

    /**
     * minute to second
     */
    public static final Long MINUTE_TO_SEC = 60L;

    /**
     * hour to minute
     */
    public static final Long HOUR_TO_MINUTE = 60L;

    /**
     * day to hour
     */
    public static final Long DAY_TO_HOUR = 24L;

    /**
     * unlock
     */
    public static final Integer UNLOCK = 0;

    /**
     * locked
     */
    public static final Integer LOCKED = 1;

    /**
     * es logs: fields
     */
    public static final String FIELDS = "fields";

    /**
     * es logs: log_type
     */
    public static final String LOG_TYPE = "log_type";

    /**
     * es logs: log_level
     */
    public static final String LOG_LEVEL = "log_level";

    /**
     * es logs: message
     */
    public static final String MESSAGE = "message";

    /**
     * es logs: clusterId
     */
    public static final String CLUSTER_ID = "clusterId";

    /**
     * es logs: nodeId
     */
    public static final String NODE_ID = "nodeId";

    /**
     * es logs: @timestamp
     */
    public static final String TIMESTAMP = "@timestamp";

    /**
     * WeCom or DingTalk send way: app
     */
    public static final Integer APP_SEND_WAY = 0;

    /**
     * WeCom or DingTalk send way: robot
     */
    public static final Integer ROBOT_SEND_WAY = 1;

    /**
     * datatime format
     */
    public static final String DATETIME_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'+08:00'";

    /**
     * prometheus evaluation_interval
     */
    public static final String EVALUATION_INTERVAL = "15s";

    /**
     * notify is unsend
     */
    public static final Integer UNSEND = 0;

    /**
     * notify is send
     */
    public static final Integer SEND = 1;

    /**
     * notify is ignored
     */
    public static final Integer SEND_IGNORE = 2;

    /**
     * exception
     */
    public static final String NODE_NOT_FOUND = "node not found";

    /**
     * jdbc protocol
     */
    public static final String JDBC_OPENGAUSS = "jdbc:opengauss://";
}

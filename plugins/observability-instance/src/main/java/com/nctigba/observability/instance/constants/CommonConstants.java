/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.constants;

public class CommonConstants {
    private CommonConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String THREAD_IS_INTERRUPTED = "thread is interrupted";
    public static final String SET_LIMIT_EXCEPTION = "set ulimit exception";
    public static final String FAILED_TO_GRANT_PERMISSION = "Failed to grant permission";
    public static final String EQUAL_SYMBOL = "=";
    public static final String BLANK = " ";
    public static final String SLASH = "/";
    public static final String CONNECTION_ESTABLISHMENT_FAIL = "Connection establishment fail";
    public static final String INSERT_INTO_PARAM_INFO_SQL = "insert into param_info(paramType,paramName,"
            + "paramDetail,suggestValue,defaultValue,unit,suggestExplain,diagnosisRule)\n";
    public static final String HASH_COND = "Hash Cond";
    public static final String COST = "(cost=";
    public static final String SUCCESS = "success";
    public static final String NAME = "success";
    public static final String CONNECTION_FAIL = "get database connection fail";
    public static final String JDBC_OPENGAUSS = "jdbc:opengauss://";
    public static final String NODE_NOT_FOUND = "node not found";
    public static final String OPEN_EULER = "openEuler";
    public static final String PROMETHEUS_YML = "/prometheus.yml";
    public static final String HOST_NOT_EXIST = "host information does not exist";
    public static final String INSTALLATION_USER_NOT_EXIST = "Installation user information does not exist";
    public static final String FAILED_TO_ESTABLISH_HOST = "Failed to establish session with host";
    public static final String CURRENT_VERSION_NOT_SUPPORT = "The current version does not support";
    public static final String GENERATE_WDR_SNAPSHOT_EXCEPTION = "Generate wdr snapshot exception";
    public static final String GENERATE_WDR__EXCEPTION = "generate wdr exception";
    public static final String HOST_NOT_FOUND = "host not found";
    public static final String FAILED_SET_ENABLE_WDR_SNAPSHOT_PARAMETER = "Failed to set the enable_wdr_snapshot parameter";
    public static final String SQLITE_ERROR = "sqlite error";
    public static final String NUM_21299200 = "21299200";
    public static final String NUM_129024 = "129024";
    public static final String NUM_65535 = "65535";
}
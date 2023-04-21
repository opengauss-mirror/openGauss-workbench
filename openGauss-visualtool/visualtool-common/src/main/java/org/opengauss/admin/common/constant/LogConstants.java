/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * LogConstants.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/constant/LogConstants.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.constant;

/**
 * System log constants
 */
public class LogConstants {

    /**
     * The log level key
     */
    public static final String SYS_LOG_CONFIG_KEY_LEVEL = "log_level";

    /**
     * The MaxFileSize key
     */
    public static final String SYS_LOG_CONFIG_KEY_MAX_FILE_SIZE = "log_max_file_size";

    /**
     * The TotalSizeCap key
     */
    public static final String SYS_LOG_CONFIG_KEY_TOTAL_SIZE_CAP = "log_total_size_cap";

    /**
     * The MaxHistory key
     */
    public static final String SYS_LOG_CONFIG_KEY_MAX_HISTORY = "log_max_history";

    public static final Integer DEFAULT_MAX_HISTORY = 60;

    public static final String DEFAULT_TOTAL_SIZE_CAP = "10gb";

    public static final String DEFAULT_MAX_FILE_SIZE = "20mb";

    public static final String DEFAULT_LEVEL = "info";

    public static final String DEFAULT_APPENDER = "file_info";

}

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

package com.tools.monitor.common.contant;

/**
 * ScheduleConstants
 *
 * @author liu
 * @since 2022-10-01
 */
public class ScheduleConstants {
    /**
     * TASK_CLASS_NAME
     */
    public static final String TASK_CLASS_NAME = "TASK_CLASS_NAME";

    /**
     * key
     */
    public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

    /**
     * MISFIRE_DEFAULT
     */
    public static final String MISFIRE_DEFAULT = "0";

    /**
     * MISFIRE_IGNORE_MISFIRES
     */
    public static final String MISFIRE_IGNORE_MISFIRES = "1";

    /**
     * MISFIRE_FIRE_AND_PROCEED
     */
    public static final String MISFIRE_FIRE_AND_PROCEED = "2";

    /**
     * MISFIRE_DO_NOTHING
     */
    public static final String MISFIRE_DO_NOTHING = "3";

    /**
     * Status
     */
    public enum Status {
        /**
         * NORMAL
         */
        NORMAL("0"),
        /**
         * PAUSE
         */
        PAUSE("1");

        private String value;

        private Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

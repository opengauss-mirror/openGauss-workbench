/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.common.contant;

/**
 * ScheduleCommon
 *
 * @author liu
 * @since 2022-10-01
 */
public class ScheduleCommon {
    /**
     * TASK_CLASS_NAME
     */
    public static final String MONITOR_CLASS_NAME = "TASK_CLASS_NAME";

    /**
     * Execute target key
     */
    public static final String MONITOR_PROPERTIES = "TASK_PROPERTIES";

    /**
     * defaulter
     */
    public static final String MONITOR_DEFAULT = "0";

    /**
     * Trigger execution immediately
     */
    public static final String MONITOR_IGNORE_MISFIRES = "1";

    /**
     * Trigger an execution
     */
    public static final String MONITOR_FIRE_AND_PROCEED = "2";

    /**
     * Does not trigger immediate execution
     */
    public static final String MONITOR_DO_NOTHING = "3";

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

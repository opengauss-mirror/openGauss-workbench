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

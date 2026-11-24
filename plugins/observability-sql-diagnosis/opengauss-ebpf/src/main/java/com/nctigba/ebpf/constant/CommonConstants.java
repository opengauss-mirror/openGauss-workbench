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

package com.nctigba.ebpf.constant;

public class CommonConstants {
    /**
     * Blank
     */
    public static final String BLANK = " ";

    /**
     * Slash
     */
    public static final String SLASH = "/";

    /**
     * Stop ebpf monitor command
     */
    public static final String STOP_EBPF = "ps -ef | grep ./%s | grep -v grep | grep python3 | awk '{print $2,$3}';";

    /**
     * Stop os monitor command
     */
    public static final String STOP_OS = "ps -ef|grep %s | grep -v grep | grep %s | awk '{print $2,$3}'";

    /**
     * Kill command
     */
    public static final String KILL = "sudo kill -2 %s";

    /**
     * Kill command
     */
    public static final String KILL_9 = "sudo kill -9 %s";

    /**
     * Database pid monitor command
     */
    public static final String DB_MONITOR = "ps -eLf |grep gaussdb|grep -v grep|awk '{print $4}'";

    /**
     * Check pid is exists
     */
    public static final String CHECK_PID = "ps -p %s >/dev/null && echo \"true\" || echo \"false\"";
}

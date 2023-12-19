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
 *  OsTypeConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/constant/OsTypeConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.constant;

/**
 * <p>
 * cpu constants
 * </p>
 *
 * @author luomeng
 * @since 2022/10/17 09:00
 */
public class OsTypeConstants {
    /**
     * System top command
     */
    public static final String DEFAULT = "top";

    /**
     * System mpstat p command
     */
    public static final String MPSTAT = "mpstatP";

    /**
     * System sar q command
     */
    public static final String SAR = "sarq";

    /**
     * System pidstat 1 command
     */
    public static final String PIDSTAT = "pidstat1";

    /**
     * System top d command
     */
    public static final String CPU_AVG_LOAD = "topd";

    /**
     * System vmstat 1 command
     */
    public static final String VM_STAT = "vmstat1";

    /**
     * System iostat x command
     */
    public static final String IO_STAT = "iostatx";

    /**
     * System sar d command
     */
    public static final String SAR_D = "sard";
}

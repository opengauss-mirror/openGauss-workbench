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
 *  AgentParamConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/constant/AgentParamConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.constant;

/**
 * Agent param
 *
 * @author luomeng
 * @since 2023/6/11
 */
public class AgentParamConstants {
    /**
     * Os call top
     */
    public static final String TOP = "top";

    /**
     * Bcc call profile
     */
    public static final String PROFILE = "profile";

    /**
     * Bcc call offcputime
     */
    public static final String OFF_CPU_TIME = "offcputime";

    /**
     * Bcc call runqlen
     */
    public static final String RUN_Q_LEN = "runqlen";

    /**
     * Bcc call runqlat
     */
    public static final String RUN_Q_LAT = "runqlat";

    /**
     * Bcc call cachestat
     */
    public static final String CACHE_STAT = "cachestat";

    /**
     * Bcc call cachetop
     */
    public static final String CACHE_TOP = "cachetop";

    /**
     * Bcc call memleak
     */
    public static final String MEM_LEAK = "memleak";

    /**
     * Bcc call xfsdist
     */
    public static final String XFS_DIST = "xfsdist";

    /**
     * Bcc call xfsslower
     */
    public static final String XFS_SLOWER = "xfsslower";

    /**
     * Bcc call biolatency
     */
    public static final String BIO_LATENCY = "biolatency";

    /**
     * Bcc call biosnoop
     */
    public static final String BIO_SNOOP = "biosnoop";

    /**
     * Bcc call filetop
     */
    public static final String FILE_TOP = "filetop";

    /**
     * Bcc call tcplife
     */
    public static final String TCP_LIFE = "tcplife";

    /**
     * Bcc call tcptop
     */
    public static final String TCP_TOP = "tcptop";

    /**
     * Os call mpstatP
     */
    public static final String MP_STAT_P = "mpstatP";

    /**
     * Os call pidstat1
     */
    public static final String PID_STAT = "pidstat1";

    /**
     * Os call osParam
     */
    public static final String OS_PARAM = "osParam";

    /**
     * Os call cpuCoreNum
     */
    public static final String CPU_CORE_NUM = "cpuCoreNum";

    /**
     * Os call cpuAvgLoad
     */
    public static final String CPU_AVG_LOAD = "topd";

    /**
     * Os call vmStat
     */
    public static final String VM_STAT = "vmstat1";

    /**
     * Os call ioStat
     */
    public static final String IO_STAT = "iostatx";

    /**
     * Os call sar d
     */
    public static final String SAR_D = "sard";

    /**
     * Os call sar q
     */
    public static final String SAR_Q = "sarq";

    /**
     * Timeout time
     */
    public static final int TIMEOUT = 3000;
}

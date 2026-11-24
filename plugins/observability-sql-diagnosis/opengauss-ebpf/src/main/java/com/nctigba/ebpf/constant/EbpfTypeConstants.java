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

/**
 * <p>
 * ebpf constants
 * </p>
 *
 * @author luomeng
 * @since 2022/10/17 09:00
 */
public class EbpfTypeConstants {
    /**
     * Bcc stackcount command
     */
    public static final String STACKCOUNT = "stackcount";

    /**
     * Bcc profile command
     */
    public static final String PROFILE = "profile";

    /**
     * Bcc offcputime command
     */
    public static final String OFFCPUTIME = "offcputime";

    /**
     * Bcc runqlen command
     */
    public static final String RUNQLEN = "runqlen";

    /**
     * Bcc runqlat command
     */
    public static final String RUNQLAT = "runqlat";

    /**
     * Bcc memleak command
     */
    public static final String MEMLEAK = "memleak";

    /**
     * Bcc cachestat command
     */
    public static final String CACHESTAT = "cachestat";

    /**
     * Bcc cachetop command
     */
    public static final String CACHETOP = "cachetop";

    /**
     * Bcc ext4slower command
     */
    public static final String EXT4SLOWER = "ext4slower";

    /**
     * Bcc ext4dist command
     */
    public static final String EXT4DIST = "ext4dist";

    /**
     * Bcc xfsdist command
     */
    public static final String XFSDIST = "xfsdist";

    /**
     * Bcc xfsslower command
     */
    public static final String XFSSLOWER = "xfsslower";

    /**
     * Bcc biolatency command
     */
    public static final String BIOLATENCY = "biolatency";

    /**
     * Bcc biosnoop command
     */
    public static final String BIOSNOOP = "biosnoop";

    /**
     * Bcc filetop command
     */
    public static final String FILETOP = "filetop";

    /**
     * Bcc tcplife command
     */
    public static final String TCPLIFE = "tcplife";

    /**
     * Bcc tcptop command
     */
    public static final String TCPTOP = "tcptop";
}

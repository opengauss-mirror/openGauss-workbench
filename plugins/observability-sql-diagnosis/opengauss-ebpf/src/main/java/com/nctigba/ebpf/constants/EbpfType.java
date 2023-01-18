/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.constants;

/**
 * <p>
 * ebpf constants
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
public class EbpfType {
    public static final String DEFAULT = "stackcount";
    public static final String PROFILE = "profile";
    public static final String OFFCPUTIME = "offcputime";
    public static final String RUNQLEN = "runqlen";
    public static final String RUNQLAT = "runqlat";
    public static final String MEMLEAK = "memleak";
    public static final String CACHESTAT = "cachestat";
    public static final String CACHETOP = "cachetop";
    public static final String EXT4SLOWER = "ext4slower";
    public static final String EXT4DIST = "ext4dist";
    public static final String XFSDIST = "xfsdist";
    public static final String XFSSLOWER = "xfsslower";
    public static final String BIOLATENCY = "biolatency";
    public static final String BIOSNOOP = "biosnoop";
    public static final String FILETOP = "filetop";
    public static final String TCPLIFE = "tcplife";
    public static final String TCPTOP = "tcptop";
}

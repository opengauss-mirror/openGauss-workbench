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
 *  EbpfTypeEnum.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/enums/EbpfTypeEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * EbpfTypeEnum
 *
 * @author luomeng
 * @since 2024/2/23
 */
@Getter
@NoArgsConstructor
public enum EbpfTypeEnum {
    STACK_COUNT("stackcount", "./stackcount -p %s c:malloc"),
    PROFILE("profile", "./profile -af -L %s"),
    OFF_CPU_TIME("offcputime", "./offcputime -df -t %s"),
    RUN_Q_LEN("runqlen", "./runqlen -T 1"),
    RUN_Q_LAT("runqlat", "./runqlat -LT 1"),
    MEM_LEAK("memleak", "./memleak -p %s $(pidof allocs)"),
    CACHE_STAT("cachestat", "./cachestat -T"),
    CACHE_TOP("cachetop", "./cachetop 1"),
    EXT4SLOWER("ext4slower", "./ext4slower 1"),
    EXT4DIST("ext4dist", "./ext4dist 1"),
    XFS_DIST("xfsdist", "./xfsdist 1"),
    XFS_SLOWER("xfsslower", "./xfsslower 1"),
    BIO_LATENCY("biolatency", "./biolatency -DT 1"),
    BIO_SNOOP("biosnoop", "./biosnoop -Q"),
    FILE_TOP("filetop", "./filetop -Cr 10 -a"),
    TCP_LIFE("tcplife", "./tcplife -Tt"),
    TCP_TOP("tcptop", "./tcptop -C 3600");
    private String type;
    private String command;

    EbpfTypeEnum(String type, String command) {
        this.type = type;
        this.command = command;
    }
}

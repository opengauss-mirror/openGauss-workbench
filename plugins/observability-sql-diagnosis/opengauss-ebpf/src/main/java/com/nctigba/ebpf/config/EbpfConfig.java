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
 *  EbpfConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/config/EbpfConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * ebpf config
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@Data
@Configuration
public class EbpfConfig {
    @Value("${ebpfconfig.profile}")
    private String profile;
    @Value("${ebpfconfig.offcputime}")
    private String offcputime;
    @Value("${ebpfconfig.runqlen}")
    private String runqlen;
    @Value("${ebpfconfig.runqlat}")
    private String runqlat;
    @Value("${ebpfconfig.memleak}")
    private String memleak;
    @Value("${ebpfconfig.cachestat}")
    private String cachestat;
    @Value("${ebpfconfig.cachetop}")
    private String cachetop;
    @Value("${ebpfconfig.ext4slower}")
    private String ext4slower;
    @Value("${ebpfconfig.ext4dist}")
    private String ext4dist;
    @Value("${ebpfconfig.xfsdist}")
    private String xfsdist;
    @Value("${ebpfconfig.xfsslower}")
    private String xfsslower;
    @Value("${ebpfconfig.biolatency}")
    private String biolatency;
    @Value("${ebpfconfig.biosnoop}")
    private String biosnoop;
    @Value("${ebpfconfig.filetop}")
    private String filetop;
    @Value("${ebpfconfig.tcplife}")
    private String tcplife;
    @Value("${ebpfconfig.tcptop}")
    private String tcptop;
    @Value("${ebpfconfig.stackcount}")
    private String stackcount;
}

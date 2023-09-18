/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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

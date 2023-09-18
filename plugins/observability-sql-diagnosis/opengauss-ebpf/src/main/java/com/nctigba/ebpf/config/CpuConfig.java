/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.ebpf.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * cpu config
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/12/06 09:00
 */
@Data
@Configuration
public class CpuConfig {
    @Value("${osconfig.mpstatP}")
    private String mpstat;
    @Value("${osconfig.sarq}")
    private String sar;
    @Value("${osconfig.pidstat1}")
    private String pidstat;
}

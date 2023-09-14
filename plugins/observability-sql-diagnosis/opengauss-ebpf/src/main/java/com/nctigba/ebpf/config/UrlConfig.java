/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.ebpf.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * url config
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/10/17 09:00
 */
@Data
@Configuration
public class UrlConfig {
    @Value("${urlconfig.bccUrl}")
    private String bccUrl;
    @Value("${urlconfig.outputUrl}")
    private String outputUrl;
    @Value("${urlconfig.fgUrl}")
    private String fgUrl;
    @Value("${urlconfig.httpUrl}")
    private String httpUrl;
}

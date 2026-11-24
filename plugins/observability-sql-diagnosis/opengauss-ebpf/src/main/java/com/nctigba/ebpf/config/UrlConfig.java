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

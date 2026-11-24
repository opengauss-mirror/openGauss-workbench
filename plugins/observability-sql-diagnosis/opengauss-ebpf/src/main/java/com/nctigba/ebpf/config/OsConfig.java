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
 * cpu config
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/12/06 09:00
 */
@Data
@Configuration
public class OsConfig {
    @Value("${osconfig.mpstatP}")
    private String mpstat;
    @Value("${osconfig.sarq}")
    private String sar;
    @Value("${osconfig.pidstat1}")
    private String pidstat;
    @Value("${osconfig.cpuAvgLoad}")
    private String cpuAvgLoad;
    @Value("${osconfig.vmstat1}")
    private String vmstat1;
    @Value("${osconfig.iostatX1}")
    private String iostatX1;
    @Value("${osconfig.cpuCoreNum}")
    private String cpuCoreNum;
    @Value("${osconfig.sard}")
    private String sarD;
}

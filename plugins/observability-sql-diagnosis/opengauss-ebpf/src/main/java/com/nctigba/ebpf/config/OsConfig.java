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
 *  OsConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/config/OsConfig.java
 *
 *  -------------------------------------------------------------------------
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

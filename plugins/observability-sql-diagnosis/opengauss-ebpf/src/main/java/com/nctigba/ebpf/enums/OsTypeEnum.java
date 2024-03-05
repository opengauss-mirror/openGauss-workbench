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
 *  OsTypeEnum.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/enums/OsTypeEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.ebpf.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OsTypeEnum
 *
 * @author luomeng
 * @since 2023/12/7
 */
@Getter
@NoArgsConstructor
public enum OsTypeEnum {
    M_P_STAT("mpstatP", "mpstat -P ALL 1"),
    SAR("sarq", "sar -q 1"),
    PID_STAT("pidstat1", "pidstat 1"),
    CPU_AVG_LOAD("topd", "top -d 1"),
    VM_STAT("vmstat1", "vmstat 1"),
    IO_STAT("iostatx", "iostat -x 1"),
    SAR_D("sard", "sar -d 1");

    private String type;
    private String command;

    OsTypeEnum(String type, String command) {
        this.type = type;
        this.command = command;
    }
}

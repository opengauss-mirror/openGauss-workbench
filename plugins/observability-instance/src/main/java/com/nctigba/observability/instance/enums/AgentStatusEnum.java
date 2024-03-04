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
 *  AgentStatusEnum.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/enums/AgentStatusEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AgentStatusEnum
 *
 * @since 2024/2/19 16:16
 */
@Getter
@NoArgsConstructor
public enum AgentStatusEnum {
    NORMAL("normal"),
    UNKNOWN("unknown"),
    STARTING("starting"),
    STOPPING("stopping"),
    MANUAL_STOP("manualStop"),
    ERROR_THREAD_NOT_EXISTS("errorThreadNotExists"),
    ERROR_PROGRAM_UNHEALTHY("errorProgramUnhealthy");
    private String status;

    AgentStatusEnum(String status) {
        this.status = status;
    }
}

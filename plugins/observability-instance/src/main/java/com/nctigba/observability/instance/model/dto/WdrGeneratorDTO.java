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
 *  WdrGeneratorDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/dto/WdrGeneratorDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.nctigba.observability.instance.model.entity.OpsWdrDO.WdrScopeEnum;
import com.nctigba.observability.instance.model.entity.OpsWdrDO.WdrTypeEnum;

import lombok.Data;

@Data
public class WdrGeneratorDTO {
    @NotBlank(message = "The cluster ID cannot be empty")
    private String clusterId;
    @NotNull(message = "The report scope cannot be empty")
    private WdrScopeEnum scope;
    private String hostId;
    @NotNull(message = "The report type cannot be empty")
    private WdrTypeEnum type;
    @NotNull(message = "start Snapshot ID cannot be empty")
    private String startId;
    @NotNull(message = "end Snapshot ID cannot be empty")
    private String endId;
}
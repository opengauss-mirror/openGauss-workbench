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

package com.nctigba.observability.instance.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
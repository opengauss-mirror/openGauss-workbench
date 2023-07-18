/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.nctigba.observability.instance.entity.OpsWdrEntity.WdrScopeEnum;
import com.nctigba.observability.instance.entity.OpsWdrEntity.WdrTypeEnum;

import lombok.Data;

@Data
public class WdrGeneratorBody {
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
/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.model;

import lombok.Data;
import lombok.Generated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author wuyuebin
 * @date 2023/4/26 16:40
 * @description
 */
@Data
@Generated
public class AlertClusterNodeConfReq {
    @NotBlank
    private String clusterNodeIds;
    @NotNull
    private Long templateId;
}

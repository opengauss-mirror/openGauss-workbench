/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author wuyuebin
 * @date 2023/4/26 16:40
 * @description
 */
@Data
public class AlertClusterNodeConfReq {
    @NotBlank
    private String clusterNodeIds; // 实例nodeId，多个，逗号分隔
    @NotNull
    private Long templateId; // 告警模板ID
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/23 18:30
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertClusterNodeAndTemplateReq {
    @NotBlank
    private String clusterNodeIds;
    @NotBlank
    private String templateName;

    @NotNull
    @Valid
    private List<AlertTemplateRuleReq> templateRuleReqList;
}

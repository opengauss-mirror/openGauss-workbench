/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author wuyuebin
 * @date 2023/5/17 00:10
 * @description
 */
@Data
public class AlertTemplateRuleReq {
    private Long templateRuleId;
    @NotNull
    private Long ruleId;
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.model;

import lombok.Data;
import lombok.Generated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/17 00:07
 * @description
 */
@Data
@Generated
public class AlertTemplateReq {
    private Long id;
    @NotBlank
    private String templateName;

    @Valid
    @NotNull
    @Size(min = 1)
    private List<AlertTemplateRuleReq> templateRuleReqList;
}

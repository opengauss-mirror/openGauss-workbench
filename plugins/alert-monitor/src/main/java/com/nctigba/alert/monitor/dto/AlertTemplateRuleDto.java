/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wuyuebin
 * @date 2023/5/13 16:18
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertTemplateRuleDto extends AlertTemplateRule {
    private String ruleExpDesc;
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import com.nctigba.alert.monitor.entity.AlertRule;

/**
 * @author wuyuebin
 * @date 2023/5/15 17:37
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertRuleDto extends AlertRule {
    private String ruleExpDesc;
}

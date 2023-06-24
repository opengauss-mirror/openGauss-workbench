/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.opengauss.plugin.alertcenter.entity.AlertRule;

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

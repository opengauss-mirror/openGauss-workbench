/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import lombok.Data;
import com.nctigba.alert.monitor.config.properties.RuleItemProperty;

/**
 * @author wuyuebin
 * @date 2023/5/29 11:07
 * @description
 */
@Data
public class RuleItemPropertyDto extends RuleItemProperty {
    private String i18nName;
}

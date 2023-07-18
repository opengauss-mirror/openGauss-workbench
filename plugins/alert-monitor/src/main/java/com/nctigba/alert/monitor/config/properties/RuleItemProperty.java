/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/29 10:46
 * @description
 */
@Data
public class RuleItemProperty {
    private String name;
    private String unit;
    @NestedConfigurationProperty
    private List<RuleItemExpProperty> ruleExps;
    @NestedConfigurationProperty
    private List<RuleItemParamProperty> params;
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/27 15:21
 * @description
 */
@ConfigurationProperties(prefix = "alert")
@Component
@Data
public class AlertProperty {
    private String ruleFilePrefix;

    private String ruleFileSuffix;
    @NestedConfigurationProperty
    private AlertmanagerProperty alertmanager;

    @NestedConfigurationProperty
    private List<RuleItemProperty> ruleItems;
}

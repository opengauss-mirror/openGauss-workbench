/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AlertProperty.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/config/property/AlertProperty.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.config.property;

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

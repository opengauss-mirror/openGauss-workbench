/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.properties;

import lombok.Data;

/**
 * @author wuyuebin
 * @date 2023/5/29 10:59
 * @description
 */
@Data
public class RuleItemParamProperty {
    private String paramName;
    private String paramValue;
    private Integer paramOrder;
}

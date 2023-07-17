/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.model;

import lombok.Data;
import lombok.Generated;

/**
 * @author wuyuebin
 * @date 2023/5/9 09:58
 * @description
 */
@Data
@Generated
public class RuleReq {
    private String ruleName;
    private String ruleType;
    private String level;
}

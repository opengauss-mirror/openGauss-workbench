/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;
import com.nctigba.alert.monitor.entity.AlertTemplate;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/15 18:48
 * @description
 */
@Data
@Generated
@Accessors(chain = true)
public class AlertTemplateDto extends AlertTemplate {
    List<AlertTemplateRuleDto> templateRuleDtoList;
}

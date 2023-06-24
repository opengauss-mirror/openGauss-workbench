/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.opengauss.plugin.alertcenter.entity.AlertTemplate;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/15 18:48
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertTemplateDto extends AlertTemplate {
    List<AlertTemplateRuleDto> templateRuleDtoList;
}

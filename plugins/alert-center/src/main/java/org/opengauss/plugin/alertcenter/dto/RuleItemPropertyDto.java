/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.dto;

import lombok.Data;
import org.opengauss.plugin.alertcenter.config.properties.RuleItemProperty;

/**
 * @author wuyuebin
 * @date 2023/5/29 11:07
 * @description
 */
@Data
public class RuleItemPropertyDto extends RuleItemProperty {
    private String i18nName;
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.plugin.alertcenter.dto.AlertTemplateRuleDto;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRule;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/24 10:31
 * @description
 */
public interface AlertTemplateRuleService extends IService<AlertTemplateRule> {
    AlertTemplateRule getTemplateRule(Long templateRuleId);

    AlertTemplateRuleDto saveTemplateRule(AlertTemplateRule alertTemplateRule);

    List<AlertTemplateRuleDto> getDtoListByTemplateId(Long templateId);
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/24 10:31
 * @description
 */
public interface AlertTemplateRuleService extends IService<AlertTemplateRule> {
    AlertTemplateRule getTemplateRule(Long templateRuleId);

    AlertTemplateRule saveTemplateRule(AlertTemplateRule alertTemplateRule);

    List<AlertTemplateRule> getListByTemplateId(Long templateId);

    /**
     * enable rules
     *
     * @param templateRuleId Long
     */
    void enableTemplateRule(Long templateRuleId);

    /**
     * disable rules
     *
     * @param templateRuleId Long
     */
    void disableTemplateRule(Long templateRuleId);
}

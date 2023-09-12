/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.dto.AlertTemplateDto;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.model.AlertTemplateReq;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/12 11:40
 * @description
 */
public interface AlertTemplateService extends IService<AlertTemplate> {
    Page<AlertTemplate> getTemplatePage(String templateName, Page page);

    Page<AlertTemplateRule> getTemplateRulePage(Long templateId, String ruleName, Page page);

    AlertTemplateDto getTemplate(Long id);

    AlertTemplate saveTemplate(AlertTemplateReq templateReq);

    List<AlertTemplate> getTemplateList();

    List<AlertTemplateRule> getTemplateRuleListById(Long templateId);

    void delTemplate(Long id);
}

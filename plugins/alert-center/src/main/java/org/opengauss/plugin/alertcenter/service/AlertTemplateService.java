/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.plugin.alertcenter.dto.AlertTemplateDto;
import org.opengauss.plugin.alertcenter.dto.AlertTemplateRuleDto;
import org.opengauss.plugin.alertcenter.entity.AlertTemplate;
import org.opengauss.plugin.alertcenter.model.AlertTemplateReq;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/12 11:40
 * @description
 */
public interface AlertTemplateService extends IService<AlertTemplate> {
    Page<AlertTemplate> getTemplatePage(String templateName, Page page);

    Page<AlertTemplateRuleDto> getTemplateRulePage(Long templateId, String ruleName, Page page);

    AlertTemplateDto getTemplate(Long id);

    AlertTemplate saveTemplate(AlertTemplateReq templateReq);

    List<AlertTemplate> getTemplateList();

    List<AlertTemplateRuleDto> getTemplateRuleListById(Long templateId);

    void delTemplate(Long id);
}

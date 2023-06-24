/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.plugin.alertcenter.dto.AlertTemplateDto;
import org.opengauss.plugin.alertcenter.dto.AlertTemplateRuleDto;
import org.opengauss.plugin.alertcenter.entity.AlertTemplate;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRule;
import org.opengauss.plugin.alertcenter.model.AlertTemplateReq;
import org.opengauss.plugin.alertcenter.service.AlertTemplateRuleService;
import org.opengauss.plugin.alertcenter.service.AlertTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/14 17:03
 * @description 告警模板相关
 */
@RestController
@RequestMapping("/alertCenter/api/v1/alertTemplate")
public class AlertTemplateController extends BaseController {
    @Autowired
    private AlertTemplateService templateService;
    @Autowired
    private AlertTemplateRuleService templateRuleService;

    @GetMapping("")
    public TableDataInfo getTemplatePage(String templateName) {
        Page<AlertTemplate> page = templateService.getTemplatePage(templateName, startPage());
        return getDataTable(page);
    }

    @GetMapping("/list")
    public AjaxResult getTemplateList() {
        List<AlertTemplate> list = templateService.getTemplateList();
        return AjaxResult.success(list);
    }

    @GetMapping("/{id}")
    public AjaxResult getTemplateDto(@PathVariable Long id) {
        AlertTemplateDto templateDto = templateService.getTemplate(id);
        return AjaxResult.success(templateDto);
    }

    @GetMapping("/{id}/rule")
    public TableDataInfo getTemplateRulePage(@PathVariable Long id, String ruleName) {
        Page<AlertTemplateRuleDto> page = templateService.getTemplateRulePage(id, ruleName, startPage());
        return getDataTable(page);
    }

    @GetMapping("/{id}/rule/list")
    public AjaxResult getTemplateRuleListById(@PathVariable Long id) {
        List<AlertTemplateRuleDto> list = templateService.getTemplateRuleListById(id);
        return AjaxResult.success(list);
    }

    @GetMapping("/ruleList/{templateRuleId}")
    public AjaxResult getTemplateRule(@PathVariable Long templateRuleId) {
        AlertTemplateRule alertTemplateRule = templateRuleService.getTemplateRule(templateRuleId);
        return AjaxResult.success(alertTemplateRule);
    }

    @PostMapping("")
    public AjaxResult saveTemplate(@RequestBody @Valid AlertTemplateReq templateReq) {
        AlertTemplate alertTemplate = templateService.saveTemplate(templateReq);
        return AjaxResult.success(alertTemplate);
    }

    @DeleteMapping
    public AjaxResult delTemplate(@RequestParam Long id) {
        templateService.delTemplate(id);
        return AjaxResult.success();
    }

    @PostMapping("/templateRule")
    public AjaxResult saveTemplateRule(@RequestBody @Valid AlertTemplateRule alertTemplateRule) {
        AlertTemplateRuleDto templateRuleDto = templateRuleService.saveTemplateRule(alertTemplateRule);
        return AjaxResult.success(templateRuleDto);
    }
}

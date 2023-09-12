/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertTemplateDto;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.model.AlertTemplateReq;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/4/14 17:03
 * @description
 */
@RestController
@RequestMapping("/api/v1/alertTemplate")
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
        Page<AlertTemplateRule> page = templateService.getTemplateRulePage(id, ruleName, startPage());
        return getDataTable(page);
    }

    @GetMapping("/{id}/rule/list")
    public AjaxResult getTemplateRuleListById(@PathVariable Long id) {
        List<AlertTemplateRule> list = templateService.getTemplateRuleListById(id);
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
    public AjaxResult saveTemplateRule(@RequestBody @Validated AlertTemplateRule alertTemplateRule) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        String errorMsg = MessageSourceUtil.get("validateFail");
        if (alertTemplateRule.getRuleType().equals(CommonConstants.INDEX_RULE)) {
            Set<ConstraintViolation<AlertTemplateRule>> error = validator.validate(alertTemplateRule,
                AlertTemplateRule.IndexRuleGroup.class);
            if (CollectionUtil.isNotEmpty(error)) {
                String messages = error.stream().map(item -> item.getPropertyPath() + item.getMessage())
                    .collect(Collectors.joining(CommonConstants.DELIMITER));
                return AjaxResult.error(errorMsg + ":" + messages);
            }
            if (alertTemplateRule.getIsSilence().equals(CommonConstants.IS_SILENCE)) {
                error = validator.validate(alertTemplateRule, AlertTemplateRule.SilenceGroup.class);
                if (CollectionUtil.isNotEmpty(error)) {
                    String messages = error.stream().map(item -> item.getPropertyPath() + item.getMessage())
                        .collect(Collectors.joining(CommonConstants.DELIMITER));
                    return AjaxResult.error(errorMsg + ":" + messages);
                }
            }
        } else {
            Set<ConstraintViolation<AlertTemplateRule>> error = validator.validate(alertTemplateRule,
                AlertTemplateRule.LogRuleGroup.class);
            if (CollectionUtil.isNotEmpty(error)) {
                String messages = error.stream().map(item -> item.getPropertyPath() + item.getMessage())
                    .collect(Collectors.joining(CommonConstants.DELIMITER));
                return AjaxResult.error(errorMsg + ":" + messages);
            }
        }
        return AjaxResult.success(templateRuleService.saveTemplateRule(alertTemplateRule));
    }

    /**
     * enable rules
     *
     * @param templateRuleId Long
     * @return AjaxResult
     */
    @PostMapping("/templateRule/{templateRuleId}/enable")
    public AjaxResult enableTemplateRule(@PathVariable Long templateRuleId) {
        templateRuleService.enableTemplateRule(templateRuleId);
        return AjaxResult.success();
    }

    /**
     * disable rules
     *
     * @param templateRuleId Long
     * @return AjaxResult
     */
    @PostMapping("/templateRule/{templateRuleId}/disable")
    public AjaxResult disableTemplateRule(@PathVariable Long templateRuleId) {
        templateRuleService.disableTemplateRule(templateRuleId);
        return AjaxResult.success();
    }
}

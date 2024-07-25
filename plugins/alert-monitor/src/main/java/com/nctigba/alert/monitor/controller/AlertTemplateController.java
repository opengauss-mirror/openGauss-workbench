/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AlertTemplateController.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/controller/AlertTemplateController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.dto.AlertRuleParamDTO;
import com.nctigba.alert.monitor.model.dto.AlertTemplateDTO;
import com.nctigba.alert.monitor.model.dto.AlertTemplateRuleDTO;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.query.AlertTemplateQuery;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
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
        Page<AlertTemplateDO> page = templateService.getTemplatePage(templateName, startPage());
        return getDataTable(page);
    }

    @GetMapping("/list")
    public AjaxResult getTemplateList(@RequestParam String type) {
        List<AlertTemplateDO> list = templateService.getTemplateList(type);
        return AjaxResult.success(list);
    }

    @GetMapping("/{id}")
    public AjaxResult getTemplateDto(@PathVariable Long id) {
        AlertTemplateDTO templateDto = templateService.getTemplate(id);
        return AjaxResult.success(templateDto);
    }

    @GetMapping("/{id}/rule")
    public TableDataInfo getTemplateRulePage(@PathVariable Long id, String ruleName) {
        Page<AlertTemplateRuleDO> page = templateService.getTemplateRulePage(id, ruleName, startPage());
        return getDataTable(page);
    }

    @GetMapping("/{id}/rule/list")
    public AjaxResult getTemplateRuleListById(@PathVariable Long id) {
        List<AlertTemplateRuleDO> list = templateService.getTemplateRuleListById(id);
        return AjaxResult.success(list);
    }

    @GetMapping("/ruleList/{templateRuleId}")
    public AjaxResult getTemplateRule(@PathVariable Long templateRuleId) {
        AlertTemplateRuleDO alertTemplateRuleDO = templateRuleService.getTemplateRule(templateRuleId);
        return AjaxResult.success(alertTemplateRuleDO);
    }

    @PostMapping("")
    public AjaxResult saveTemplate(@RequestBody @Valid AlertTemplateQuery templateReq) {
        AlertTemplateDO alertTemplateDO = templateService.saveTemplate(templateReq);
        return AjaxResult.success(alertTemplateDO);
    }

    @DeleteMapping
    public AjaxResult delTemplate(@RequestParam Long id) {
        templateService.delTemplate(id);
        return AjaxResult.success();
    }

    @PostMapping("/templateRule")
    public AjaxResult saveTemplateRule(@RequestBody @Validated AlertTemplateRuleDO alertTemplateRuleDO) {
        String messages = "";
        if (alertTemplateRuleDO.getRuleType().equals(CommonConstants.INDEX_RULE)) {
            messages = validate(alertTemplateRuleDO, AlertTemplateRuleDO.IndexRuleGroup.class);
            if (alertTemplateRuleDO.getIsSilence().equals(CommonConstants.IS_SILENCE)) {
                messages += CommonConstants.DELIMITER + validate(alertTemplateRuleDO, AlertRuleDO.SilenceGroup.class);
            }
        } else if (alertTemplateRuleDO.getRuleType().equals(CommonConstants.LOG_RULE)) {
            messages = validate(alertTemplateRuleDO, AlertTemplateRuleDO.LogRuleGroup.class);
        } else {
            messages = validate(alertTemplateRuleDO, AlertTemplateRuleDO.PluginRuleGroup.class);
        }
        String errorMsg = MessageSourceUtils.get("validateFail");
        if (StrUtil.isNotBlank(messages)) {
            return AjaxResult.error(errorMsg + ":" + messages);
        }
        return AjaxResult.success(templateRuleService.saveTemplateRule(alertTemplateRuleDO));
    }

    private String validate(AlertTemplateRuleDO alertTemplateRule, Class clz) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<AlertTemplateRuleDO>> error = validator.validate(alertTemplateRule, clz);
        if (CollectionUtil.isEmpty(error)) {
            return "";
        }
        return error.stream().map(item -> item.getPropertyPath() + item.getMessage())
            .collect(Collectors.joining(CommonConstants.DELIMITER));
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

    @GetMapping("/templateRule")
    public AjaxResult getTemplateRuleByRuleId(@RequestParam Long ruleId) {
        return AjaxResult.success(templateRuleService.getDtoListByRuleId(ruleId));
    }
}
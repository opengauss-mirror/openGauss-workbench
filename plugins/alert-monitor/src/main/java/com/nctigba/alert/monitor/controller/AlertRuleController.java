/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.model.RuleReq;
import com.nctigba.alert.monitor.service.AlertRuleService;
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
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/4/13 17:39
 * @description
 */
@RestController
@RequestMapping("/api/v1/alertRule")
public class AlertRuleController extends BaseController {
    @Autowired
    private AlertRuleService alertRuleService;

    @GetMapping("")
    public TableDataInfo getRuleListPage(RuleReq ruleReq) {
        Page<AlertRule> ruleIPage = alertRuleService.getRulePage(ruleReq, startPage());
        return getDataTable(ruleIPage);
    }

    @GetMapping("/ruleList")
    public AjaxResult getRuleList() {
        return AjaxResult.success(alertRuleService.getRuleList());
    }


    @GetMapping("/{ruleId}")
    public AjaxResult getRuleById(@PathVariable Long ruleId) {
        return AjaxResult.success(alertRuleService.getRuleById(ruleId));
    }

    @GetMapping("/ruleItem/properties")
    public AjaxResult getRuleItemProperties() {
        return AjaxResult.success(alertRuleService.getRuleItemProperties());
    }

    /**
     * getRuleItemSrcList, for select
     *
     * @return AjaxResult.success: List<AlertRuleItemSrc>
     */
    @GetMapping("/ruleItemSrc/list")
    public AjaxResult getRuleItemSrcList() {
        return AjaxResult.success(alertRuleService.getRuleItemSrcList());
    }

    /**
     * for select
     *
     * @param ruleItemSrcId Long
     * @return AjaxResult
     */
    @GetMapping("/ruleItemSrc/{ruleItemSrcId}/ruleItemExpSrcList")
    public AjaxResult getRuleItemExpSrcListByRuleItemSrcId(@PathVariable Long ruleItemSrcId) {
        return AjaxResult.success(alertRuleService.getRuleItemExpSrcListByRuleItemSrcId(ruleItemSrcId));
    }

    /**
     * save the alertRule
     *
     * @param alertRule AlertRule
     * @return AjaxResult.success()
     */
    @PostMapping
    public AjaxResult saveRule(@RequestBody @Validated AlertRule alertRule) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        String errorMsg = MessageSourceUtil.get("validateFail");
        if (alertRule.getRuleType().equals(CommonConstants.INDEX_RULE)) {
            Set<ConstraintViolation<AlertRule>> error = validator.validate(alertRule, AlertRule.IndexRuleGroup.class);
            if (CollectionUtil.isNotEmpty(error)) {
                String messages = error.stream().map(item -> item.getPropertyPath() + item.getMessage())
                    .collect(Collectors.joining(CommonConstants.DELIMITER));
                return AjaxResult.error(errorMsg + ":" + messages);
            }
            if (alertRule.getIsSilence().equals(CommonConstants.IS_SILENCE)) {
                error = validator.validate(alertRule, AlertRule.SilenceGroup.class);
                if (CollectionUtil.isNotEmpty(error)) {
                    String messages = error.stream().map(item -> item.getPropertyPath() + item.getMessage())
                        .collect(Collectors.joining(CommonConstants.DELIMITER));
                    return AjaxResult.error(errorMsg + ":" + messages);
                }
            }
        } else {
            Set<ConstraintViolation<AlertRule>> error = validator.validate(alertRule, AlertRule.LogRuleGroup.class);
            if (CollectionUtil.isNotEmpty(error)) {
                String messages = error.stream().map(item -> item.getPropertyPath() + item.getMessage())
                    .collect(Collectors.joining(CommonConstants.DELIMITER));
                return AjaxResult.error(errorMsg + ":" + messages);
            }
        }
        alertRuleService.saveRule(alertRule);
        return AjaxResult.success();
    }

    /**
     * delete by ID
     *
     * @param id id
     * @return AjaxResult.success()
     */
    @DeleteMapping("/{id}")
    public AjaxResult delRuleById(@PathVariable Long id) {
        alertRuleService.delRuleById(id);
        return AjaxResult.success();
    }
}

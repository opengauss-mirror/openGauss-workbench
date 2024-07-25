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
 *  AlertRuleController.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/controller/AlertRuleController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.dto.AlertRuleParamDTO;
import com.nctigba.alert.monitor.model.query.RuleQuery;
import com.nctigba.alert.monitor.service.AlertRuleService;
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
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;
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
    public TableDataInfo getRuleListPage(RuleQuery ruleQuery) {
        Page<AlertRuleDO> ruleIPage = alertRuleService.getRulePage(ruleQuery, startPage());
        return getDataTable(ruleIPage);
    }

    @GetMapping("/ruleList")
    public AjaxResult getRuleList(@RequestParam String ruleTypes) {
        List<String> ruleTypeList = Arrays.asList(ruleTypes.split(CommonConstants.DELIMITER));
        return AjaxResult.success(alertRuleService.getRuleList(ruleTypeList));
    }


    @GetMapping("/{ruleId}")
    public AjaxResult getRuleById(@PathVariable Long ruleId) {
        return AjaxResult.success(alertRuleService.getRuleById(ruleId));
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
     * @param alertRule
     * @return AjaxResult.success()
     */
    @PostMapping
    public AjaxResult saveRule(@Validated @RequestBody AlertRuleParamDTO alertRule) {
        String messages = "";
        if (alertRule.getRuleType().equals(CommonConstants.INDEX_RULE)) {
            messages = validate(alertRule, AlertRuleDO.IndexRuleGroup.class);
            if (alertRule.getIsSilence().equals(CommonConstants.IS_SILENCE)) {
                messages += CommonConstants.DELIMITER + validate(alertRule, AlertRuleDO.SilenceGroup.class);
            }
        } else if (alertRule.getRuleType().equals(CommonConstants.LOG_RULE)) {
            messages = validate(alertRule, AlertRuleDO.LogRuleGroup.class);
        } else {
            messages = validate(alertRule, AlertRuleDO.PluginRuleGroup.class);
        }
        String errorMsg = MessageSourceUtils.get("validateFail");
        if (StrUtil.isNotBlank(messages)) {
            return AjaxResult.error(errorMsg + ":" + messages);
        }
        alertRuleService.saveRule(alertRule);
        return AjaxResult.success();
    }

    private String validate(AlertRuleParamDTO alertRule, Class clz) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<AlertRuleParamDTO>> error = validator.validate(alertRule, clz);
        if (CollectionUtil.isEmpty(error)) {
            return "";
        }
        return error.stream().map(item -> item.getPropertyPath() + item.getMessage())
            .collect(Collectors.joining(CommonConstants.DELIMITER));
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
/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.plugin.alertcenter.dto.AlertRuleDto;
import org.opengauss.plugin.alertcenter.model.RuleReq;
import org.opengauss.plugin.alertcenter.service.AlertRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wuyuebin
 * @date 2023/4/13 17:39
 * @description
 */
@RestController
@RequestMapping("/alertCenter/api/v1/alertRule")
public class AlertRuleController extends BaseController {
    @Autowired
    private AlertRuleService alertRuleService;

    @GetMapping("")
    public TableDataInfo getRuleListPage(RuleReq ruleReq) {
        Page<AlertRuleDto> ruleIPage = alertRuleService.getRulePage(ruleReq, startPage());
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
}
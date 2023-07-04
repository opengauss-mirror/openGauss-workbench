/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.dto.AlertRuleDto;
import com.nctigba.alert.monitor.dto.RuleItemPropertyDto;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.model.RuleReq;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/9 10:08
 * @description
 */
public interface AlertRuleService extends IService<AlertRule> {
    Page<AlertRuleDto> getRulePage(RuleReq ruleReq, Page page);

    AlertRule getRuleById(Long id);

    List<RuleItemPropertyDto> getRuleItemProperties();

    List<AlertRuleDto> getRuleList();
}

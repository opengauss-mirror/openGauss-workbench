/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.dto.RuleItemPropertyDto;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.entity.AlertRuleItemExpSrc;
import com.nctigba.alert.monitor.entity.AlertRuleItemSrc;
import com.nctigba.alert.monitor.model.RuleReq;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/9 10:08
 * @description
 */
public interface AlertRuleService extends IService<AlertRule> {
    Page<AlertRule> getRulePage(RuleReq ruleReq, Page page);

    AlertRule getRuleById(Long id);

    List<RuleItemPropertyDto> getRuleItemProperties();

    List<AlertRule> getRuleList();

    /**
     * getRuleItemSrcList
     *
     * @return List<AlertRuleItemSrc>
     */
    List<AlertRuleItemSrc> getRuleItemSrcList();

    /**
     * getRuleItemExpSrcListByRuleItemSrcId
     *
     * @param ruleItemSrcId Long
     * @return List<AlertRuleItemExpSrc>
     */
    List<AlertRuleItemExpSrc> getRuleItemExpSrcListByRuleItemSrcId(Long ruleItemSrcId);

    /**
     * saveRule
     *
     * @param alertRule AlertRule
     */
    void saveRule(AlertRule alertRule);

    /**
     * delete rule by ID
     *
     * @param id Long
     */
    void delRuleById(Long id);
}

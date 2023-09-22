/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertTemplateRuleDto;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.PrometheusService;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/5/24 10:32
 * @description
 */
@Service
public class AlertTemplateRuleServiceImpl extends ServiceImpl<AlertTemplateRuleMapper, AlertTemplateRule>
    implements AlertTemplateRuleService {
    @Autowired
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;
    @Autowired
    private AlertTemplateRuleItemService templateRuleItemService;
    @Autowired
    private PrometheusService prometheusService;

    @Override
    public AlertTemplateRule getTemplateRule(Long templateRuleId) {
        AlertTemplateRule alertTemplateRule = this.baseMapper.selectById(templateRuleId);
        if (alertTemplateRule == null) {
            throw new ServiceException("the rule is not found");
        }
        List<AlertTemplateRuleItem> alertTemplateRuleItems = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItem>lambdaQuery().eq(AlertTemplateRuleItem::getTemplateRuleId,
                    alertTemplateRule.getId())
                .eq(AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        alertTemplateRule.setAlertRuleItemList(alertTemplateRuleItems);
        return alertTemplateRule;
    }

    @Override
    @Transactional
    public AlertTemplateRule saveTemplateRule(AlertTemplateRule alertTemplateRule) {
        if (alertTemplateRule.getId() == null) {
            alertTemplateRule.setCreateTime(LocalDateTime.now());
        } else {
            alertTemplateRule.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(alertTemplateRule);
        List<AlertTemplateRuleItem> templateRuleItemList = alertTemplateRule.getAlertRuleItemList();
        if (CollectionUtil.isEmpty(templateRuleItemList)) {
            return alertTemplateRule;
        }
        for (AlertTemplateRuleItem templateRuleItem : templateRuleItemList) {
            templateRuleItem.setTemplateRuleId(alertTemplateRule.getId());
            if (templateRuleItem.getId() == null) {
                templateRuleItem.setCreateTime(LocalDateTime.now());
            } else {
                templateRuleItem.setUpdateTime(LocalDateTime.now());
            }
        }
        templateRuleItemService.saveOrUpdateBatch(templateRuleItemList);
        if (alertTemplateRule.getRuleType().equals(CommonConstants.INDEX_RULE)
            && alertTemplateRule.getTemplateId() != null) {
            prometheusService.updateRuleByTemplateRule(alertTemplateRule);
        }
        return alertTemplateRule;
    }


    public List<AlertTemplateRule> getListByTemplateId(Long templateId) {
        List<AlertTemplateRule> alertRules =
            this.baseMapper.selectList(
                Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId, templateId).eq(
                    AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                    AlertTemplateRule::getId));
        List<Long> ruleIdList = alertRules.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItem> alertRuleItems = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItem>lambdaQuery().in(AlertTemplateRuleItem::getTemplateRuleId,
                ruleIdList).eq(AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertTemplateRule alertRule : alertRules) {
            AlertTemplateRuleDto ruleDto = new AlertTemplateRuleDto();
            BeanUtil.copyProperties(alertRule, ruleDto);
            List<AlertTemplateRuleItem> ruleItems = alertRuleItems.stream().filter(
                item -> item.getTemplateRuleId().equals(alertRule.getId())).collect(Collectors.toList());
            alertRule.setAlertRuleItemList(ruleItems);
        }
        return alertRules;
    }

    /**
     * enable rules
     *
     * @param templateRuleId Long
     */
    @Override
    @Transactional
    public void enableTemplateRule(Long templateRuleId) {
        AlertTemplateRule alertTemplateRule = this.baseMapper.selectById(templateRuleId);
        alertTemplateRule.setEnable(CommonConstants.ENABLE).setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(alertTemplateRule);
        if (alertTemplateRule.getRuleType().equals(CommonConstants.INDEX_RULE)) {
            prometheusService.updateRuleByTemplateRule(alertTemplateRule);
        }
    }

    /**
     * disable rules
     *
     * @param templateRuleId Long
     */
    @Override
    @Transactional
    public void disableTemplateRule(Long templateRuleId) {
        AlertTemplateRule alertTemplateRule = this.baseMapper.selectById(templateRuleId);
        alertTemplateRule.setEnable(CommonConstants.DISABLE).setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(alertTemplateRule);
        if (alertTemplateRule.getRuleType().equals(CommonConstants.INDEX_RULE)) {
            prometheusService.removeRuleByTemplateRule(alertTemplateRule);
        }
    }
}

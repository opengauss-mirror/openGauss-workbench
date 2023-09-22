/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.config.properties.AlertProperty;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.entity.AlertRuleItem;
import com.nctigba.alert.monitor.entity.AlertRuleItemExpSrc;
import com.nctigba.alert.monitor.entity.AlertRuleItemSrc;
import com.nctigba.alert.monitor.mapper.AlertRuleItemExpSrcMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemSrcMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.model.RuleReq;
import com.nctigba.alert.monitor.service.AlertRuleItemParamService;
import com.nctigba.alert.monitor.service.AlertRuleItemService;
import com.nctigba.alert.monitor.service.AlertRuleService;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/5/9 10:13
 * @description
 */
@Service
public class AlertRuleServiceImpl extends ServiceImpl<AlertRuleMapper, AlertRule> implements AlertRuleService {
    @Autowired
    private AlertProperty alertProperty;
    @Autowired
    private AlertRuleItemMapper alertRuleItemMapper;
    @Autowired
    private AlertRuleItemSrcMapper ruleItemSrcMapper;
    @Autowired
    private AlertRuleItemExpSrcMapper ruleItemExpSrcMapper;
    @Autowired
    private AlertRuleItemService ruleItemService;
    @Autowired
    private AlertRuleItemParamService ruleItemParamService;

    @Override
    public Page<AlertRule> getRulePage(RuleReq ruleReq, Page page) {
        Page<AlertRule> alertRulePage = this.baseMapper.selectPage(page, Wrappers.<AlertRule>lambdaQuery()
            .like(StrUtil.isNotBlank(ruleReq.getRuleName()), AlertRule::getRuleName, ruleReq.getRuleName())
            .eq(StrUtil.isNotBlank(ruleReq.getRuleType()), AlertRule::getRuleType, ruleReq.getRuleType())
            .eq(StrUtil.isNotBlank(ruleReq.getLevel()), AlertRule::getLevel, ruleReq.getLevel())
            .eq(AlertRule::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(AlertRule::getId));
        List<AlertRule> records = alertRulePage.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            return alertRulePage;
        }
        List<Long> ruleIdList = records.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItem> alertRuleItems = alertRuleItemMapper.selectList(
            Wrappers.<AlertRuleItem>lambdaQuery().in(AlertRuleItem::getRuleId, ruleIdList).eq(
                AlertRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertRule record : records) {
            List<AlertRuleItem> ruleItems = alertRuleItems.stream().filter(
                item -> item.getRuleId().equals(record.getId())).collect(Collectors.toList());
            record.setAlertRuleItemList(ruleItems);
        }
        return alertRulePage;
    }

    @Override
    public AlertRule getRuleById(Long id) {
        AlertRule alertRule = this.baseMapper.selectById(id);
        if (alertRule == null) {
            throw new ServiceException("the rule is not found");
        }
        List<AlertRuleItem> alertRuleItems = alertRuleItemMapper.selectList(
            Wrappers.<AlertRuleItem>lambdaQuery().eq(AlertRuleItem::getRuleId, id).eq(AlertRuleItem::getIsDeleted,
                CommonConstants.IS_NOT_DELETE));
        alertRule.setAlertRuleItemList(alertRuleItems);
        return alertRule;
    }

    @Override
    public List<AlertRule> getRuleList() {
        List<AlertRule> alertRules =
            this.baseMapper.selectList(Wrappers.<AlertRule>lambdaQuery().eq(AlertRule::getIsDeleted,
                CommonConstants.IS_NOT_DELETE).orderByDesc(AlertRule::getId));
        List<Long> ruleIdList = alertRules.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItem> alertRuleItems = alertRuleItemMapper.selectList(
            Wrappers.<AlertRuleItem>lambdaQuery().in(AlertRuleItem::getRuleId, ruleIdList).eq(
                AlertRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertRule alertRule : alertRules) {
            List<AlertRuleItem> ruleItems = alertRuleItems.stream().filter(
                item -> item.getRuleId().equals(alertRule.getId())).collect(Collectors.toList());
            alertRule.setAlertRuleItemList(ruleItems);
        }
        return alertRules;
    }

    /**
     * getRuleItemSrcList
     *
     * @return List<AlertRuleItemSrc>
     */
    @Override
    public List<AlertRuleItemSrc> getRuleItemSrcList() {
        return ruleItemSrcMapper.selectList(new LambdaUpdateWrapper<>());
    }

    /**
     * getRuleItemExpSrcListByRuleItemSrcId
     *
     * @param ruleItemSrcId Long
     * @return List<AlertRuleItemExpSrc>
     */
    @Override
    public List<AlertRuleItemExpSrc> getRuleItemExpSrcListByRuleItemSrcId(Long ruleItemSrcId) {
        return ruleItemExpSrcMapper.selectList(
            Wrappers.<AlertRuleItemExpSrc>lambdaQuery().eq(AlertRuleItemExpSrc::getRuleItemSrcId, ruleItemSrcId));
    }

    /**
     * save rule
     *
     * @param alertRule AlertRule
     */
    @Override
    @Transactional
    public void saveRule(AlertRule alertRule) {
        this.saveOrUpdate(alertRule);
        List<AlertRuleItem> ruleItemList = alertRule.getAlertRuleItemList();
        // delete the old ruleItem
        List<Long> itemIdList = ruleItemList.stream().filter(item -> item.getRuleId() != null).map(
            item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItem> delItemList = ruleItemService.list(
            Wrappers.<AlertRuleItem>lambdaQuery().eq(AlertRuleItem::getRuleId,
                    alertRule.getId()).notIn(CollectionUtil.isNotEmpty(itemIdList), AlertRuleItem::getId, itemIdList)
                .eq(AlertRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(delItemList)) {
            List<Long> delItemIds = delItemList.stream().map(item -> item.getId()).collect(Collectors.toList());
            LambdaUpdateWrapper<AlertRuleItem> itemUpdateWrapper =
                new LambdaUpdateWrapper<AlertRuleItem>().set(AlertRuleItem::getIsDeleted,
                    CommonConstants.IS_DELETE).set(AlertRuleItem::getUpdateTime, LocalDateTime.now()).in(
                    AlertRuleItem::getId, delItemIds).eq(AlertRuleItem::getIsDeleted,
                    CommonConstants.IS_NOT_DELETE);
            ruleItemService.update(itemUpdateWrapper);
        }
        // update new ruleItem
        ruleItemList.forEach(item -> item.setRuleId(alertRule.getId()));
        ruleItemService.saveOrUpdateBatch(ruleItemList);
    }

    /**
     * delete rule by ID
     *
     * @param id Long
     */
    @Override
    @Transactional
    public void delRuleById(Long id) {
        LambdaUpdateWrapper<AlertRule> ruleUpdateWrapper =
            new LambdaUpdateWrapper<AlertRule>().set(AlertRule::getIsDeleted, CommonConstants.IS_DELETE)
                .set(AlertRule::getUpdateTime, LocalDateTime.now()).eq(AlertRule::getId, id);
        this.update(ruleUpdateWrapper);
        LambdaUpdateWrapper<AlertRuleItem> ruleItemUpdateWrapper =
            new LambdaUpdateWrapper<AlertRuleItem>().set(AlertRuleItem::getIsDeleted, CommonConstants.IS_DELETE)
                .set(AlertRuleItem::getUpdateTime, LocalDateTime.now()).in(AlertRuleItem::getRuleId, id)
                .eq(AlertRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE);
        ruleItemService.update(ruleItemUpdateWrapper);
    }
}

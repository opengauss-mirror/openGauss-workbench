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
 *  AlertRuleServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertRuleServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.config.property.AlertProperty;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemExpSrcDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemSrcDO;
import com.nctigba.alert.monitor.mapper.AlertRuleItemExpSrcMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemSrcMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.model.dto.AlertRuleParamDTO;
import com.nctigba.alert.monitor.model.query.RuleQuery;
import com.nctigba.alert.monitor.service.AlertRuleItemService;
import com.nctigba.alert.monitor.service.AlertRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/5/9 10:13
 * @description
 */
@Service
public class AlertRuleServiceImpl extends ServiceImpl<AlertRuleMapper, AlertRuleDO> implements AlertRuleService {
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
    private AlertTemplateRuleService templateRuleService;
    @Autowired
    private AlertTemplateRuleItemService templateRuleItemService;

    @Override
    public Page<AlertRuleDO> getRulePage(RuleQuery ruleQuery, Page page) {
        Page<AlertRuleDO> alertRulePage = this.baseMapper.selectPage(page, Wrappers.<AlertRuleDO>lambdaQuery()
            .like(StrUtil.isNotBlank(ruleQuery.getRuleName()), AlertRuleDO::getRuleName, ruleQuery.getRuleName())
            .eq(StrUtil.isNotBlank(ruleQuery.getRuleType()), AlertRuleDO::getRuleType, ruleQuery.getRuleType())
            .eq(StrUtil.isNotBlank(ruleQuery.getLevel()), AlertRuleDO::getLevel, ruleQuery.getLevel())
            .eq(AlertRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(AlertRuleDO::getId));
        List<AlertRuleDO> records = alertRulePage.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            return alertRulePage;
        }
        List<Long> ruleIdList = records.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItemDO> alertRuleItemDOS = alertRuleItemMapper.selectList(
            Wrappers.<AlertRuleItemDO>lambdaQuery().in(AlertRuleItemDO::getRuleId, ruleIdList).eq(
                AlertRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertRuleDO record : records) {
            List<AlertRuleItemDO> ruleItems = alertRuleItemDOS.stream().filter(
                item -> item.getRuleId().equals(record.getId())).collect(Collectors.toList());
            record.setAlertRuleItemList(ruleItems);
        }
        return alertRulePage;
    }

    @Override
    public AlertRuleDO getRuleById(Long id) {
        AlertRuleDO alertRuleDO = this.baseMapper.selectById(id);
        if (alertRuleDO == null) {
            throw new ServiceException("the rule is not found");
        }
        List<AlertRuleItemDO> alertRuleItemDOS = alertRuleItemMapper.selectList(
            Wrappers.<AlertRuleItemDO>lambdaQuery().eq(AlertRuleItemDO::getRuleId, id).eq(AlertRuleItemDO::getIsDeleted,
                CommonConstants.IS_NOT_DELETE));
        alertRuleDO.setAlertRuleItemList(alertRuleItemDOS);
        return alertRuleDO;
    }

    @Override
    public List<AlertRuleDO> getRuleList(List<String> ruleTypes) {
        List<AlertRuleDO> alertRuleDOS =
            this.baseMapper.selectList(Wrappers.<AlertRuleDO>lambdaQuery().in(AlertRuleDO::getRuleType, ruleTypes)
                .eq(AlertRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(AlertRuleDO::getId));
        List<Long> ruleIdList = alertRuleDOS.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItemDO> alertRuleItemDOS = alertRuleItemMapper.selectList(
            Wrappers.<AlertRuleItemDO>lambdaQuery().in(AlertRuleItemDO::getRuleId, ruleIdList).eq(
                AlertRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertRuleDO alertRuleDO : alertRuleDOS) {
            List<AlertRuleItemDO> ruleItems = alertRuleItemDOS.stream().filter(
                item -> item.getRuleId().equals(alertRuleDO.getId())).collect(Collectors.toList());
            alertRuleDO.setAlertRuleItemList(ruleItems);
        }
        return alertRuleDOS;
    }

    /**
     * getRuleItemSrcList
     *
     * @return List<AlertRuleItemSrc>
     */
    @Override
    public List<AlertRuleItemSrcDO> getRuleItemSrcList() {
        return ruleItemSrcMapper.selectList(new LambdaUpdateWrapper<>());
    }

    /**
     * getRuleItemExpSrcListByRuleItemSrcId
     *
     * @param ruleItemSrcId Long
     * @return List<AlertRuleItemExpSrc>
     */
    @Override
    public List<AlertRuleItemExpSrcDO> getRuleItemExpSrcListByRuleItemSrcId(Long ruleItemSrcId) {
        return ruleItemExpSrcMapper.selectList(
            Wrappers.<AlertRuleItemExpSrcDO>lambdaQuery().eq(AlertRuleItemExpSrcDO::getRuleItemSrcId, ruleItemSrcId));
    }

    /**
     * save rule
     *
     * @param alertRule AlertRule
     */
    @Override
    @Transactional
    public void saveRule(AlertRuleParamDTO alertRule) {
        AlertRuleDO alertRuleDO = new AlertRuleDO();
        BeanUtil.copyProperties(alertRule, alertRuleDO);
        this.saveOrUpdate(alertRuleDO);
        if (CommonConstants.PLUGIN_RULE.equals(alertRule.getRuleType())) {
            updateTemplateRule(alertRuleDO, new ArrayList<>(), alertRule.getTemplateRuleIds());
            return;
        }
        List<AlertRuleItemDO> ruleItemList = alertRuleDO.getAlertRuleItemList();
        // delete the old ruleItem
        List<Long> itemIdList = ruleItemList.stream().filter(item -> item.getRuleId() != null).map(
            item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItemDO> delItemList = ruleItemService.list(
            Wrappers.<AlertRuleItemDO>lambdaQuery().eq(AlertRuleItemDO::getRuleId, alertRuleDO.getId())
                .notIn(CollectionUtil.isNotEmpty(itemIdList), AlertRuleItemDO::getId, itemIdList)
                .eq(AlertRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(delItemList)) {
            List<Long> delItemIds = delItemList.stream().map(item -> item.getId()).collect(Collectors.toList());
            LambdaUpdateWrapper<AlertRuleItemDO> itemUpdateWrapper =
                new LambdaUpdateWrapper<AlertRuleItemDO>().set(AlertRuleItemDO::getIsDeleted,
                    CommonConstants.IS_DELETE).set(AlertRuleItemDO::getUpdateTime, LocalDateTime.now()).in(
                    AlertRuleItemDO::getId, delItemIds).eq(AlertRuleItemDO::getIsDeleted,
                    CommonConstants.IS_NOT_DELETE);
            ruleItemService.update(itemUpdateWrapper);
        }
        // update new ruleItem
        ruleItemList.forEach(item -> {
            item.setRuleId(alertRuleDO.getId());
            if (item.getId() == null) {
                item.setCreateTime(LocalDateTime.now());
            } else {
                item.setUpdateTime(LocalDateTime.now());
            }
            if (alertRule.getRuleType().equals(CommonConstants.LOG_RULE)) {
                if (item.getRuleExpName() == null || item.getRuleExpName().length() == 0) {
                    item.setRuleExpName(CommonConstants.ALERT_LOG_EXP_NAME);
                }
                if (item.getRuleExp() == null || item.getRuleExp().length() == 0) {
                    item.setRuleExp(CommonConstants.ALERT_LOG_EXP);
                }
            }
        });
        ruleItemService.saveOrUpdateBatch(ruleItemList);

        updateTemplateRule(alertRuleDO, ruleItemList, alertRule.getTemplateRuleIds());
    }

    private void updateTemplateRule(AlertRuleDO alertRuleDO, List<AlertRuleItemDO> ruleItemList,
                                    List<Long> templateRuleIds) {
        if (CollectionUtil.isEmpty(templateRuleIds)) {
            return;
        }
        for (Long templateRuleId : templateRuleIds) {
            AlertTemplateRuleDO templateRule = templateRuleService.getById(templateRuleId);
            Long id = templateRule.getId();
            BeanUtil.copyProperties(alertRuleDO, templateRule);
            templateRule.setId(id).setRuleId(alertRuleDO.getId());
            List<AlertTemplateRuleItemDO> templateRuleItemSources = templateRuleItemService.list(
                Wrappers.<AlertTemplateRuleItemDO>lambdaQuery()
                    .eq(AlertTemplateRuleItemDO::getTemplateRuleId, templateRuleId)
                    .eq(AlertTemplateRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
            List<AlertTemplateRuleItemDO> templateRuleItemList = ruleItemList.stream().map(ruleItem -> {
                AlertTemplateRuleItemDO templateRuleItem = new AlertTemplateRuleItemDO();
                BeanUtil.copyProperties(ruleItem, templateRuleItem);
                AlertTemplateRuleItemDO templateRuleItemSource = templateRuleItemSources.stream()
                    .filter(item -> item.getRuleItemId().equals(ruleItem.getId())).findFirst().orElse(null);
                if (templateRuleItemSource == null) {
                    templateRuleItem.setId(null).setCreateTime(LocalDateTime.now()).setTemplateRuleId(templateRuleId)
                        .setRuleItemId(ruleItem.getId());
                } else {
                    templateRuleItem.setId(templateRuleItemSource.getId()).setTemplateRuleId(templateRuleId)
                        .setUpdateTime(LocalDateTime.now()).setCreateTime(templateRuleItemSource.getCreateTime())
                        .setRuleItemId(templateRuleItemSource.getRuleItemId());
                }
                return templateRuleItem;
            }).collect(Collectors.toList());
            templateRule.setAlertRuleItemList(templateRuleItemList);
            templateRuleService.saveTemplateRule(templateRule);
        }
    }

    /**
     * delete rule by ID
     *
     * @param id Long
     */
    @Override
    @Transactional
    public void delRuleById(Long id) {
        List<AlertTemplateRuleDO> list = templateRuleService.list(
            Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getIsDeleted,
                    CommonConstants.IS_NOT_DELETE).isNotNull(AlertTemplateRuleDO::getTemplateId)
                .eq(AlertTemplateRuleDO::getRuleId, id)
                .eq(AlertTemplateRuleDO::getIsIncluded, CommonConstants.IS_INCLUDED));
        if (CollectionUtil.isNotEmpty(list)) {
            throw new ServiceException(MessageSourceUtils.get("ruleIsUsed"));
        }
        LambdaUpdateWrapper<AlertRuleDO> ruleUpdateWrapper =
            new LambdaUpdateWrapper<AlertRuleDO>().set(AlertRuleDO::getIsDeleted, CommonConstants.IS_DELETE)
                .set(AlertRuleDO::getUpdateTime, LocalDateTime.now()).eq(AlertRuleDO::getId, id);
        this.update(ruleUpdateWrapper);
        LambdaUpdateWrapper<AlertRuleItemDO> ruleItemUpdateWrapper =
            new LambdaUpdateWrapper<AlertRuleItemDO>().set(AlertRuleItemDO::getIsDeleted, CommonConstants.IS_DELETE)
                .set(AlertRuleItemDO::getUpdateTime, LocalDateTime.now()).in(AlertRuleItemDO::getRuleId, id)
                .eq(AlertRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE);
        ruleItemService.update(ruleItemUpdateWrapper);

        List<AlertTemplateRuleDO> tempRuleList = templateRuleService.list(Wrappers.<AlertTemplateRuleDO>lambdaQuery()
            .eq(AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE)
            .isNotNull(AlertTemplateRuleDO::getTemplateId)
            .eq(AlertTemplateRuleDO::getRuleId, id)
            .eq(AlertTemplateRuleDO::getIsIncluded, CommonConstants.IS_NOT_INCLUDED));
        if (CollectionUtil.isEmpty(tempRuleList)) {
            return;
        }
        List<Long> tempRuleIds = tempRuleList.stream().map(item -> item.getId()).collect(Collectors.toList());
        LambdaUpdateWrapper<AlertTemplateRuleDO> tempRuleWrapper = new LambdaUpdateWrapper<AlertTemplateRuleDO>()
            .set(AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_DELETE)
            .set(AlertTemplateRuleDO::getUpdateTime, LocalDateTime.now())
            .eq(AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE)
            .in(AlertTemplateRuleDO::getId, tempRuleIds);
        templateRuleService.update(tempRuleWrapper);
        LambdaUpdateWrapper<AlertTemplateRuleItemDO> tempRuleItemUpdateWrapper =
            new LambdaUpdateWrapper<AlertTemplateRuleItemDO>().set(AlertTemplateRuleItemDO::getIsDeleted,
                    CommonConstants.IS_DELETE)
                .set(AlertTemplateRuleItemDO::getUpdateTime, LocalDateTime.now())
                .in(AlertTemplateRuleItemDO::getTemplateRuleId, id)
                .eq(AlertTemplateRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE);
        templateRuleItemService.update(tempRuleItemUpdateWrapper);
    }
}
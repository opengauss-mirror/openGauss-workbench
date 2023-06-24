/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.plugin.alertcenter.config.properties.AlertProperty;
import org.opengauss.plugin.alertcenter.config.properties.RuleItemProperty;
import org.opengauss.plugin.alertcenter.constant.CommonConstants;
import org.opengauss.plugin.alertcenter.dto.AlertRuleDto;
import org.opengauss.plugin.alertcenter.dto.RuleItemPropertyDto;
import org.opengauss.plugin.alertcenter.entity.AlertRule;
import org.opengauss.plugin.alertcenter.entity.AlertRuleItem;
import org.opengauss.plugin.alertcenter.entity.AlertRuleItemParam;
import org.opengauss.plugin.alertcenter.mapper.AlertRuleItemMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertRuleItemParamMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertRuleMapper;
import org.opengauss.plugin.alertcenter.model.RuleReq;
import org.opengauss.plugin.alertcenter.service.AlertRuleService;
import org.opengauss.plugin.alertcenter.utils.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private AlertRuleItemParamMapper ruleItemParamMapper;

    @Override
    public Page<AlertRuleDto> getRulePage(RuleReq ruleReq, Page page) {
        MessageSourceUtil.reset();
        Page<AlertRuleDto> ruleDtoPage = new Page<>();
        Page<AlertRule> alertRulePage = this.baseMapper.selectPage(page, Wrappers.<AlertRule>lambdaQuery()
                .like(StrUtil.isNotBlank(ruleReq.getRuleName()), AlertRule::getRuleName, ruleReq.getRuleName())
                .eq(StrUtil.isNotBlank(ruleReq.getRuleType()), AlertRule::getRuleType, ruleReq.getRuleType())
                .eq(StrUtil.isNotBlank(ruleReq.getLevel()), AlertRule::getLevel, ruleReq.getLevel()).orderByDesc(
                        AlertRule::getId));
        ruleDtoPage.setSize(alertRulePage.getSize()).setTotal(alertRulePage.getTotal()).setCurrent(
                alertRulePage.getCurrent());
        List<AlertRule> records = alertRulePage.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            ruleDtoPage.setRecords(new ArrayList<>());
            return ruleDtoPage;
        }
        List<Long> ruleIdList = records.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItem> alertRuleItems = alertRuleItemMapper.selectList(
                Wrappers.<AlertRuleItem>lambdaQuery().in(AlertRuleItem::getRuleId, ruleIdList).eq(
                        AlertRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<Long> itemIdList = alertRuleItems.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItemParam> itemParamList = ruleItemParamMapper.selectList(
                Wrappers.<AlertRuleItemParam>lambdaQuery().in(AlertRuleItemParam::getItemId, itemIdList).eq(
                        AlertRuleItemParam::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<AlertRuleDto> list = new ArrayList<>();
        for (AlertRule record : records) {
            AlertRuleDto ruleDto = new AlertRuleDto();
            BeanUtil.copyProperties(record, ruleDto);
            List<AlertRuleItem> ruleItems = alertRuleItems.stream().filter(
                    item -> item.getRuleId().equals(record.getId())).collect(Collectors.toList());
            String ruleExpDesc = ruleItems.stream().map(item -> {
                List<String> paramVals = itemParamList.stream().filter(
                        item0 -> item0.getItemId().equals(item.getId())).map(item0 -> item0.getParamValue()).collect(
                        Collectors.toList());
                return "[" + item.getRuleMark() + "]:" + MessageSourceUtil.get(item.getRuleExpName())
                        + (CollectionUtil.isNotEmpty(paramVals)
                        ? "(" + paramVals.stream().collect(Collectors.joining(CommonConstants.DELIMITER)) + ")" : "")
                        + " " + (item.getAction().equals("normal")
                        ? MessageSourceUtil.get("rule.ruleItem.normalAction") + " " + item.getOperate()
                        + item.getLimitValue() + (StrUtil.isNotBlank(
                        item.getUnit()) ? item.getUnit() : "") : item.getAction().equals(
                        "increase") ? MessageSourceUtil.get("rule.ruleItem.increaseAction") : MessageSourceUtil.get(
                        "rule.ruleItem.decreaseAction"));
            }).collect(Collectors.joining("<br/>"));
            ruleDto.setRuleExpDesc(ruleExpDesc);
            list.add(ruleDto);
        }
        ruleDtoPage.setRecords(list);
        return ruleDtoPage;
    }

    @Override
    public AlertRule getRuleById(Long id) {
        MessageSourceUtil.reset();
        AlertRule alertRule = this.baseMapper.selectById(id);
        if (alertRule == null) {
            throw new ServiceException("the rule is not found");
        }
        alertRule.setRuleName(MessageSourceUtil.get(alertRule.getRuleName()));
        List<AlertRuleItem> alertRuleItems = alertRuleItemMapper.selectList(
                Wrappers.<AlertRuleItem>lambdaQuery().eq(AlertRuleItem::getRuleId, id));
        List<Long> itemIdList = alertRuleItems.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItemParam> itemParamList = ruleItemParamMapper.selectList(
                Wrappers.<AlertRuleItemParam>lambdaQuery().in(AlertRuleItemParam::getItemId, itemIdList).eq(
                        AlertRuleItemParam::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertRuleItem alertRuleItem : alertRuleItems) {
            List<AlertRuleItemParam> params = itemParamList.stream().filter(
                    item -> item.getItemId().equals(alertRuleItem.getId())).collect(Collectors.toList());
            alertRuleItem.setItemParamList(params);
        }
        alertRule.setAlertRuleItemList(alertRuleItems);
        return alertRule;
    }

    @Override
    public List<RuleItemPropertyDto> getRuleItemProperties() {
        MessageSourceUtil.reset();
        List<RuleItemPropertyDto> ruleItemDtoList = new ArrayList<>();
        List<RuleItemProperty> ruleItems = alertProperty.getRuleItems();
        for (RuleItemProperty ruleItem : ruleItems) {
            RuleItemPropertyDto ruleItemPropertyDto = new RuleItemPropertyDto();
            BeanUtil.copyProperties(ruleItem, ruleItemPropertyDto);
            String name = ruleItemPropertyDto.getName();
            ruleItemPropertyDto.setI18nName(MessageSourceUtil.get(name));
            ruleItemDtoList.add(ruleItemPropertyDto);
        }
        return ruleItemDtoList;
    }

    @Override
    public List<AlertRuleDto> getRuleList() {
        MessageSourceUtil.reset();
        List<AlertRule> alertRules =
                this.baseMapper.selectList(Wrappers.<AlertRule>lambdaQuery().eq(AlertRule::getIsDeleted,
                        CommonConstants.IS_NOT_DELETE).orderByDesc(AlertRule::getId));
        List<Long> ruleIdList = alertRules.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItem> alertRuleItems = alertRuleItemMapper.selectList(
                Wrappers.<AlertRuleItem>lambdaQuery().in(AlertRuleItem::getRuleId, ruleIdList).eq(
                        AlertRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<Long> itemIdList = alertRuleItems.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertRuleItemParam> itemParamList = ruleItemParamMapper.selectList(
                Wrappers.<AlertRuleItemParam>lambdaQuery().in(AlertRuleItemParam::getItemId, itemIdList).eq(
                        AlertRuleItemParam::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<AlertRuleDto> list = new ArrayList<>();
        for (AlertRule alertRule : alertRules) {
            AlertRuleDto ruleDto = new AlertRuleDto();
            BeanUtil.copyProperties(alertRule, ruleDto);
            List<AlertRuleItem> ruleItems = alertRuleItems.stream().filter(
                    item -> item.getRuleId().equals(alertRule.getId())).collect(Collectors.toList());
            String ruleExpDesc = ruleItems.stream().map(item -> {
                List<String> paramVals = itemParamList.stream().filter(
                        item0 -> item0.getItemId().equals(item.getId())).map(item0 -> item0.getParamValue()).collect(
                        Collectors.toList());
                return "[" + item.getRuleMark() + "]:" + MessageSourceUtil.get(item.getRuleExpName())
                        + (CollectionUtil.isNotEmpty(paramVals)
                        ? "(" + paramVals.stream().collect(Collectors.joining(CommonConstants.DELIMITER)) + ")" : "")
                        + " " + (item.getAction().equals("normal")
                        ? MessageSourceUtil.get("rule.ruleItem.normalAction") + " " + item.getOperate()
                        + item.getLimitValue() + (StrUtil.isNotBlank(
                        item.getUnit()) ? item.getUnit() : "") : item.getAction().equals(
                        "increase") ? MessageSourceUtil.get("rule.ruleItem.increaseAction") : MessageSourceUtil.get(
                        "rule.ruleItem.decreaseAction"));
            }).collect(Collectors.joining("<br/>"));
            ruleDto.setRuleExpDesc(ruleExpDesc);
            list.add(ruleDto);
        }
        return list;
    }
}

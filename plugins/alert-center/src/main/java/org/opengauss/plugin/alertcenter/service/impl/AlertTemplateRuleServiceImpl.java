/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.plugin.alertcenter.constant.CommonConstants;
import org.opengauss.plugin.alertcenter.dto.AlertTemplateRuleDto;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRule;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRuleItem;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRuleItemParam;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateRuleItemMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateRuleItemParamMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateRuleMapper;
import org.opengauss.plugin.alertcenter.service.AlertTemplateRuleItemParamService;
import org.opengauss.plugin.alertcenter.service.AlertTemplateRuleItemService;
import org.opengauss.plugin.alertcenter.service.AlertTemplateRuleService;
import org.opengauss.plugin.alertcenter.utils.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private AlertTemplateRuleItemParamMapper ruleItemParamMapper;
    @Autowired
    private AlertTemplateRuleItemParamService ruleItemParamService;

    @Override
    public AlertTemplateRule getTemplateRule(Long templateRuleId) {
        AlertTemplateRule alertTemplateRule = this.baseMapper.selectById(templateRuleId);
        if (alertTemplateRule == null) {
            throw new ServiceException("the rule is not found");
        }
        List<AlertTemplateRuleItem> alertTemplateRuleItems = alertTemplateRuleItemMapper.selectList(
                Wrappers.<AlertTemplateRuleItem>lambdaQuery().eq(AlertTemplateRuleItem::getTemplateRuleId,
                        alertTemplateRule.getId()));
        List<Long> itemIdList = alertTemplateRuleItems.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItemParam> itemParamList = ruleItemParamMapper.selectList(
                Wrappers.<AlertTemplateRuleItemParam>lambdaQuery().in(AlertTemplateRuleItemParam::getItemId,
                        itemIdList).eq(AlertTemplateRuleItemParam::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertTemplateRuleItem alertRuleItem : alertTemplateRuleItems) {
            List<AlertTemplateRuleItemParam> params = itemParamList.stream().filter(
                    item -> item.getItemId().equals(alertRuleItem.getId())).collect(Collectors.toList());
            alertRuleItem.setItemParamList(params);
        }
        alertTemplateRule.setAlertRuleItemList(alertTemplateRuleItems);
        return alertTemplateRule;
    }

    @Override
    public AlertTemplateRuleDto saveTemplateRule(AlertTemplateRule alertTemplateRule) {
        MessageSourceUtil.reset();
        AlertTemplateRuleDto templateRuleDto = new AlertTemplateRuleDto();
        if (alertTemplateRule.getId() == null) {
            alertTemplateRule.setCreateTime(LocalDateTime.now());
        } else {
            alertTemplateRule.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(alertTemplateRule);
        List<AlertTemplateRuleItem> templateRuleItemList = alertTemplateRule.getAlertRuleItemList();
        if (CollectionUtil.isEmpty(templateRuleItemList)) {
            BeanUtil.copyProperties(alertTemplateRule, templateRuleDto);
            return templateRuleDto;
        }
        for (AlertTemplateRuleItem templateRuleItem : templateRuleItemList) {
            templateRuleItem.setTemplateRuleId(alertTemplateRule.getId());
            if (templateRuleItem.getId() == null) {
                templateRuleItem.setCreateTime(LocalDateTime.now());
            } else {
                templateRuleItem.setUpdateTime(LocalDateTime.now());
            }
            templateRuleItemService.saveOrUpdate(templateRuleItem);
            List<AlertTemplateRuleItemParam> itemParamList = templateRuleItem.getItemParamList();
            if (CollectionUtil.isEmpty(itemParamList)) {
                continue;
            }
            for (AlertTemplateRuleItemParam itemParam : itemParamList) {
                itemParam.setItemId(templateRuleItem.getId());
                if (itemParam.getId() == null) {
                    itemParam.setCreateTime(LocalDateTime.now());
                } else {
                    itemParam.setUpdateTime(LocalDateTime.now());
                }
            }
            ruleItemParamService.saveOrUpdateBatch(itemParamList);
        }
        BeanUtil.copyProperties(alertTemplateRule, templateRuleDto);
        templateRuleDto.buildRuleExpDesc();
        return templateRuleDto;
    }


    public List<AlertTemplateRuleDto> getDtoListByTemplateId(Long templateId) {
        MessageSourceUtil.reset();
        List<AlertTemplateRule> alertRules =
                this.baseMapper.selectList(
                        Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId, templateId).eq(
                                AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                                AlertTemplateRule::getId));
        List<Long> ruleIdList = alertRules.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItem> alertRuleItems = alertTemplateRuleItemMapper.selectList(
                Wrappers.<AlertTemplateRuleItem>lambdaQuery().in(AlertTemplateRuleItem::getTemplateRuleId,
                        ruleIdList).eq(AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<Long> itemIdList = alertRuleItems.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItemParam> itemParamList = ruleItemParamMapper.selectList(
                Wrappers.<AlertTemplateRuleItemParam>lambdaQuery().in(AlertTemplateRuleItemParam::getItemId,
                        itemIdList).eq(AlertTemplateRuleItemParam::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<AlertTemplateRuleDto> list = new ArrayList<>();
        for (AlertTemplateRule alertRule : alertRules) {
            AlertTemplateRuleDto ruleDto = new AlertTemplateRuleDto();
            BeanUtil.copyProperties(alertRule, ruleDto);
            List<AlertTemplateRuleItem> ruleItems = alertRuleItems.stream().filter(
                    item -> item.getTemplateRuleId().equals(alertRule.getId())).collect(Collectors.toList());
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

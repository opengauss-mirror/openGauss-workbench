/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.plugin.alertcenter.constant.CommonConstants;
import org.opengauss.plugin.alertcenter.dto.AlertTemplateDto;
import org.opengauss.plugin.alertcenter.dto.AlertTemplateRuleDto;
import org.opengauss.plugin.alertcenter.entity.AlertClusterNodeConf;
import org.opengauss.plugin.alertcenter.entity.AlertRule;
import org.opengauss.plugin.alertcenter.entity.AlertRuleItem;
import org.opengauss.plugin.alertcenter.entity.AlertRuleItemParam;
import org.opengauss.plugin.alertcenter.entity.AlertTemplate;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRule;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRuleItem;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRuleItemParam;
import org.opengauss.plugin.alertcenter.mapper.AlertRuleItemMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertRuleItemParamMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertRuleMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateRuleItemMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateRuleItemParamMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateRuleMapper;
import org.opengauss.plugin.alertcenter.model.AlertTemplateReq;
import org.opengauss.plugin.alertcenter.model.AlertTemplateRuleReq;
import org.opengauss.plugin.alertcenter.service.AlertClusterNodeConfService;
import org.opengauss.plugin.alertcenter.service.AlertTemplateRuleItemParamService;
import org.opengauss.plugin.alertcenter.service.AlertTemplateRuleService;
import org.opengauss.plugin.alertcenter.service.AlertTemplateService;
import org.opengauss.plugin.alertcenter.utils.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/5/12 11:55
 * @description
 */
@Service
public class AlertTemplateServiceImpl extends ServiceImpl<AlertTemplateMapper, AlertTemplate>
    implements AlertTemplateService {
    @Autowired
    private AlertRuleMapper alertRuleMapper;
    @Autowired
    private AlertRuleItemMapper alertRuleItemMapper;
    @Autowired
    private AlertRuleItemParamMapper ruleItemParamMapper;
    @Autowired
    private AlertTemplateRuleMapper alertTemplateRuleMapper;
    @Autowired
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;
    @Autowired
    private AlertTemplateRuleService templateRuleService;
    @Autowired
    private AlertTemplateRuleItemParamMapper tRuleItemParamMapper;
    @Autowired
    private AlertTemplateRuleItemParamService templateRuleItemParamService;

    @Autowired
    private AlertClusterNodeConfService nodeConfService;

    @Override
    public Page<AlertTemplate> getTemplatePage(String templateName, Page page) {
        return this.baseMapper.selectPage(page,
            Wrappers.<AlertTemplate>lambdaQuery().like(StrUtil.isNotBlank(templateName),
                AlertTemplate::getTemplateName, templateName).eq(AlertTemplate::getIsDeleted,
                CommonConstants.IS_NOT_DELETE).orderByDesc(AlertTemplate::getId));
    }

    @Override
    public Page<AlertTemplateRuleDto> getTemplateRulePage(Long templateId, String ruleName, Page page) {
        MessageSourceUtil.reset();
        Page<AlertTemplateRuleDto> ruleDtoPage = new Page<>();
        Page<AlertTemplateRule> rulePage = alertTemplateRuleMapper.selectPage(page,
            Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId, templateId).like(
                StrUtil.isNotBlank(ruleName), AlertTemplateRule::getRuleName, ruleName).eq(
                AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                AlertTemplateRule::getId));
        ruleDtoPage.setSize(rulePage.getSize()).setTotal(rulePage.getTotal()).setCurrent(rulePage.getCurrent());
        List<AlertTemplateRule> records = rulePage.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            ruleDtoPage.setRecords(new ArrayList<>());
            return ruleDtoPage;
        }
        List<Long> ruleIdList = records.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItem> alertTemplateRuleItems = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItem>lambdaQuery().in(AlertTemplateRuleItem::getTemplateRuleId,
                ruleIdList).eq(AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<Long> itemIdList = alertTemplateRuleItems.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItemParam> itemParamList = tRuleItemParamMapper.selectList(
            Wrappers.<AlertTemplateRuleItemParam>lambdaQuery().in(AlertTemplateRuleItemParam::getItemId,
                itemIdList).eq(AlertTemplateRuleItemParam::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<AlertTemplateRuleDto> ruleDtoList = new ArrayList<>();
        for (AlertTemplateRule record : records) {
            AlertTemplateRuleDto ruleDto = new AlertTemplateRuleDto();
            BeanUtil.copyProperties(record, ruleDto);
            List<AlertTemplateRuleItem> templateRuleItems = alertTemplateRuleItems.stream().filter(
                item -> item.getTemplateRuleId().equals(record.getId())).collect(Collectors.toList());
            String ruleExpDesc = templateRuleItems.stream().map(item -> {
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
            ruleDtoList.add(ruleDto);
        }
        ruleDtoPage.setRecords(ruleDtoList);
        return ruleDtoPage;
    }


    @Override
    public AlertTemplateDto getTemplate(Long id) {
        AlertTemplate alertTemplate = this.baseMapper.selectById(id);
        AlertTemplateDto templateDto = new AlertTemplateDto();
        BeanUtil.copyProperties(alertTemplate, templateDto);
        List<AlertTemplateRuleDto> ruleDtoList = templateRuleService.getDtoListByTemplateId(id);
        templateDto.setTemplateRuleDtoList(ruleDtoList);
        return templateDto;
    }

    @Override
    @Transactional
    public AlertTemplate saveTemplate(AlertTemplateReq templateReq) {
        AlertTemplate alertTemplate = null;
        if (templateReq.getId() != null) {
            alertTemplate = this.baseMapper.selectById(templateReq.getId());
        }
        if (alertTemplate == null) {
            alertTemplate = new AlertTemplate();
            BeanUtil.copyProperties(templateReq, alertTemplate);
            alertTemplate.setCreateTime(LocalDateTime.now()).setIsDeleted(0);
        } else {
            alertTemplate.setTemplateName(templateReq.getTemplateName()).setUpdateTime(
                LocalDateTime.now()).setIsDeleted(0);
        }
        saveOrUpdate(alertTemplate);

        List<AlertTemplateRuleReq> templateRuleReqList = templateReq.getTemplateRuleReqList();
        List<Long> templateRuleIds = templateRuleReqList.stream().map(item -> item.getTemplateRuleId()).filter(
            item -> item != null).collect(Collectors.toList());
        // 删除不需要的规则
        List<AlertTemplateRule> alertTemplateRules = alertTemplateRuleMapper.selectList(
            Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId,
                alertTemplate.getId()).notIn(CollectionUtil.isNotEmpty(templateRuleIds),
                AlertTemplateRule::getId, templateRuleIds));
        alertTemplateRules.forEach(item -> {
            item.setIsDeleted(CommonConstants.IS_DELETE);
            alertTemplateRuleMapper.updateById(item);
            alertTemplateRuleItemMapper.update(null,
                new LambdaUpdateWrapper<AlertTemplateRuleItem>().set(AlertTemplateRuleItem::getIsDeleted,
                        CommonConstants.IS_DELETE).set(AlertTemplateRuleItem::getUpdateTime, LocalDateTime.now())
                    .eq(AlertTemplateRuleItem::getTemplateRuleId, item.getId())
                    .eq(AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        });
        for (AlertTemplateRuleReq alertTemplateRuleReq : templateRuleReqList) {
            updateByAlertTemplateRuleReq(alertTemplateRuleReq, alertTemplate.getId());
        }
        return alertTemplate;
    }

    private void updateByAlertTemplateRuleReq(AlertTemplateRuleReq alertTemplateRuleReq, Long templateId) {
        if (alertTemplateRuleReq.getTemplateRuleId() == null) {
            AlertRule alertRule = alertRuleMapper.selectById(alertTemplateRuleReq.getRuleId());
            AlertTemplateRule alertTemplateRule = new AlertTemplateRule();
            BeanUtil.copyProperties(alertRule, alertTemplateRule);
            alertTemplateRule.setId(null).setTemplateId(templateId).setRuleId(
                alertRule.getId()).setIsDeleted(0).setCreateTime(LocalDateTime.now()).setUpdateTime(null);
            alertTemplateRuleMapper.insert(alertTemplateRule);
            List<AlertRuleItem> alertRuleItems = alertRuleItemMapper.selectList(
                Wrappers.<AlertRuleItem>lambdaQuery().eq(AlertRuleItem::getRuleId, alertRule.getId()).eq(
                    AlertRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
            for (AlertRuleItem alertRuleItem : alertRuleItems) {
                AlertTemplateRuleItem templateRuleItem = new AlertTemplateRuleItem();
                BeanUtil.copyProperties(alertRuleItem, templateRuleItem);
                templateRuleItem.setId(null).setTemplateRuleId(alertTemplateRule.getId()).setRuleItemId(
                    alertRuleItem.getId()).setIsDeleted(0).setCreateTime(LocalDateTime.now()).setUpdateTime(null);
                alertTemplateRuleItemMapper.insert(templateRuleItem);
                List<AlertRuleItemParam> itemParamList = ruleItemParamMapper.selectList(
                    Wrappers.<AlertRuleItemParam>lambdaQuery().eq(AlertRuleItemParam::getItemId,
                        alertRuleItem.getId()).eq(AlertRuleItemParam::getIsDeleted,
                        CommonConstants.IS_NOT_DELETE));
                if (CollectionUtil.isEmpty(itemParamList)) {
                    continue;
                }
                List<AlertTemplateRuleItemParam> tItemParamList = new ArrayList<>();
                for (AlertRuleItemParam alertRuleItemParam : itemParamList) {
                    AlertTemplateRuleItemParam tRuleItemParam = new AlertTemplateRuleItemParam();
                    BeanUtil.copyProperties(alertRuleItemParam, tRuleItemParam);
                    tRuleItemParam.setId(null).setItemId(templateRuleItem.getId()).setCreateTime(
                        LocalDateTime.now()).setIsDeleted(CommonConstants.IS_NOT_DELETE);
                    tItemParamList.add(tRuleItemParam);
                }
                templateRuleItemParamService.saveBatch(tItemParamList);
            }
        } else {
            AlertTemplateRule alertTemplateRule = alertTemplateRuleMapper.selectById(
                alertTemplateRuleReq.getTemplateRuleId());
            alertTemplateRule.setTemplateId(templateId);
            alertTemplateRuleMapper.updateById(alertTemplateRule);
        }
    }

    @Override
    public List<AlertTemplate> getTemplateList() {
        return this.baseMapper.selectList(Wrappers.<AlertTemplate>lambdaQuery().eq(AlertTemplate::getIsDeleted,
            CommonConstants.IS_NOT_DELETE).orderByDesc(AlertTemplate::getId));
    }

    @Override
    public List<AlertTemplateRuleDto> getTemplateRuleListById(Long templateId) {
        MessageSourceUtil.reset();
        List<AlertTemplateRuleDto> ruleDtoList = new ArrayList<>();
        List<AlertTemplateRule> templateRuleList =
            alertTemplateRuleMapper.selectList(
                Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId, templateId).eq(
                    AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                    AlertTemplateRule::getId));
        if (CollectionUtil.isEmpty(templateRuleList)) {
            return ruleDtoList;
        }
        List<Long> ruleIdList = templateRuleList.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItem> alertTemplateRuleItems = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItem>lambdaQuery().in(AlertTemplateRuleItem::getTemplateRuleId,
                ruleIdList).eq(AlertTemplateRuleItem::getIsDeleted, 0));
        List<Long> itemIdList = alertTemplateRuleItems.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItemParam> itemParamList = tRuleItemParamMapper.selectList(
            Wrappers.<AlertTemplateRuleItemParam>lambdaQuery().in(AlertTemplateRuleItemParam::getItemId,
                itemIdList).eq(AlertTemplateRuleItemParam::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertTemplateRule templateRule : templateRuleList) {
            AlertTemplateRuleDto ruleDto = new AlertTemplateRuleDto();
            BeanUtil.copyProperties(templateRule, ruleDto);
            List<AlertTemplateRuleItem> templateRuleItems = alertTemplateRuleItems.stream().filter(
                item -> item.getTemplateRuleId().equals(templateRule.getId())).collect(Collectors.toList());
            String ruleExpDesc = templateRuleItems.stream().map(item -> {
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
            ruleDtoList.add(ruleDto);
        }
        return ruleDtoList;
    }

    @Override
    public void delTemplate(Long id) {
        MessageSourceUtil.reset();
        List<AlertClusterNodeConf> nodeConfList = nodeConfService.list(
            Wrappers.<AlertClusterNodeConf>lambdaQuery().eq(AlertClusterNodeConf::getTemplateId, id).eq(
                AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(nodeConfList)) {
            throw new ServiceException(MessageSourceUtil.get("templateIsUsed"));
        }
        LambdaUpdateWrapper<AlertTemplate> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(AlertTemplate::getIsDeleted, CommonConstants.IS_DELETE).set(AlertTemplate::getUpdateTime,
            LocalDateTime.now()).eq(AlertTemplate::getId, id).eq(AlertTemplate::getIsDeleted,
            CommonConstants.IS_NOT_DELETE);
        this.baseMapper.update(null, updateWrapper);
        List<AlertTemplateRule> templateRuleList = alertTemplateRuleMapper.selectList(
            Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId, id).eq(
                AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        alertTemplateRuleMapper.update(null,
            new LambdaUpdateWrapper<AlertTemplateRule>().set(AlertTemplateRule::getIsDeleted, 1).set(
                AlertTemplateRule::getUpdateTime, LocalDateTime.now()).eq(AlertTemplateRule::getTemplateId,
                id).eq(AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<Long> templateRuleIds = templateRuleList.stream().map(item -> item.getId()).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(templateRuleIds)) {
            return;
        }
        alertTemplateRuleItemMapper.update(null,
            new LambdaUpdateWrapper<AlertTemplateRuleItem>().set(AlertTemplateRuleItem::getIsDeleted,
                CommonConstants.IS_DELETE).set(AlertTemplateRuleItem::getUpdateTime, LocalDateTime.now()).in(
                AlertTemplateRuleItem::getTemplateRuleId, templateRuleIds).eq(
                AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
    }
}

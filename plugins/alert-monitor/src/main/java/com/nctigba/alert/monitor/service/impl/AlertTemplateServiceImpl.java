/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertTemplateDto;
import com.nctigba.alert.monitor.entity.AlertClusterNodeConf;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.entity.AlertRuleItem;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.model.AlertTemplateReq;
import com.nctigba.alert.monitor.model.AlertTemplateRuleReq;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.service.PrometheusService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    private AlertTemplateRuleMapper alertTemplateRuleMapper;
    @Autowired
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;
    @Autowired
    private AlertTemplateRuleService templateRuleService;
    @Autowired
    private AlertClusterNodeConfService nodeConfService;
    @Autowired
    private PrometheusService prometheusService;

    @Override
    public Page<AlertTemplate> getTemplatePage(String templateName, Page page) {
        return this.baseMapper.selectPage(page,
            Wrappers.<AlertTemplate>lambdaQuery().like(StrUtil.isNotBlank(templateName),
                AlertTemplate::getTemplateName, templateName).eq(AlertTemplate::getIsDeleted,
                CommonConstants.IS_NOT_DELETE).orderByDesc(AlertTemplate::getId));
    }

    @Override
    public Page<AlertTemplateRule> getTemplateRulePage(Long templateId, String ruleName, Page page) {
        Page<AlertTemplateRule> rulePage = alertTemplateRuleMapper.selectPage(page,
            Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId, templateId).like(
                StrUtil.isNotBlank(ruleName), AlertTemplateRule::getRuleName, ruleName).eq(
                AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                AlertTemplateRule::getId));
        List<AlertTemplateRule> records = rulePage.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            return rulePage;
        }
        List<Long> ruleIdList = records.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItem> alertTemplateRuleItems = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItem>lambdaQuery().in(AlertTemplateRuleItem::getTemplateRuleId,
                ruleIdList).eq(AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertTemplateRule record : records) {
            List<AlertTemplateRuleItem> templateRuleItems = alertTemplateRuleItems.stream().filter(
                item -> item.getTemplateRuleId().equals(record.getId())).collect(Collectors.toList());
            record.setAlertRuleItemList(templateRuleItems);
        }
        return rulePage;
    }


    @Override
    public AlertTemplateDto getTemplate(Long id) {
        AlertTemplate alertTemplate = this.baseMapper.selectById(id);
        AlertTemplateDto templateDto = new AlertTemplateDto();
        BeanUtil.copyProperties(alertTemplate, templateDto);
        List<AlertTemplateRule> ruleDtoList = templateRuleService.getListByTemplateId(id);
        templateDto.setTemplateRuleList(ruleDtoList);
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
            alertTemplate.setCreateTime(LocalDateTime.now()).setIsDeleted(CommonConstants.IS_NOT_DELETE);
        } else {
            alertTemplate.setTemplateName(templateReq.getTemplateName()).setUpdateTime(
                LocalDateTime.now()).setIsDeleted(CommonConstants.IS_NOT_DELETE);
        }
        saveOrUpdate(alertTemplate);

        List<AlertTemplateRuleReq> templateRuleReqList = templateReq.getTemplateRuleReqList();
        List<Long> templateRuleIds = templateRuleReqList.stream().map(item -> item.getTemplateRuleId()).filter(
            item -> item != null).collect(Collectors.toList());
        // delete the old rules
        List<AlertTemplateRule> alertTemplateRules = alertTemplateRuleMapper.selectList(
            Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId,
                    alertTemplate.getId()).notIn(CollectionUtil.isNotEmpty(templateRuleIds),
                    AlertTemplateRule::getId, templateRuleIds)
                .eq(AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(alertTemplateRules)) {
            alertTemplateRules.forEach(item -> item.setIsDeleted(CommonConstants.IS_DELETE)
                .setUpdateTime(LocalDateTime.now()));
            templateRuleService.updateBatchById(alertTemplateRules);
            List<Long> delTemplateRuleIds =
                alertTemplateRules.stream().map(item -> item.getId()).collect(Collectors.toList());
            alertTemplateRuleItemMapper.update(null,
                new LambdaUpdateWrapper<AlertTemplateRuleItem>().set(AlertTemplateRuleItem::getIsDeleted,
                        CommonConstants.IS_DELETE).set(AlertTemplateRuleItem::getUpdateTime, LocalDateTime.now())
                    .in(AlertTemplateRuleItem::getTemplateRuleId, delTemplateRuleIds)
                    .eq(AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        }
        // update
        for (AlertTemplateRuleReq alertTemplateRuleReq : templateRuleReqList) {
            updateByAlertTemplateRuleReq(alertTemplateRuleReq, alertTemplate.getId());
        }
        Long id = alertTemplate.getId();
        CompletableFuture.runAsync(() -> {
            prometheusService.updateRuleConfigByTemplateId(id);
        });
        return alertTemplate;
    }

    private void updateByAlertTemplateRuleReq(AlertTemplateRuleReq alertTemplateRuleReq, Long templateId) {
        if (alertTemplateRuleReq.getTemplateRuleId() == null) {
            AlertRule alertRule = alertRuleMapper.selectById(alertTemplateRuleReq.getRuleId());
            AlertTemplateRule alertTemplateRule = new AlertTemplateRule();
            BeanUtil.copyProperties(alertRule, alertTemplateRule);
            alertTemplateRule.setId(null).setTemplateId(templateId).setRuleId(alertRule.getId())
                .setIsDeleted(CommonConstants.IS_NOT_DELETE).setCreateTime(LocalDateTime.now()).setUpdateTime(null);
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
    public List<AlertTemplateRule> getTemplateRuleListById(Long templateId) {
        List<AlertTemplateRule> templateRuleList =
            alertTemplateRuleMapper.selectList(
                Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getTemplateId, templateId).eq(
                    AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                    AlertTemplateRule::getId));
        if (CollectionUtil.isEmpty(templateRuleList)) {
            return templateRuleList;
        }
        List<Long> ruleIdList = templateRuleList.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItem> alertTemplateRuleItems = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItem>lambdaQuery().in(AlertTemplateRuleItem::getTemplateRuleId,
                ruleIdList).eq(AlertTemplateRuleItem::getIsDeleted, 0));
        for (AlertTemplateRule templateRule : templateRuleList) {
            List<AlertTemplateRuleItem> templateRuleItems = alertTemplateRuleItems.stream().filter(
                item -> item.getTemplateRuleId().equals(templateRule.getId())).collect(Collectors.toList());
            templateRule.setAlertRuleItemList(templateRuleItems);
        }
        return templateRuleList;
    }

    @Override
    @Transactional
    public void delTemplate(Long id) {
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
            new LambdaUpdateWrapper<AlertTemplateRule>().set(AlertTemplateRule::getIsDeleted, CommonConstants.IS_DELETE)
                .set(AlertTemplateRule::getUpdateTime, LocalDateTime.now()).eq(AlertTemplateRule::getTemplateId, id)
                .eq(AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE));
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

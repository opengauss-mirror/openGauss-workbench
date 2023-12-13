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
 *  AlertTemplateServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertTemplateServiceImpl.java
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
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.dto.AlertTemplateDTO;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.model.query.AlertTemplateQuery;
import com.nctigba.alert.monitor.model.query.AlertTemplateRuleQuery;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
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
public class AlertTemplateServiceImpl extends ServiceImpl<AlertTemplateMapper, AlertTemplateDO>
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
    private PrometheusServiceImpl prometheusService;

    @Override
    public Page<AlertTemplateDO> getTemplatePage(String templateName, Page page) {
        return this.baseMapper.selectPage(page,
            Wrappers.<AlertTemplateDO>lambdaQuery().like(StrUtil.isNotBlank(templateName),
                AlertTemplateDO::getTemplateName, templateName).eq(AlertTemplateDO::getIsDeleted,
                CommonConstants.IS_NOT_DELETE).orderByDesc(AlertTemplateDO::getId));
    }

    @Override
    public Page<AlertTemplateRuleDO> getTemplateRulePage(Long templateId, String ruleName, Page page) {
        Page<AlertTemplateRuleDO> rulePage = alertTemplateRuleMapper.selectPage(page,
            Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getTemplateId, templateId).like(
                StrUtil.isNotBlank(ruleName), AlertTemplateRuleDO::getRuleName, ruleName).eq(
                AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                AlertTemplateRuleDO::getId));
        List<AlertTemplateRuleDO> records = rulePage.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            return rulePage;
        }
        List<Long> ruleIdList = records.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItemDO>lambdaQuery().in(AlertTemplateRuleItemDO::getTemplateRuleId,
                ruleIdList).eq(AlertTemplateRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertTemplateRuleDO record : records) {
            List<AlertTemplateRuleItemDO> templateRuleItems = alertTemplateRuleItemDOS.stream().filter(
                item -> item.getTemplateRuleId().equals(record.getId())).collect(Collectors.toList());
            record.setAlertRuleItemList(templateRuleItems);
        }
        return rulePage;
    }


    @Override
    public AlertTemplateDTO getTemplate(Long id) {
        AlertTemplateDO alertTemplateDO = this.baseMapper.selectById(id);
        AlertTemplateDTO templateDto = new AlertTemplateDTO();
        BeanUtil.copyProperties(alertTemplateDO, templateDto);
        List<AlertTemplateRuleDO> ruleDtoList = templateRuleService.getListByTemplateId(id);
        templateDto.setTemplateRuleList(ruleDtoList);
        return templateDto;
    }

    @Override
    @Transactional
    public AlertTemplateDO saveTemplate(AlertTemplateQuery templateReq) {
        AlertTemplateDO alertTemplateDO = null;
        if (templateReq.getId() != null) {
            alertTemplateDO = this.baseMapper.selectById(templateReq.getId());
        }
        if (alertTemplateDO == null) {
            alertTemplateDO = new AlertTemplateDO();
            BeanUtil.copyProperties(templateReq, alertTemplateDO);
            alertTemplateDO.setCreateTime(LocalDateTime.now()).setIsDeleted(CommonConstants.IS_NOT_DELETE);
        } else {
            alertTemplateDO.setTemplateName(templateReq.getTemplateName()).setUpdateTime(
                LocalDateTime.now()).setIsDeleted(CommonConstants.IS_NOT_DELETE);
        }
        saveOrUpdate(alertTemplateDO);

        List<AlertTemplateRuleQuery> templateRuleReqList = templateReq.getTemplateRuleReqList();
        List<Long> templateRuleIds = templateRuleReqList.stream().map(item -> item.getTemplateRuleId()).filter(
            item -> item != null).collect(Collectors.toList());
        // delete the old rules
        List<AlertTemplateRuleDO> alertTemplateRuleDOS = alertTemplateRuleMapper.selectList(
            Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getTemplateId,
                    alertTemplateDO.getId()).notIn(CollectionUtil.isNotEmpty(templateRuleIds),
                    AlertTemplateRuleDO::getId, templateRuleIds)
                .eq(AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(alertTemplateRuleDOS)) {
            alertTemplateRuleDOS.forEach(item -> item.setIsDeleted(CommonConstants.IS_DELETE)
                .setUpdateTime(LocalDateTime.now()));
            templateRuleService.updateBatchById(alertTemplateRuleDOS);
            List<Long> delTemplateRuleIds =
                alertTemplateRuleDOS.stream().map(item -> item.getId()).collect(Collectors.toList());
            alertTemplateRuleItemMapper.update(null,
                new LambdaUpdateWrapper<AlertTemplateRuleItemDO>().set(AlertTemplateRuleItemDO::getIsDeleted,
                        CommonConstants.IS_DELETE).set(AlertTemplateRuleItemDO::getUpdateTime, LocalDateTime.now())
                    .in(AlertTemplateRuleItemDO::getTemplateRuleId, delTemplateRuleIds)
                    .eq(AlertTemplateRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        }
        // update
        for (AlertTemplateRuleQuery alertTemplateRuleQuery : templateRuleReqList) {
            updateByAlertTemplateRuleReq(alertTemplateRuleQuery, alertTemplateDO.getId());
        }
        Long id = alertTemplateDO.getId();
        CompletableFuture.runAsync(() -> {
            prometheusService.updateRuleConfigByTemplateId(id);
        });
        return alertTemplateDO;
    }

    private void updateByAlertTemplateRuleReq(AlertTemplateRuleQuery alertTemplateRuleQuery, Long templateId) {
        if (alertTemplateRuleQuery.getTemplateRuleId() == null) {
            AlertRuleDO alertRuleDO = alertRuleMapper.selectById(alertTemplateRuleQuery.getRuleId());
            AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO();
            BeanUtil.copyProperties(alertRuleDO, alertTemplateRuleDO);
            alertTemplateRuleDO.setId(null).setTemplateId(templateId).setRuleId(alertRuleDO.getId())
                .setIsDeleted(CommonConstants.IS_NOT_DELETE).setCreateTime(LocalDateTime.now()).setUpdateTime(null);
            alertTemplateRuleMapper.insert(alertTemplateRuleDO);
            List<AlertRuleItemDO> alertRuleItemDOS = alertRuleItemMapper.selectList(
                Wrappers.<AlertRuleItemDO>lambdaQuery().eq(AlertRuleItemDO::getRuleId, alertRuleDO.getId()).eq(
                    AlertRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
            for (AlertRuleItemDO alertRuleItemDO : alertRuleItemDOS) {
                AlertTemplateRuleItemDO templateRuleItem = new AlertTemplateRuleItemDO();
                BeanUtil.copyProperties(alertRuleItemDO, templateRuleItem);
                templateRuleItem.setId(null).setTemplateRuleId(alertTemplateRuleDO.getId()).setRuleItemId(
                    alertRuleItemDO.getId()).setIsDeleted(0).setCreateTime(LocalDateTime.now()).setUpdateTime(null);
                alertTemplateRuleItemMapper.insert(templateRuleItem);
            }
        } else {
            AlertTemplateRuleDO alertTemplateRuleDO = alertTemplateRuleMapper.selectById(
                alertTemplateRuleQuery.getTemplateRuleId());
            alertTemplateRuleDO.setTemplateId(templateId);
            alertTemplateRuleMapper.updateById(alertTemplateRuleDO);
        }
    }

    @Override
    public List<AlertTemplateDO> getTemplateList() {
        return this.baseMapper.selectList(Wrappers.<AlertTemplateDO>lambdaQuery().eq(AlertTemplateDO::getIsDeleted,
            CommonConstants.IS_NOT_DELETE).orderByDesc(AlertTemplateDO::getId));
    }

    @Override
    public List<AlertTemplateRuleDO> getTemplateRuleListById(Long templateId) {
        List<AlertTemplateRuleDO> templateRuleList =
            alertTemplateRuleMapper.selectList(
                Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getTemplateId, templateId).eq(
                    AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                    AlertTemplateRuleDO::getId));
        if (CollectionUtil.isEmpty(templateRuleList)) {
            return templateRuleList;
        }
        List<Long> ruleIdList = templateRuleList.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItemDO>lambdaQuery().in(AlertTemplateRuleItemDO::getTemplateRuleId,
                ruleIdList).eq(AlertTemplateRuleItemDO::getIsDeleted, 0));
        for (AlertTemplateRuleDO templateRule : templateRuleList) {
            List<AlertTemplateRuleItemDO> templateRuleItems = alertTemplateRuleItemDOS.stream().filter(
                item -> item.getTemplateRuleId().equals(templateRule.getId())).collect(Collectors.toList());
            templateRule.setAlertRuleItemList(templateRuleItems);
        }
        return templateRuleList;
    }

    @Override
    @Transactional
    public void delTemplate(Long id) {
        List<AlertClusterNodeConfDO> nodeConfList = nodeConfService.list(
            Wrappers.<AlertClusterNodeConfDO>lambdaQuery().eq(AlertClusterNodeConfDO::getTemplateId, id).eq(
                AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(nodeConfList)) {
            throw new ServiceException(MessageSourceUtils.get("templateIsUsed"));
        }
        LambdaUpdateWrapper<AlertTemplateDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(AlertTemplateDO::getIsDeleted, CommonConstants.IS_DELETE).set(AlertTemplateDO::getUpdateTime,
            LocalDateTime.now()).eq(AlertTemplateDO::getId, id).eq(AlertTemplateDO::getIsDeleted,
            CommonConstants.IS_NOT_DELETE);
        this.baseMapper.update(null, updateWrapper);
        List<AlertTemplateRuleDO> templateRuleList = alertTemplateRuleMapper.selectList(
            Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getTemplateId, id).eq(
                AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        alertTemplateRuleMapper.update(null,
            new LambdaUpdateWrapper<AlertTemplateRuleDO>()
                .set(AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_DELETE)
                .set(AlertTemplateRuleDO::getUpdateTime, LocalDateTime.now()).eq(AlertTemplateRuleDO::getTemplateId, id)
                .eq(AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<Long> templateRuleIds = templateRuleList.stream().map(item -> item.getId()).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(templateRuleIds)) {
            return;
        }
        alertTemplateRuleItemMapper.update(null,
            new LambdaUpdateWrapper<AlertTemplateRuleItemDO>().set(AlertTemplateRuleItemDO::getIsDeleted,
                CommonConstants.IS_DELETE).set(AlertTemplateRuleItemDO::getUpdateTime, LocalDateTime.now()).in(
                AlertTemplateRuleItemDO::getTemplateRuleId, templateRuleIds).eq(
                AlertTemplateRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
    }
}

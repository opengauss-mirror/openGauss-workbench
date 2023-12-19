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
 *  AlertTemplateRuleServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertTemplateRuleServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.dto.AlertTemplateRuleDTO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
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
public class AlertTemplateRuleServiceImpl extends ServiceImpl<AlertTemplateRuleMapper, AlertTemplateRuleDO>
    implements AlertTemplateRuleService {
    @Autowired
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;
    @Autowired
    private AlertTemplateRuleItemService templateRuleItemService;
    @Autowired
    private PrometheusServiceImpl prometheusService;

    @Override
    public AlertTemplateRuleDO getTemplateRule(Long templateRuleId) {
        AlertTemplateRuleDO alertTemplateRuleDO = this.baseMapper.selectById(templateRuleId);
        if (alertTemplateRuleDO == null) {
            throw new ServiceException("the rule is not found");
        }
        List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItemDO>lambdaQuery().eq(AlertTemplateRuleItemDO::getTemplateRuleId,
                    alertTemplateRuleDO.getId())
                .eq(AlertTemplateRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        alertTemplateRuleDO.setAlertRuleItemList(alertTemplateRuleItemDOS);
        return alertTemplateRuleDO;
    }

    @Override
    @Transactional
    public AlertTemplateRuleDO saveTemplateRule(AlertTemplateRuleDO alertTemplateRuleDO) {
        if (alertTemplateRuleDO.getId() == null) {
            alertTemplateRuleDO.setCreateTime(LocalDateTime.now());
        } else {
            alertTemplateRuleDO.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(alertTemplateRuleDO);
        List<AlertTemplateRuleItemDO> templateRuleItemList = alertTemplateRuleDO.getAlertRuleItemList();
        if (CollectionUtil.isEmpty(templateRuleItemList)) {
            return alertTemplateRuleDO;
        }
        for (AlertTemplateRuleItemDO templateRuleItem : templateRuleItemList) {
            templateRuleItem.setTemplateRuleId(alertTemplateRuleDO.getId());
            if (templateRuleItem.getId() == null) {
                templateRuleItem.setCreateTime(LocalDateTime.now());
            } else {
                templateRuleItem.setUpdateTime(LocalDateTime.now());
            }
        }
        templateRuleItemService.saveOrUpdateBatch(templateRuleItemList);
        if (alertTemplateRuleDO.getRuleType().equals(CommonConstants.INDEX_RULE)
            && alertTemplateRuleDO.getTemplateId() != null) {
            prometheusService.updateRuleByTemplateRule(alertTemplateRuleDO);
        }
        return alertTemplateRuleDO;
    }


    public List<AlertTemplateRuleDO> getListByTemplateId(Long templateId) {
        List<AlertTemplateRuleDO> alertRules =
            this.baseMapper.selectList(
                Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getTemplateId, templateId).eq(
                    AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                    AlertTemplateRuleDO::getId));
        List<Long> ruleIdList = alertRules.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<AlertTemplateRuleItemDO> alertRuleItems = alertTemplateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItemDO>lambdaQuery().in(AlertTemplateRuleItemDO::getTemplateRuleId,
                ruleIdList).eq(AlertTemplateRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        for (AlertTemplateRuleDO alertRule : alertRules) {
            AlertTemplateRuleDTO ruleDto = new AlertTemplateRuleDTO();
            BeanUtil.copyProperties(alertRule, ruleDto);
            List<AlertTemplateRuleItemDO> ruleItems = alertRuleItems.stream().filter(
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
        AlertTemplateRuleDO alertTemplateRuleDO = this.baseMapper.selectById(templateRuleId);
        alertTemplateRuleDO.setEnable(CommonConstants.ENABLE).setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(alertTemplateRuleDO);
        if (alertTemplateRuleDO.getRuleType().equals(CommonConstants.INDEX_RULE)) {
            prometheusService.updateRuleByTemplateRule(alertTemplateRuleDO);
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
        AlertTemplateRuleDO alertTemplateRuleDO = this.baseMapper.selectById(templateRuleId);
        alertTemplateRuleDO.setEnable(CommonConstants.DISABLE).setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(alertTemplateRuleDO);
        if (alertTemplateRuleDO.getRuleType().equals(CommonConstants.INDEX_RULE)) {
            prometheusService.removeRuleByTemplateRule(alertTemplateRuleDO);
        }
    }

    /**
     * get AlertTemplateRuleDTO List by ruleId, not templateRuleId
     *
     * @param ruleId Long
     * @return List<AlertTemplateRuleDTO>
     */
    @Override
    public List<AlertTemplateRuleDTO> getDtoListByRuleId(Long ruleId) {
        return this.baseMapper.getDtoListByRuleId(ruleId);
    }
}

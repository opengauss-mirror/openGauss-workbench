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
 *  NotifyWayServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/NotifyWayServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.dto.NotifyWayDTO;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.service.AlertRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.CommunicationService;
import com.nctigba.alert.monitor.service.NotifyTemplateService;
import com.nctigba.alert.monitor.service.NotifyWayService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/25 15:54
 * @description
 */
@Service
@Slf4j
public class NotifyWayServiceImpl extends ServiceImpl<NotifyWayMapper, NotifyWayDO>
    implements NotifyWayService {
    @Autowired
    private AlertRuleService ruleService;
    @Autowired
    private AlertTemplateRuleService templateRuleService;
    @Autowired
    private NotifyTemplateService notifyTemplateService;
    @Autowired
    private List<CommunicationService> communicationServices;

    @Override
    public Page<NotifyWayDTO> getListPage(String name, String notifyType, Page page) {
        QueryWrapper<NotifyWayDO> ew =
            Wrappers.<NotifyWayDO>query().like(StrUtil.isNotBlank(name), "t1.name", name).eq(
                StrUtil.isNotBlank(notifyType), "t1.notify_type", notifyType).eq("t1.is_deleted",
                CommonConstants.IS_NOT_DELETE).orderByDesc("t1.id");
        return this.baseMapper.selectDtoPage(page, ew);
    }

    @Override
    public List<NotifyWayDO> getList(String notifyType) {
        return this.baseMapper.selectList(Wrappers.<NotifyWayDO>lambdaQuery().eq(NotifyWayDO::getIsDeleted,
            CommonConstants.IS_NOT_DELETE).eq(StrUtil.isNotBlank(notifyType), NotifyWayDO::getNotifyType,
            notifyType).orderByDesc(NotifyWayDO::getId));
    }

    @Override
    public void saveNotifyWay(NotifyWayDO notifyWayDO) {
        if (notifyWayDO.getId() == null) {
            notifyWayDO.setCreateTime(LocalDateTime.now());
        } else {
            notifyWayDO.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(notifyWayDO);
    }

    @Override
    public void delById(Long id) {
        List<AlertRuleDO> ruleList = ruleService.list(
            Wrappers.<AlertRuleDO>query().eq("is_deleted", CommonConstants.IS_NOT_DELETE).and(
                wrapper -> wrapper.like("notify_way_ids", id + ",").or().like(
                    "notify_way_ids", "," + id).or().eq("notify_way_ids", id + "")));
        List<AlertTemplateRuleDO> templateRuleList = templateRuleService.list(
            Wrappers.<AlertTemplateRuleDO>query().eq("is_deleted", CommonConstants.IS_NOT_DELETE).isNotNull(
                "template_id").and(wrapper -> wrapper.like("notify_way_ids", id + ",").or().like(
                    "notify_way_ids", "," + id).or().eq("notify_way_ids", id + "")));
        if (CollectionUtil.isNotEmpty(ruleList) || CollectionUtil.isNotEmpty(templateRuleList)) {
            throw new ServiceException(MessageSourceUtils.get("notifyWayIsUsed"));
        }
        LambdaUpdateWrapper<NotifyWayDO> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.set(NotifyWayDO::getIsDeleted, CommonConstants.IS_DELETE).eq(NotifyWayDO::getId, id).eq(
            NotifyWayDO::getIsDeleted, CommonConstants.IS_NOT_DELETE);
        this.update(queryWrapper);
    }

    /**
     * test notify way
     *
     * @param notifyWayDO NotifyWay
     * @return boolean
     */
    @Override
    public boolean testNotifyWay(NotifyWayDO notifyWayDO) {
        CommunicationService communicationService = communicationServices.stream().filter(
                item -> item.getType().equals(notifyWayDO.getNotifyType())).findFirst().orElse(null);
        if (communicationService == null) {
            throw new ServiceException("Testing for this type of notifyWay is not exist");
        }
        if (notifyWayDO.getNotifyType().equals(CommonConstants.WEBHOOK)
            || notifyWayDO.getNotifyType().equals(CommonConstants.SNMP)) {
            return communicationService.sendTest(null, notifyWayDO);
        } else {
            throw new ServiceException("Testing for this type of notifyWay is not supported");
        }
    }
}

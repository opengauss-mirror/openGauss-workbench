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
 *  NotifyConfigServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/NotifyConfigServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.NotifyConfigDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.mapper.NotifyConfigMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.model.query.NotifyConfigQuery;
import com.nctigba.alert.monitor.service.CommunicationService;
import com.nctigba.alert.monitor.service.NotifyConfigService;
import com.nctigba.alert.monitor.service.impl.communication.DingTalkServiceImpl;
import com.nctigba.alert.monitor.service.impl.communication.EmailServiceImpl;
import com.nctigba.alert.monitor.service.impl.communication.WeComServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/6/1 01:46
 * @description
 */
@Service
@Slf4j
public class NotifyConfigServiceImpl extends ServiceImpl<NotifyConfigMapper, NotifyConfigDO>
    implements NotifyConfigService {
    @Autowired
    private EmailServiceImpl emailServiceImpl;
    @Autowired
    private NotifyWayMapper notifyWayMapper;
    @Autowired
    private NotifyTemplateMapper notifyTemplateMapper;
    @Autowired
    private SmartValidator smartValidator;
    @Autowired
    private WeComServiceImpl weComServiceImpl;
    @Autowired
    private DingTalkServiceImpl dingTalkServiceImpl;
    @Autowired
    private List<CommunicationService> communicationServices;

    @Override
    public List<NotifyConfigDO> getAllList() {
        List<NotifyConfigDO> notifyConfigDOList = new ArrayList<>();
        List<NotifyConfigDO> list = this.list(
            Wrappers.<NotifyConfigDO>lambdaQuery().eq(NotifyConfigDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        Map<String, List<NotifyConfigDO>> map = list.stream().collect(Collectors.groupingBy(NotifyConfigDO::getType));
        map.forEach((key, val) -> {
            if (CollectionUtil.isNotEmpty(val)) {
                notifyConfigDOList.add(val.get(0));
            }
        });
        return notifyConfigDOList;
    }

    @Override
    public void saveList(List<NotifyConfigDO> list) {
        if (CollectionUtil.isEmpty(list)) {
            throw new ServiceException("the notify config list is empty");
        }
        // validate
        for (NotifyConfigDO notifyConfigDO : list) {
            Errors errors = new BeanPropertyBindingResult(notifyConfigDO, "notifyConfig");
            if (notifyConfigDO.getEnable() == null || notifyConfigDO.getEnable().equals(CommonConstants.DISABLE)) {
                continue;
            }
            smartValidator.validate(notifyConfigDO, errors);
            if (errors.hasErrors()) {
                throw new ServiceException(StrUtil.join(",", errors.getAllErrors()));
            }
        }

        Set<String> typeSet = list.stream().map(item -> item.getType()).collect(Collectors.toSet());
        List<NotifyConfigDO> oldList = this.list(
            Wrappers.<NotifyConfigDO>lambdaQuery().in(NotifyConfigDO::getType, typeSet).eq(NotifyConfigDO::getIsDeleted,
                CommonConstants.IS_NOT_DELETE));
        for (NotifyConfigDO notifyConfigDO : list) {
            String type = notifyConfigDO.getType();
            List<NotifyConfigDO> list0 = oldList.stream().filter(item -> item.getType().equals(type)).collect(
                Collectors.toList());
            if (CollectionUtil.isEmpty(list0)) {
                notifyConfigDO.setCreateTime(LocalDateTime.now());
                continue;
            }
            notifyConfigDO.setId(list0.get(0).getId()).setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdateBatch(list);
    }

    @Override
    public boolean testConfig(NotifyConfigQuery notifyConfigReq) {
        NotifyWayDO notifyWayDO = notifyWayMapper.selectById(notifyConfigReq.getNotifyWayId());
        CommunicationService communicationService = communicationServices.stream().filter(
            item -> item.getType().equals(notifyConfigReq.getType())).findFirst().orElse(null);

        if (communicationService == null) {
            throw new ServiceException("Not support this type:" + notifyConfigReq.getType());
        }
        return communicationService.sendTest(notifyConfigReq, notifyWayDO);
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyConfig;
import com.nctigba.alert.monitor.entity.NotifyTemplate;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.NotifyConfigMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.model.NotifyConfigReq;
import com.nctigba.alert.monitor.service.DingTalkService;
import com.nctigba.alert.monitor.service.EmailService;
import com.nctigba.alert.monitor.service.NotifyConfigService;
import com.nctigba.alert.monitor.service.WeComService;
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
public class NotifyConfigServiceImpl extends ServiceImpl<NotifyConfigMapper, NotifyConfig>
    implements NotifyConfigService {
    @Autowired
    private EmailService emailService;
    @Autowired
    private NotifyWayMapper notifyWayMapper;
    @Autowired
    private NotifyTemplateMapper notifyTemplateMapper;
    @Autowired
    private SmartValidator smartValidator;
    @Autowired
    private WeComService weComService;
    @Autowired
    private DingTalkService dingTalkService;

    @Override
    public List<NotifyConfig> getAllList() {
        List<NotifyConfig> notifyConfigList = new ArrayList<>();
        List<NotifyConfig> list = this.list(
            Wrappers.<NotifyConfig>lambdaQuery().eq(NotifyConfig::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        Map<String, List<NotifyConfig>> map = list.stream().collect(Collectors.groupingBy(NotifyConfig::getType));
        map.forEach((key, val) -> {
            if (CollectionUtil.isNotEmpty(val)) {
                notifyConfigList.add(val.get(0));
            }
        });
        return notifyConfigList;
    }

    @Override
    public void saveList(List<NotifyConfig> list) {
        if (CollectionUtil.isEmpty(list)) {
            throw new ServiceException("the notify config list is empty");
        }
        // validate
        for (NotifyConfig notifyConfig : list) {
            Errors errors = new BeanPropertyBindingResult(notifyConfig, "notifyConfig");
            if (notifyConfig.getEnable() == null || notifyConfig.getEnable().equals(CommonConstants.DISABLE)) {
                continue;
            }
            smartValidator.validate(notifyConfig, errors);
            if (errors.hasErrors()) {
                throw new ServiceException(StrUtil.join(",", errors.getAllErrors()));
            }
        }

        Set<String> typeSet = list.stream().map(item -> item.getType()).collect(Collectors.toSet());
        List<NotifyConfig> oldList = this.list(
            Wrappers.<NotifyConfig>lambdaQuery().in(NotifyConfig::getType, typeSet).eq(NotifyConfig::getIsDeleted,
                CommonConstants.IS_NOT_DELETE));
        for (NotifyConfig notifyConfig : list) {
            String type = notifyConfig.getType();
            List<NotifyConfig> list0 = oldList.stream().filter(item -> item.getType().equals(type)).collect(
                Collectors.toList());
            if (CollectionUtil.isEmpty(list0)) {
                notifyConfig.setCreateTime(LocalDateTime.now());
                continue;
            }
            notifyConfig.setId(list0.get(0).getId()).setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdateBatch(list);
    }

    @Override
    public boolean testConfig(NotifyConfigReq notifyConfigReq) {
        NotifyWay notifyWay = notifyWayMapper.selectById(notifyConfigReq.getNotifyWayId());
        NotifyTemplate notifyTemplate = notifyTemplateMapper.selectById(notifyWay.getNotifyTemplateId());
        if (notifyConfigReq.getType().equals(CommonConstants.EMAIL)) {
            emailService.sendTest(notifyConfigReq, notifyWay.getEmail(), notifyTemplate.getNotifyTitle(),
                notifyTemplate.getNotifyContent());
            return true;
        } else if (notifyConfigReq.getType().equals(CommonConstants.WE_COM)) {
            return weComService.sendTest(notifyConfigReq, notifyWay, notifyTemplate.getNotifyContent());
        } else if (notifyConfigReq.getType().equals(CommonConstants.DING_TALK)) {
            return dingTalkService.sendTest(notifyConfigReq, notifyWay, notifyTemplate.getNotifyContent());
        } else {
            throw new ServiceException("test fail");
        }
    }
}

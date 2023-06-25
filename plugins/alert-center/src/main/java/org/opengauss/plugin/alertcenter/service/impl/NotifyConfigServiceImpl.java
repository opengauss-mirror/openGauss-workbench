/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.plugin.alertcenter.constant.CommonConstants;
import org.opengauss.plugin.alertcenter.entity.NotifyConfig;
import org.opengauss.plugin.alertcenter.entity.NotifyTemplate;
import org.opengauss.plugin.alertcenter.entity.NotifyWay;
import org.opengauss.plugin.alertcenter.mapper.NotifyConfigMapper;
import org.opengauss.plugin.alertcenter.mapper.NotifyTemplateMapper;
import org.opengauss.plugin.alertcenter.mapper.NotifyWayMapper;
import org.opengauss.plugin.alertcenter.model.NotifyConfigReq;
import org.opengauss.plugin.alertcenter.service.EmailService;
import org.opengauss.plugin.alertcenter.service.NotifyConfigService;
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
            smartValidator.validate(notifyConfig, errors, NotifyConfig.DefaultGroup.class);
            if (errors.hasErrors()) {
                throw new ServiceException(StrUtil.join(",", errors.getAllErrors()));
            }
            if (notifyConfig.getType().equals(CommonConstants.EMAIL)
                    && notifyConfig.getEnable() == CommonConstants.ENABLE) {
                smartValidator.validate(notifyConfig, errors, NotifyConfig.EmailGroup.class);
            }
            if (notifyConfig.getType().equals(CommonConstants.WE_COM)
                    && notifyConfig.getEnable() == CommonConstants.ENABLE) {
                smartValidator.validate(notifyConfig, errors, NotifyConfig.WeComGroup.class);
            }
            if (notifyConfig.getType().equals(CommonConstants.DING_TALK)
                    && notifyConfig.getEnable() == CommonConstants.ENABLE) {
                smartValidator.validate(notifyConfig, errors, NotifyConfig.DingTalkGroup.class);
            }
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
    public void testConfig(NotifyConfigReq notifyConfigReq) {
        NotifyWay notifyWay = notifyWayMapper.selectById(notifyConfigReq.getNotifyWayId());
        NotifyTemplate notifyTemplate = notifyTemplateMapper.selectById(notifyWay.getNotifyTemplateId());
        if (notifyConfigReq.getType().equals(CommonConstants.EMAIL)) {
            emailService.sendTest(notifyConfigReq, notifyWay.getEmail(), notifyTemplate.getNotifyTitle(),
                    notifyTemplate.getNotifyContent());
        } else {
            throw new ServiceException("test fail");
        }
    }
}

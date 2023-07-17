/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyTemplate;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.service.NotifyTemplateService;
import com.nctigba.alert.monitor.service.NotifyWayService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/31 10:18
 * @description
 */
@Service
@Slf4j
public class NotifyTemplateServiceImpl extends ServiceImpl<NotifyTemplateMapper, NotifyTemplate>
        implements NotifyTemplateService {
    @Autowired
    private NotifyWayService notifyWayService;

    @Override
    public Page getListPage(String notifyTemplateName, String notifyTemplateType, Page page) {
        return this.page(page,
                Wrappers.<NotifyTemplate>lambdaQuery().like(StrUtil.isNotBlank(notifyTemplateName),
                        NotifyTemplate::getNotifyTemplateName, notifyTemplateName).eq(
                        StrUtil.isNotBlank(notifyTemplateType), NotifyTemplate::getNotifyTemplateType,
                        notifyTemplateType).eq(NotifyTemplate::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                        NotifyTemplate::getId));
    }

    @Override
    public void saveTemplate(NotifyTemplate notifyTemplate) {
        if (notifyTemplate.getId() == null) {
            notifyTemplate.setCreateTime(LocalDateTime.now());
        } else {
            notifyTemplate.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(notifyTemplate);
    }

    @Override
    public void delById(Long id) {
        List<NotifyWay> list = notifyWayService.list(
                Wrappers.<NotifyWay>lambdaQuery().eq(NotifyWay::getNotifyTemplateId, id).eq(NotifyWay::getIsDeleted,
                        CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(list)) {
            throw new ServiceException(MessageSourceUtil.get("notifyTemplateIsUsed"));
        }
        LambdaUpdateWrapper<NotifyTemplate> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(NotifyTemplate::getIsDeleted, CommonConstants.IS_DELETE).set(NotifyTemplate::getUpdateTime,
                LocalDateTime.now()).eq(NotifyTemplate::getId, id);
        this.update(null, updateWrapper);
    }

    @Override
    public List<NotifyTemplate> getList(String notifyTemplateType) {
        return this.list(Wrappers.<NotifyTemplate>lambdaQuery().eq(NotifyTemplate::getIsDeleted,
                CommonConstants.IS_NOT_DELETE).eq(StrUtil.isNotBlank(notifyTemplateType),
                NotifyTemplate::getNotifyTemplateType, notifyTemplateType).orderByDesc(NotifyTemplate::getId));
    }
}

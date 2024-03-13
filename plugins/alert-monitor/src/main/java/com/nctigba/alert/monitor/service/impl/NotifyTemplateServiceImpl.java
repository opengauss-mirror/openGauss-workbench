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
 *  NotifyTemplateServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/NotifyTemplateServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.service.NotifyTemplateService;
import com.nctigba.alert.monitor.service.NotifyWayService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
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
public class NotifyTemplateServiceImpl extends ServiceImpl<NotifyTemplateMapper, NotifyTemplateDO>
        implements NotifyTemplateService {
    @Autowired
    private NotifyWayService notifyWayService;

    @Override
    public Page getListPage(String notifyTemplateName, String notifyTemplateType, Page page) {
        return this.page(page,
                Wrappers.<NotifyTemplateDO>lambdaQuery().like(StrUtil.isNotBlank(notifyTemplateName),
                        NotifyTemplateDO::getNotifyTemplateName, notifyTemplateName)
                    .eq(NotifyTemplateDO::getIsDeleted, CommonConstants.IS_NOT_DELETE)
                    .orderByDesc(NotifyTemplateDO::getId));
    }

    @Override
    public void saveTemplate(NotifyTemplateDO notifyTemplateDO) {
        if (notifyTemplateDO.getId() == null) {
            notifyTemplateDO.setCreateTime(LocalDateTime.now());
        } else {
            notifyTemplateDO.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(notifyTemplateDO);
    }

    @Override
    public void delById(Long id) {
        List<NotifyWayDO> list = notifyWayService.list(
                Wrappers.<NotifyWayDO>lambdaQuery().eq(NotifyWayDO::getNotifyTemplateId, id)
                    .eq(NotifyWayDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(list)) {
            throw new ServiceException(MessageSourceUtils.get("notifyTemplateIsUsed"));
        }
        LambdaUpdateWrapper<NotifyTemplateDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(NotifyTemplateDO::getIsDeleted, CommonConstants.IS_DELETE)
            .set(NotifyTemplateDO::getUpdateTime, LocalDateTime.now()).eq(NotifyTemplateDO::getId, id);
        this.update(null, updateWrapper);
    }

    @Override
    public List<NotifyTemplateDO> getList() {
        return this.list(Wrappers.<NotifyTemplateDO>lambdaQuery().eq(NotifyTemplateDO::getIsDeleted,
                CommonConstants.IS_NOT_DELETE).orderByDesc(NotifyTemplateDO::getId));
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.plugin.alertcenter.constant.CommonConstants;
import org.opengauss.plugin.alertcenter.dto.NotifyWayDto;
import org.opengauss.plugin.alertcenter.entity.AlertRule;
import org.opengauss.plugin.alertcenter.entity.AlertTemplateRule;
import org.opengauss.plugin.alertcenter.entity.NotifyWay;
import org.opengauss.plugin.alertcenter.mapper.NotifyWayMapper;
import org.opengauss.plugin.alertcenter.service.AlertRuleService;
import org.opengauss.plugin.alertcenter.service.AlertTemplateRuleService;
import org.opengauss.plugin.alertcenter.service.NotifyWayService;
import org.opengauss.plugin.alertcenter.utils.MessageSourceUtil;
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
public class NotifyWayServiceImpl extends ServiceImpl<NotifyWayMapper, NotifyWay>
        implements NotifyWayService {
    @Autowired
    private AlertRuleService ruleService;
    @Autowired
    private AlertTemplateRuleService templateRuleService;

    @Override
    public Page<NotifyWayDto> getListPage(String name, String notifyType, Page page) {
        QueryWrapper<NotifyWay> ew =
                Wrappers.<NotifyWay>query().like(StrUtil.isNotBlank(name), "t1.name", name).eq(
                        StrUtil.isNotBlank(notifyType), "t1.notify_type", notifyType).eq("t1.is_deleted",
                        CommonConstants.IS_NOT_DELETE).orderByDesc("t1.id");
        return this.baseMapper.selectDtoPage(page, ew);
    }

    @Override
    public List<NotifyWay> getList(String notifyType) {
        return this.baseMapper.selectList(Wrappers.<NotifyWay>lambdaQuery().eq(NotifyWay::getIsDeleted,
                CommonConstants.IS_NOT_DELETE).eq(StrUtil.isNotBlank(notifyType), NotifyWay::getNotifyType,
                notifyType).orderByDesc(NotifyWay::getId));
    }

    @Override
    public void saveNotifyWay(NotifyWay notifyWay) {
        if (notifyWay.getId() == null) {
            notifyWay.setCreateTime(LocalDateTime.now());
        } else {
            notifyWay.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(notifyWay);
    }

    @Override
    public void delById(Long id) {
        MessageSourceUtil.reset();
        List<AlertRule> ruleList = ruleService.list(
                Wrappers.<AlertRule>query().eq("is_deleted", CommonConstants.IS_NOT_DELETE).and(
                        wrapper -> wrapper.gt("position('" + id + ",' in notify_way_ids)", 0).or().gt(
                                "position('," + id + "' in notify_way_ids)", 0)));
        List<AlertTemplateRule> templateRuleList = templateRuleService.list(
                Wrappers.<AlertTemplateRule>query().eq("is_deleted", CommonConstants.IS_NOT_DELETE).and(
                        wrapper -> wrapper.gt("position('" + id + ",' in notify_way_ids)", 0).or().gt(
                                "position('," + id + "' in notify_way_ids)", 0)));
        if (CollectionUtil.isNotEmpty(ruleList) || CollectionUtil.isNotEmpty(templateRuleList)) {
            throw new ServiceException(MessageSourceUtil.get("notifyWayIsUsed"));
        }
        LambdaUpdateWrapper<NotifyWay> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.set(NotifyWay::getIsDeleted, CommonConstants.IS_DELETE).eq(NotifyWay::getId, id).eq(
                NotifyWay::getIsDeleted, CommonConstants.IS_NOT_DELETE);
        this.update(queryWrapper);
    }
}

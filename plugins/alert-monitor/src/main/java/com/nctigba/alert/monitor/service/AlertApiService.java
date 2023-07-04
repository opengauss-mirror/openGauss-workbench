/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertContentParamDto;
import com.nctigba.alert.monitor.entity.AlertRecord;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.entity.NotifyTemplate;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.AlertRecordMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.model.api.AlertApiReq;
import com.nctigba.alert.monitor.model.api.AlertLabels;
import com.nctigba.alert.monitor.utils.TextParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/5/2 10:05
 * @description
 */
@Service
@Slf4j
public class AlertApiService {
    @Autowired
    private AlertRecordMapper alertRecordMapper;
    @Autowired
    private AlertTemplateMapper templateMapper;
    @Autowired
    private AlertTemplateRuleMapper templateRuleMapper;
    @Autowired
    private NotifyWayMapper notifyWayMapper;
    @Autowired
    private NotifyTemplateMapper notifyTemplateMapper;
    @Autowired
    private NotifyMessageMapper notifyMessageMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterService clusterService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterNodeService clusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    private HostFacade hostFacade;

    @Transactional
    public void alerts(List<AlertApiReq> alertApiReqList) {
        for (AlertApiReq alertApiReq : alertApiReqList) {
            AlertLabels labels = alertApiReq.getLabels();
            Long templateRuleId = labels.getTemplateRuleId();
            AlertTemplateRule alertTemplateRule = templateRuleMapper.selectById(templateRuleId);
            if (alertTemplateRule == null) {
                return;
            }
            String notifyWayIds = alertTemplateRule.getNotifyWayIds();
            String notifyWayNames = "";
            if (StrUtil.isNotBlank(notifyWayIds)) {
                List<NotifyWay> notifyWays = notifyWayMapper.selectBatchIds(
                    Arrays.asList(notifyWayIds.split(CommonConstants.DELIMITER)));
                if (CollectionUtil.isNotEmpty(notifyWays)) {
                    notifyWayNames = notifyWays.stream().map(item -> item.getName()).collect(
                        Collectors.joining(CommonConstants.DELIMITER));
                }
            }
            String clusterNodeId = labels.getInstance();
            AlertContentParamDto contentParamDto = setAndGetAlertContentParamDto(clusterNodeId,
                alertApiReq.getStartsAt(), labels.getLevel());

            AlertRecord alertRecord = saveAndGetRecord(alertApiReq, notifyWayNames, alertTemplateRule, contentParamDto);
            contentParamDto.setContent(alertRecord.getAlertContent());

            Integer isRepeat = alertTemplateRule.getIsRepeat();
            if (isRepeat == CommonConstants.IS_NOT_REPEAT) {
                Long count = notifyMessageMapper.selectCount(
                    Wrappers.<NotifyMessage>lambdaQuery().eq(NotifyMessage::getRecordId, alertRecord.getId()));
                if (count > 0) {
                    continue;
                }
            }
            Integer isSilence = alertTemplateRule.getIsSilence(); // 是否静默
            LocalDateTime now = LocalDateTime.now();
            if (isSilence == CommonConstants.IS_SILENCE && now.isAfter(alertTemplateRule.getSilenceStartTime())
                && now.isBefore(alertTemplateRule.getSilenceEndTime())) {
                continue;
            }
            saveNotifyMessage(notifyWayIds, contentParamDto, alertRecord.getId());
        }
    }

    private AlertRecord saveAndGetRecord(
        AlertApiReq alertApiReq, String notifyWayNames, AlertTemplateRule alertTemplateRule,
        AlertContentParamDto contentParamDto) {
        String clusterNodeId = alertApiReq.getLabels().getInstance();
        Long templateId = alertApiReq.getLabels().getTemplateId();
        List<AlertRecord> alertRecords = alertRecordMapper.selectList(Wrappers.<AlertRecord>lambdaQuery()
            .eq(AlertRecord::getClusterNodeId, clusterNodeId).eq(AlertRecord::getTemplateId, templateId)
            .eq(AlertRecord::getTemplateRuleId, alertTemplateRule.getId()).eq(AlertRecord::getStartTime,
                alertApiReq.getStartsAt())
            .orderByDesc(AlertRecord::getUpdateTime));
        AlertRecord alertRecord = null;
        LocalDateTime endsAt = alertApiReq.getEndsAt();
        LocalDateTime now = LocalDateTime.now();
        endsAt = endsAt.isAfter(now) ? now : endsAt;
        if (CollectionUtils.isEmpty(alertRecords)) {
            alertRecord = new AlertRecord();
            AlertTemplate alertTemplate = templateMapper.selectById(templateId);
            alertRecord.setClusterNodeId(clusterNodeId).setTemplateId(templateId).setTemplateRuleId(
                alertTemplateRule.getId()).setStartTime(alertApiReq.getStartsAt()).setEndTime(endsAt)
                .setCreateTime(LocalDateTime.now());
            alertRecord.setDuration(
                Duration.between(alertRecord.getStartTime(), alertRecord.getEndTime()).toSeconds());
            alertRecord.setTemplateName(alertTemplate.getTemplateName()).setTemplateRuleName(
                    alertTemplateRule.getRuleName())
                .setTemplateRuleType(alertTemplateRule.getRuleType()).setLevel(alertTemplateRule.getLevel())
                .setNotifyWayIds(alertTemplateRule.getNotifyWayIds()).setNotifyWayNames(notifyWayNames);
            String ruleContent = alertTemplateRule.getRuleContent();
            TextParser textParser = new TextParser();
            alertRecord.setAlertContent(textParser.parse(ruleContent, contentParamDto));
            alertRecordMapper.insert(alertRecord);
            log.info("insert new alert record");
        } else {
            alertRecord = alertRecords.get(0);
            if (alertRecord.getAlertStatus() == CommonConstants.RECOVER_STATUS) {
                return alertRecord;
            }
            alertRecord.setNotifyWayIds(alertTemplateRule.getNotifyWayIds()).setNotifyWayNames(notifyWayNames);
            alertRecord.setAlertStatus(alertRecord.getEndTime().equals(endsAt)
                || alertRecord.getEndTime().isAfter(endsAt)
                ? CommonConstants.RECOVER_STATUS : CommonConstants.FIRING_STATUS);
            alertRecord.setEndTime(endsAt);
            alertRecord.setDuration(
                Duration.between(alertRecord.getStartTime(), alertRecord.getEndTime()).toSeconds());
            alertRecord.setUpdateTime(LocalDateTime.now());
            alertRecordMapper.updateById(alertRecord);
            log.info("update alert record");
        }
        return alertRecord;
    }

    private AlertContentParamDto setAndGetAlertContentParamDto(
        String clusterNodeId, LocalDateTime alertTime, String level) {
        OpsClusterNodeEntity opsClusterNodeEntity = clusterNodeService.getById(clusterNodeId);
        if (opsClusterNodeEntity == null) {
            throw new ServiceException("cluster node is not found");
        }
        OpsHostEntity opsHost = hostFacade.getById(opsClusterNodeEntity.getHostId());
        if (opsHost == null) {
            throw new ServiceException("host is not found");
        }
        OpsClusterEntity opsClusterEntity = clusterService.getById(opsClusterNodeEntity.getClusterId());
        if (opsClusterEntity == null) {
            throw new ServiceException("cluster is not found");
        }
        AlertContentParamDto contentParamDto = new AlertContentParamDto();
        String nodeName =
            opsClusterEntity.getClusterId() + "/" + opsHost.getPublicIp() + ":" + opsClusterEntity.getPort()
                + "(" + opsClusterNodeEntity.getClusterRole() + ")";
        contentParamDto.setHostname(opsHost.getHostname()).setNodeName(nodeName).setPort(
            opsClusterEntity.getPort() != null ? opsHost.getPort().toString() : "").setHostIp(
            opsHost.getPublicIp()).setAlertTime(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(alertTime)).setLevel(
            level);
        return contentParamDto;
    }

    private void saveNotifyMessage(String notifyWayIds, AlertContentParamDto contentParamDto, Long alertRecordId) {
        if (StrUtil.isBlank(notifyWayIds)) {
            return;
        }
        String[] notifyWayIdArr = notifyWayIds.split(CommonConstants.DELIMITER);
        for (String notifyWayId : notifyWayIdArr) {
            NotifyMessage notifyMessage = new NotifyMessage();
            NotifyWay notifyWay = notifyWayMapper.selectById(notifyWayId);
            Long notifyTemplateId = notifyWay.getNotifyTemplateId();
            NotifyTemplate notifyTemplate = notifyTemplateMapper.selectById(notifyTemplateId);
            notifyMessage.setMessageType(notifyWay.getNotifyType());
            notifyMessage.setTitle(notifyTemplate.getNotifyTitle());
            String content = new TextParser().parse(notifyTemplate.getNotifyContent(), contentParamDto);
            notifyMessage.setContent(content);
            notifyMessage.setEmail(notifyWay.getEmail());
            notifyMessage.setPersonId(notifyWay.getPersonId());
            notifyMessage.setDeptId(notifyWay.getDeptId());
            notifyMessage.setCreateTime(LocalDateTime.now());
            notifyMessage.setRecordId(alertRecordId);
            notifyMessageMapper.insert(notifyMessage);
        }
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertContentParamDto;
import com.nctigba.alert.monitor.dto.NotifySnmpDto;
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
import com.nctigba.alert.monitor.utils.AlertContentParamUtil;
import com.nctigba.alert.monitor.utils.TextParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    private AlertContentParamUtil contentParamUtil;
    @Autowired
    private AlertRecordService recordService;

    @Transactional
    public void alerts(List<AlertApiReq> alertApiReqList) {
        for (AlertApiReq alertApiReq : alertApiReqList) {
            AlertLabels labels = alertApiReq.getLabels();
            Long templateRuleId = labels.getTemplateRuleId();
            AlertTemplateRule alertTemplateRule = templateRuleMapper.selectById(templateRuleId);
            if (alertTemplateRule == null) {
                continue;
            }
            String notifyWayIds = alertTemplateRule.getNotifyWayIds();
            if (StrUtil.isBlank(notifyWayIds)) {
                continue;
            }
            String clusterNodeId = labels.getInstance();
            AlertContentParamDto contentParamDto = contentParamUtil.setAndGetAlertContentParamDto(clusterNodeId,
                alertApiReq.getStartsAt(), labels.getLevel(), alertTemplateRule.getRuleContent());
            List<NotifyWay> notifyWays = notifyWayMapper.selectBatchIds(
                Arrays.asList(notifyWayIds.split(CommonConstants.DELIMITER)));

            Optional<AlertRecord> optional = record(alertApiReq, alertTemplateRule, contentParamDto, notifyWays);
            if (optional.isEmpty()) {
                continue;
            }
            notify(alertTemplateRule, optional.get(), notifyWays, contentParamDto);
        }
    }

    private Optional<AlertRecord> record(
        AlertApiReq alertApiReq, AlertTemplateRule alertTemplateRule,
        AlertContentParamDto contentParamDto, List<NotifyWay> notifyWays) {
        String clusterNodeId = alertApiReq.getLabels().getInstance();
        Long templateId = alertApiReq.getLabels().getTemplateId();
        List<AlertRecord> alertRecords = alertRecordMapper.selectList(Wrappers.<AlertRecord>lambdaQuery()
            .eq(AlertRecord::getClusterNodeId, clusterNodeId).eq(AlertRecord::getTemplateId, templateId)
            .eq(AlertRecord::getTemplateRuleId, alertTemplateRule.getId()).eq(AlertRecord::getStartTime,
                alertApiReq.getStartsAt()).orderByDesc(AlertRecord::getUpdateTime));
        LocalDateTime endsAt = alertApiReq.getEndsAt();
        LocalDateTime now = LocalDateTime.now();
        endsAt = endsAt.isAfter(now) ? now : endsAt;
        AlertRecord alertRecord = null;
        if (CollectionUtil.isEmpty(alertRecords)) {
            alertRecord = new AlertRecord();
            AlertTemplate alertTemplate = templateMapper.selectById(templateId);
            alertRecord.setClusterNodeId(clusterNodeId).setTemplateId(templateId).setTemplateRuleId(
                    alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
                .setTemplateName(alertTemplate.getTemplateName()).setTemplateRuleName(
                    alertTemplateRule.getRuleName()).setAlertStatus(CommonConstants.FIRING_STATUS)
                .setTemplateRuleType(alertTemplateRule.getRuleType()).setLevel(alertTemplateRule.getLevel())
                .setAlertContent(contentParamDto.getContent()).setRecordStatus(CommonConstants.UNREAD_STATUS);
        } else {
            alertRecord = alertRecords.get(0);
            if (alertRecord.getAlertStatus() == CommonConstants.RECOVER_STATUS) {
                return Optional.empty();
            }
            alertRecord.setAlertStatus(alertRecord.getEndTime().equals(endsAt)
                || alertRecord.getEndTime().isAfter(endsAt)
                ? CommonConstants.RECOVER_STATUS : CommonConstants.FIRING_STATUS).setUpdateTime(LocalDateTime.now());
        }
        String notifyWayNames = notifyWays.stream().map(item -> item.getName()).collect(
            Collectors.joining(CommonConstants.DELIMITER));
        alertRecord.setNotifyWayIds(alertTemplateRule.getNotifyWayIds()).setNotifyWayNames(notifyWayNames)
            .setEndTime(endsAt).setStartTime(alertApiReq.getStartsAt()).setDuration(
                Duration.between(alertRecord.getStartTime(), alertRecord.getEndTime()).toSeconds());
        recordService.saveOrUpdate(alertRecord);
        return Optional.of(alertRecord);
    }

    private void notify(
        AlertTemplateRule alertTemplateRule, AlertRecord alertRecord,
        List<NotifyWay> notifyWays, AlertContentParamDto contentParamDto) {
        if (!shouldNotify(alertTemplateRule, alertRecord)) {
            return;
        }
        for (NotifyWay notifyWay : notifyWays) {
            NotifyMessage notifyMessage = new NotifyMessage();
            Long notifyTemplateId = notifyWay.getNotifyTemplateId();
            NotifyTemplate notifyTemplate = notifyTemplateMapper.selectById(notifyTemplateId);
            String notifyType = notifyWay.getNotifyType();
            notifyMessage.setMessageType(notifyType);
            notifyMessage.setTitle(notifyTemplate.getNotifyTitle());
            String content = new TextParser().parse(notifyTemplate.getNotifyContent(), contentParamDto);
            notifyMessage.setContent(content);
            notifyMessage.setEmail(notifyWay.getEmail());
            if (notifyType.equals(CommonConstants.EMAIL)) {
                notifyMessage.setEmail(notifyWay.getEmail());
            } else if (notifyType.equals(CommonConstants.WE_COM) || notifyType.equals(CommonConstants.DING_TALK)) {
                Integer sendWay = notifyWay.getSendWay();
                if (sendWay != null && sendWay == 1) {
                    notifyMessage.setWebhook(notifyWay.getWebhook());
                    notifyMessage.setSign(notifyType.equals(CommonConstants.DING_TALK) ? notifyWay.getSign() : "");
                } else {
                    notifyMessage.setPersonId(notifyWay.getPersonId());
                    notifyMessage.setDeptId(notifyWay.getDeptId());
                }
            } else if (notifyType.equals(CommonConstants.WEBHOOK)) {
                notifyMessage.setWebhook(notifyWay.getWebhook());
                notifyMessage.setWebhookInfo(getWebhookInfo(notifyWay));
            } else {
                NotifySnmpDto notifySnmpDto = new NotifySnmpDto();
                BeanUtil.copyProperties(notifyWay, notifySnmpDto);
                JSONObject snmpJson = JSONUtil.parseObj(notifySnmpDto);
                notifyMessage.setSnmpInfo(snmpJson.toString());
            }
            notifyMessage.setCreateTime(LocalDateTime.now());
            notifyMessage.setRecordId(alertRecord.getId());
            notifyMessageMapper.insert(notifyMessage);
        }
        Integer sendCount = alertRecord.getSendCount() != null ? alertRecord.getSendCount() : 0;
        alertRecord.setSendCount(sendCount + 1).setSendTime(LocalDateTime.now());
        recordService.updateById(alertRecord);
    }

    private String getWebhookInfo(NotifyWay notifyWay) {
        JSONObject webhookInfo = new JSONObject();
        String header = notifyWay.getHeader();
        if (StrUtil.isNotBlank(header)) {
            webhookInfo.put("header", new JSONObject(header));
        }
        String params = notifyWay.getParams();
        if (StrUtil.isNotBlank(params)) {
            webhookInfo.put("params", new JSONObject(params));
        }
        String body = notifyWay.getBody();
        if (StrUtil.isNotBlank(body)) {
            webhookInfo.put("body", body);
        }
        String resultCode = notifyWay.getResultCode();
        if (StrUtil.isNotBlank(resultCode)) {
            webhookInfo.put("resultCode", new JSONObject(resultCode));
        }
        return webhookInfo.toString();
    }

    private boolean shouldNotify(AlertTemplateRule alertTemplateRule, AlertRecord alertRecord) {
        if (alertRecord.getRecordStatus().equals(CommonConstants.READ_STATUS)) {
            return false;
        }
        if (StrUtil.isBlank(alertTemplateRule.getAlertNotify())) {
            return false;
        }
        List<String> alertNotifyList = Arrays.asList(alertTemplateRule.getAlertNotify()
            .split(CommonConstants.DELIMITER));
        String alertStatus = alertRecord.getAlertStatus().equals(CommonConstants.FIRING_STATUS)
            ? "firing" : "recover";
        if (!alertNotifyList.contains(alertStatus)) {
            return false;
        }
        Integer isRepeat = alertTemplateRule.getIsRepeat();
        if (isRepeat == CommonConstants.IS_NOT_REPEAT) {
            Long count = notifyMessageMapper.selectCount(
                Wrappers.<NotifyMessage>lambdaQuery().eq(NotifyMessage::getRecordId, alertRecord.getId()));
            if (count > 0) {
                return false;
            }
        }
        Integer maxRepeatCount = alertTemplateRule.getMaxRepeatCount();
        Integer sendCount = alertRecord.getSendCount() != null ? alertRecord.getSendCount() : 0;
        if (maxRepeatCount != null && maxRepeatCount <= sendCount) {
            return false;
        }
        Integer nextRepeat = alertTemplateRule.getNextRepeat();
        String nextRepeatUnit = alertTemplateRule.getNextRepeatUnit();
        LocalDateTime sendTime = alertRecord.getSendTime();
        if (sendTime != null && nextRepeat != null && StrUtil.isNotBlank(nextRepeatUnit)) {
            LocalDateTime nextTime = nextRepeatUnit.equals(CommonConstants.SECOND) ? sendTime.plusSeconds(nextRepeat)
                : nextRepeatUnit.equals(CommonConstants.MINUTE) ? sendTime.plusMinutes(nextRepeat)
                : nextRepeatUnit.equals(CommonConstants.HOUR) ? sendTime.plusHours(nextRepeat)
                : sendTime.plusHours(nextRepeat);
            if (nextTime.isAfter(LocalDateTime.now())) {
                return false;
            }
        }
        Integer isSilence = alertTemplateRule.getIsSilence();
        LocalDateTime now = LocalDateTime.now();
        if (isSilence == CommonConstants.IS_SILENCE && now.isAfter(alertTemplateRule.getSilenceStartTime())
            && now.isBefore(alertTemplateRule.getSilenceEndTime())) {
            return false;
        }
        return true;
    }
}

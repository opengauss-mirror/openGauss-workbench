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
 *  NotifyListener.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/listener/NotifyListener.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.event.NotifyEvent;
import com.nctigba.alert.monitor.model.dto.AlertRecordDTO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import com.nctigba.alert.monitor.util.TextParserUtils;
import com.nctigba.alert.monitor.model.dto.NotifySnmpDTO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDetailDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.NotifyMessageDO;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
import com.nctigba.alert.monitor.service.AlertRecordDetailService;
import com.nctigba.alert.monitor.service.AlertRecordService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.CommunicationService;
import com.nctigba.alert.monitor.service.NotifyTemplateService;
import com.nctigba.alert.monitor.service.NotifyWayService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NotifyListener
 *
 * @since 2023/11/24 09:44
 */
@Component
@Slf4j
public class NotifyListener implements ApplicationListener<NotifyEvent> {
    private static volatile Integer status = 0;

    @Autowired
    private AlertRecordDetailService recordDetailService;
    @Autowired
    private AlertRecordService recordService;
    @Autowired
    private AlertTemplateRuleService templateRuleService;
    @Autowired
    private NotifyWayService notifyWayService;
    @Autowired
    private NotifyTemplateService notifyTemplateService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterService clusterService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterNodeService clusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    private HostFacade hostFacade;
    @Autowired
    private List<CommunicationService> communicationServices;

    @Override
    @Async
    public void onApplicationEvent(NotifyEvent event) {
        synchronized (this) {
            if (status.equals(1)) {
                return;
            }
            status = 1;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                status = 0;
                throw new ServiceException(e.getMessage());
            }
        }
        try {
            while (recordDetailService.unnotifyCount() > 0) {
                alertHandle();
            }
        } finally {
            synchronized (this) {
                status = 0;
            }
        }
    }
    private boolean filterByRule(AlertRecordDetailDO recordDetail, List<AlertTemplateRuleDO> templateRules) {
        AlertRecordDTO recordDto = recordService.getById(recordDetail.getRecordId());
        if (!recordDto.getAlertStatus().equals(recordDetail.getAlertStatus())) {
            return false;
        }
        AlertTemplateRuleDO templateRule = templateRules.stream()
            .filter(item -> item.getId().equals(recordDetail.getTemplateRuleId())).findFirst().get();
        List<String> alertNotifyList = Arrays.asList(templateRule.getAlertNotify().split(CommonConstants.DELIMITER));
        String alertStatus = recordDto.getAlertStatus().equals(CommonConstants.FIRING_STATUS) ? "firing" : "recover";
        if (!alertNotifyList.contains(alertStatus)) {
            return false;
        }
        if (recordDto.getRecordStatus().equals(CommonConstants.READ_STATUS)) {
            return false;
        }
        Integer isRepeat = templateRule.getIsRepeat();
        if (isRepeat != null && isRepeat.equals(CommonConstants.IS_NOT_REPEAT) && recordDto.getSendCount() != null
            && recordDto.getSendCount() > 0) {
            return false;
        }
        Integer maxRepeatCount = templateRule.getMaxRepeatCount();
        Integer sendCount = recordDto.getSendCount() != null ? recordDto.getSendCount() : 0;
        if (isRepeat != null && isRepeat.equals(CommonConstants.IS_REPEAT) && maxRepeatCount != null
            && maxRepeatCount.compareTo(sendCount) <= 0) {
            return false;
        }
        Integer nextRepeat = templateRule.getNextRepeat();
        String nextRepeatUnit = templateRule.getNextRepeatUnit();
        LocalDateTime sendTime = recordDto.getSendTime();
        if (isRepeat != null && isRepeat.equals(CommonConstants.IS_REPEAT)
            && sendTime != null && nextRepeat != null && StrUtil.isNotBlank(nextRepeatUnit)) {
            LocalDateTime nextTime = nextRepeatUnit.equals(CommonConstants.SECOND) ? sendTime.plusSeconds(nextRepeat)
                : nextRepeatUnit.equals(CommonConstants.MINUTE) ? sendTime.plusMinutes(nextRepeat)
                : nextRepeatUnit.equals(CommonConstants.HOUR) ? sendTime.plusHours(nextRepeat)
                : sendTime.plusHours(nextRepeat);
            if (nextTime.withNano(0).isAfter(LocalDateTime.now().withNano(0))) {
                return false;
            }
        }
        Integer isSilence = templateRule.getIsSilence();
        LocalDateTime now = LocalDateTime.now();
        if (isSilence.equals(CommonConstants.IS_SILENCE) && now.isAfter(templateRule.getSilenceStartTime())
            && now.isBefore(templateRule.getSilenceEndTime())) {
            return false;
        }
        return true;
    }
    private List<NotifyMessageDO> generateNotifyMessageList(Map<String, List<AlertRecordDetailDO>> detailsGroups) {
        Map<String, Map<String, String>> alertParamsMapGroup = new HashMap<>();
        return detailsGroups.entrySet().stream().map(listEntry -> {
            String[] keyArr = listEntry.getKey().split(",");
            Long notifyId = Long.valueOf(keyArr[1]);
            NotifyWayDO notifyWayDO = notifyWayService.getById(notifyId);
            NotifyTemplateDO notifyTemplateDO = notifyTemplateService.getById(notifyWayDO.getNotifyTemplateId());

            NotifyMessageDO notifyMessageDO = new NotifyMessageDO();
            String notifyType = notifyWayDO.getNotifyType();
            notifyMessageDO.setMessageType(notifyType);
            notifyMessageDO.setTitle(notifyTemplateDO.getNotifyTitle());
            String content = listEntry.getValue().stream().map(detail -> {
                Map<String, String> alertParamsMap = alertParamsMapGroup.computeIfAbsent(detail.getClusterNodeId(),
                    k -> generateAlertParams(detail));
                return TextParserUtils.parse(notifyTemplateDO.getNotifyContent(), alertParamsMap);
            }).collect(Collectors.joining(CommonConstants.LINE_SEPARATOR));
            notifyMessageDO.setContent(content);
            notifyMessageDO.setEmail(notifyWayDO.getEmail());
            if (notifyType.equals(CommonConstants.EMAIL)) {
                notifyMessageDO.setEmail(notifyWayDO.getEmail());
            } else if (notifyType.equals(CommonConstants.WE_COM) || notifyType.equals(CommonConstants.DING_TALK)) {
                Integer sendWay = notifyWayDO.getSendWay();
                if (sendWay != null && sendWay == 1) {
                    notifyMessageDO.setWebhook(notifyWayDO.getWebhook());
                    notifyMessageDO.setSign(notifyType.equals(CommonConstants.DING_TALK) ? notifyWayDO.getSign() : "");
                } else {
                    notifyMessageDO.setPersonId(notifyWayDO.getPersonId());
                    notifyMessageDO.setDeptId(notifyWayDO.getDeptId());
                }
            } else if (notifyType.equals(CommonConstants.WEBHOOK)) {
                notifyMessageDO.setWebhook(notifyWayDO.getWebhook());
                notifyMessageDO.setWebhookInfo(getWebhookInfo(notifyWayDO));
            } else {
                NotifySnmpDTO notifySnmpDto = new NotifySnmpDTO();
                BeanUtil.copyProperties(notifyWayDO, notifySnmpDto);
                JSONObject snmpJson = JSONUtil.parseObj(notifySnmpDto);
                notifyMessageDO.setSnmpInfo(snmpJson.toString());
            }
            notifyMessageDO.setCreateTime(LocalDateTime.now());
            String recordIds = listEntry.getValue().stream().map(item -> item.getRecordId().toString())
                .collect(Collectors.joining(CommonConstants.DELIMITER));
            notifyMessageDO.setRecordIds(recordIds);
            return notifyMessageDO;
        }).collect(Collectors.toList());
    }

    private String getWebhookInfo(NotifyWayDO notifyWayDO) {
        JSONObject webhookInfo = new JSONObject();
        String header = notifyWayDO.getHeader();
        if (StrUtil.isNotBlank(header)) {
            webhookInfo.put("header", new JSONObject(header));
        }
        String params = notifyWayDO.getParams();
        if (StrUtil.isNotBlank(params)) {
            webhookInfo.put("params", new JSONObject(params));
        }
        String body = notifyWayDO.getBody();
        if (StrUtil.isNotBlank(body)) {
            webhookInfo.put("body", body);
        }
        String resultCode = notifyWayDO.getResultCode();
        if (StrUtil.isNotBlank(resultCode)) {
            webhookInfo.put("resultCode", new JSONObject(resultCode));
        }
        return webhookInfo.toString();
    }

    private Map<String, String> generateAlertParams(AlertRecordDetailDO detail) {
        Map<String, String> alertParams = new HashMap<>();
        LocalDateTime alertTime = detail.getStartTime();
        alertParams.put("content", detail.getAlertContent());
        alertParams.put("level", detail.getLevel());
        alertParams.put("alertTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(alertTime));
        if (detail.getAlertStatus().equals(CommonConstants.FIRING_STATUS)) {
            alertParams.put("alertStatus", MessageSourceUtils.get("alerting", Locale.getDefault()));
        } else {
            alertParams.put("alertStatus", MessageSourceUtils.get("alerted", Locale.getDefault()));
        }
        String clusterNodeId = detail.getClusterNodeId();
        alertParams.put("clusterNodeId", clusterNodeId);
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
        String nodeName =
            opsClusterEntity.getClusterId() + "/" + opsHost.getPublicIp() + ":" + opsClusterEntity.getPort()
                + "(" + opsClusterNodeEntity.getClusterRole() + ")";
        alertParams.put("nodeName", nodeName);
        alertParams.put("hostname", opsHost.getHostname());
        alertParams.put("port", opsClusterEntity.getPort() != null ? opsClusterEntity.getPort().toString() : "");
        alertParams.put("hostIp", opsHost.getPublicIp());
        alertParams.put("cluster",
            StrUtil.isNotBlank(opsClusterEntity.getClusterName()) ? opsClusterEntity.getClusterName()
                : opsClusterEntity.getClusterId());
        alertParams.put("clusterId", opsClusterEntity.getClusterId());
        return alertParams;
    }

    @Transactional
    private void alertHandle() {
        List<AlertRecordDetailDO> recordDetails = recordDetailService.unnotifyList();
        // remove the repeat
        Map<String, List<AlertRecordDetailDO>> listMap = recordDetails.stream().collect(
            Collectors.groupingBy(item -> item.getClusterNodeId() + item.getTemplateRuleId()));
        List<AlertRecordDetailDO> notifyRecordDetails = new ArrayList<>();
        List<AlertRecordDetailDO> ignoreRecordDetails = new ArrayList<>();
        for (List<AlertRecordDetailDO> list : listMap.values()) {
            notifyRecordDetails.add(list.get(0));
            ignoreRecordDetails.addAll(list.subList(1, list.size()));
        }
        Set<Long> templateRuleIds = notifyRecordDetails.stream().map(item -> item.getTemplateRuleId()).collect(
            Collectors.toSet());
        List<AlertTemplateRuleDO> templateRules = templateRuleService.listByIds(templateRuleIds);
        // filterByRule
        notifyRecordDetails = notifyRecordDetails.stream()
            .filter(item -> {
                boolean isFilter = filterByRule(item, templateRules);
                if (!isFilter) {
                    ignoreRecordDetails.add(item);
                }
                return isFilter;
            }).collect(Collectors.toList());

        if (CollectionUtil.isNotEmpty(notifyRecordDetails)) {
            notifyHandle(notifyRecordDetails);
        }

        notifyRecordDetails.forEach(item -> item.setNotifyStatus(CommonConstants.SEND));
        recordDetailService.saveOrUpdateBatch(notifyRecordDetails);
        ignoreRecordDetails.forEach(item -> item.setNotifyStatus(CommonConstants.SEND_IGNORE));
        recordDetailService.saveOrUpdateBatch(ignoreRecordDetails);
    }

    @Transactional
    private void notifyHandle(List<AlertRecordDetailDO> notifyRecordDetails) {
        // group by notifyWay
        Map<String, List<AlertRecordDetailDO>> notifyRecordDetailsGroups = new HashMap<>();
        for (AlertRecordDetailDO detail : notifyRecordDetails) {
            String[] notifyWayIdArr = detail.getNotifyWayIds().split(",");
            Arrays.stream(notifyWayIdArr).forEach(notifyWayId -> {
                notifyRecordDetailsGroups.computeIfAbsent(detail.getAlertStatus() + "," + notifyWayId,
                    k -> new ArrayList<>()).add(detail);
            });
        }
        List<NotifyMessageDO> notifyMessageDOS = generateNotifyMessageList(notifyRecordDetailsGroups);
        for (CommunicationService communicationService : communicationServices) {
            communicationService.send(notifyMessageDOS);
        }
        LocalDateTime now = LocalDateTime.now();
        List<Long> recordIds = notifyRecordDetails
            .stream().map(item -> item.getRecordId()).collect(Collectors.toList());
        List<AlertRecordDO> records = recordService.listByIds(recordIds).stream().map(item -> {
            Integer sendCount = item.getSendCount() != null ? item.getSendCount() : 0;
            item.setSendCount(sendCount + 1).setSendTime(now);
            return item;
        }).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(records)) {
            recordService.updateBatchById(records);
        }
    }
}
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
 *  AlertApiServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertApiServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.event.NotifyEvent;
import com.nctigba.alert.monitor.model.entity.AlertRecordDO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDetailDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.mapper.AlertRecordMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.model.query.api.AlertApiReq;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import com.nctigba.alert.monitor.util.TextParserUtils;
import com.nctigba.alert.monitor.service.AlertApiService;
import com.nctigba.alert.monitor.service.AlertRecordDetailService;
import com.nctigba.alert.monitor.service.AlertRecordService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.forEach;

/**
 * AlertApiServiceImpl
 *
 * @author wuyuebin
 * @since 2023/5/2 10:05
 */
@Service
@Slf4j
public class AlertApiServiceImpl implements AlertApiService {
    private static final Integer LIMIT = 1000;

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
    private AlertRecordService recordService;
    @Autowired
    private AlertRecordDetailService recordDetailService;
    @Autowired
    private ApplicationContext context;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterService clusterService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterNodeService clusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.MAIN_PLUGIN)
    private HostFacade hostFacade;


    /**
     * prometheus msg
     *
     * @param alertApiReqList List<AlertApiReq>
     */
    public void alerts(List<AlertApiReq> alertApiReqList) {
        List<AlertRecordDO> recordList = new ArrayList<>();
        for (AlertApiReq alertApiReq : alertApiReqList) {
            try {
                Map<String, String> labels = alertApiReq.getLabels();
                Long templateRuleId = Long.valueOf(labels.get("templateRuleId"));
                AlertTemplateRuleDO alertTemplateRuleDO = templateRuleMapper.selectById(templateRuleId);
                if (alertTemplateRuleDO == null || alertTemplateRuleDO.getId() == null) {
                    continue;
                }
                if (StrUtil.isBlank(alertTemplateRuleDO.getNotifyWayIds())) {
                    continue;
                }
                Optional<AlertRecordDO> optional = generateRecord(alertApiReq, alertTemplateRuleDO);
                if (optional.isEmpty()) {
                    continue;
                }
                recordList.add(optional.get());
                if (recordList.size() >= LIMIT) {
                    saveRecordList(recordList);
                    recordList.clear();
                }
            } catch (NumberFormatException | NullPointerException | ServiceException e) {
                log.error("alertApiReq is ", alertApiReq);
                log.error("exception: ", e);
            }
        }
        if (CollectionUtil.isNotEmpty(recordList)) {
            saveRecordList(recordList);
        }
        context.publishEvent(new NotifyEvent(this));
    }

    private Optional<AlertRecordDO> generateRecord(AlertApiReq alertApiReq, AlertTemplateRuleDO alertTemplateRuleDO) {
        String clusterNodeId = alertApiReq.getLabels().get("instance");
        Long templateId = Long.valueOf(alertApiReq.getLabels().get("templateId"));
        LocalDateTime startsAt = alertApiReq.getStartsAt();
        List<AlertRecordDO> alertRecordDOS =
            recordService.getList(clusterNodeId, templateId, alertTemplateRuleDO.getId(), startsAt);
        AlertRecordDO alertRecordDO = null;
        if (CollectionUtil.isEmpty(alertRecordDOS)) {
            alertRecordDO = new AlertRecordDO();
            AlertTemplateDO alertTemplateDO = templateMapper.selectById(templateId);
            alertRecordDO.setClusterNodeId(clusterNodeId).setTemplateId(templateId).setTemplateRuleId(
                    alertTemplateRuleDO.getId()).setCreateTime(LocalDateTime.now())
                .setTemplateName(alertTemplateDO.getTemplateName()).setTemplateRuleName(
                    alertTemplateRuleDO.getRuleName()).setAlertStatus(alertApiReq.getAlertStatus())
                .setTemplateRuleType(alertTemplateRuleDO.getRuleType()).setLevel(alertTemplateRuleDO.getLevel())
                .setRecordStatus(CommonConstants.UNREAD_STATUS);
        } else {
            alertRecordDO = alertRecordDOS.get(0);
            if (alertRecordDO.getAlertStatus() == CommonConstants.RECOVER_STATUS) {
                return Optional.empty();
            }
            alertRecordDO.setAlertStatus(alertApiReq.getAlertStatus()).setUpdateTime(LocalDateTime.now());
        }
        List<NotifyWayDO> notifyWayDOS = notifyWayMapper.selectBatchIds(
            Arrays.asList(alertTemplateRuleDO.getNotifyWayIds().split(CommonConstants.DELIMITER)));
        String notifyWayNames = notifyWayDOS.stream().map(item -> item.getName()).collect(
            Collectors.joining(CommonConstants.DELIMITER));
        Map<String, String> alertParams = generateAlertParams(alertApiReq);
        alertRecordDO.setNotifyWayIds(alertTemplateRuleDO.getNotifyWayIds()).setNotifyWayNames(notifyWayNames)
            .setEndTime(alertApiReq.getEndsAt()).setStartTime(alertApiReq.getStartsAt()).setDuration(
                Duration.between(alertRecordDO.getStartTime(), alertRecordDO.getEndTime()).toSeconds())
            .setClusterId(alertParams.get("clusterId"))
            .setAlertContent(TextParserUtils.parse(alertTemplateRuleDO.getRuleContent(), alertParams));
        AlertRecordDetailDO detail = new AlertRecordDetailDO();
        detail.setClusterId(alertRecordDO.getClusterId()).setRecordId(alertRecordDO.getId())
            .setLevel(alertRecordDO.getLevel()).setNotifyWayIds(alertRecordDO.getNotifyWayIds())
            .setNotifyWayNames(alertRecordDO.getNotifyWayNames()).setClusterNodeId(alertRecordDO.getClusterNodeId())
            .setAlertContent(alertRecordDO.getAlertContent()).setAlertStatus(alertRecordDO.getAlertStatus())
            .setTemplateId(alertRecordDO.getTemplateId()).setTemplateName(alertRecordDO.getTemplateName())
            .setTemplateRuleId(alertRecordDO.getTemplateRuleId())
            .setTemplateRuleName(alertRecordDO.getTemplateRuleName()).setAlertContent(alertRecordDO.getAlertContent())
            .setTemplateRuleType(alertRecordDO.getTemplateRuleType())
            .setStartTime(alertRecordDO.getStartTime()).setEndTime(alertRecordDO.getEndTime())
            .setNotifyStatus(CommonConstants.UNSEND).setCreateTime(LocalDateTime.now());
        List<AlertRecordDetailDO> details = new ArrayList<>();
        details.add(detail);
        alertRecordDO.setRecordDetailList(details);
        return Optional.of(alertRecordDO);
    }

    private Map<String, String> generateAlertParams(AlertApiReq alertApiReq) {
        Map<String, String> labels = alertApiReq.getLabels();
        Map<String, String> alertParams = new HashMap<>();
        alertParams.putAll(labels);
        Map annotations = alertApiReq.getAnnotations();
        if (CollectionUtil.isNotEmpty(annotations) && annotations.get("value") != null) {
            BigDecimal val = new BigDecimal(annotations.get("value").toString())
                .setScale(2, BigDecimal.ROUND_HALF_UP);
            alertParams.put("value", val.toString());
        }
        LocalDateTime alertTime = alertApiReq.getStartsAt();
        alertParams.put("alertTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(alertTime));
        if (alertApiReq.getAlertStatus().equals(CommonConstants.FIRING_STATUS)) {
            alertParams.put("alertStatus", MessageSourceUtils.get("alerting"));
        } else {
            alertParams.put("alertStatus", MessageSourceUtils.get("alerted"));
        }
        String clusterNodeId = labels.get("instance");
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
    private void saveRecordList(List<AlertRecordDO> recordList) {
        recordService.saveOrUpdateBatch(recordList);
        List<AlertRecordDetailDO> recordDetails = recordList.stream().flatMap(item -> {
            List<AlertRecordDetailDO> recordDetailList = item.getRecordDetailList();
            recordDetailList.forEach(detail -> {
                detail.setRecordId(item.getId());
            });
            return recordDetailList.stream();
        }).collect(Collectors.toList());
        recordDetailService.saveOrUpdateBatch(recordDetails);
    }
}
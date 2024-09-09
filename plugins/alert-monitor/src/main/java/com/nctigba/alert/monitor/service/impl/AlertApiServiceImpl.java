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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.event.NotifyEvent;
import com.nctigba.alert.monitor.mapper.AlertShieldingMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDetailDO;
import com.nctigba.alert.monitor.model.entity.AlertShieldingDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.model.query.api.AlertApiReq;
import com.nctigba.alert.monitor.service.AlertApiService;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertRecordDetailService;
import com.nctigba.alert.monitor.service.AlertRecordService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import com.nctigba.alert.monitor.util.TextParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AlertApiServiceImpl
 *
 * @author wuyuebin
 * @since 2023/5/2 10:05
 */
@Service
@Slf4j
public class AlertApiServiceImpl implements AlertApiService {
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
    @Autowired
    private AlertShieldingMapper shieldingMapper;
    @Autowired
    private AlertClusterNodeConfService clusterNodeConfService;


    /**
     * prometheus msg
     *
     * @param alertApiReqList List<AlertApiReq>
     */
    public synchronized void alerts(List<AlertApiReq> alertApiReqList) {
        for (AlertApiReq alertApiReq : alertApiReqList) {
            try {
                Map<String, String> labels = alertApiReq.getLabels();
                Long templateId = Long.valueOf(labels.get("templateId"));
                String clusterNodeId = labels.get("instance");
                long count = clusterNodeConfService.count(Wrappers.<AlertClusterNodeConfDO>lambdaQuery()
                    .eq(AlertClusterNodeConfDO::getTemplateId, templateId)
                    .eq(AlertClusterNodeConfDO::getClusterNodeId, clusterNodeId)
                    .eq(AlertClusterNodeConfDO::getType, CommonConstants.INSTANCE)
                    .eq(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
                if (count == 0) {
                    continue;
                }
                Long templateRuleId = Long.valueOf(labels.get("templateRuleId"));
                AlertTemplateRuleDO alertTemplateRuleDO = templateRuleMapper.selectById(templateRuleId);
                if (alertTemplateRuleDO == null || alertTemplateRuleDO.getId() == null) {
                    continue;
                }
                if (StrUtil.isBlank(alertTemplateRuleDO.getNotifyWayIds())) {
                    continue;
                }
                if (checkIsShielding(clusterNodeId)) {
                    if (alertApiReq.getAlertStatus().equals(CommonConstants.RECOVER_STATUS)) {
                        alertToRecover(clusterNodeId, templateId, templateRuleId);
                    }
                    continue;
                }
                Optional<AlertRecordDO> optional = generateRecord(alertApiReq, alertTemplateRuleDO);
                if (optional.isEmpty()) {
                    continue;
                }
                AlertRecordDO alertRecordDO = optional.get();
                recordService.saveOrUpdate(alertRecordDO);
                List<AlertRecordDetailDO> recordDetailList = alertRecordDO.getRecordDetailList();
                recordDetailList.forEach(detail -> {
                    detail.setRecordId(alertRecordDO.getId());
                });
                recordDetailService.saveOrUpdateBatch(recordDetailList);
            } catch (NumberFormatException | NullPointerException | ServiceException e) {
                log.error("alertApiReq is {}", alertApiReq);
                log.error("exception: {}", e);
            }
        }
        context.publishEvent(new NotifyEvent(this));
    }

    private boolean checkIsShielding(String clusterNodeId) {
        List<AlertShieldingDO> shieldingDOList = shieldingMapper.selectList(Wrappers.<AlertShieldingDO>lambdaQuery()
            .eq(AlertShieldingDO::getIsDeleted, CommonConstants.IS_NOT_DELETE)
            .eq(AlertShieldingDO::getIsEnable, CommonConstants.IS_ENABLE));
        shieldingDOList = shieldingDOList.stream().filter(
            f -> f.getClusterNodeIds().contains(clusterNodeId)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(shieldingDOList)) {
            return false;
        }
        for (AlertShieldingDO shielding : shieldingDOList) {
            String type = shielding.getType();
            if (CommonConstants.SHIELDING_CONTINUOUSLY.equals(type)) {
                return true;
            }
            Date startDate = shielding.getStartDate();
            Date endDate = shielding.getEndDate();
            Date endTime = shielding.getEndTime();
            Date startTime = shielding.getStartTime();
            boolean isOneEmptyTime = startDate == null || endDate == null || startTime == null || endTime == null;
            Date now = new Date();
            if (CommonConstants.SHIELDING_PERIOD.equals(type) && !isOneEmptyTime) {
                Date start = transferToFullTime(startDate, startTime);
                Date end = transferToFullTime(endDate, endTime);
                if (now.getTime() >= start.getTime() && now.getTime() <= end.getTime()) {
                    return true;
                }
            }
            if (CommonConstants.SHIELDING_SCHEDULED.equals(type) && !isOneEmptyTime
                && isDateInRange(now, startDate, endDate) && isTimeInRange(now, startTime, endTime)) {
                return true;
            }
        }
        return false;
    }

    private Date transferToFullTime(Date date, Date time) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = dateFormat.format(date);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String timeStr = timeFormat.format(time);
            SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return fullFormat.parse(dateStr + " " + timeStr);
        } catch (ParseException e) {
            log.error("transfer to fulltime fail: " + e.getMessage());
            throw new CustomException("transfer to fulltime fail: " + e.getMessage());
        }
    }

    private boolean isDateInRange(Date current, Date startDate, Date endDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String startDateStr = dateFormat.format(startDate);
            String endDateStr = dateFormat.format(endDate);
            SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = fullFormat.parse(startDateStr + " 00:00:00");
            Date end = fullFormat.parse(endDateStr + " 23:59:59");
            return current.getTime() >= start.getTime() && current.getTime() <= end.getTime();
        } catch (ParseException e) {
            log.error("isDateInRange fail: " + e.getMessage());
            throw new CustomException("isTimeInRange fail: " + e.getMessage());
        }
    }

    private static boolean isTimeInRange(Date current, Date startTime, Date endTime) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String curStr = dateFormat.format(current);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = fullFormat.parse(curStr + " " + timeFormat.format(startTime));
            Date end = fullFormat.parse(curStr + " " + timeFormat.format(endTime));
            return current.getTime() >= start.getTime() && current.getTime() <= end.getTime();
        } catch (ParseException e) {
            log.error("isTimeInRange fail: " + e.getMessage());
            throw new CustomException("isTimeInRange fail: " + e.getMessage());
        }
    }

    private void alertToRecover(String clusterNodeId, Long templateId, Long templateRuleId) {
        List<AlertRecordDO> alertRecords = recordService.getList(clusterNodeId, templateId, templateRuleId);
        if (CollectionUtil.isEmpty(alertRecords)
            || CommonConstants.RECOVER_STATUS.equals(alertRecords.get(0).getAlertStatus())) {
            return;
        }
        AlertRecordDO alertRecord = alertRecords.get(0);
        alertRecord.setAlertStatus(CommonConstants.RECOVER_STATUS).setUpdateTime(LocalDateTime.now());
        recordService.updateById(alertRecord);
    }

    private Optional<AlertRecordDO> generateRecord(AlertApiReq alertApiReq, AlertTemplateRuleDO alertTemplateRuleDO) {
        String clusterNodeId = alertApiReq.getLabels().get("instance");
        Long templateId = Long.valueOf(alertApiReq.getLabels().get("templateId"));
        List<AlertRecordDO> alertRecordDOS =
                recordService.getList(clusterNodeId, templateId, alertTemplateRuleDO.getId());
        AlertRecordDO alertRecordDO = null;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (CollectionUtil.isEmpty(alertRecordDOS)
            || alertRecordDOS.get(0).getAlertStatus().equals(CommonConstants.RECOVER_STATUS)
            || !df.format(alertApiReq.getStartsAt()).equals(df.format(alertRecordDOS.get(0).getStartTime()))
        ) {
            if (alertApiReq.getAlertStatus().equals(CommonConstants.RECOVER_STATUS)) {
                return Optional.empty();
            }
            alertRecordDO = new AlertRecordDO();
            AlertTemplateDO alertTemplateDO = templateMapper.selectById(templateId);
            alertRecordDO.setClusterNodeId(clusterNodeId).setTemplateId(templateId).setTemplateRuleId(
                            alertTemplateRuleDO.getId()).setCreateTime(LocalDateTime.now())
                    .setTemplateName(alertTemplateDO.getTemplateName()).setTemplateRuleName(
                            alertTemplateRuleDO.getRuleName()).setStartTime(alertApiReq.getStartsAt())
                    .setTemplateRuleType(alertTemplateRuleDO.getRuleType()).setLevel(alertTemplateRuleDO.getLevel())
                    .setRecordStatus(CommonConstants.UNREAD_STATUS);
        } else {
            alertRecordDO = alertRecordDOS.get(0);
            alertRecordDO.setAlertStatus(alertApiReq.getAlertStatus()).setUpdateTime(LocalDateTime.now());
        }
        List<NotifyWayDO> notifyWayDOS = notifyWayMapper.selectBatchIds(
                Arrays.asList(alertTemplateRuleDO.getNotifyWayIds().split(CommonConstants.DELIMITER)));
        String notifyWayNames = notifyWayDOS.stream().map(item -> item.getName()).collect(
                Collectors.joining(CommonConstants.DELIMITER));
        Map<String, String> alertParams = generateAlertParams(alertApiReq);
        alertRecordDO.setNotifyWayIds(alertTemplateRuleDO.getNotifyWayIds()).setNotifyWayNames(notifyWayNames)
            .setEndTime(alertApiReq.getEndsAt()).setDuration(
                Duration.between(alertRecordDO.getStartTime(), alertRecordDO.getEndTime()).toSeconds())
            .setClusterId(alertParams.get("clusterId")).setAlertStatus(alertApiReq.getAlertStatus())
            .setAlertContent(TextParserUtils.parse(alertTemplateRuleDO.getRuleContent(), alertParams))
            .setType(CommonConstants.INSTANCE);
        AlertRecordDetailDO detail = new AlertRecordDetailDO();
        detail.setClusterId(alertRecordDO.getClusterId()).setRecordId(alertRecordDO.getId())
            .setLevel(alertRecordDO.getLevel()).setNotifyWayIds(alertRecordDO.getNotifyWayIds())
            .setNotifyWayNames(alertRecordDO.getNotifyWayNames()).setClusterNodeId(alertRecordDO.getClusterNodeId())
            .setAlertContent(alertRecordDO.getAlertContent()).setAlertStatus(alertRecordDO.getAlertStatus())
            .setTemplateId(alertRecordDO.getTemplateId()).setTemplateName(alertRecordDO.getTemplateName())
            .setTemplateRuleId(alertRecordDO.getTemplateRuleId()).setType(CommonConstants.INSTANCE)
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
        String level = labels.get("level");
        if (StrUtil.isNotBlank(level)) {
            alertParams.put("level", MessageSourceUtils.get("alertRecord." + level, Locale.getDefault()));
        }
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
        alertParams.put(
                "cluster",
                StrUtil.isNotBlank(opsClusterEntity.getClusterName()) ? opsClusterEntity.getClusterName()
                        : opsClusterEntity.getClusterId());
        alertParams.put("clusterId", opsClusterEntity.getClusterId());
        return alertParams;
    }
}
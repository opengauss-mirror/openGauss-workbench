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
 *  DataSourceConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/alert/monitor/supplier/AlertSupplier.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.supplier;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.annotation.Supplier;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.event.NotifyEvent;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.model.dto.AlertPluginInfoDTO;
import com.nctigba.alert.monitor.model.dto.AlertRuleParamDTO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDetailDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.service.AlertRecordDetailService;
import com.nctigba.alert.monitor.service.AlertRecordService;
import com.nctigba.alert.monitor.service.AlertRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import com.nctigba.alert.monitor.util.TextParserUtils;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AlertSupplier
 *
 * @author wuyuebin
 * @since 2024/7/22 17:37
 */
@Supplier("alert")
public class AlertSupplier {
    @Autowired
    private AlertRuleService ruleService;
    @Autowired
    private AlertTemplateService templateService;
    @Autowired
    private AlertTemplateRuleService templateRuleService;
    @Autowired
    private NotifyWayMapper notifyWayMapper;
    @Autowired
    private AlertRecordService recordService;
    @Autowired
    private AlertRecordDetailService recordDetailService;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private AlertTemplateRuleMapper alertTemplateRuleMapper;

    /**
     * saveAlertRule
     *
     * @param alertRule AlertRuleParamDTO
     * @return AjaxResult
     */
    @Supplier.Method("saveAlertRule")
    public AjaxResult saveAlertRule(AlertRuleParamDTO alertRule) {
        if (StrUtil.isBlank(alertRule.getRuleName()) || StrUtil.isBlank(alertRule.getPluginCode())
            || StrUtil.isBlank(alertRule.getRuleCode())) {
            throw new CustomException("ruleName ruleCode pluginCode is not empty!");
        }
        List<AlertRuleDO> list = ruleService.list(Wrappers.<AlertRuleDO>lambdaQuery().eq(AlertRuleDO::getRuleType,
                CommonConstants.PLUGIN_RULE).eq(AlertRuleDO::getPluginCode, alertRule.getPluginCode())
            .eq(AlertRuleDO::getRuleCode, alertRule.getRuleCode())
            .eq(AlertRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(list)) {
            return AjaxResult.success();
        }
        alertRule.setRuleType(CommonConstants.PLUGIN_RULE);
        alertRule.setIsSilence(CommonConstants.IS_NOT_SILENCE);
        alertRule.setAlertNotify("firing");
        List<NotifyWayDO> notifyWays = notifyWayMapper.selectList(Wrappers.<NotifyWayDO>lambdaQuery()
            .eq(NotifyWayDO::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByAsc(NotifyWayDO::getCreateTime));
        if (CollectionUtil.isNotEmpty(notifyWays)) {
            alertRule.setNotifyWayIds(notifyWays.get(0).getId().toString());
        }
        ruleService.saveRule(alertRule);
        return AjaxResult.success();
    }

    /**
     * alerts
     *
     * @param paramList List<Map<String, Object>>
     * @return AjaxResult
     */
    @Supplier.Method("alerts")
    public AjaxResult alerts(List<Map<String, Object>> paramList) {
        if (CollectionUtil.isEmpty(paramList)) {
            AjaxResult.error("alert info is empty!");
        }
        List<AlertPluginInfoDTO> pluginInfoList = paramList.stream().map(item -> {
            AlertPluginInfoDTO info = new AlertPluginInfoDTO();
            BeanUtil.copyProperties(item, info);
            return info;
        }).collect(Collectors.toList());
        for (AlertPluginInfoDTO info : pluginInfoList) {
            if (StrUtil.isBlank(info.getPluginCode()) || StrUtil.isBlank(info.getRuleCode())) {
                continue;
            }
            List<AlertTemplateRuleDO> ruleList =
                alertTemplateRuleMapper.getAlertConfigTemplateRuleList(info.getPluginCode(), info.getRuleCode());
            if (CollectionUtil.isEmpty(ruleList)) {
                continue;
            }
            for (AlertTemplateRuleDO templateRule : ruleList) {
                if (StrUtil.isBlank(templateRule.getNotifyWayIds())) {
                    continue;
                }
                List<NotifyWayDO> notifyWays = notifyWayMapper.selectBatchIds(
                    Arrays.asList(templateRule.getNotifyWayIds().split(CommonConstants.DELIMITER)));
                String notifyWayNames = notifyWays.stream().map(item -> item.getName()).collect(
                    Collectors.joining(CommonConstants.DELIMITER));
                AlertTemplateDO template = templateService.getById(templateRule.getTemplateId());
                Map<String, String> params = getParams(info, templateRule.getLevel());
                AlertRecordDO alertRecord = new AlertRecordDO();
                alertRecord.setClusterNodeId(info.getInstanceId()).setIp(info.getIp()).setPort(info.getPort())
                    .setType(CommonConstants.PLUGIN).setStartTime(info.getAlertTime())
                    .setNodeName(info.getInstance()).setClusterId(info.getInstanceId())
                    .setAlertContent(TextParserUtils.parse(templateRule.getRuleContent(), params))
                    .setNotifyWayIds(templateRule.getNotifyWayIds()).setNotifyWayNames(notifyWayNames)
                    .setAlertStatus(CommonConstants.RECOVER_STATUS).setLevel(templateRule.getLevel())
                    .setTemplateId(template.getId()).setTemplateName(template.getTemplateName())
                    .setTemplateRuleId(templateRule.getId()).setTemplateRuleName(templateRule.getRuleName())
                    .setTemplateRuleType(templateRule.getRuleType()).setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now()).setRecordStatus(CommonConstants.UNREAD_STATUS);
                recordService.save(alertRecord);

                AlertRecordDetailDO detail = new AlertRecordDetailDO();
                detail.setClusterId(alertRecord.getClusterId()).setRecordId(alertRecord.getId())
                    .setLevel(alertRecord.getLevel()).setNotifyWayIds(alertRecord.getNotifyWayIds())
                    .setNotifyWayNames(alertRecord.getNotifyWayNames()).setClusterNodeId(alertRecord.getClusterNodeId())
                    .setAlertContent(alertRecord.getAlertContent()).setAlertStatus(alertRecord.getAlertStatus())
                    .setTemplateId(alertRecord.getTemplateId()).setTemplateName(alertRecord.getTemplateName())
                    .setTemplateRuleId(alertRecord.getTemplateRuleId())
                    .setTemplateRuleName(alertRecord.getTemplateRuleName()).setAlertContent(
                        alertRecord.getAlertContent())
                    .setTemplateRuleType(alertRecord.getTemplateRuleType())
                    .setIp(alertRecord.getIp()).setType(alertRecord.getType()).setPort(alertRecord.getPort())
                    .setNodeName(alertRecord.getNodeName())
                    .setStartTime(alertRecord.getStartTime()).setEndTime(alertRecord.getEndTime())
                    .setNotifyStatus(CommonConstants.UNSEND).setCreateTime(LocalDateTime.now());
                recordDetailService.save(detail);
            }
        }
        context.publishEvent(new NotifyEvent(this));
        return AjaxResult.success();
    }
    private Map<String, String> getParams(AlertPluginInfoDTO info, String level) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("nodeName", info.getInstance());
        paramMap.put("ip", info.getIp());
        paramMap.put("hostIp", info.getPort());
        paramMap.put("alertTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(info.getAlertTime()));
        paramMap.put("level", MessageSourceUtils.get("alertRecord." + level, Locale.getDefault()));
        return paramMap;
    }
}

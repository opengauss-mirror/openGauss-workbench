/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.alert.monitor.config.ElasticsearchProvider;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertContentParamDto;
import com.nctigba.alert.monitor.dto.NotifySnmpDto;
import com.nctigba.alert.monitor.entity.AlertClusterNodeConf;
import com.nctigba.alert.monitor.entity.AlertRecord;
import com.nctigba.alert.monitor.entity.AlertSchedule;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.entity.NotifyTemplate;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.AlertRecordMapper;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertScheduleService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.utils.AlertContentParamUtil;
import com.nctigba.alert.monitor.utils.SpringContextUtils;
import com.nctigba.alert.monitor.utils.TextParser;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * log runnable
 *
 * @since 2023/8/1 11:18
 */
@Slf4j
public class AlertLogSchedulingRunnable implements Runnable {
    private String jobName;

    public AlertLogSchedulingRunnable(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public void run() {
        log.info("the " + jobName + " is start");
        AlertScheduleService scheduleService = SpringContextUtils.getBean(AlertScheduleService.class);
        AlertSchedule alertSchedule = null;
        synchronized (AlertLogSchedulingRunnable.class) {
            List<AlertSchedule> list = scheduleService.list(
                Wrappers.<AlertSchedule>lambdaQuery().eq(AlertSchedule::getJobName, jobName).eq(
                    AlertSchedule::getIsLocked, CommonConstants.UNLOCK));
            if (CollectionUtil.isEmpty(list)) {
                return;
            }
            alertSchedule = list.get(0).setIsLocked(CommonConstants.LOCKED).setUpdateTime(LocalDateTime.now());
            scheduleService.updateById(alertSchedule);
        }

        try {
            Long ruleId = Long.valueOf(jobName.substring(CommonConstants.THREAD_NAME_PREFIX.length()));
            AlertTemplateRuleService templateRuleService = SpringContextUtils.getBean(AlertTemplateRuleService.class);
            List<AlertTemplateRule> templateRules = templateRuleService.list(
                Wrappers.<AlertTemplateRule>lambdaQuery().eq(AlertTemplateRule::getRuleId, ruleId).eq(
                    AlertTemplateRule::getIsDeleted, CommonConstants.IS_NOT_DELETE).eq(AlertTemplateRule::getEnable,
                    CommonConstants.ENABLE));
            if (CollectionUtil.isEmpty(templateRules)) {
                return;
            }
            Set<Long> templateIds = templateRules.stream().map(item -> item.getTemplateId()).collect(
                Collectors.toSet());
            AlertClusterNodeConfService clusterNodeConfService =
                SpringContextUtils.getBean(AlertClusterNodeConfService.class);
            List<AlertClusterNodeConf> clusterNodeConfs = clusterNodeConfService.list(
                Wrappers.<AlertClusterNodeConf>lambdaQuery().in(AlertClusterNodeConf::getTemplateId, templateIds)
                    .eq(AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_NOT_DELETE));
            if (CollectionUtil.isEmpty(clusterNodeConfs)) {
                return;
            }

            runRuleTask(clusterNodeConfs, templateRules);
        } finally {
            alertSchedule.setIsLocked(CommonConstants.UNLOCK).setUpdateTime(LocalDateTime.now())
                .setLastTime(LocalDateTime.now());
            scheduleService.updateById(alertSchedule);
            log.info("the " + jobName + " is end");
        }
    }

    private void runRuleTask(List<AlertClusterNodeConf> clusterNodeConfs, List<AlertTemplateRule> templateRules) {
        LocalDateTime now = LocalDateTime.now();
        for (AlertClusterNodeConf clusterNodeConf : clusterNodeConfs) {
            try {
                AlertTemplateRule templateRule = templateRules.stream().filter(
                    item -> item.getTemplateId().equals(clusterNodeConf.getTemplateId())).findFirst().get();
                String unit = templateRule.getNotifyDurationUnit();
                Integer duration = templateRule.getNotifyDuration();
                LocalDateTime start = unit.equals(CommonConstants.SECOND) ? now.minusSeconds(duration)
                    : unit.equals(CommonConstants.MINUTE) ? now.minusMinutes(duration) : unit.equals(
                    CommonConstants.HOUR) ? now.minusHours(duration) : now.minusDays(duration);

                Long templateRuleId = templateRule.getId();
                AlertTemplateRuleItemService templateRuleItemService =
                    SpringContextUtils.getBean(AlertTemplateRuleItemService.class);
                List<AlertTemplateRuleItem> templateRuleItems = templateRuleItemService.list(
                    Wrappers.<AlertTemplateRuleItem>lambdaQuery().eq(AlertTemplateRuleItem::getTemplateRuleId,
                        templateRuleId).eq(AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));

                Map<String, Boolean> boolMap = new HashMap<>();
                for (AlertTemplateRuleItem ruleItem : templateRuleItems) {
                    Long count = queryLogCount(clusterNodeConf.getClusterNodeId(), ruleItem.getKeyword(),
                        ruleItem.getBlockWord(), start, now);
                    String ruleMark = ruleItem.getRuleMark();
                    String operate = ruleItem.getOperate();
                    Long limitValue = Long.valueOf(ruleItem.getLimitValue());
                    boolMap.put(ruleMark, operate.equals(">") ? count > limitValue
                        : operate.equals(">=") ? count >= limitValue : operate.equals("=") ? count == limitValue
                        : operate.equals("<=") ? count <= limitValue : count < limitValue);
                }
                boolean isAlert = Arrays.stream(templateRule.getRuleExpComb().split("or")).map(andComb ->
                        Arrays.stream(andComb.split("and")).map(mark -> boolMap.get(mark.trim()))
                            .reduce((item1, item2) -> item1 && item2).get())
                    .reduce((item1, item2) -> item1 || item2).get();
                if (isAlert) {
                    recordAndNotify(clusterNodeConf, templateRule, start, now);
                }
            } catch (IOException e) {
                log.error("the task " + jobName + "search fail,nodeId is " + clusterNodeConf.getClusterNodeId(), e);
            }
        }
    }

    private Long queryLogCount(
        String clusterNodeId, String keyword, String blockWord, LocalDateTime start,
        LocalDateTime end) throws IOException {
        ElasticsearchProvider elasticsearchProvider = SpringContextUtils.getBean(ElasticsearchProvider.class);
        ElasticsearchClient client = elasticsearchProvider.client();
        Query.Builder filterBuilder = new Query.Builder();
        filterBuilder.range(range -> range.field("@timestamp")
            .gte(JsonData.of(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+08:00'"))))
            .lt(JsonData.of(end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+08:00'")))));
        SearchResponse<HashMap> response = client.search(search -> {
            search.index("ob-*-" + clusterNodeId);
            search.size(0);
            search.query(qry -> {
                qry.bool(bool -> {
                    bool = bool.filter(filterBuilder.build());
                    String[] keywords = keyword.split(CommonConstants.DELIMITER);
                    for (String key : keywords) {
                        bool = bool.must(must ->
                            must.matchPhrase(match -> match.field("message").query(key)));
                    }
                    if (StrUtil.isNotBlank(blockWord)) {
                        for (String bw : blockWord.split(",")) {
                            bool = bool.mustNot(mustNot -> mustNot.matchPhrase(match -> match.field(
                                "message").query(bw)));
                        }
                    }
                    return bool;
                });
                return qry;
            });
            return search;
        }, HashMap.class);
        return response.hits().total().value();
    }

    private void recordAndNotify(
        AlertClusterNodeConf clusterNodeConf, AlertTemplateRule templateRule,
        LocalDateTime start, LocalDateTime end) {
        if (StrUtil.isBlank(templateRule.getNotifyWayIds())) {
            return;
        }
        String[] notifyWayIdArr = templateRule.getNotifyWayIds().split(CommonConstants.DELIMITER);
        NotifyWayMapper notifyWayMapper = SpringContextUtils.getBean(NotifyWayMapper.class);
        List<NotifyWay> notifyWays = notifyWayMapper.selectBatchIds(Arrays.asList(notifyWayIdArr));
        if (CollectionUtil.isEmpty(notifyWays)) {
            return;
        }
        AlertContentParamUtil alertContentParamUtil = SpringContextUtils.getBean(AlertContentParamUtil.class);
        AlertContentParamDto contentParamDto = alertContentParamUtil.setAndGetAlertContentParamDto(
            clusterNodeConf.getClusterNodeId(), end, templateRule.getLevel(), templateRule.getRuleContent());
        AlertTemplateService templateService = SpringContextUtils.getBean(AlertTemplateService.class);
        AlertTemplate template = templateService.getById(clusterNodeConf.getTemplateId());
        String notifyWayNames = "";
        notifyWayNames = notifyWays.stream().map(item -> item.getName()).collect(
            Collectors.joining(CommonConstants.DELIMITER));
        AlertRecord record = new AlertRecord();
        record.setClusterNodeId(clusterNodeConf.getClusterNodeId())
            .setTemplateId(clusterNodeConf.getTemplateId()).setTemplateRuleId(templateRule.getId())
            .setLevel(templateRule.getLevel()).setRecordStatus(CommonConstants.UNREAD_STATUS)
            .setAlertStatus(CommonConstants.RECOVER_STATUS).setTemplateRuleType(templateRule.getRuleType())
            .setNotifyWayIds(templateRule.getNotifyWayIds()).setNotifyWayNames(notifyWayNames)
            .setStartTime(start).setEndTime(end).setIsDeleted(CommonConstants.IS_NOT_DELETE)
            .setDuration(Duration.between(start, end).toSeconds())
            .setAlertContent(contentParamDto.getContent()).setTemplateRuleName(templateRule.getRuleName())
            .setCreateTime(LocalDateTime.now()).setTemplateName(template.getTemplateName());
        AlertRecordMapper recordMapper = SpringContextUtils.getBean(AlertRecordMapper.class);
        recordMapper.insert(record);

        Integer isSilence = templateRule.getIsSilence();
        if (isSilence == CommonConstants.IS_SILENCE && end.isAfter(templateRule.getSilenceStartTime())
            && end.isBefore(templateRule.getSilenceEndTime())) {
            return;
        }
        saveNotifyMessage(notifyWays, contentParamDto, record.getId());
    }

    private void saveNotifyMessage(List<NotifyWay> notifyWays, AlertContentParamDto contentParamDto, Long recordId) {
        NotifyTemplateMapper notifyTemplateMapper = SpringContextUtils.getBean(NotifyTemplateMapper.class);
        NotifyMessageMapper notifyMessageMapper = SpringContextUtils.getBean(NotifyMessageMapper.class);
        for (NotifyWay notifyWay : notifyWays) {
            NotifyMessage notifyMessage = new NotifyMessage();
            Long notifyTemplateId = notifyWay.getNotifyTemplateId();
            NotifyTemplate notifyTemplate = notifyTemplateMapper.selectById(notifyTemplateId);
            String notifyType = notifyWay.getNotifyType();
            notifyMessage.setMessageType(notifyType);
            notifyMessage.setTitle(notifyTemplate.getNotifyTitle());
            String content = new TextParser().parse(notifyTemplate.getNotifyContent(), contentParamDto);
            notifyMessage.setContent(content);
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
            notifyMessage.setRecordId(recordId);
            notifyMessageMapper.insert(notifyMessage);
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (o instanceof AlertLogSchedulingRunnable) {
            AlertLogSchedulingRunnable that = (AlertLogSchedulingRunnable) o;
            return jobName.equals(that.jobName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName);
    }
}

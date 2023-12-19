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
 *  AlertLogSchedulingRunnable.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/schedule/AlertLogSchedulingRunnable.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.alert.monitor.config.ElasticsearchProviderConfig;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.AlertRecordDetailMapper;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDetailDO;
import com.nctigba.alert.monitor.model.entity.AlertScheduleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.mapper.AlertRecordMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertScheduleService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import com.nctigba.alert.monitor.util.SpringContextUtils;
import com.nctigba.alert.monitor.util.TextParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;

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
        AlertScheduleDO alertScheduleDO = null;
        synchronized (AlertLogSchedulingRunnable.class) {
            List<AlertScheduleDO> list = scheduleService.list(
                Wrappers.<AlertScheduleDO>lambdaQuery().eq(AlertScheduleDO::getJobName, jobName).eq(
                    AlertScheduleDO::getIsLocked, CommonConstants.UNLOCK));
            if (CollectionUtil.isEmpty(list)) {
                return;
            }
            alertScheduleDO = list.get(0).setIsLocked(CommonConstants.LOCKED).setUpdateTime(LocalDateTime.now());
            scheduleService.updateById(alertScheduleDO);
        }

        try {
            Long ruleId = Long.valueOf(jobName.substring(CommonConstants.THREAD_NAME_PREFIX.length()));
            AlertTemplateRuleService templateRuleService = SpringContextUtils.getBean(AlertTemplateRuleService.class);
            List<AlertTemplateRuleDO> templateRules = templateRuleService.list(
                Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getRuleId, ruleId).eq(
                    AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE).eq(AlertTemplateRuleDO::getEnable,
                    CommonConstants.ENABLE));
            if (CollectionUtil.isEmpty(templateRules)) {
                return;
            }
            Set<Long> templateIds = templateRules.stream().map(item -> item.getTemplateId()).collect(
                Collectors.toSet());
            AlertClusterNodeConfService clusterNodeConfService =
                SpringContextUtils.getBean(AlertClusterNodeConfService.class);
            List<AlertClusterNodeConfDO> clusterNodeConfs = clusterNodeConfService.list(
                Wrappers.<AlertClusterNodeConfDO>lambdaQuery().in(AlertClusterNodeConfDO::getTemplateId, templateIds)
                    .eq(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
            if (CollectionUtil.isEmpty(clusterNodeConfs)) {
                return;
            }

            runRuleTask(clusterNodeConfs, templateRules);
        } finally {
            alertScheduleDO.setIsLocked(CommonConstants.UNLOCK).setUpdateTime(LocalDateTime.now())
                .setLastTime(LocalDateTime.now());
            scheduleService.updateById(alertScheduleDO);
            log.info("the " + jobName + " is end");
        }
    }

    private void runRuleTask(List<AlertClusterNodeConfDO> clusterNodeConfs, List<AlertTemplateRuleDO> templateRules) {
        LocalDateTime now = LocalDateTime.now();
        for (AlertClusterNodeConfDO clusterNodeConf : clusterNodeConfs) {
            try {
                AlertTemplateRuleDO templateRule = templateRules.stream().filter(
                    item -> item.getTemplateId().equals(clusterNodeConf.getTemplateId())).findFirst().get();
                String unit = templateRule.getNotifyDurationUnit();
                Integer duration = templateRule.getNotifyDuration();
                LocalDateTime start = unit.equals(CommonConstants.SECOND) ? now.minusSeconds(duration)
                    : unit.equals(CommonConstants.MINUTE) ? now.minusMinutes(duration) : unit.equals(
                    CommonConstants.HOUR) ? now.minusHours(duration) : now.minusDays(duration);

                Long templateRuleId = templateRule.getId();
                AlertTemplateRuleItemService templateRuleItemService =
                    SpringContextUtils.getBean(AlertTemplateRuleItemService.class);
                List<AlertTemplateRuleItemDO> templateRuleItems = templateRuleItemService.list(
                    Wrappers.<AlertTemplateRuleItemDO>lambdaQuery().eq(AlertTemplateRuleItemDO::getTemplateRuleId,
                        templateRuleId).eq(AlertTemplateRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));

                Map<String, Boolean> boolMap = new HashMap<>();
                for (AlertTemplateRuleItemDO ruleItem : templateRuleItems) {
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
                    record(clusterNodeConf, templateRule, start, now);
                }
            } catch (IOException e) {
                log.error("the task " + jobName + "search fail,nodeId is " + clusterNodeConf.getClusterNodeId(), e);
            }
        }
    }

    private Long queryLogCount(
        String clusterNodeId, String keyword, String blockWord, LocalDateTime start,
        LocalDateTime end) throws IOException {
        ElasticsearchProviderConfig elasticsearchProviderConfig = SpringContextUtils.getBean(
            ElasticsearchProviderConfig.class);
        ElasticsearchClient client = elasticsearchProviderConfig.client();
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

    private void record(
        AlertClusterNodeConfDO clusterNodeConf, AlertTemplateRuleDO templateRule,
        LocalDateTime start, LocalDateTime end) {
        if (StrUtil.isBlank(templateRule.getNotifyWayIds())) {
            return;
        }
        String[] notifyWayIdArr = templateRule.getNotifyWayIds().split(CommonConstants.DELIMITER);
        NotifyWayMapper notifyWayMapper = SpringContextUtils.getBean(NotifyWayMapper.class);
        List<NotifyWayDO> notifyWayDOS = notifyWayMapper.selectBatchIds(Arrays.asList(notifyWayIdArr));
        if (CollectionUtil.isEmpty(notifyWayDOS)) {
            return;
        }
        Map<String, String> alertParams = generateAlertParams(clusterNodeConf.getClusterNodeId(),
            CommonConstants.RECOVER_STATUS, end);
        AlertTemplateService templateService = SpringContextUtils.getBean(AlertTemplateService.class);
        AlertTemplateDO template = templateService.getById(clusterNodeConf.getTemplateId());
        String notifyWayNames = "";
        notifyWayNames = notifyWayDOS.stream().map(item -> item.getName()).collect(
            Collectors.joining(CommonConstants.DELIMITER));
        AlertRecordDO record = new AlertRecordDO();
        record.setClusterNodeId(clusterNodeConf.getClusterNodeId()).setClusterId(alertParams.get("clusterId"))
            .setTemplateId(clusterNodeConf.getTemplateId()).setTemplateRuleId(templateRule.getId())
            .setLevel(templateRule.getLevel()).setRecordStatus(CommonConstants.UNREAD_STATUS)
            .setAlertStatus(CommonConstants.RECOVER_STATUS).setTemplateRuleType(templateRule.getRuleType())
            .setNotifyWayIds(templateRule.getNotifyWayIds()).setNotifyWayNames(notifyWayNames)
            .setStartTime(start).setEndTime(end).setIsDeleted(CommonConstants.IS_NOT_DELETE)
            .setDuration(Duration.between(start, end).toSeconds())
            .setAlertContent(TextParserUtils.parse(templateRule.getRuleContent(), alertParams))
            .setTemplateRuleName(templateRule.getRuleName())
            .setCreateTime(LocalDateTime.now()).setTemplateName(template.getTemplateName());
        AlertRecordMapper recordMapper = SpringContextUtils.getBean(AlertRecordMapper.class);
        recordMapper.insert(record);
        AlertRecordDetailDO detail = new AlertRecordDetailDO();
        BeanUtil.copyProperties(record, detail);
        detail.setRecordId(record.getId()).setId(null).setNotifyStatus(CommonConstants.UNSEND);
        AlertRecordDetailMapper detailMapper = SpringContextUtils.getBean(AlertRecordDetailMapper.class);
        detailMapper.insert(detail);
    }

    private Map<String, String>
    generateAlertParams(String clusterNodeId, Integer alertStatus, LocalDateTime alertTime) {
        Map<String, String> alertParams = new HashMap<>();
        alertParams.put("alertTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(alertTime));
        if (alertStatus.equals(CommonConstants.FIRING_STATUS)) {
            alertParams.put("alertStatus", MessageSourceUtils.get("alerting"));
        } else {
            alertParams.put("alertStatus", MessageSourceUtils.get("alerted"));
        }
        alertParams.put("clusterNodeId", clusterNodeId);
        IOpsClusterNodeService clusterNodeService = SpringContextUtils.getBean(IOpsClusterNodeService.class);
        OpsClusterNodeEntity opsClusterNodeEntity = clusterNodeService.getById(clusterNodeId);
        if (opsClusterNodeEntity == null) {
            throw new ServiceException("cluster node is not found");
        }
        HostFacade hostFacade = SpringContextUtils.getBean(HostFacade.class);
        OpsHostEntity opsHost = hostFacade.getById(opsClusterNodeEntity.getHostId());
        if (opsHost == null) {
            throw new ServiceException("host is not found");
        }
        IOpsClusterService clusterService = SpringContextUtils.getBean(IOpsClusterService.class);
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

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
 *  AlertLogSchedulingRunnableTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/schedule/AlertLogSchedulingRunnableTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.schedule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.nctigba.alert.monitor.config.ElasticsearchProviderConfig;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.entity.AlertScheduleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.mapper.AlertRecordMapper;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertScheduleService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.util.SpringContextUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AlertLogSchedulingRunnable
 *
 * @since 2023/8/28 01:00
 */
@RunWith(SpringRunner.class)
public class AlertLogSchedulingRunnableTest {
    @Mock
    private AlertScheduleService scheduleService;
    @InjectMocks
    private AlertLogSchedulingRunnable logSchedulingRunnable =
        new AlertLogSchedulingRunnable(CommonConstants.THREAD_NAME_PREFIX + 1);
    @Mock
    private AlertTemplateRuleService templateRuleService;
    @Mock
    private AlertClusterNodeConfService clusterNodeConfService;
    @Mock
    private AlertTemplateRuleItemService templateRuleItemService;
    @Mock
    private ElasticsearchProviderConfig elasticsearchProviderConfig;
    @Mock
    private ElasticsearchClient client;
    @Mock
    private NotifyWayMapper notifyWayMapper;
    @Mock
    private AlertTemplateService templateService;
    @Mock
    private AlertRecordMapper recordMapper;
    @Mock
    private NotifyTemplateMapper notifyTemplateMapper;
    @Mock
    private NotifyMessageMapper notifyMessageMapper;

    @Test
    public void testRunWithEmptySchedule() {
        try (MockedStatic<SpringContextUtils> mockedStatic = mockStatic(SpringContextUtils.class)) {
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertScheduleService.class)).thenReturn(scheduleService);
            List<AlertScheduleDO> list = new ArrayList<>();
            when(scheduleService.list(any())).thenReturn(list);
            logSchedulingRunnable.run();
        }
    }

    @Test
    public void testRunWithEmptyTemplateRules() {
        try (MockedStatic<SpringContextUtils> mockedStatic = mockStatic(SpringContextUtils.class)) {
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertScheduleService.class)).thenReturn(scheduleService);
            List<AlertScheduleDO> list = new ArrayList<>();
            list.add(new AlertScheduleDO());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRuleDO> templateRules = new ArrayList<>();
            when(templateRuleService.list(any())).thenReturn(templateRules);

            logSchedulingRunnable.run();
            verify(scheduleService, times(2)).updateById(any());
        }
    }

    @Test
    public void testRunWithEmptyClusterNodeConf() {
        try (MockedStatic<SpringContextUtils> mockedStatic = mockStatic(SpringContextUtils.class)) {
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertScheduleService.class)).thenReturn(scheduleService);
            List<AlertScheduleDO> list = new ArrayList<>();
            list.add(new AlertScheduleDO());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRuleDO> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRuleDO().setTemplateId(1L));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            List<AlertClusterNodeConfDO> clusterNodeConfs = new ArrayList<>();
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertClusterNodeConfService.class)).thenReturn(clusterNodeConfService);
            when(clusterNodeConfService.list(any())).thenReturn(clusterNodeConfs);

            logSchedulingRunnable.run();
            verify(scheduleService, times(2)).updateById(any());
        }
    }

    @Test
    public void testRunWithoutAlert() throws IOException {
        try (MockedStatic<SpringContextUtils> mockedStatic = mockStatic(SpringContextUtils.class)) {
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertScheduleService.class)).thenReturn(scheduleService);
            List<AlertScheduleDO> list = new ArrayList<>();
            list.add(new AlertScheduleDO());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRuleDO> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRuleDO().setTemplateId(1L).setId(1L).setRuleId(1L).setRuleExpComb("A")
                .setNotifyDuration(5).setNotifyDurationUnit(CommonConstants.MINUTE));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            List<AlertClusterNodeConfDO> clusterNodeConfs = new ArrayList<>();
            clusterNodeConfs.add(new AlertClusterNodeConfDO().setTemplateId(1L).setClusterNodeId("node1"));
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertClusterNodeConfService.class)).thenReturn(clusterNodeConfService);
            when(clusterNodeConfService.list(any())).thenReturn(clusterNodeConfs);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleItemService.class)).thenReturn(templateRuleItemService);
            List<AlertTemplateRuleItemDO> templateRuleItems = new ArrayList<>();
            templateRuleItems.add(new AlertTemplateRuleItemDO().setRuleMark("A").setKeyword("abc,efg").setBlockWord(
                "bcd").setOperate(">").setLimitValue("10"));
            when(templateRuleItemService.list(any())).thenReturn(templateRuleItems);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(ElasticsearchProviderConfig.class)).thenReturn(elasticsearchProviderConfig);
            when(elasticsearchProviderConfig.client()).thenReturn(client);
            when(client.search(any(Function.class), eq(HashMap.class))).thenReturn(mockEsResponse());

            logSchedulingRunnable.run();
            verify(scheduleService, times(2)).updateById(any());
        }
    }

    @Test
    public void testRunWithoutNotifyWayIds() throws IOException {
        try (MockedStatic<SpringContextUtils> mockedStatic = mockStatic(SpringContextUtils.class)) {
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertScheduleService.class)).thenReturn(scheduleService);
            List<AlertScheduleDO> list = new ArrayList<>();
            list.add(new AlertScheduleDO());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRuleDO> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRuleDO().setTemplateId(1L).setId(1L).setRuleId(1L).setRuleExpComb("A")
                .setNotifyDuration(5).setNotifyDurationUnit(CommonConstants.SECOND));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            List<AlertClusterNodeConfDO> clusterNodeConfs = new ArrayList<>();
            clusterNodeConfs.add(new AlertClusterNodeConfDO().setTemplateId(1L).setClusterNodeId("node1"));
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertClusterNodeConfService.class)).thenReturn(clusterNodeConfService);
            when(clusterNodeConfService.list(any())).thenReturn(clusterNodeConfs);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleItemService.class)).thenReturn(templateRuleItemService);
            List<AlertTemplateRuleItemDO> templateRuleItems = new ArrayList<>();
            templateRuleItems.add(new AlertTemplateRuleItemDO().setRuleMark("A").setKeyword("abc,efg").setBlockWord(
                "bcd").setOperate(">=").setLimitValue("5"));
            when(templateRuleItemService.list(any())).thenReturn(templateRuleItems);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(ElasticsearchProviderConfig.class)).thenReturn(elasticsearchProviderConfig);
            when(elasticsearchProviderConfig.client()).thenReturn(client);
            when(client.search(any(Function.class), eq(HashMap.class))).thenReturn(mockEsResponse());

            logSchedulingRunnable.run();
            verify(scheduleService, times(2)).updateById(any());
        }
    }

    @Test
    public void testRunWithoutNotifyWayList() throws IOException {
        try (MockedStatic<SpringContextUtils> mockedStatic = mockStatic(SpringContextUtils.class)) {
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertScheduleService.class)).thenReturn(scheduleService);
            List<AlertScheduleDO> list = new ArrayList<>();
            list.add(new AlertScheduleDO());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRuleDO> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRuleDO().setTemplateId(1L).setId(1L).setRuleId(1L).setRuleExpComb("A")
                .setNotifyDuration(5).setNotifyDurationUnit(CommonConstants.HOUR).setNotifyWayIds("1"));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            List<AlertClusterNodeConfDO> clusterNodeConfs = new ArrayList<>();
            clusterNodeConfs.add(new AlertClusterNodeConfDO().setTemplateId(1L).setClusterNodeId("node1"));
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertClusterNodeConfService.class)).thenReturn(clusterNodeConfService);
            when(clusterNodeConfService.list(any())).thenReturn(clusterNodeConfs);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleItemService.class)).thenReturn(templateRuleItemService);
            List<AlertTemplateRuleItemDO> templateRuleItems = new ArrayList<>();
            templateRuleItems.add(new AlertTemplateRuleItemDO().setRuleMark("A").setKeyword("abc,efg").setBlockWord(
                "bcd").setOperate(">=").setLimitValue("5"));
            when(templateRuleItemService.list(any())).thenReturn(templateRuleItems);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(ElasticsearchProviderConfig.class)).thenReturn(elasticsearchProviderConfig);
            when(elasticsearchProviderConfig.client()).thenReturn(client);
            when(client.search(any(Function.class), eq(HashMap.class))).thenReturn(mockEsResponse());
            mockedStatic.when(() ->
                SpringContextUtils.getBean(NotifyWayMapper.class)).thenReturn(notifyWayMapper);
            List<NotifyWayDO> notifyWayDOS = new ArrayList<>();
            when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWayDOS);

            logSchedulingRunnable.run();
            verify(scheduleService, times(2)).updateById(any());
        }
    }

    private List<NotifyWayDO> mockNotifyWays() {
        List<NotifyWayDO> notifyWayDOS = new ArrayList<>();
        notifyWayDOS.add(new NotifyWayDO().setId(1L).setName("name").setNotifyType(CommonConstants.EMAIL)
            .setEmail("email").setNotifyTemplateId(1L));
        notifyWayDOS.add(new NotifyWayDO().setId(1L).setName("name").setNotifyType(CommonConstants.WE_COM)
            .setSendWay(CommonConstants.APP_SEND_WAY).setDeptId("1").setPersonId("1").setNotifyTemplateId(1L));
        notifyWayDOS.add(new NotifyWayDO().setId(1L).setName("name").setNotifyType(CommonConstants.WE_COM)
            .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook").setNotifyTemplateId(1L));
        notifyWayDOS.add(new NotifyWayDO().setId(1L).setName("name").setNotifyType(CommonConstants.DING_TALK)
            .setSendWay(CommonConstants.APP_SEND_WAY).setDeptId("1").setPersonId("1").setNotifyTemplateId(1L));
        notifyWayDOS.add(new NotifyWayDO().setId(1L).setName("name").setNotifyType(CommonConstants.DING_TALK)
            .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook").setSign("sign").setNotifyTemplateId(1L));
        notifyWayDOS.add(new NotifyWayDO().setId(1L).setName("name").setNotifyType(CommonConstants.DING_TALK)
            .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook").setSign("").setNotifyTemplateId(1L));
        notifyWayDOS.add(new NotifyWayDO().setId(1L).setName("name").setNotifyType(CommonConstants.WEBHOOK)
            .setWebhook("webhook").setNotifyTemplateId(1L));
        notifyWayDOS.add(new NotifyWayDO().setId(1L).setName("name").setNotifyType(CommonConstants.WEBHOOK)
            .setWebhook("webhook").setHeader("{\"a\":\"b\"}").setParams("{\"k\":\"v\"}").setBody("body")
            .setResultCode("{\"errCode\":0}").setNotifyTemplateId(1L));
        notifyWayDOS.add(new NotifyWayDO().setId(1L).setName("name").setNotifyType(CommonConstants.SNMP)
            .setSnmpIp("127.0.0.1").setSnmpPort("162").setSnmpVersion(SnmpConstants.version3).setSnmpCommunity(
                "community").setSnmpOid("oid").setSnmpUsername("user").setSnmpAuthPasswd("123").setSnmpPrivPasswd(
                "1234").setNotifyTemplateId(1L));
        return notifyWayDOS;
    }

    private SearchResponse<HashMap> mockEsResponse() {
        TotalHits.Builder totalBuilder = new TotalHits.Builder();
        totalBuilder.value(9L).relation(TotalHitsRelation.Eq);
        HitsMetadata.Builder<HashMap> hitsMetadataBuilder = new HitsMetadata.Builder<HashMap>();
        hitsMetadataBuilder.total(totalBuilder.build()).hits(new ArrayList<>());
        SearchResponse.Builder<HashMap> hashMapBuilder = new SearchResponse.Builder<>();
        hashMapBuilder.hits(hitsMetadataBuilder.build()).took(4028L).timedOut(false);
        ShardStatistics.Builder statisticsBuilder = new ShardStatistics.Builder();
        statisticsBuilder.failed(0).successful(14).total(14).skipped(0);
        hashMapBuilder.shards(statisticsBuilder.build());
        return hashMapBuilder.build();
    }
}

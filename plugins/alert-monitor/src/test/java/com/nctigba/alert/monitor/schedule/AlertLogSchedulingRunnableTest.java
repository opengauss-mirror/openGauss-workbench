/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.schedule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.nctigba.alert.monitor.config.ElasticsearchProvider;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertContentParamDto;
import com.nctigba.alert.monitor.entity.AlertClusterNodeConf;
import com.nctigba.alert.monitor.entity.AlertSchedule;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private ElasticsearchProvider elasticsearchProvider;
    @Mock
    private ElasticsearchClient client;
    @Mock
    private NotifyWayMapper notifyWayMapper;
    @Mock
    private AlertContentParamUtil alertContentParamUtil;
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
            List<AlertSchedule> list = new ArrayList<>();
            when(scheduleService.list(any())).thenReturn(list);
            logSchedulingRunnable.run();
        }
    }

    @Test
    public void testRunWithEmptyTemplateRules() {
        try (MockedStatic<SpringContextUtils> mockedStatic = mockStatic(SpringContextUtils.class)) {
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertScheduleService.class)).thenReturn(scheduleService);
            List<AlertSchedule> list = new ArrayList<>();
            list.add(new AlertSchedule());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRule> templateRules = new ArrayList<>();
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
            List<AlertSchedule> list = new ArrayList<>();
            list.add(new AlertSchedule());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRule> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRule().setTemplateId(1L));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            List<AlertClusterNodeConf> clusterNodeConfs = new ArrayList<>();
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
            List<AlertSchedule> list = new ArrayList<>();
            list.add(new AlertSchedule());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRule> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRule().setTemplateId(1L).setId(1L).setRuleId(1L).setRuleExpComb("A")
                .setNotifyDuration(5).setNotifyDurationUnit(CommonConstants.MINUTE));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            List<AlertClusterNodeConf> clusterNodeConfs = new ArrayList<>();
            clusterNodeConfs.add(new AlertClusterNodeConf().setTemplateId(1L).setClusterNodeId("node1"));
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertClusterNodeConfService.class)).thenReturn(clusterNodeConfService);
            when(clusterNodeConfService.list(any())).thenReturn(clusterNodeConfs);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleItemService.class)).thenReturn(templateRuleItemService);
            List<AlertTemplateRuleItem> templateRuleItems = new ArrayList<>();
            templateRuleItems.add(new AlertTemplateRuleItem().setRuleMark("A").setKeyword("abc,efg").setBlockWord(
                "bcd").setOperate(">").setLimitValue("10"));
            when(templateRuleItemService.list(any())).thenReturn(templateRuleItems);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(ElasticsearchProvider.class)).thenReturn(elasticsearchProvider);
            when(elasticsearchProvider.client()).thenReturn(client);
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
            List<AlertSchedule> list = new ArrayList<>();
            list.add(new AlertSchedule());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRule> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRule().setTemplateId(1L).setId(1L).setRuleId(1L).setRuleExpComb("A")
                .setNotifyDuration(5).setNotifyDurationUnit(CommonConstants.SECOND));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            List<AlertClusterNodeConf> clusterNodeConfs = new ArrayList<>();
            clusterNodeConfs.add(new AlertClusterNodeConf().setTemplateId(1L).setClusterNodeId("node1"));
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertClusterNodeConfService.class)).thenReturn(clusterNodeConfService);
            when(clusterNodeConfService.list(any())).thenReturn(clusterNodeConfs);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleItemService.class)).thenReturn(templateRuleItemService);
            List<AlertTemplateRuleItem> templateRuleItems = new ArrayList<>();
            templateRuleItems.add(new AlertTemplateRuleItem().setRuleMark("A").setKeyword("abc,efg").setBlockWord(
                "bcd").setOperate(">=").setLimitValue("5"));
            when(templateRuleItemService.list(any())).thenReturn(templateRuleItems);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(ElasticsearchProvider.class)).thenReturn(elasticsearchProvider);
            when(elasticsearchProvider.client()).thenReturn(client);
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
            List<AlertSchedule> list = new ArrayList<>();
            list.add(new AlertSchedule());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRule> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRule().setTemplateId(1L).setId(1L).setRuleId(1L).setRuleExpComb("A")
                .setNotifyDuration(5).setNotifyDurationUnit(CommonConstants.HOUR).setNotifyWayIds("1"));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            List<AlertClusterNodeConf> clusterNodeConfs = new ArrayList<>();
            clusterNodeConfs.add(new AlertClusterNodeConf().setTemplateId(1L).setClusterNodeId("node1"));
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertClusterNodeConfService.class)).thenReturn(clusterNodeConfService);
            when(clusterNodeConfService.list(any())).thenReturn(clusterNodeConfs);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleItemService.class)).thenReturn(templateRuleItemService);
            List<AlertTemplateRuleItem> templateRuleItems = new ArrayList<>();
            templateRuleItems.add(new AlertTemplateRuleItem().setRuleMark("A").setKeyword("abc,efg").setBlockWord(
                "bcd").setOperate(">=").setLimitValue("5"));
            when(templateRuleItemService.list(any())).thenReturn(templateRuleItems);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(ElasticsearchProvider.class)).thenReturn(elasticsearchProvider);
            when(elasticsearchProvider.client()).thenReturn(client);
            when(client.search(any(Function.class), eq(HashMap.class))).thenReturn(mockEsResponse());
            mockedStatic.when(() ->
                SpringContextUtils.getBean(NotifyWayMapper.class)).thenReturn(notifyWayMapper);
            List<NotifyWay> notifyWays = new ArrayList<>();
            when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);

            logSchedulingRunnable.run();
            verify(scheduleService, times(2)).updateById(any());
        }
    }

    @Test
    public void testRunIsSilence() throws IOException {
        try (MockedStatic<SpringContextUtils> mockedStatic = mockStatic(SpringContextUtils.class)) {
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertScheduleService.class)).thenReturn(scheduleService);
            List<AlertSchedule> list = new ArrayList<>();
            list.add(new AlertSchedule());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRule> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRule().setTemplateId(1L).setId(1L).setRuleId(1L).setRuleExpComb("A")
                .setNotifyDuration(5).setNotifyDurationUnit(CommonConstants.DAY).setNotifyWayIds("1")
                .setIsSilence(CommonConstants.IS_SILENCE).setSilenceStartTime(LocalDateTime.now().minusHours(1))
                .setSilenceEndTime(LocalDateTime.now().plusHours(1)).setLevel(CommonConstants.WARN).setRuleContent(
                    "content"));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            testRunIsSilenceSub(mockedStatic);

            logSchedulingRunnable.run();
            verify(scheduleService, times(2)).updateById(any());
        }
    }

    @Test
    public void testRunWithSendMsg() throws IOException {
        try (MockedStatic<SpringContextUtils> mockedStatic = mockStatic(SpringContextUtils.class)) {
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertScheduleService.class)).thenReturn(scheduleService);
            List<AlertSchedule> list = new ArrayList<>();
            list.add(new AlertSchedule());
            when(scheduleService.list(any())).thenReturn(list);
            when(scheduleService.updateById(any())).thenReturn(true);
            mockedStatic.when(() ->
                SpringContextUtils.getBean(AlertTemplateRuleService.class)).thenReturn(templateRuleService);
            List<AlertTemplateRule> templateRules = new ArrayList<>();
            templateRules.add(new AlertTemplateRule().setTemplateId(1L).setId(1L).setRuleId(1L).setRuleExpComb("A")
                .setNotifyDuration(5).setNotifyDurationUnit(CommonConstants.DAY).setNotifyWayIds("1")
                .setIsSilence(CommonConstants.IS_NOT_SILENCE).setLevel(CommonConstants.WARN).setRuleContent("content"));
            when(templateRuleService.list(any())).thenReturn(templateRules);
            testRunIsSilenceSub(mockedStatic);

            // send msg test
            mockedStatic.when(() ->
                SpringContextUtils.getBean(NotifyTemplateMapper.class)).thenReturn(notifyTemplateMapper);
            when(notifyTemplateMapper.selectById(anyLong())).thenReturn(new NotifyTemplate().setNotifyTitle("title")
                .setNotifyContent("content"));
            mockedStatic.when(() ->
                SpringContextUtils.getBean(NotifyMessageMapper.class)).thenReturn(notifyMessageMapper);
            when(notifyMessageMapper.insert(any())).thenReturn(1);

            logSchedulingRunnable.run();
            verify(scheduleService, times(2)).updateById(any());
        }
    }

    private void testRunIsSilenceSub(MockedStatic<SpringContextUtils> mockedStatic) throws IOException {
        List<AlertClusterNodeConf> clusterNodeConfs = new ArrayList<>();
        clusterNodeConfs.add(new AlertClusterNodeConf().setTemplateId(1L).setClusterNodeId("node1"));
        mockedStatic.when(() ->
            SpringContextUtils.getBean(AlertClusterNodeConfService.class)).thenReturn(clusterNodeConfService);
        when(clusterNodeConfService.list(any())).thenReturn(clusterNodeConfs);
        mockedStatic.when(() ->
            SpringContextUtils.getBean(AlertTemplateRuleItemService.class)).thenReturn(templateRuleItemService);
        List<AlertTemplateRuleItem> templateRuleItems = new ArrayList<>();
        templateRuleItems.add(new AlertTemplateRuleItem().setRuleMark("A").setKeyword("abc,efg").setBlockWord(
            "bcd").setOperate(">=").setLimitValue("5"));
        when(templateRuleItemService.list(any())).thenReturn(templateRuleItems);
        mockedStatic.when(() ->
            SpringContextUtils.getBean(ElasticsearchProvider.class)).thenReturn(elasticsearchProvider);
        when(elasticsearchProvider.client()).thenReturn(client);
        when(client.search(any(Function.class), eq(HashMap.class))).thenReturn(mockEsResponse());
        mockedStatic.when(() ->
            SpringContextUtils.getBean(NotifyWayMapper.class)).thenReturn(notifyWayMapper);
        List<NotifyWay> notifyWays = mockNotifyWays();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
        mockedStatic.when(() ->
            SpringContextUtils.getBean(AlertContentParamUtil.class)).thenReturn(alertContentParamUtil);
        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("recover").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(alertContentParamUtil.setAndGetAlertContentParamDto(any(), any(), any(), any())).thenReturn(
            contentParamDto);
        mockedStatic.when(() ->
            SpringContextUtils.getBean(AlertTemplateService.class)).thenReturn(templateService);
        when(templateService.getById(anyLong())).thenReturn(new AlertTemplate().setTemplateName("name").setId(1L));
        mockedStatic.when(() ->
            SpringContextUtils.getBean(AlertRecordMapper.class)).thenReturn(recordMapper);
        when(recordMapper.insert(any())).thenReturn(1);
    }

    private List<NotifyWay> mockNotifyWays() {
        List<NotifyWay> notifyWays = new ArrayList<>();
        notifyWays.add(new NotifyWay().setId(1L).setName("name").setNotifyType(CommonConstants.EMAIL)
            .setEmail("email").setNotifyTemplateId(1L));
        notifyWays.add(new NotifyWay().setId(1L).setName("name").setNotifyType(CommonConstants.WE_COM)
            .setSendWay(CommonConstants.APP_SEND_WAY).setDeptId("1").setPersonId("1").setNotifyTemplateId(1L));
        notifyWays.add(new NotifyWay().setId(1L).setName("name").setNotifyType(CommonConstants.WE_COM)
            .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook").setNotifyTemplateId(1L));
        notifyWays.add(new NotifyWay().setId(1L).setName("name").setNotifyType(CommonConstants.DING_TALK)
            .setSendWay(CommonConstants.APP_SEND_WAY).setDeptId("1").setPersonId("1").setNotifyTemplateId(1L));
        notifyWays.add(new NotifyWay().setId(1L).setName("name").setNotifyType(CommonConstants.DING_TALK)
            .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook").setSign("sign").setNotifyTemplateId(1L));
        notifyWays.add(new NotifyWay().setId(1L).setName("name").setNotifyType(CommonConstants.DING_TALK)
            .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook").setSign("").setNotifyTemplateId(1L));
        notifyWays.add(new NotifyWay().setId(1L).setName("name").setNotifyType(CommonConstants.WEBHOOK)
            .setWebhook("webhook").setNotifyTemplateId(1L));
        notifyWays.add(new NotifyWay().setId(1L).setName("name").setNotifyType(CommonConstants.WEBHOOK)
            .setWebhook("webhook").setHeader("{\"a\":\"b\"}").setParams("{\"k\":\"v\"}").setBody("body")
            .setResultCode("{\"errCode\":0}").setNotifyTemplateId(1L));
        notifyWays.add(new NotifyWay().setId(1L).setName("name").setNotifyType(CommonConstants.SNMP)
            .setSnmpIp("127.0.0.1").setSnmpPort("162").setSnmpVersion(SnmpConstants.version3).setSnmpCommunity(
                "community").setSnmpOid("oid").setSnmpUsername("user").setSnmpAuthPasswd("123").setSnmpPrivPasswd(
                "1234").setNotifyTemplateId(1L));
        return notifyWays;
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

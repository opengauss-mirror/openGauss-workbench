/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertContentParamDto;
import com.nctigba.alert.monitor.entity.AlertRecord;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
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
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *  AlertApiService function test
 *
 * @since 2023/7/15 18:42
 */

@RunWith(SpringRunner.class)
public class AlertApiServiceTest {
    @InjectMocks
    private AlertApiService alertApiService;
    @Mock
    private AlertRecordMapper alertRecordMapper;
    @Mock
    private AlertTemplateMapper templateMapper;
    @Mock
    private AlertTemplateRuleMapper templateRuleMapper;
    @Mock
    private NotifyWayMapper notifyWayMapper;
    @Mock
    private NotifyTemplateMapper notifyTemplateMapper;
    @Mock
    private NotifyMessageMapper notifyMessageMapper;
    @Mock
    private AlertContentParamUtil contentParamUtil;
    @Mock
    private AlertRecordService recordService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAlertsNull() {
        AlertTemplateRule alertTemplateRule = null;
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = new AlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertLabels labels = new AlertLabels();
        labels.setTemplateRuleId(1L);
        alertApiReq.setLabels(labels);
        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
    }

    @Test
    public void testAlertsWithNullNotifyWays() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule();
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(contentParamUtil.setAndGetAlertContentParamDto(anyString(), any(), anyString(), anyString()))
            .thenReturn(contentParamDto);

        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);
        alertApiService.alerts(alertApiReqList);
    }

    @Test
    public void testAlertsWithoutRecord() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
                .setIsRepeat(CommonConstants.IS_NOT_REPEAT).setRuleContent("content")
                .setIsSilence(CommonConstants.IS_NOT_SILENCE);
            when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
            List<AlertApiReq> alertApiReqList = new ArrayList<>();
            AlertApiReq alertApiReq = getAlertApiReq();
            alertApiReqList.add(alertApiReq);
            AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
                .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
                .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
            when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
                alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
                .thenReturn(contentParamDto);
            List<NotifyWay> notifyWays = mockNotifyWays();
            when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
            List<AlertRecord> alertRecords = new ArrayList<>();
            when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
            AlertTemplate alertTemplate = new AlertTemplate().setId(1L).setTemplateName("template");
            when(templateMapper.selectById(anyLong())).thenReturn(alertTemplate);
            when(recordService.saveOrUpdate(any(AlertRecord.class))).thenReturn(true);
            when(notifyMessageMapper.selectCount(any())).thenReturn(0L);
            NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyTitle("title").setNotifyContent("content");
            when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
            when(notifyMessageMapper.insert(any())).thenReturn(1);
            when(recordService.updateById(any())).thenReturn(true);

            alertApiService.alerts(alertApiReqList);
        }
    }

    @Test
    public void testAlertsHasRecord() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
                .setIsRepeat(CommonConstants.IS_REPEAT).setRuleContent("content")
                .setIsSilence(CommonConstants.IS_NOT_SILENCE);
            when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
            List<AlertApiReq> alertApiReqList = new ArrayList<>();
            AlertApiReq alertApiReq = getAlertApiReq();
            alertApiReqList.add(alertApiReq);
            AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
                .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
                .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
            when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
                alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
                .thenReturn(contentParamDto);
            List<NotifyWay> notifyWays = mockNotifyWays();
            when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
            List<AlertRecord> alertRecords = new ArrayList<>();
            alertRecords.add(new AlertRecord().setClusterNodeId(alertApiReq.getLabels().getInstance()).setTemplateId(1L)
                .setTemplateRuleId(alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
                .setTemplateName("template").setTemplateRuleName(alertTemplateRule.getRuleName())
                .setAlertStatus(CommonConstants.FIRING_STATUS).setTemplateRuleType(alertTemplateRule.getRuleType())
                .setLevel(alertTemplateRule.getLevel()).setAlertContent(contentParamDto.getContent())
                .setRecordStatus(CommonConstants.UNREAD_STATUS).setSendTime(LocalDateTime.now().minusHours(1))
                .setSendCount(10).setEndTime(LocalDateTime.now().minusHours(1))
                .setStartTime(LocalDateTime.now().minusHours(2)));
            when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
            when(recordService.saveOrUpdate(any(AlertRecord.class))).thenReturn(true);
            when(notifyMessageMapper.selectCount(any())).thenReturn(0L);
            NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyTitle("title").setNotifyContent("content");
            when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
            when(notifyMessageMapper.insert(any())).thenReturn(1);
            when(recordService.updateById(any())).thenReturn(true);

            alertApiService.alerts(alertApiReqList);
        }
    }

    @Test
    public void testAlertsHasRecordIsRecover() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
            .setIsRepeat(CommonConstants.IS_REPEAT).setRuleContent("content")
            .setIsSilence(CommonConstants.IS_NOT_SILENCE);
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
            alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
            .thenReturn(contentParamDto);
        List<NotifyWay> notifyWays = mockNotifyWays();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
        List<AlertRecord> alertRecords = new ArrayList<>();
        alertRecords.add(new AlertRecord().setClusterNodeId(alertApiReq.getLabels().getInstance()).setTemplateId(1L)
            .setTemplateRuleId(alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
            .setTemplateName("template").setTemplateRuleName(alertTemplateRule.getRuleName())
            .setAlertStatus(CommonConstants.FIRING_STATUS).setTemplateRuleType(alertTemplateRule.getRuleType())
            .setLevel(alertTemplateRule.getLevel()).setAlertContent(contentParamDto.getContent())
            .setRecordStatus(CommonConstants.UNREAD_STATUS).setSendTime(LocalDateTime.now().minusHours(1))
            .setSendCount(10).setEndTime(LocalDateTime.now().plusMinutes(10))
            .setStartTime(LocalDateTime.now().minusHours(2)));
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(recordService.saveOrUpdate(any(AlertRecord.class))).thenReturn(true);

        alertApiService.alerts(alertApiReqList);
    }

    @Test
    public void testAlertsHasRecovered() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
            .setIsRepeat(CommonConstants.IS_REPEAT).setRuleContent("content")
            .setIsSilence(CommonConstants.IS_NOT_SILENCE);
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
            alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
            .thenReturn(contentParamDto);
        List<NotifyWay> notifyWays = mockNotifyWays();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
        List<AlertRecord> alertRecords = new ArrayList<>();
        alertRecords.add(new AlertRecord().setClusterNodeId(alertApiReq.getLabels().getInstance()).setTemplateId(1L)
            .setTemplateRuleId(alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
            .setTemplateName("template").setTemplateRuleName(alertTemplateRule.getRuleName())
            .setAlertStatus(CommonConstants.RECOVER_STATUS).setTemplateRuleType(alertTemplateRule.getRuleType())
            .setLevel(alertTemplateRule.getLevel()).setAlertContent(contentParamDto.getContent())
            .setRecordStatus(CommonConstants.UNREAD_STATUS).setSendTime(LocalDateTime.now().minusHours(1))
            .setSendCount(10).setEndTime(LocalDateTime.now().plusMinutes(10))
            .setStartTime(LocalDateTime.now().minusHours(2)));
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);

        alertApiService.alerts(alertApiReqList);
    }

    @Test
    public void testAlertsIsRead() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
            .setIsRepeat(CommonConstants.IS_REPEAT).setRuleContent("content")
            .setIsSilence(CommonConstants.IS_NOT_SILENCE);
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
            alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
            .thenReturn(contentParamDto);
        List<NotifyWay> notifyWays = mockNotifyWays();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
        List<AlertRecord> alertRecords = new ArrayList<>();
        alertRecords.add(new AlertRecord().setClusterNodeId(alertApiReq.getLabels().getInstance()).setTemplateId(1L)
            .setTemplateRuleId(alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
            .setTemplateName("template").setTemplateRuleName(alertTemplateRule.getRuleName())
            .setAlertStatus(CommonConstants.FIRING_STATUS).setTemplateRuleType(alertTemplateRule.getRuleType())
            .setLevel(alertTemplateRule.getLevel()).setAlertContent(contentParamDto.getContent())
            .setRecordStatus(CommonConstants.READ_STATUS).setSendTime(LocalDateTime.now().minusHours(1))
            .setSendCount(10).setEndTime(LocalDateTime.now().minusHours(1))
            .setStartTime(LocalDateTime.now().minusHours(2)));
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(recordService.saveOrUpdate(any(AlertRecord.class))).thenReturn(true);

        alertApiService.alerts(alertApiReqList);
    }

    @Test
    public void testAlertsWithoutAlertNotify() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("")
            .setIsRepeat(CommonConstants.IS_REPEAT).setRuleContent("content")
            .setIsSilence(CommonConstants.IS_NOT_SILENCE);
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
            alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
            .thenReturn(contentParamDto);
        List<NotifyWay> notifyWays = mockNotifyWays();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
        List<AlertRecord> alertRecords = new ArrayList<>();
        alertRecords.add(new AlertRecord().setClusterNodeId(alertApiReq.getLabels().getInstance()).setTemplateId(1L)
            .setTemplateRuleId(alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
            .setTemplateName("template").setTemplateRuleName(alertTemplateRule.getRuleName())
            .setAlertStatus(CommonConstants.FIRING_STATUS).setTemplateRuleType(alertTemplateRule.getRuleType())
            .setLevel(alertTemplateRule.getLevel()).setAlertContent(contentParamDto.getContent())
            .setRecordStatus(CommonConstants.UNREAD_STATUS).setSendTime(LocalDateTime.now().minusHours(1))
            .setSendCount(10).setEndTime(LocalDateTime.now().minusHours(1))
            .setStartTime(LocalDateTime.now().minusHours(2)));
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(recordService.saveOrUpdate(any(AlertRecord.class))).thenReturn(true);

        alertApiService.alerts(alertApiReqList);
    }

    @Test
    public void testAlertsIsNotRepeat() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
            .setIsRepeat(CommonConstants.IS_NOT_REPEAT).setRuleContent("content")
            .setIsSilence(CommonConstants.IS_NOT_SILENCE);
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
            alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
            .thenReturn(contentParamDto);
        List<NotifyWay> notifyWays = mockNotifyWays();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
        List<AlertRecord> alertRecords = new ArrayList<>();
        alertRecords.add(new AlertRecord().setClusterNodeId(alertApiReq.getLabels().getInstance()).setTemplateId(1L)
            .setTemplateRuleId(alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
            .setTemplateName("template").setTemplateRuleName(alertTemplateRule.getRuleName())
            .setAlertStatus(CommonConstants.FIRING_STATUS).setTemplateRuleType(alertTemplateRule.getRuleType())
            .setLevel(alertTemplateRule.getLevel()).setAlertContent(contentParamDto.getContent())
            .setRecordStatus(CommonConstants.UNREAD_STATUS).setSendTime(LocalDateTime.now().minusHours(1))
            .setSendCount(10).setEndTime(LocalDateTime.now().minusHours(1))
            .setStartTime(LocalDateTime.now().minusHours(2)));
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(recordService.saveOrUpdate(any(AlertRecord.class))).thenReturn(true);
        when(notifyMessageMapper.selectCount(any())).thenReturn(1L);

        alertApiService.alerts(alertApiReqList);
    }

    @Test
    public void testAlertsOverMaxRepeat() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
            .setIsRepeat(CommonConstants.IS_REPEAT).setRuleContent("content")
            .setIsSilence(CommonConstants.IS_NOT_SILENCE).setMaxRepeatCount(5);
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
            alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
            .thenReturn(contentParamDto);
        List<NotifyWay> notifyWays = mockNotifyWays();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
        List<AlertRecord> alertRecords = new ArrayList<>();
        alertRecords.add(new AlertRecord().setClusterNodeId(alertApiReq.getLabels().getInstance()).setTemplateId(1L)
            .setTemplateRuleId(alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
            .setTemplateName("template").setTemplateRuleName(alertTemplateRule.getRuleName())
            .setAlertStatus(CommonConstants.FIRING_STATUS).setTemplateRuleType(alertTemplateRule.getRuleType())
            .setLevel(alertTemplateRule.getLevel()).setAlertContent(contentParamDto.getContent())
            .setRecordStatus(CommonConstants.UNREAD_STATUS).setSendTime(LocalDateTime.now().minusHours(1))
            .setSendCount(10).setEndTime(LocalDateTime.now().minusHours(1))
            .setStartTime(LocalDateTime.now().minusHours(2)));
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(recordService.saveOrUpdate(any(AlertRecord.class))).thenReturn(true);
        when(notifyMessageMapper.selectCount(any())).thenReturn(0L);
        NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyTitle("title").setNotifyContent("content");
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        when(notifyMessageMapper.insert(any())).thenReturn(1);
        when(recordService.updateById(any())).thenReturn(true);

        alertApiService.alerts(alertApiReqList);
    }

    @Test
    public void testAlertsIsNotNextRepeat() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
            .setIsRepeat(CommonConstants.IS_REPEAT).setRuleContent("content").setMaxRepeatCount(15).setNextRepeat(2)
            .setIsSilence(CommonConstants.IS_NOT_SILENCE).setNextRepeatUnit(CommonConstants.HOUR);
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
            alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
            .thenReturn(contentParamDto);
        List<NotifyWay> notifyWays = mockNotifyWays();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
        List<AlertRecord> alertRecords = new ArrayList<>();
        alertRecords.add(new AlertRecord().setClusterNodeId(alertApiReq.getLabels().getInstance()).setTemplateId(1L)
            .setTemplateRuleId(alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
            .setTemplateName("template").setTemplateRuleName(alertTemplateRule.getRuleName())
            .setAlertStatus(CommonConstants.FIRING_STATUS).setTemplateRuleType(alertTemplateRule.getRuleType())
            .setLevel(alertTemplateRule.getLevel()).setAlertContent(contentParamDto.getContent())
            .setRecordStatus(CommonConstants.UNREAD_STATUS).setSendTime(LocalDateTime.now().minusHours(1))
            .setSendCount(10).setEndTime(LocalDateTime.now().minusHours(1))
            .setStartTime(LocalDateTime.now().minusHours(2)));
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(recordService.saveOrUpdate(any(AlertRecord.class))).thenReturn(true);
        when(notifyMessageMapper.selectCount(any())).thenReturn(0L);
        NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyTitle("title").setNotifyContent("content");
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        when(notifyMessageMapper.insert(any())).thenReturn(1);
        when(recordService.updateById(any())).thenReturn(true);

        alertApiService.alerts(alertApiReqList);
    }

    @Test
    public void testAlertsIsSilence() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
            .setIsRepeat(CommonConstants.IS_REPEAT).setRuleContent("content")
            .setIsSilence(CommonConstants.IS_SILENCE).setSilenceStartTime(LocalDateTime.now().minusHours(10))
            .setSilenceEndTime(LocalDateTime.now().plusHours(10));
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertContentParamDto contentParamDto = new AlertContentParamDto().setNodeName("nodeName").setHostIp("ip")
            .setLevel(CommonConstants.WARN).setHostname("centos").setPort("8080")
            .setAlertStatus("firing").setAlertTime("2023-08-27 22:19:01").setContent("content");
        when(contentParamUtil.setAndGetAlertContentParamDto(alertApiReq.getLabels().getInstance(),
            alertApiReq.getStartsAt(), alertApiReq.getLabels().getLevel(), alertTemplateRule.getRuleContent()))
            .thenReturn(contentParamDto);
        List<NotifyWay> notifyWays = mockNotifyWays();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);
        List<AlertRecord> alertRecords = new ArrayList<>();
        alertRecords.add(new AlertRecord().setClusterNodeId(alertApiReq.getLabels().getInstance()).setTemplateId(1L)
            .setTemplateRuleId(alertTemplateRule.getId()).setCreateTime(LocalDateTime.now())
            .setTemplateName("template").setTemplateRuleName(alertTemplateRule.getRuleName())
            .setAlertStatus(CommonConstants.FIRING_STATUS).setTemplateRuleType(alertTemplateRule.getRuleType())
            .setLevel(alertTemplateRule.getLevel()).setAlertContent(contentParamDto.getContent())
            .setRecordStatus(CommonConstants.UNREAD_STATUS).setSendTime(LocalDateTime.now().minusHours(1))
            .setSendCount(10).setEndTime(LocalDateTime.now().minusHours(1))
            .setStartTime(LocalDateTime.now().minusHours(2)));
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(recordService.saveOrUpdate(any(AlertRecord.class))).thenReturn(true);
        when(notifyMessageMapper.selectCount(any())).thenReturn(0L);
        NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyTitle("title").setNotifyContent("content");
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        when(notifyMessageMapper.insert(any())).thenReturn(1);
        when(recordService.updateById(any())).thenReturn(true);

        alertApiService.alerts(alertApiReqList);
    }

    private AlertApiReq getAlertApiReq() {
        AlertApiReq alertApiReq = new AlertApiReq();
        alertApiReq.setStartsAt(LocalDateTime.now().minusHours(1));
        alertApiReq.setEndsAt(LocalDateTime.now());
        AlertLabels labels = new AlertLabels();
        labels.setLevel("warn");
        labels.setTemplateId(1L);
        labels.setInstance("node1");
        labels.setTemplateRuleId(1L);
        labels.setAlertname("name");
        alertApiReq.setLabels(labels);
        return alertApiReq;
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
}

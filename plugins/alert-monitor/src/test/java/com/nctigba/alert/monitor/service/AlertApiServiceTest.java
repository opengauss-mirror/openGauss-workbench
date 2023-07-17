/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.nctigba.alert.monitor.constant.CommonConstants;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    private IOpsClusterService clusterService;
    @Mock
    private IOpsClusterNodeService clusterNodeService;
    @Mock
    private HostFacade hostFacade;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAlerts1() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = new AlertApiReq();
        alertApiReqList.add(alertApiReq);
        AlertLabels labels = new AlertLabels();
        labels.setTemplateRuleId(1L);
        alertApiReq.setLabels(labels);

        AlertTemplateRule alertTemplateRule = null;
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
    }

    @Test(expected = ServiceException.class)
    public void testAlerts2() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule = new AlertTemplateRule();
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        OpsClusterNodeEntity opsClusterNodeEntity = null;
        when(clusterNodeService.getById(anyLong())).thenReturn(opsClusterNodeEntity);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(clusterNodeService, times(1)).getById(anyLong());
    }

    @Test(expected = ServiceException.class)
    public void testAlerts3() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1");
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        List<NotifyWay> notifyWays = new ArrayList<>();
        NotifyWay notifyWay = new NotifyWay().setId(1L).setName("notifyName");
        notifyWays.add(notifyWay);
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);

        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId("node3");
        opsClusterNodeEntity.setHostId("1");
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);

        OpsHostEntity opsHost = null;
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(notifyWayMapper, times(1)).selectById(anyLong());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(hostFacade, times(1)).getById(opsClusterNodeEntity.getHostId());
    }

    @Test(expected = ServiceException.class)
    public void testAlerts4() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1");
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        List<NotifyWay> notifyWays = new ArrayList<>();
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);

        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId("node3");
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setClusterId("test");
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);

        OpsHostEntity opsHost = new OpsHostEntity();
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);
        OpsClusterEntity opsClusterEntity = null;
        when(clusterService.getById(opsClusterNodeEntity.getClusterId())).thenReturn(opsClusterEntity);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(notifyWayMapper, times(1)).selectById(anyLong());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(hostFacade, times(1)).getById(opsClusterNodeEntity.getHostId());
        verify(clusterService, times(1)).getById(opsClusterNodeEntity.getClusterId());
    }

    @Test
    public void testAlerts5() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule =
            new AlertTemplateRule().setNotifyWayIds("1").setRuleContent("content").setRuleName("ruleName")
                .setRuleType("index").setLevel("warn").setNotifyWayIds("1").setIsRepeat(CommonConstants.IS_NOT_REPEAT)
                .setAlertNotify("firing");
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        List<NotifyWay> notifyWays = new ArrayList<>();
        NotifyWay notifyWay = new NotifyWay().setId(1L).setName("notifyName");
        notifyWays.add(notifyWay);
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);

        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId("node3");
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setClusterId("test");
        opsClusterNodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);

        OpsHostEntity opsHost = new OpsHostEntity();
        opsHost.setPublicIp("127.0.0.1");
        opsHost.setHostname("centos");
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        when(clusterService.getById(opsClusterNodeEntity.getClusterId())).thenReturn(opsClusterEntity);
        List<AlertRecord> alertRecords = new ArrayList<>();
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);

        AlertTemplate alertTemplate = new AlertTemplate().setTemplateName("name").setId(1L);
        when(templateMapper.selectById(anyLong())).thenReturn(alertTemplate);
        when(alertRecordMapper.insert(any(AlertRecord.class))).thenReturn(1);
        when(notifyMessageMapper.selectCount(any())).thenReturn(1L);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(notifyWayMapper, times(1)).selectBatchIds(anyList());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(hostFacade, times(1)).getById(opsClusterNodeEntity.getHostId());
        verify(clusterService, times(1)).getById(opsClusterNodeEntity.getClusterId());
        verify(alertRecordMapper, times(1)).selectList(any());
        verify(templateMapper, times(1)).selectById(anyLong());
        verify(alertRecordMapper, times(1)).insert(any(AlertRecord.class));
        verify(notifyMessageMapper, times(1)).selectCount(any());
    }

    @Test
    public void testAlerts6() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1");
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        List<NotifyWay> notifyWays = new ArrayList<>();
        NotifyWay notifyWay = new NotifyWay().setId(1L).setName("notifyName");
        notifyWays.add(notifyWay);
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);

        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId("node3");
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setClusterId("test");
        opsClusterNodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);

        OpsHostEntity opsHost = new OpsHostEntity();
        opsHost.setPublicIp("127.0.0.1");
        opsHost.setHostname("centos");
        opsHost.setPort(22);
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(80);
        when(clusterService.getById(opsClusterNodeEntity.getClusterId())).thenReturn(opsClusterEntity);

        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord = new AlertRecord().setAlertStatus(CommonConstants.RECOVER_STATUS);
        alertRecords.add(alertRecord);
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(notifyWayMapper, times(1)).selectBatchIds(anyList());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(hostFacade, times(1)).getById(opsClusterNodeEntity.getHostId());
        verify(clusterService, times(1)).getById(opsClusterNodeEntity.getClusterId());
        verify(alertRecordMapper, times(1)).selectList(any());
    }

    @Test
    public void testAlerts7() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing");
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        List<NotifyWay> notifyWays = new ArrayList<>();
        NotifyWay notifyWay = new NotifyWay().setId(1L).setName("notifyName");
        notifyWays.add(notifyWay);
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);

        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId("node3");
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setClusterId("test");
        opsClusterNodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);

        OpsHostEntity opsHost = new OpsHostEntity();
        opsHost.setPublicIp("127.0.0.1");
        opsHost.setHostname("centos");
        opsHost.setPort(22);
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(80);
        when(clusterService.getById(opsClusterNodeEntity.getClusterId())).thenReturn(opsClusterEntity);

        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord = new AlertRecord().setAlertStatus(CommonConstants.RECOVER_STATUS);
        alertRecords.add(alertRecord);
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(notifyWayMapper, times(1)).selectBatchIds(anyList());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(hostFacade, times(1)).getById(opsClusterNodeEntity.getHostId());
        verify(clusterService, times(1)).getById(opsClusterNodeEntity.getClusterId());
        verify(alertRecordMapper, times(1)).selectList(any());
    }

    @Test
    public void testAlerts8() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule =
            new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing");
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        setCommon();

        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord =
            new AlertRecord().setAlertStatus(CommonConstants.FIRING_STATUS).setEndTime(alertApiReq.getEndsAt())
                .setStartTime(alertApiReq.getStartsAt()).setId(1L);
        alertRecords.add(alertRecord);
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(alertRecordMapper.updateById(alertRecord)).thenReturn(1);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(notifyWayMapper, times(1)).selectBatchIds(anyList());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(alertRecordMapper, times(1)).selectList(any());
        verify(alertRecordMapper, times(1)).updateById(alertRecord);
    }

    @Test
    public void testAlerts9() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReq.setEndsAt(LocalDateTime.now().plusHours(1));
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule =
            new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing");
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        setCommon();

        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord =
            new AlertRecord().setAlertStatus(CommonConstants.FIRING_STATUS)
                .setEndTime(alertApiReq.getEndsAt().plusHours(1))
                .setStartTime(alertApiReq.getStartsAt()).setId(1L);
        alertRecords.add(alertRecord);
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(alertRecordMapper.updateById(alertRecord)).thenReturn(1);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(notifyWayMapper, times(1)).selectBatchIds(anyList());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(alertRecordMapper, times(1)).selectList(any());
        verify(alertRecordMapper, times(1)).updateById(alertRecord);
    }

    @Test
    public void testAlerts10() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule =
            new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
                .setIsRepeat(CommonConstants.IS_NOT_REPEAT).setIsSilence(CommonConstants.IS_SILENCE)
                .setSilenceStartTime(LocalDateTime.now().minusHours(1))
                .setSilenceEndTime(LocalDateTime.now().plusHours(1));
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        setCommon();

        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord =
            new AlertRecord().setAlertStatus(CommonConstants.FIRING_STATUS)
                .setEndTime(alertApiReq.getEndsAt().minusMinutes(30))
                .setStartTime(alertApiReq.getStartsAt()).setId(1L);
        alertRecords.add(alertRecord);
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(alertRecordMapper.updateById(alertRecord)).thenReturn(1);
        when(notifyMessageMapper.selectCount(any())).thenReturn(0L);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(notifyWayMapper, times(1)).selectBatchIds(anyList());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(alertRecordMapper, times(1)).selectList(any());
        verify(alertRecordMapper, times(1)).updateById(alertRecord);
        verify(notifyMessageMapper, times(1)).selectCount(any());
    }

    @Test
    public void testAlerts11() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule =
            new AlertTemplateRule().setAlertNotify("firing")
                .setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(CommonConstants.IS_NOT_SILENCE);
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        setCommon();

        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord =
            new AlertRecord().setAlertStatus(CommonConstants.FIRING_STATUS)
                .setEndTime(alertApiReq.getEndsAt().minusMinutes(30))
                .setStartTime(alertApiReq.getStartsAt()).setId(1L);
        alertRecords.add(alertRecord);
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(alertRecordMapper.updateById(alertRecord)).thenReturn(1);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(alertRecordMapper, times(1)).selectList(any());
        verify(alertRecordMapper, times(1)).updateById(alertRecord);
    }

    @Test
    public void testAlerts12() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule =
            new AlertTemplateRule().setNotifyWayIds("1").setAlertNotify("firing")
                .setIsRepeat(CommonConstants.IS_NOT_REPEAT).setIsSilence(CommonConstants.IS_NOT_SILENCE)
                .setSilenceStartTime(LocalDateTime.now().minusHours(1))
                .setSilenceEndTime(LocalDateTime.now().plusHours(1));
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        setCommon();

        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord =
            new AlertRecord().setAlertStatus(CommonConstants.FIRING_STATUS)
                .setEndTime(alertApiReq.getEndsAt().minusMinutes(30))
                .setStartTime(alertApiReq.getStartsAt()).setId(1L);
        alertRecords.add(alertRecord);
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(alertRecordMapper.updateById(alertRecord)).thenReturn(1);

        NotifyWay notifyWay = new NotifyWay().setNotifyTemplateId(1L);
        when(notifyWayMapper.selectById(any())).thenReturn(notifyWay);
        NotifyTemplate notifyTemplate = new NotifyTemplate();
        notifyTemplate.setNotifyContent("notifyContent").setId(1L);
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        when(notifyMessageMapper.insert(any(NotifyMessage.class))).thenReturn(1);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(notifyWayMapper, times(1)).selectBatchIds(anyList());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(alertRecordMapper, times(1)).selectList(any());
        verify(alertRecordMapper, times(1)).updateById(alertRecord);
        verify(notifyWayMapper, times(1)).selectById(any());
        verify(notifyTemplateMapper, times(1)).selectById(anyLong());
        verify(notifyMessageMapper, times(1)).insert(any(NotifyMessage.class));
    }

    @Test
    public void testAlerts13() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule =
            new AlertTemplateRule().setAlertNotify("firing")
                .setIsRepeat(CommonConstants.IS_NOT_REPEAT).setIsSilence(CommonConstants.IS_SILENCE)
                .setSilenceStartTime(LocalDateTime.now().plusMinutes(30))
                .setSilenceEndTime(LocalDateTime.now().plusHours(1));
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        setCommon();

        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord =
            new AlertRecord().setAlertStatus(CommonConstants.FIRING_STATUS)
                .setEndTime(alertApiReq.getEndsAt().minusMinutes(30))
                .setStartTime(alertApiReq.getStartsAt()).setId(1L);
        alertRecords.add(alertRecord);
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(alertRecordMapper.updateById(alertRecord)).thenReturn(1);
        when(notifyMessageMapper.selectCount(any())).thenReturn(0L);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(alertRecordMapper, times(1)).selectList(any());
        verify(alertRecordMapper, times(1)).updateById(alertRecord);
        verify(notifyMessageMapper, times(1)).selectCount(any());
    }

    @Test
    public void testAlerts14() {
        List<AlertApiReq> alertApiReqList = new ArrayList<>();
        AlertApiReq alertApiReq = getAlertApiReq();
        alertApiReqList.add(alertApiReq);

        AlertTemplateRule alertTemplateRule =
            new AlertTemplateRule().setAlertNotify("firing")
                .setIsRepeat(CommonConstants.IS_NOT_REPEAT).setIsSilence(CommonConstants.IS_SILENCE)
                .setSilenceStartTime(LocalDateTime.now().minusHours(2))
                .setSilenceEndTime(LocalDateTime.now().minusHours(1));
        when(templateRuleMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        setCommon();

        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord =
            new AlertRecord().setAlertStatus(CommonConstants.FIRING_STATUS)
                .setEndTime(alertApiReq.getEndsAt().minusMinutes(30))
                .setStartTime(alertApiReq.getStartsAt()).setId(1L);
        alertRecords.add(alertRecord);
        when(alertRecordMapper.selectList(any())).thenReturn(alertRecords);
        when(alertRecordMapper.updateById(alertRecord)).thenReturn(1);
        when(notifyMessageMapper.selectCount(any())).thenReturn(0L);

        alertApiService.alerts(alertApiReqList);
        verify(templateRuleMapper, times(1)).selectById(anyLong());
        verify(clusterNodeService, times(1)).getById(anyString());
        verify(alertRecordMapper, times(1)).selectList(any());
        verify(alertRecordMapper, times(1)).updateById(alertRecord);
        verify(notifyMessageMapper, times(1)).selectCount(any());
    }

    private void setCommon() {
        List<NotifyWay> notifyWays = new ArrayList<>();
        NotifyWay notifyWay = new NotifyWay().setId(1L).setName("notifyName");
        notifyWays.add(notifyWay);
        when(notifyWayMapper.selectBatchIds(anyList())).thenReturn(notifyWays);

        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId("node3");
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setClusterId("test");
        opsClusterNodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);

        OpsHostEntity opsHost = new OpsHostEntity();
        opsHost.setPublicIp("127.0.0.1");
        opsHost.setHostname("centos");
        opsHost.setPort(22);
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(80);
        when(clusterService.getById(opsClusterNodeEntity.getClusterId())).thenReturn(opsClusterEntity);
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
}

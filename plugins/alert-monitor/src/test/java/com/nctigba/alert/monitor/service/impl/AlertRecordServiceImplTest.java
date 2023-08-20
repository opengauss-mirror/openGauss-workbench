/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertRecordDto;
import com.nctigba.alert.monitor.dto.AlertRelationDto;
import com.nctigba.alert.monitor.dto.AlertStatisticsDto;
import com.nctigba.alert.monitor.entity.AlertRecord;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.entity.NctigbaEnv;
import com.nctigba.alert.monitor.mapper.AlertRecordMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.model.AlertRecordReq;
import com.nctigba.alert.monitor.model.AlertStatisticsReq;
import com.nctigba.alert.monitor.service.PrometheusService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the AlertRecordService
 *
 * @since 2023/7/10 08:41
 */
@RunWith(SpringRunner.class)
public class AlertRecordServiceImplTest {
    @Mock
    private IOpsClusterService clusterService;

    @Mock
    private IOpsClusterNodeService clusterNodeService;

    @Mock
    private HostFacade hostFacade;

    @Mock
    private AlertTemplateRuleItemMapper templateRuleItemMapper;

    @Mock
    private PrometheusService prometheusService;

    @Mock
    private NctigbaEnvMapper envMapper;

    @InjectMocks
    private AlertRecordServiceImpl alertRecordService;

    @Mock
    private AlertRecordMapper baseMapper;


    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), AlertRecord.class);
    }

    @Test
    public void testGetListPageNull() {
        AlertRecordReq alertRecordReq = new AlertRecordReq();
        alertRecordReq.setClusterNodeId("node123");
        Page<AlertRecord> page = new Page<>(1, 10);
        when(baseMapper.selectPage(any(Page.class), any())).thenReturn(page);
        Page<AlertRecordDto> listPage = alertRecordService.getListPage(alertRecordReq, page);
        assertEquals(0, listPage.getTotal());
        assertEquals(page.getRecords(), listPage.getRecords());
    }

    @Test
    public void testGetListPage() {
        OpsClusterNodeEntity opsClusterNode = new OpsClusterNodeEntity();
        opsClusterNode.setClusterNodeId("node123");
        opsClusterNode.setClusterId("test");
        opsClusterNode.setHostId("1");
        opsClusterNode.setClusterRole(ClusterRoleEnum.MASTER);
        List<OpsClusterNodeEntity> opsClusterNodeEntities = new ArrayList<>();
        opsClusterNodeEntities.add(opsClusterNode);
        OpsHostEntity opsHostEntity = new OpsHostEntity();
        opsHostEntity.setHostId("1");
        opsHostEntity.setPublicIp("127.0.0.1");
        List<OpsHostEntity> opsHostEntities = new ArrayList<>();
        opsHostEntities.add(opsHostEntity);
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(80);
        List<OpsClusterEntity> opsClusterEntities = new ArrayList<>();
        opsClusterEntities.add(opsClusterEntity);
        // Mock dependencies
        when(clusterNodeService.listByIds(anyList())).thenReturn(opsClusterNodeEntities);
        when(hostFacade.listByIds(anyList())).thenReturn(opsHostEntities);
        when(clusterService.listByIds(anyList())).thenReturn(opsClusterEntities);
        // Mock database query result
        List<AlertRecord> records = new ArrayList<>();
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
            .setStartTime(LocalDateTime.parse("2022-01-01 01:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .setEndTime(LocalDateTime.parse("2022-01-01 02:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        records.add(alertRecord);
        Page<AlertRecord> page = new Page<>(1, 10);
        page.setRecords(records);
        page.setTotal(1);
        when(baseMapper.selectPage(any(Page.class), any())).thenReturn(page);
        // Act
        AlertRecordReq alertRecordReq = new AlertRecordReq();
        Page pageReq = new Page(1, 10);
        Page<AlertRecordDto> result = alertRecordService.getListPage(alertRecordReq, pageReq);
        // Assert
        assertEquals(1, result.getTotal());
        AlertRecordDto dto = result.getRecords().get(0);
        assertEquals(alertRecord.getId(), dto.getId());
        assertEquals("node123", dto.getClusterNodeId());
        assertEquals("test", dto.getClusterId());
        assertEquals("127.0.0.1:80", dto.getHostIpAndPort());
        assertEquals("MASTER", dto.getNodeRole());
        assertEquals("test/127.0.0.1:80(MASTER)", dto.getClusterNodeName());
    }

    @Test
    public void testGetListPage2() {
        OpsClusterNodeEntity opsClusterNode = new OpsClusterNodeEntity();
        opsClusterNode.setClusterNodeId("node123");
        opsClusterNode.setClusterId("test");
        opsClusterNode.setHostId("1");
        opsClusterNode.setClusterRole(ClusterRoleEnum.MASTER);
        List<OpsClusterNodeEntity> opsClusterNodeEntities = new ArrayList<>();
        opsClusterNodeEntities.add(opsClusterNode);
        OpsHostEntity opsHostEntity = new OpsHostEntity();
        opsHostEntity.setHostId("1");
        opsHostEntity.setPublicIp("127.0.0.1");
        List<OpsHostEntity> opsHostEntities = new ArrayList<>();
        opsHostEntities.add(opsHostEntity);
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(80);
        List<OpsClusterEntity> opsClusterEntities = new ArrayList<>();
        opsClusterEntities.add(opsClusterEntity);
        // Mock dependencies
        when(clusterNodeService.listByIds(anyList())).thenReturn(opsClusterNodeEntities);
        when(hostFacade.listByIds(anyList())).thenReturn(opsHostEntities);
        when(clusterService.listByIds(anyList())).thenReturn(opsClusterEntities);
        // Mock database query result
        List<AlertRecord> records = new ArrayList<>();
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
            .setStartTime(LocalDateTime.parse("2022-01-01 01:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .setEndTime(LocalDateTime.parse("2022-01-01 02:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        records.add(alertRecord);
        Page<AlertRecord> page = new Page<>(1, 10);
        page.setRecords(records);
        page.setTotal(1);
        when(baseMapper.selectPage(any(Page.class), any())).thenReturn(page);
        // Act
        AlertRecordReq alertRecordReq = new AlertRecordReq();
        alertRecordReq.setStartTime("2022-01-01 01:00:00").setClusterNodeId("node123")
            .setEndTime("2022-01-01 02:00:00").setAlertLevel("warn").setAlertStatus("0").setRecordStatus("0");
        Page pageReq = new Page(1, 10);
        Page<AlertRecordDto> result = alertRecordService.getListPage(alertRecordReq, pageReq);
        // Assert
        assertEquals(1, result.getTotal());
        AlertRecordDto dto = result.getRecords().get(0);
        assertEquals(alertRecord.getId(), dto.getId());
        assertEquals("test/127.0.0.1:80(MASTER)", dto.getClusterNodeName());
    }

    @Test
    public void testAlertRecordStatistics() {
        Long total = 3L;
        when(baseMapper.selectCount(any())).thenReturn(total);
        AlertStatisticsReq alertStatisticsReq = new AlertStatisticsReq();
        AlertStatisticsDto alertStatisticsDto = alertRecordService.alertRecordStatistics(alertStatisticsReq);

        Integer totalNum = total.intValue();
        assertEquals(totalNum, alertStatisticsDto.getTotalNum());
    }

    @Test
    public void testAlertRecordStatistics2() {
        Long total = 3L;
        when(baseMapper.selectCount(any())).thenReturn(total);

        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("alertstatus", CommonConstants.FIRING_STATUS);
        map1.put("count", 1L);
        mapList.add(map1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("alertstatus", CommonConstants.RECOVER_STATUS);
        map2.put("count", 1L);
        mapList.add(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("recordstatus", CommonConstants.UNREAD_STATUS);
        map3.put("count", 1L);
        mapList.add(map3);
        Map<String, Object> map4 = new HashMap<>();
        map4.put("recordstatus", CommonConstants.READ_STATUS);
        map4.put("count", 1L);
        mapList.add(map4);
        Map<String, Object> map5 = new HashMap<>();
        map5.put("level", CommonConstants.SERIOUS);
        map5.put("count", 1L);
        mapList.add(map5);
        Map<String, Object> map6 = new HashMap<>();
        map6.put("level", CommonConstants.WARN);
        map6.put("count", 1L);
        mapList.add(map6);
        Map<String, Object> map7 = new HashMap<>();
        map7.put("level", CommonConstants.INFO);
        map7.put("count", 1L);
        mapList.add(map7);

        when(baseMapper.selectMaps(any())).thenReturn(mapList);

        AlertStatisticsReq alertStatisticsReq = new AlertStatisticsReq();
        alertStatisticsReq.setStartTime("2022-01-01 01:00:00").setClusterNodeId("node123")
            .setEndTime("2022-01-01 02:00:00");
        AlertStatisticsDto alertStatisticsDto = alertRecordService.alertRecordStatistics(alertStatisticsReq);

        Integer totalNum = total.intValue();
        assertEquals(totalNum, alertStatisticsDto.getTotalNum());
        verify(baseMapper, times(1)).selectCount(any());
        verify(baseMapper, times(3)).selectMaps(any());
    }

    @Test
    public void testMarkAsRead() {
        String ids = "1,2,3";
        String result = alertRecordService.markAsRead(ids);
        assertEquals("success", result);
    }

    @Test
    public void testMarkAsUnRead() {
        String ids = "1,2,3";
        String result = alertRecordService.markAsUnread(ids);
        assertEquals("success", result);
    }

    @Test
    public void testGetByIdNull() {
        AlertRecord alertRecord = null;
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        AlertRecordDto alertRecordDto = alertRecordService.getById(any());
        verify(baseMapper, times(1)).selectById(any());
        assertNull(alertRecordDto.getId());
    }

    @Test
    public void testGetById() {
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
            .setStartTime(LocalDateTime.parse("2022-01-01 01:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .setEndTime(LocalDateTime.parse("2022-01-01 02:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        when(baseMapper.selectById(any())).thenReturn(alertRecord);

        OpsClusterNodeEntity opsClusterNode = new OpsClusterNodeEntity();
        opsClusterNode.setClusterNodeId("node123");
        opsClusterNode.setClusterId("test");
        opsClusterNode.setHostId("1");
        opsClusterNode.setClusterRole(ClusterRoleEnum.MASTER);
        when(clusterNodeService.getById(alertRecord.getClusterNodeId())).thenReturn(opsClusterNode);

        OpsHostEntity opsHostEntity = new OpsHostEntity();
        opsHostEntity.setHostId("1");
        opsHostEntity.setPublicIp("127.0.0.1");
        when(hostFacade.getById(opsClusterNode.getHostId())).thenReturn(opsHostEntity);

        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(80);
        when(clusterService.getById(opsClusterNode.getClusterId())).thenReturn(opsClusterEntity);

        AlertRecordDto dto = alertRecordService.getById(any());

        verify(baseMapper, times(1)).selectById(any());
        verify(clusterNodeService, times(1)).getById(alertRecord.getClusterNodeId());
        verify(hostFacade, times(1)).getById(opsClusterNode.getHostId());
        verify(clusterService, times(1)).getById(opsClusterNode.getClusterId());
        assertEquals(alertRecord.getId(), dto.getId());
        assertEquals("node123", dto.getClusterNodeId());
        assertEquals("test", dto.getClusterId());
        assertEquals("127.0.0.1:80", dto.getHostIpAndPort());
        assertEquals("MASTER", dto.getNodeRole());
        assertEquals("test/127.0.0.1:80(MASTER)", dto.getClusterNodeName());
    }

    @Test
    public void testGetById2() {
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
            .setStartTime(LocalDateTime.parse("2022-01-01 01:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .setEndTime(LocalDateTime.parse("2022-01-01 02:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        when(baseMapper.selectById(any())).thenReturn(alertRecord);

        OpsClusterNodeEntity opsClusterNode = new OpsClusterNodeEntity();
        when(clusterNodeService.getById(alertRecord.getClusterNodeId())).thenReturn(opsClusterNode);

        AlertRecordDto dto = alertRecordService.getById(any());

        verify(baseMapper, times(1)).selectById(any());
        verify(clusterNodeService, times(1)).getById(alertRecord.getClusterNodeId());
        assertEquals(alertRecord.getId(), dto.getId());
        assertEquals("node123", dto.getClusterNodeId());
    }

    @Test
    public void testGetById3() {
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
            .setStartTime(LocalDateTime.parse("2022-01-01 01:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .setEndTime(LocalDateTime.parse("2022-01-01 02:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        when(baseMapper.selectById(any())).thenReturn(alertRecord);

        OpsClusterNodeEntity opsClusterNode = null;
        when(clusterNodeService.getById(alertRecord.getClusterNodeId())).thenReturn(opsClusterNode);

        AlertRecordDto dto = alertRecordService.getById(any());

        verify(baseMapper, times(1)).selectById(any());
        verify(clusterNodeService, times(1)).getById(alertRecord.getClusterNodeId());
        assertEquals(alertRecord.getId(), dto.getId());
        assertEquals("node123", dto.getClusterNodeId());
    }

    @Test
    public void testGetRelationData() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            AlertRecord alertRecord = new AlertRecord();
            alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
                .setStartTime(LocalDateTime.parse("2022-01-01 01:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .setEndTime(LocalDateTime.parse("2022-01-01 02:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).setTemplateId(1L);
            when(baseMapper.selectById(any())).thenReturn(alertRecord);
            AlertTemplateRuleItem ruleItem = new AlertTemplateRuleItem();
            ruleItem.setRuleExpName("ruleExpName").setUnit("unit").setLimitValue("10").setRuleExp("ruleExp");
            List<AlertTemplateRuleItem> ruleItemList = new ArrayList<>();
            ruleItemList.add(ruleItem);
            when(templateRuleItemMapper.selectList(any())).thenReturn(ruleItemList);
            NctigbaEnv promEnv = new NctigbaEnv();
            promEnv.setHostid("1").setPort(9090);
            when(envMapper.selectOne(any())).thenReturn(promEnv);
            OpsHostEntity opsHostEntity = new OpsHostEntity();
            opsHostEntity.setHostId("1");
            opsHostEntity.setPublicIp("127.0.0.1");
            when(hostFacade.getById(promEnv.getHostid())).thenReturn(opsHostEntity);

            String name = "ruleExpName";
            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn(name);

            Number[][] datas = new Number[0][0];
            when(prometheusService.queryRange(any(), any(), any(), any(), any())).thenReturn(datas);
            List<AlertRelationDto> relationDataList = alertRecordService.getRelationData(any());
            verify(baseMapper, times(1)).selectById(any());
            verify(templateRuleItemMapper, times(1)).selectList(any());
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(any());
            verify(prometheusService, times(1)).queryRange(any(), any(), any(), any(), any());
            assertEquals(1, relationDataList.size());
        }
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.config.ElasticsearchProvider;
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
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

    @Mock
    private ElasticsearchProvider clientProvider;

    @Mock
    private TemplateEngine templateEngine;


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
    public void testGetEmptyRelationData1() {
        AlertRecord alertRecord = new AlertRecord();
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        alertRecordService.getRelationData(any());
        verify(baseMapper, times(1)).selectById(any());
    }

    @Test
    public void testGetEmptyRelationData2() {
        AlertRecord alertRecord = new AlertRecord().setTemplateRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        alertRecordService.getRelationData(any());
        verify(baseMapper, times(1)).selectById(any());
    }

    @Test
    public void testGetRelationData() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            AlertRecord alertRecord = new AlertRecord();
            alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
                .setStartTime(LocalDateTime.parse("2022-01-01 01:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .setEndTime(LocalDateTime.parse("2022-01-01 02:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).setTemplateId(1L)
                .setTemplateRuleType(CommonConstants.INDEX_RULE);
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

    @Test
    public void testGetEmptyRelationLog1() {
        AlertRecord alertRecord = new AlertRecord();
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        alertRecordService.getRelationLog(anyLong(), false, "");
        verify(baseMapper, times(1)).selectById(any());
    }

    @Test
    public void testGetEmptyRelationLog2() {
        AlertRecord alertRecord = new AlertRecord().setTemplateRuleType(CommonConstants.INDEX_RULE);
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        alertRecordService.getRelationLog(anyLong(), false, "");
        verify(baseMapper, times(1)).selectById(any());
    }

    @Test
    public void testGetEmptyRelationLog3() {
        AlertRecord alertRecord = new AlertRecord().setTemplateRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        List<AlertTemplateRuleItem> templateRuleItems = new ArrayList<>();
        when(templateRuleItemMapper.selectList(any())).thenReturn(templateRuleItems);
        alertRecordService.getRelationLog(anyLong(), false, "");
        verify(baseMapper, times(1)).selectById(any());
        verify(templateRuleItemMapper, times(1)).selectList(any());
    }

    @Test
    public void testGetRelationLogWithShowAlertLog() throws IOException {
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
            .setStartTime(LocalDateTime.parse("2022-01-01 01:00:02",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .setEndTime(LocalDateTime.parse("2022-01-01 02:00:02",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).setTemplateId(1L).setTemplateRuleId(1L)
            .setTemplateRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        AlertTemplateRuleItem ruleItem1 = new AlertTemplateRuleItem().setKeyword("abc").setBlockWord("bcd");
        AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setKeyword("abc,efg");
        List<AlertTemplateRuleItem> templateRuleItems = new ArrayList<>();
        templateRuleItems.add(ruleItem1);
        templateRuleItems.add(ruleItem2);
        when(templateRuleItemMapper.selectList(any())).thenReturn(templateRuleItems);
        // Es
        ElasticsearchClient client = mock(ElasticsearchClient.class);
        when(clientProvider.client()).thenReturn(client);
        SearchResponse<HashMap> response = mockRealResponse();
        when(client.search(any(Function.class), eq(HashMap.class))).thenReturn(response);

        alertRecordService.getRelationLog(1L, true, "");

        verify(baseMapper, times(1)).selectById(any());
        verify(templateRuleItemMapper, times(1)).selectList(any());
        verify(clientProvider, times(1)).client();
        verify(client, times(1)).search(any(Function.class), eq(HashMap.class));
    }

    @Test
    public void testGetRelationLogWithoutShowAlertLog() throws IOException {
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
            .setStartTime(LocalDateTime.parse("2022-01-01 01:00:02",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .setEndTime(LocalDateTime.parse("2022-01-01 02:00:02",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).setTemplateId(1L).setTemplateRuleId(1L)
            .setTemplateRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        AlertTemplateRuleItem ruleItem1 = new AlertTemplateRuleItem().setKeyword("abc").setBlockWord("bcd");
        AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setKeyword("abc,efg");
        List<AlertTemplateRuleItem> templateRuleItems = new ArrayList<>();
        templateRuleItems.add(ruleItem1);
        templateRuleItems.add(ruleItem2);
        when(templateRuleItemMapper.selectList(any())).thenReturn(templateRuleItems);
        // Es
        ElasticsearchClient client = mock(ElasticsearchClient.class);
        when(clientProvider.client()).thenReturn(client);
        SearchResponse<HashMap> response = mockRealResponse();
        when(client.search(any(Function.class), eq(HashMap.class))).thenReturn(response);

        alertRecordService.getRelationLog(1L, false, "123124");

        verify(baseMapper, times(1)).selectById(any());
        verify(templateRuleItemMapper, times(1)).selectList(any());
        verify(clientProvider, times(1)).client();
        verify(client, times(1)).search(any(Function.class), eq(HashMap.class));
    }

    @Test
    public void testGetRelationLogWithShowAlertLogReturnEmptyHit() throws IOException {
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
            .setStartTime(LocalDateTime.parse("2022-01-01 01:00:02",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .setEndTime(LocalDateTime.parse("2022-01-01 02:00:02",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).setTemplateId(1L).setTemplateRuleId(1L)
            .setTemplateRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        AlertTemplateRuleItem ruleItem1 = new AlertTemplateRuleItem().setKeyword("abc").setBlockWord("bcd");
        AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setKeyword("abc,efg");
        List<AlertTemplateRuleItem> templateRuleItems = new ArrayList<>();
        templateRuleItems.add(ruleItem1);
        templateRuleItems.add(ruleItem2);
        when(templateRuleItemMapper.selectList(any())).thenReturn(templateRuleItems);
        // Es
        ElasticsearchClient client = mock(ElasticsearchClient.class);
        when(clientProvider.client()).thenReturn(client);
        SearchResponse<HashMap> response = mockEmptyHisResponse();
        when(client.search(any(Function.class), eq(HashMap.class))).thenReturn(response);

        alertRecordService.getRelationLog(1L, true, "");

        verify(baseMapper, times(1)).selectById(any());
        verify(templateRuleItemMapper, times(1)).selectList(any());
        verify(clientProvider, times(1)).client();
        verify(client, times(1)).search(any(Function.class), eq(HashMap.class));
    }

    @Test
    public void testGetRelationLogWithShowAlertLogReturnEmptySorts() throws IOException {
        AlertRecord alertRecord = new AlertRecord();
        alertRecord.setId(1L).setClusterNodeId("node123").setAlertStatus(0).setRecordStatus(0).setLevel("warn")
            .setStartTime(LocalDateTime.parse("2022-01-01 01:00:02",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .setEndTime(LocalDateTime.parse("2022-01-01 02:00:02",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).setTemplateId(1L).setTemplateRuleId(1L)
            .setTemplateRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(any())).thenReturn(alertRecord);
        AlertTemplateRuleItem ruleItem1 = new AlertTemplateRuleItem().setKeyword("abc").setBlockWord("bcd");
        AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setKeyword("abc,efg");
        List<AlertTemplateRuleItem> templateRuleItems = new ArrayList<>();
        templateRuleItems.add(ruleItem1);
        templateRuleItems.add(ruleItem2);
        when(templateRuleItemMapper.selectList(any())).thenReturn(templateRuleItems);
        // Es
        ElasticsearchClient client = mock(ElasticsearchClient.class);
        when(clientProvider.client()).thenReturn(client);
        SearchResponse<HashMap> response = mockEmptySortResponse();
        when(client.search(any(Function.class), eq(HashMap.class))).thenReturn(response);

        alertRecordService.getRelationLog(1L, true, "");

        verify(baseMapper, times(1)).selectById(any());
        verify(templateRuleItemMapper, times(1)).selectList(any());
        verify(clientProvider, times(1)).client();
        verify(client, times(1)).search(any(Function.class), eq(HashMap.class));
    }

    private SearchResponse<HashMap> mockRealResponse() {
        Map logTypeMap = new HashMap<>();
        logTypeMap.put(CommonConstants.LOG_TYPE, "os-run-log");
        HashMap map = new HashMap<>();
        map.put(CommonConstants.FIELDS, logTypeMap);
        map.put(CommonConstants.LOG_LEVEL, "LOG");
        map.put(CommonConstants.MESSAGE, "Starting session: command for root from 127.0.0.1 port 36252 id 0");
        map.put(CommonConstants.CLUSTER_ID, "test");
        map.put(CommonConstants.NODE_ID, "node123");
        map.put(CommonConstants.TIMESTAMP, "2023-08-14T04:38:38.000+08:00");
        Hit.Builder<HashMap> hitBuilder = new Hit.Builder<>();
        hitBuilder.source(map);
        List<String> sorts = Arrays.asList("2023-08-14T04:38:38.000+08:00", "12345");
        hitBuilder.sort(sorts).index("ob-*-node123").id("abcd");
        List<Hit<HashMap>> hitList = new ArrayList<>();
        hitList.add(hitBuilder.build());
        HitsMetadata.Builder<HashMap> hitsMetadataBuilder = new HitsMetadata.Builder<HashMap>();
        hitsMetadataBuilder.hits(hitList);
        SearchResponse.Builder<HashMap> hashMapBuilder = new SearchResponse.Builder<>();
        hashMapBuilder.hits(hitsMetadataBuilder.build()).took(4028L).timedOut(false);
        ShardStatistics.Builder statisticsBuilder = new ShardStatistics.Builder();
        statisticsBuilder.failed(0).successful(14).total(14).skipped(0);
        hashMapBuilder.shards(statisticsBuilder.build());
        return hashMapBuilder.build();
    }

    private SearchResponse<HashMap> mockEmptyHisResponse() {
        List<Hit<HashMap>> hitList = new ArrayList<>();
        HitsMetadata.Builder<HashMap> hitsMetadataBuilder = new HitsMetadata.Builder<HashMap>();
        hitsMetadataBuilder.hits(hitList);
        SearchResponse.Builder<HashMap> hashMapBuilder = new SearchResponse.Builder<>();
        hashMapBuilder.hits(hitsMetadataBuilder.build()).took(4028L).timedOut(false);
        ShardStatistics.Builder statisticsBuilder = new ShardStatistics.Builder();
        statisticsBuilder.failed(0).successful(14).total(14).skipped(0);
        hashMapBuilder.shards(statisticsBuilder.build());
        return hashMapBuilder.build();
    }

    private SearchResponse<HashMap> mockEmptySortResponse() {
        Map logTypeMap = new HashMap<>();
        logTypeMap.put(CommonConstants.LOG_TYPE, "os-run-log");
        HashMap map = new HashMap<>();
        map.put(CommonConstants.FIELDS, logTypeMap);
        map.put(CommonConstants.LOG_LEVEL, "LOG");
        map.put(CommonConstants.MESSAGE, "Starting session: command for root from 127.0.0.1 port 36252 id 0");
        map.put(CommonConstants.CLUSTER_ID, "test");
        map.put(CommonConstants.NODE_ID, "node123");
        map.put(CommonConstants.TIMESTAMP, "2023-08-14T04:38:38.000+08:00");
        Hit.Builder<HashMap> hitBuilder = new Hit.Builder<>();
        hitBuilder.source(map);
        hitBuilder.index("ob-*-node123").id("abcd");
        List<Hit<HashMap>> hitList = new ArrayList<>();
        hitList.add(hitBuilder.build());
        HitsMetadata.Builder<HashMap> hitsMetadataBuilder = new HitsMetadata.Builder<HashMap>();
        hitsMetadataBuilder.hits(hitList);
        SearchResponse.Builder<HashMap> hashMapBuilder = new SearchResponse.Builder<>();
        hashMapBuilder.hits(hitsMetadataBuilder.build()).took(4028L).timedOut(false);
        ShardStatistics.Builder statisticsBuilder = new ShardStatistics.Builder();
        statisticsBuilder.failed(0).successful(14).total(14).skipped(0);
        hashMapBuilder.shards(statisticsBuilder.build());
        return hashMapBuilder.build();
    }

    @Test
    public void testExportWorkbookWithEmpty() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn("alertRecord");
            List<AlertRecord> alertRecords = new ArrayList<>();
            when(baseMapper.selectList(any())).thenReturn(alertRecords);
            AlertStatisticsReq alertStatisticsReq = new AlertStatisticsReq();
            alertRecordService.exportWorkbook(alertStatisticsReq);
            verify(baseMapper, times(1)).selectList(any());
        }
    }

    @Test
    public void testExportWorkbook() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn("alertRecord");
            List<AlertRecord> alertRecords = mockRealAlertRecords();
            when(baseMapper.selectList(any())).thenReturn(alertRecords);
            OpsClusterNodeEntity nodeEntity = new OpsClusterNodeEntity();
            nodeEntity.setClusterNodeId("nodeId1");
            nodeEntity.setHostId("1");
            nodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
            nodeEntity.setClusterId("test");
            List<OpsClusterNodeEntity> opsClusterNodeEntities = new ArrayList<>();
            opsClusterNodeEntities.add(nodeEntity);
            List<String> clusterNodeIdList = alertRecords.stream().map(item -> item.getClusterNodeId()).collect(
                Collectors.toList());
            when(clusterNodeService.listByIds(clusterNodeIdList)).thenReturn(opsClusterNodeEntities);
            OpsHostEntity hostEntity = new OpsHostEntity();
            hostEntity.setHostId("1");
            hostEntity.setPublicIp("127.0.0.1");
            List<OpsHostEntity> opsHostEntities = new ArrayList<>();
            opsHostEntities.add(hostEntity);
            List<String> hostIds = opsClusterNodeEntities.stream().map(item -> item.getHostId()).collect(
                Collectors.toList());
            when(hostFacade.listByIds(hostIds)).thenReturn(opsHostEntities);
            OpsClusterEntity clusterEntity = new OpsClusterEntity();
            clusterEntity.setPort(8080);
            clusterEntity.setClusterId("test");
            List<OpsClusterEntity> opsClusterEntities = new ArrayList<>();
            opsClusterEntities.add(clusterEntity);
            List<String> clusterIds = opsClusterNodeEntities.stream().map(item -> item.getClusterId()).collect(
                Collectors.toList());
            when(clusterService.listByIds(clusterIds)).thenReturn(opsClusterEntities);

            AlertStatisticsReq alertStatisticsReq =
                new AlertStatisticsReq().setStartTime("2023-08-24 10:13:11").setEndTime("2023-08-24 11:13:11");
            alertRecordService.exportWorkbook(alertStatisticsReq);
            verify(baseMapper, times(1)).selectList(any());
            verify(clusterNodeService, times(1)).listByIds(clusterNodeIdList);
            verify(hostFacade, times(1)).listByIds(hostIds);
            verify(clusterService, times(1)).listByIds(clusterIds);
        }
    }

    private List<AlertRecord> mockRealAlertRecords() {
        List<AlertRecord> alertRecords = new ArrayList<>();
        AlertRecord alertRecord1 =
            new AlertRecord().setStartTime(LocalDateTime.now().minusHours(2)).setEndTime(LocalDateTime.now())
                .setDuration(0L).setAlertStatus(CommonConstants.FIRING_STATUS)
                .setRecordStatus(CommonConstants.READ_STATUS).setLevel("warn")
                .setTemplateRuleType(CommonConstants.INDEX_RULE).setTemplateId(1L).setTemplateRuleId(1L)
                .setClusterNodeId("nodeId1").setNotifyWayIds("1").setNotifyWayNames("a");
        alertRecords.add(alertRecord1);
        AlertRecord alertRecord2 =
            new AlertRecord().setStartTime(LocalDateTime.now().minusHours(2)).setEndTime(LocalDateTime.now())
                .setDuration(3661L).setAlertStatus(CommonConstants.FIRING_STATUS)
                .setRecordStatus(CommonConstants.UNREAD_STATUS).setLevel("warn")
                .setTemplateRuleType(CommonConstants.INDEX_RULE).setTemplateId(1L).setTemplateRuleId(1L)
                .setClusterNodeId("nodeId1").setNotifyWayIds("1").setNotifyWayNames("a");
        alertRecords.add(alertRecord2);
        AlertRecord alertRecord3 =
            new AlertRecord().setStartTime(LocalDateTime.now().minusHours(2)).setEndTime(LocalDateTime.now())
                .setDuration(36610L).setAlertStatus(CommonConstants.RECOVER_STATUS)
                .setRecordStatus(CommonConstants.UNREAD_STATUS).setLevel("warn")
                .setTemplateRuleType(CommonConstants.LOG_RULE).setTemplateId(1L).setTemplateRuleId(1L)
                .setClusterNodeId("nodeId1").setNotifyWayIds("1").setNotifyWayNames("a");
        alertRecords.add(alertRecord3);
        return alertRecords;
    }

    @Test
    public void testExportReport() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn("alertRecord");
            AlertStatisticsDto alertStatisticsDto = new AlertStatisticsDto();
            AlertRecordServiceImpl recordService = mock(AlertRecordServiceImpl.class);
            when(recordService.alertRecordStatistics(any())).thenReturn(alertStatisticsDto);
            List<AlertRecord> alertRecords = mockRealAlertRecords();
            when(baseMapper.selectList(any())).thenReturn(alertRecords);
            OpsClusterNodeEntity nodeEntity = new OpsClusterNodeEntity();
            nodeEntity.setClusterNodeId("nodeId1");
            nodeEntity.setHostId("1");
            nodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
            nodeEntity.setClusterId("test");
            List<OpsClusterNodeEntity> opsClusterNodeEntities = new ArrayList<>();
            opsClusterNodeEntities.add(nodeEntity);
            List<String> clusterNodeIdList = alertRecords.stream().map(item -> item.getClusterNodeId()).collect(
                Collectors.toList());
            when(clusterNodeService.listByIds(clusterNodeIdList)).thenReturn(opsClusterNodeEntities);
            OpsHostEntity hostEntity = new OpsHostEntity();
            hostEntity.setHostId("1");
            hostEntity.setPublicIp("127.0.0.1");
            List<OpsHostEntity> opsHostEntities = new ArrayList<>();
            opsHostEntities.add(hostEntity);
            List<String> hostIds = opsClusterNodeEntities.stream().map(item -> item.getHostId()).collect(
                Collectors.toList());
            when(hostFacade.listByIds(hostIds)).thenReturn(opsHostEntities);
            OpsClusterEntity clusterEntity = new OpsClusterEntity();
            clusterEntity.setPort(8080);
            clusterEntity.setClusterId("test");
            List<OpsClusterEntity> opsClusterEntities = new ArrayList<>();
            opsClusterEntities.add(clusterEntity);
            List<String> clusterIds = opsClusterNodeEntities.stream().map(item -> item.getClusterId()).collect(
                Collectors.toList());
            when(clusterService.listByIds(clusterIds)).thenReturn(opsClusterEntities);
            String html = "html";
            when(templateEngine.process(anyString(), any())).thenReturn(html);

            AlertStatisticsReq alertStatisticsReq =
                new AlertStatisticsReq().setStartTime("2023-08-24 10:13:11").setEndTime("2023-08-24 11:13:11");
            String result = alertRecordService.exportReport(alertStatisticsReq);
            verify(baseMapper, times(1)).selectList(any());
            verify(clusterNodeService, times(1)).listByIds(clusterNodeIdList);
            verify(hostFacade, times(1)).listByIds(hostIds);
            verify(clusterService, times(1)).listByIds(clusterIds);
            assertEquals(html, result);
        }
    }
}

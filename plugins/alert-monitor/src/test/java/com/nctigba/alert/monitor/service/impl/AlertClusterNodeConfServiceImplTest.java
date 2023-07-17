/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.dto.AlertClusterNodeConfDto;
import com.nctigba.alert.monitor.entity.AlertClusterNodeConf;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.mapper.AlertClusterNodeConfMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.model.AlertClusterNodeAndTemplateReq;
import com.nctigba.alert.monitor.model.AlertClusterNodeConfReq;
import com.nctigba.alert.monitor.model.AlertTemplateReq;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.service.PrometheusService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the AlertClusterNodeConfServiceImpl
 *
 * @since 2023/7/11 22:57
 */
@RunWith(SpringRunner.class)
public class AlertClusterNodeConfServiceImplTest {
    @InjectMocks
    @Spy
    private AlertClusterNodeConfServiceImpl alertClusterNodeConfService;

    @Mock
    private AlertClusterNodeConfMapper baseMapper;

    @Mock
    private PrometheusService prometheusService;

    @Mock
    private AlertTemplateService templateService;

    @Mock
    private AlertTemplateMapper templateMapper;

    @Mock
    private IOpsClusterService clusterService;

    @Mock
    private IOpsClusterNodeService clusterNodeService;

    @Mock
    private HostFacade hostFacade;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertClusterNodeConf.class);
    }

    @Test
    public void testSaveClusterNodeConfNull() {
        AlertClusterNodeConfReq alertClusterNodeConfReq = new AlertClusterNodeConfReq();
        alertClusterNodeConfReq.setClusterNodeIds("node1");
        alertClusterNodeConfReq.setTemplateId(1L);
        AlertClusterNodeConf clusterNodeConf = new AlertClusterNodeConf();
        clusterNodeConf.setId(1L).setTemplateId(1L).setClusterNodeId("node1");
        List<AlertClusterNodeConf> oldList = List.of(clusterNodeConf);
        when(baseMapper.selectList(any())).thenReturn(oldList);
        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfReq);
        verify(baseMapper, times(1)).selectList(any());
    }

    @Test
    public void testSaveClusterNodeConf1() {
        AlertClusterNodeConfReq alertClusterNodeConfReq = new AlertClusterNodeConfReq();
        alertClusterNodeConfReq.setClusterNodeIds("node1,node2");
        alertClusterNodeConfReq.setTemplateId(1L);
        AlertClusterNodeConf alertClusterNodeConf = new AlertClusterNodeConf();
        alertClusterNodeConf.setClusterNodeId("node2").setTemplateId(2L);
        List<AlertClusterNodeConf> oldList = new ArrayList<>();
        oldList.add(alertClusterNodeConf);
        when(baseMapper.selectList(any())).thenReturn(oldList);
        List list = anyList();
        when(alertClusterNodeConfService.saveBatch(list)).thenReturn(true);
        doNothing().when(prometheusService).updateRuleConfig(anyMap());

        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfReq);

        verify(baseMapper, times(3)).selectList(any());
        verify(alertClusterNodeConfService, times(1)).saveBatch(list);
        verify(alertClusterNodeConfService, times(1)).update(any(), any(LambdaUpdateWrapper.class));
        verify(prometheusService, times(1)).updateRuleConfig(anyMap());
    }

    @Test
    public void testSaveClusterNodeConf2() {
        AlertClusterNodeConfReq alertClusterNodeConfReq = new AlertClusterNodeConfReq();
        alertClusterNodeConfReq.setClusterNodeIds("node1,node2");
        alertClusterNodeConfReq.setTemplateId(1L);
        List<AlertClusterNodeConf> oldList = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(oldList);
        List list = anyList();
        when(alertClusterNodeConfService.saveBatch(list)).thenReturn(true);
        doNothing().when(prometheusService).updateRuleConfig(anyMap());

        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfReq);

        verify(baseMapper, times(2)).selectList(any());
        verify(alertClusterNodeConfService, times(1)).saveBatch(list);
        verify(alertClusterNodeConfService, times(1)).update(any(), any(LambdaUpdateWrapper.class));
        verify(prometheusService, times(1)).updateRuleConfig(anyMap());
    }

    @Test
    public void testGetByClusterNodeIdNull() {
        String clusterNodeId = "node1";
        when(baseMapper.selectList(any())).thenReturn(anyList());
        AlertClusterNodeConf alertClusterNodeConf = alertClusterNodeConfService.getByClusterNodeId(clusterNodeId);
        verify(baseMapper, times(1)).selectList(any());
        assertNotNull(alertClusterNodeConf);
        assertNull(alertClusterNodeConf.getClusterNodeId());
    }

    @Test
    public void testGetByClusterNodeId() {
        String clusterNodeId = "node1";
        List<AlertClusterNodeConf> list = List.of(new AlertClusterNodeConf().setClusterNodeId("node1"));
        when(baseMapper.selectList(any())).thenReturn(list);
        AlertClusterNodeConf alertClusterNodeConf1 = alertClusterNodeConfService.getByClusterNodeId(clusterNodeId);
        verify(baseMapper, times(1)).selectList(any());
        assertEquals("node1", alertClusterNodeConf1.getClusterNodeId());
    }

    @Test
    public void testSaveAlertTemplateAndConfig() {
        AlertClusterNodeAndTemplateReq clusterNodeAndTemplateReq =
            new AlertClusterNodeAndTemplateReq().setClusterNodeIds("node1,node2").setTemplateName("templateName")
                .setTemplateRuleReqList(new ArrayList<>());
        AlertTemplateReq templateReq = new AlertTemplateReq();
        BeanUtil.copyProperties(clusterNodeAndTemplateReq, templateReq);
        AlertTemplate alertTemplate = new AlertTemplate().setTemplateName(templateReq.getTemplateName()).setId(1L);
        when(templateService.saveTemplate(any())).thenReturn(alertTemplate);
        doNothing().when(alertClusterNodeConfService).saveClusterNodeConf(any());

        alertClusterNodeConfService.saveAlertTemplateAndConfig(clusterNodeAndTemplateReq);

        verify(templateService, times(1)).saveTemplate(any());
        verify(alertClusterNodeConfService, times(1)).saveClusterNodeConf(any());
    }

    @Test
    public void testGetList1() {
        List<OpsClusterNodeEntity> list = new ArrayList<>();
        OpsClusterNodeEntity opsClusterNode = new OpsClusterNodeEntity();
        opsClusterNode.setClusterNodeId("node123");
        opsClusterNode.setClusterId("test");
        opsClusterNode.setHostId("1");
        opsClusterNode.setClusterRole(ClusterRoleEnum.MASTER);
        list.add(opsClusterNode);
        when(clusterNodeService.list()).thenReturn(list);

        Set<String> set = list.stream().map(item -> item.getHostId()).collect(Collectors.toSet());
        List<OpsHostEntity> opsHostEntities = new ArrayList<>();
        OpsHostEntity opsHostEntity = new OpsHostEntity();
        opsHostEntity.setHostId("1");
        opsHostEntity.setPublicIp("127.0.0.1");
        opsHostEntities.add(opsHostEntity);
        when(hostFacade.listByIds(set)).thenReturn(opsHostEntities);

        List<OpsClusterEntity> opsClusterEntities = new ArrayList<>();
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(80);
        opsClusterEntities.add(opsClusterEntity);
        when(clusterService.listByIds(any())).thenReturn(opsClusterEntities);

        List<AlertClusterNodeConf> alertClusterNodeConfs = new ArrayList<>();
        AlertClusterNodeConf alertClusterNodeConf =
            new AlertClusterNodeConf().setClusterNodeId("node123").setTemplateId(1L);
        alertClusterNodeConfs.add(alertClusterNodeConf);
        when(baseMapper.selectList(any())).thenReturn(alertClusterNodeConfs);

        AlertTemplate alertTemplate = new AlertTemplate().setId(1L).setTemplateName("name");
        when(templateMapper.selectById(any())).thenReturn(alertTemplate);

        List<AlertClusterNodeConfDto> resultList = alertClusterNodeConfService.getList();

        verify(clusterNodeService, times(1)).list();
        verify(hostFacade, times(1)).listByIds(set);
        verify(clusterService, times(1)).listByIds(anyList());
        verify(baseMapper, times(1)).selectList(any());
        verify(templateMapper, times(1)).selectById(any());
        assertEquals(1, resultList.size());
        AlertClusterNodeConfDto alertClusterNodeConfDto = new AlertClusterNodeConfDto();
        alertClusterNodeConfDto.setClusterNodeId("node123").setNodeName("test/127.0.0.1:80(MASTER)")
            .setTemplateId(1L).setTemplateName("name");
        assertEquals(alertClusterNodeConfDto, resultList.get(0));
    }

    @Test
    public void testGetList2() {
        List<OpsClusterNodeEntity> list = new ArrayList<>();
        OpsClusterNodeEntity opsClusterNode = new OpsClusterNodeEntity();
        opsClusterNode.setClusterNodeId("node123");
        opsClusterNode.setClusterId("test");
        opsClusterNode.setHostId("1");
        opsClusterNode.setClusterRole(ClusterRoleEnum.MASTER);
        list.add(opsClusterNode);
        when(clusterNodeService.list()).thenReturn(list);

        Set<String> set = list.stream().map(item -> item.getHostId()).collect(Collectors.toSet());
        List<OpsHostEntity> opsHostEntities = new ArrayList<>();
        OpsHostEntity opsHostEntity = new OpsHostEntity();
        opsHostEntity.setHostId("1");
        opsHostEntity.setPublicIp("127.0.0.1");
        opsHostEntities.add(opsHostEntity);
        when(hostFacade.listByIds(set)).thenReturn(opsHostEntities);

        List<OpsClusterEntity> opsClusterEntities = new ArrayList<>();
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(80);
        opsClusterEntities.add(opsClusterEntity);
        when(clusterService.listByIds(any())).thenReturn(opsClusterEntities);

        List<AlertClusterNodeConf> alertClusterNodeConfs = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(alertClusterNodeConfs);

        List<AlertClusterNodeConfDto> resultList = alertClusterNodeConfService.getList();

        verify(clusterNodeService, times(1)).list();
        verify(hostFacade, times(1)).listByIds(set);
        verify(clusterService, times(1)).listByIds(anyList());
        verify(baseMapper, times(1)).selectList(any());
        assertEquals(1, resultList.size());
        AlertClusterNodeConfDto alertClusterNodeConfDto = new AlertClusterNodeConfDto();
        alertClusterNodeConfDto.setClusterNodeId("node123").setNodeName("test/127.0.0.1:80(MASTER)");
        assertEquals(alertClusterNodeConfDto, resultList.get(0));
    }
}

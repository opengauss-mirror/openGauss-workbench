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
 *  AlertClusterNodeConfServiceImplTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/impl/AlertClusterNodeConfServiceImplTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.AlertClusterNodeConfMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.model.dto.AlertClusterNodeConfDTO;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeAndTemplateQuery;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeConfQuery;
import com.nctigba.alert.monitor.model.query.AlertTemplateQuery;
import com.nctigba.alert.monitor.service.AlertScheduleService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
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
    private PrometheusServiceImpl prometheusService;

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

    @Mock
    private AlertTemplateRuleService templateRuleService;

    @Mock
    private AlertScheduleService alertScheduleService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertClusterNodeConfDO.class);
    }

    @Test
    public void testSaveClusterNodeConfNull() {
        AlertClusterNodeConfQuery alertClusterNodeConfQuery = new AlertClusterNodeConfQuery();
        alertClusterNodeConfQuery.setClusterNodeIds("node1");
        alertClusterNodeConfQuery.setTemplateId(1L);
        AlertClusterNodeConfDO clusterNodeConf = new AlertClusterNodeConfDO();
        clusterNodeConf.setId(1L).setTemplateId(1L).setClusterNodeId("node1");
        List<AlertClusterNodeConfDO> oldList = new ArrayList<>();
        oldList.add(clusterNodeConf);
        when(baseMapper.selectList(any())).thenReturn(oldList);
        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfQuery);
        verify(baseMapper, times(1)).selectList(any());
    }

    @Test
    public void testSaveClusterNodeConf1() {
        AlertClusterNodeConfQuery alertClusterNodeConfQuery = new AlertClusterNodeConfQuery();
        alertClusterNodeConfQuery.setClusterNodeIds("node1,node2");
        alertClusterNodeConfQuery.setTemplateId(1L);
        AlertClusterNodeConfDO alertClusterNodeConfDO = new AlertClusterNodeConfDO();
        alertClusterNodeConfDO.setClusterNodeId("node2").setTemplateId(2L);
        List<AlertClusterNodeConfDO> oldList = new ArrayList<>();
        oldList.add(alertClusterNodeConfDO);
        when(baseMapper.selectList(any())).thenReturn(oldList);
        List list = anyList();
        when(alertClusterNodeConfService.saveBatch(list)).thenReturn(true);

        doNothing().when(prometheusService).updateRuleConfig(anyMap());
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        AlertTemplateRuleDO templateRule = new AlertTemplateRuleDO().setRuleType(CommonConstants.LOG_RULE).setId(1L);
        templateRuleList.add(templateRule);
        when(templateRuleService.list(any())).thenReturn(templateRuleList);
        Set<Long> ruleIdSet = templateRuleList.stream().map(item -> item.getRuleId()).collect(Collectors.toSet());
        doNothing().when(alertScheduleService).addTasks(ruleIdSet);

        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfQuery);

        verify(baseMapper, times(3)).selectList(any());
        verify(alertClusterNodeConfService, times(1)).saveBatch(list);
        verify(alertClusterNodeConfService, times(1)).update(any(), any(LambdaUpdateWrapper.class));
        verify(prometheusService, times(1)).updateRuleConfig(anyMap());
        verify(templateRuleService, times(1)).list(any());
        verify(alertScheduleService, times(1)).addTasks(ruleIdSet);
    }

    @Test
    public void testSaveClusterNodeConf2() {
        AlertClusterNodeConfQuery alertClusterNodeConfQuery = new AlertClusterNodeConfQuery();
        alertClusterNodeConfQuery.setClusterNodeIds("node1,node2");
        alertClusterNodeConfQuery.setTemplateId(1L);
        List<AlertClusterNodeConfDO> oldList = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(oldList);
        List list = anyList();
        when(alertClusterNodeConfService.saveBatch(list)).thenReturn(true);
        doNothing().when(prometheusService).updateRuleConfig(anyMap());
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        AlertTemplateRuleDO templateRule = new AlertTemplateRuleDO().setRuleType(CommonConstants.LOG_RULE).setId(1L);
        templateRuleList.add(templateRule);
        when(templateRuleService.list(any())).thenReturn(templateRuleList);
        Set<Long> ruleIdSet = templateRuleList.stream().map(item -> item.getRuleId()).collect(Collectors.toSet());
        doNothing().when(alertScheduleService).addTasks(ruleIdSet);

        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfQuery);

        verify(baseMapper, times(2)).selectList(any());
        verify(alertClusterNodeConfService, times(1)).saveBatch(list);
        verify(alertClusterNodeConfService, times(1)).update(any(), any(LambdaUpdateWrapper.class));
        verify(prometheusService, times(1)).updateRuleConfig(anyMap());
        verify(templateRuleService, times(1)).list(any());
        verify(alertScheduleService, times(1)).addTasks(ruleIdSet);
    }

    @Test
    public void testSaveClusterNodeConfWithDelTask() {
        AlertClusterNodeConfQuery alertClusterNodeConfQuery = new AlertClusterNodeConfQuery();
        alertClusterNodeConfQuery.setClusterNodeIds("node1,node2");
        alertClusterNodeConfQuery.setTemplateId(1L);
        AlertClusterNodeConfDO alertClusterNodeConfDO = new AlertClusterNodeConfDO();
        alertClusterNodeConfDO.setClusterNodeId("node2").setTemplateId(2L);
        List<AlertClusterNodeConfDO> oldList = new ArrayList<>();
        oldList.add(alertClusterNodeConfDO);
        List<AlertClusterNodeConfDO> oldListByNodeIds = new ArrayList<>();
        oldListByNodeIds.add(new AlertClusterNodeConfDO().setClusterNodeId("node2").setTemplateId(2L));
        List<AlertClusterNodeConfDO> alertClusterNodeConfDOS = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(oldList).thenReturn(oldListByNodeIds)
            .thenReturn(alertClusterNodeConfDOS);
        List list = anyList();
        when(alertClusterNodeConfService.saveBatch(list)).thenReturn(true);

        doNothing().when(prometheusService).updateRuleConfig(anyMap());
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        templateRuleList.add(new AlertTemplateRuleDO().setRuleType(CommonConstants.LOG_RULE).setId(1L));
        List<AlertTemplateRuleDO> logTemplateRuleList = new ArrayList<>();
        logTemplateRuleList.add(new AlertTemplateRuleDO().setId(2L).setRuleId(1L));
        when(templateRuleService.list(any())).thenReturn(templateRuleList).thenReturn(logTemplateRuleList);
        Set<Long> ruleIdSet = templateRuleList.stream().map(item -> item.getRuleId()).collect(Collectors.toSet());
        doNothing().when(alertScheduleService).addTasks(ruleIdSet);
        doNothing().when(alertScheduleService).removeTasks(anyList());

        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfQuery);

        verify(baseMapper, times(3)).selectList(any());
        verify(alertClusterNodeConfService, times(1)).saveBatch(list);
        verify(alertClusterNodeConfService, times(1)).update(any(), any(LambdaUpdateWrapper.class));
        verify(prometheusService, times(1)).updateRuleConfig(anyMap());
        verify(templateRuleService, times(2)).list(any());
        verify(alertScheduleService, times(1)).addTasks(ruleIdSet);
        verify(alertScheduleService, times(1)).removeTasks(anyList());
    }

    @Test
    public void testSaveClusterNodeConfWithoutDelTask1() {
        AlertClusterNodeConfQuery alertClusterNodeConfQuery = new AlertClusterNodeConfQuery();
        alertClusterNodeConfQuery.setClusterNodeIds("node1,node2");
        alertClusterNodeConfQuery.setTemplateId(1L);
        AlertClusterNodeConfDO alertClusterNodeConfDO = new AlertClusterNodeConfDO();
        alertClusterNodeConfDO.setClusterNodeId("node2").setTemplateId(2L);
        List<AlertClusterNodeConfDO> oldList = new ArrayList<>();
        oldList.add(alertClusterNodeConfDO);
        List<AlertClusterNodeConfDO> oldListByNodeIds = new ArrayList<>();
        oldListByNodeIds.add(new AlertClusterNodeConfDO().setClusterNodeId("node2").setTemplateId(2L));
        List<AlertClusterNodeConfDO> alertClusterNodeConfDOS = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(oldList).thenReturn(oldListByNodeIds)
            .thenReturn(alertClusterNodeConfDOS);
        List list = anyList();
        when(alertClusterNodeConfService.saveBatch(list)).thenReturn(true);

        doNothing().when(prometheusService).updateRuleConfig(anyMap());
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        templateRuleList.add(new AlertTemplateRuleDO().setRuleType(CommonConstants.LOG_RULE).setId(1L));
        List<AlertTemplateRuleDO> logTemplateRuleList = new ArrayList<>();
        logTemplateRuleList.add(new AlertTemplateRuleDO().setId(2L).setRuleId(1L));
        when(templateRuleService.list(any())).thenReturn(templateRuleList).thenReturn(logTemplateRuleList);
        Set<Long> ruleIdSet = templateRuleList.stream().map(item -> item.getRuleId()).collect(Collectors.toSet());
        doNothing().when(alertScheduleService).addTasks(ruleIdSet);
        List<Long> ruleIdList = new ArrayList<>();
        ruleIdList.add(1L);
        when(baseMapper.getRuleIdExcludeNoIds(any())).thenReturn(ruleIdList);

        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfQuery);

        verify(baseMapper, times(3)).selectList(any());
        verify(alertClusterNodeConfService, times(1)).saveBatch(list);
        verify(alertClusterNodeConfService, times(1)).update(any(), any(LambdaUpdateWrapper.class));
        verify(prometheusService, times(1)).updateRuleConfig(anyMap());
        verify(templateRuleService, times(2)).list(any());
        verify(alertScheduleService, times(1)).addTasks(ruleIdSet);
    }

    @Test
    public void testSaveClusterNodeConfWithoutDelTask2() {
        AlertClusterNodeConfQuery alertClusterNodeConfQuery = new AlertClusterNodeConfQuery();
        alertClusterNodeConfQuery.setClusterNodeIds("node1,node2");
        alertClusterNodeConfQuery.setTemplateId(1L);
        AlertClusterNodeConfDO alertClusterNodeConfDO = new AlertClusterNodeConfDO();
        alertClusterNodeConfDO.setClusterNodeId("node2").setTemplateId(2L);
        List<AlertClusterNodeConfDO> oldList = new ArrayList<>();
        oldList.add(alertClusterNodeConfDO);
        List<AlertClusterNodeConfDO> oldListByNodeIds = new ArrayList<>();
        oldListByNodeIds.add(new AlertClusterNodeConfDO().setClusterNodeId("node2").setTemplateId(2L));
        List<AlertClusterNodeConfDO> alertClusterNodeConfDOS = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(oldList).thenReturn(oldListByNodeIds)
            .thenReturn(alertClusterNodeConfDOS);
        List list = anyList();
        when(alertClusterNodeConfService.saveBatch(list)).thenReturn(true);

        doNothing().when(prometheusService).updateRuleConfig(anyMap());
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        templateRuleList.add(new AlertTemplateRuleDO().setRuleType(CommonConstants.LOG_RULE).setId(1L));
        List<AlertTemplateRuleDO> logTemplateRuleList = new ArrayList<>();
        when(templateRuleService.list(any())).thenReturn(templateRuleList).thenReturn(logTemplateRuleList);
        Set<Long> ruleIdSet = templateRuleList.stream().map(item -> item.getRuleId()).collect(Collectors.toSet());
        doNothing().when(alertScheduleService).addTasks(ruleIdSet);

        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfQuery);

        verify(baseMapper, times(3)).selectList(any());
        verify(alertClusterNodeConfService, times(1)).saveBatch(list);
        verify(alertClusterNodeConfService, times(1)).update(any(), any(LambdaUpdateWrapper.class));
        verify(prometheusService, times(1)).updateRuleConfig(anyMap());
        verify(templateRuleService, times(2)).list(any());
        verify(alertScheduleService, times(1)).addTasks(ruleIdSet);
    }

    @Test
    public void testGetByClusterNodeIdNull() {
        when(baseMapper.selectList(any())).thenReturn(anyList());
        String clusterNodeId = "node1";
        AlertClusterNodeConfDO alertClusterNodeConfDO = alertClusterNodeConfService.getByClusterNodeId(clusterNodeId,
            CommonConstants.INSTANCE);
        verify(baseMapper, times(1)).selectList(any());
        assertNotNull(alertClusterNodeConfDO);
        assertNull(alertClusterNodeConfDO.getClusterNodeId());
    }

    @Test
    public void testGetByClusterNodeId() {
        AlertClusterNodeConfDO alertClusterNodeConfDO = new AlertClusterNodeConfDO().setClusterNodeId("node1");
        List<AlertClusterNodeConfDO> list = new ArrayList<>();
        list.add(alertClusterNodeConfDO);
        when(baseMapper.selectList(any())).thenReturn(list);
        String clusterNodeId = "node1";
        AlertClusterNodeConfDO alertClusterNodeConfDO1 = alertClusterNodeConfService.getByClusterNodeId(clusterNodeId
            , CommonConstants.INSTANCE);
        verify(baseMapper, times(1)).selectList(any());
        assertEquals("node1", alertClusterNodeConfDO1.getClusterNodeId());
    }

    @Test
    public void testSaveAlertTemplateAndConfig() {
        AlertClusterNodeAndTemplateQuery clusterNodeAndTemplateReq =
            new AlertClusterNodeAndTemplateQuery().setClusterNodeIds("node1,node2").setTemplateName("templateName")
                .setTemplateRuleReqList(new ArrayList<>());
        AlertTemplateQuery templateReq = new AlertTemplateQuery();
        BeanUtil.copyProperties(clusterNodeAndTemplateReq, templateReq);
        AlertTemplateDO alertTemplateDO =
            new AlertTemplateDO().setTemplateName(templateReq.getTemplateName()).setId(1L);
        when(templateService.saveTemplate(any())).thenReturn(alertTemplateDO);
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

        List<AlertClusterNodeConfDO> alertClusterNodeConfDOS = new ArrayList<>();
        AlertClusterNodeConfDO alertClusterNodeConfDO =
            new AlertClusterNodeConfDO().setClusterNodeId("node123").setTemplateId(1L);
        alertClusterNodeConfDOS.add(alertClusterNodeConfDO);
        when(baseMapper.selectList(any())).thenReturn(alertClusterNodeConfDOS);

        AlertTemplateDO alertTemplateDO = new AlertTemplateDO().setId(1L).setTemplateName("name");
        when(templateMapper.selectById(any())).thenReturn(alertTemplateDO);

        List<AlertClusterNodeConfDTO> resultList = alertClusterNodeConfService.getList(CommonConstants.INSTANCE);

        verify(clusterNodeService, times(1)).list();
        verify(hostFacade, times(1)).listByIds(set);
        verify(clusterService, times(1)).listByIds(anyList());
        verify(baseMapper, times(1)).selectList(any());
        verify(templateMapper, times(1)).selectById(any());
        assertEquals(1, resultList.size());
        AlertClusterNodeConfDTO alertClusterNodeConfDto = new AlertClusterNodeConfDTO();
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

        List<AlertClusterNodeConfDO> alertClusterNodeConfDOS = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(alertClusterNodeConfDOS);

        List<AlertClusterNodeConfDTO> resultList = alertClusterNodeConfService.getList(CommonConstants.INSTANCE);

        verify(clusterNodeService, times(1)).list();
        verify(hostFacade, times(1)).listByIds(set);
        verify(clusterService, times(1)).listByIds(anyList());
        verify(baseMapper, times(1)).selectList(any());
        assertEquals(1, resultList.size());
        AlertClusterNodeConfDTO alertClusterNodeConfDto = new AlertClusterNodeConfDTO();
        alertClusterNodeConfDto.setClusterNodeId("node123").setNodeName("test/127.0.0.1:80(MASTER)");
        assertEquals(alertClusterNodeConfDto, resultList.get(0));
    }
}

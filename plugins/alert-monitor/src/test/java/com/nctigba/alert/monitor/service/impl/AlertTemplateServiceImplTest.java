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
 *  AlertTemplateServiceImplTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/impl/AlertTemplateServiceImplTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.model.dto.AlertTemplateDTO;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.model.query.AlertTemplateQuery;
import com.nctigba.alert.monitor.model.query.AlertTemplateRuleQuery;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the AlertTemplateServiceImpl
 *
 * @since 2023/7/13 17:53
 */
@RunWith(SpringRunner.class)
public class AlertTemplateServiceImplTest {
    @InjectMocks
    @Spy
    private AlertTemplateServiceImpl alertTemplateService;
    @Mock
    private AlertTemplateMapper baseMapper;
    @Mock
    private AlertRuleMapper alertRuleMapper;
    @Mock
    private AlertRuleItemMapper alertRuleItemMapper;
    @Mock
    private AlertTemplateRuleMapper alertTemplateRuleMapper;
    @Mock
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;
    @Mock
    private AlertTemplateRuleService templateRuleService;
    @Mock
    private AlertClusterNodeConfService nodeConfService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertTemplateDO.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertTemplateRuleItemDO.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertTemplateRuleDO.class);
    }

    @Test
    public void testGetTemplatePage() {
        Page page = new Page(1, 10);
        Page<AlertTemplateDO> templatePage = new Page<>(1, 10);
        when(baseMapper.selectPage(eq(page), any())).thenReturn(templatePage);

        Page<AlertTemplateDO> result = alertTemplateService.getTemplatePage("", page);

        verify(baseMapper, times(1)).selectPage(eq(page), any());
        assertEquals(0, result.getTotal());
    }

    @Test
    public void testGetTemplateRulePageNull() {
        Page page = new Page(1, 10);
        Page<AlertTemplateRuleDO> rulePage = new Page<>(1, 10);
        when(alertTemplateRuleMapper.selectPage(eq(page), any())).thenReturn(rulePage);

        Page<AlertTemplateRuleDO> result = alertTemplateService.getTemplateRulePage(1L, "", page);

        verify(alertTemplateRuleMapper, times(1)).selectPage(eq(page), any());
        assertEquals(0, result.getTotal());
    }

    @Test
    public void testGetTemplateRulePage() {
        Page page = new Page(1, 10);
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        AlertTemplateRuleDO templateRule = new AlertTemplateRuleDO().setId(1L);
        templateRuleList.add(templateRule);
        Page<AlertTemplateRuleDO> rulePage = new Page<>(1, 10);
        rulePage.setTotal(1L).setRecords(templateRuleList);
        when(alertTemplateRuleMapper.selectPage(eq(page), any())).thenReturn(rulePage);

        AlertTemplateRuleItemDO ruleItem = new AlertTemplateRuleItemDO().setId(1L).setTemplateRuleId(1L);
        List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = new ArrayList<>();
        alertTemplateRuleItemDOS.add(ruleItem);
        when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertTemplateRuleItemDOS);

        Page<AlertTemplateRuleDO> result = alertTemplateService.getTemplateRulePage(1L, "", page);

        verify(alertTemplateRuleMapper, times(1)).selectPage(eq(page), any());
        verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
        assertEquals(1, result.getTotal());
    }

    @Test
    public void testGetTemplate() {
        AlertTemplateDO alertTemplateDO = new AlertTemplateDO().setId(1L);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateDO);

        List<AlertTemplateRuleDO> ruleList = new ArrayList<>();
        when(templateRuleService.getListByTemplateId(anyLong())).thenReturn(ruleList);

        AlertTemplateDTO template = alertTemplateService.getTemplate(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(templateRuleService, times(1)).getListByTemplateId(anyLong());
        assertEquals(alertTemplateDO.getId(), template.getId());
    }

    @Test
    public void testGetTemplateList() {
        List<AlertTemplateDO> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);

        List<AlertTemplateDO> templateList = alertTemplateService.getTemplateList(CommonConstants.INSTANCE);

        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list, templateList);
    }

    @Test
    public void testSaveTemplate1() {
        AlertTemplateQuery templateReq = new AlertTemplateQuery();
        templateReq.setId(1L);
        templateReq.setTemplateName("name");
        List<AlertTemplateRuleQuery> templateRuleReqList = new ArrayList<>();
        AlertTemplateRuleQuery ruleReq1 = new AlertTemplateRuleQuery();
        ruleReq1.setRuleId(2L);
        templateRuleReqList.add(ruleReq1);
        templateReq.setTemplateRuleReqList(templateRuleReqList);
        AlertTemplateDO alertTemplateDO = new AlertTemplateDO();
        when(baseMapper.selectById(templateReq.getId())).thenReturn(alertTemplateDO);

        List<AlertTemplateRuleDO> alertTemplateRuleDOS = new ArrayList<>();
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRuleDOS);

        for (AlertTemplateRuleQuery alertTemplateRuleQuery : templateRuleReqList) {
            AlertRuleDO alertRuleDO = new AlertRuleDO().setId(1L);
            when(alertRuleMapper.selectById(alertTemplateRuleQuery.getRuleId())).thenReturn(alertRuleDO);
            when(alertTemplateRuleMapper.insert(any(AlertTemplateRuleDO.class))).thenReturn(1);
            AlertRuleItemDO alertRuleItemDO1 = new AlertRuleItemDO().setRuleId(1L).setId(1L);
            List<AlertRuleItemDO> alertRuleItemDOS = new ArrayList<>();
            alertRuleItemDOS.add(alertRuleItemDO1);
            when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItemDOS);
            when(alertTemplateRuleItemMapper.insert(any(AlertTemplateRuleItemDO.class))).thenReturn(1);
        }

        AlertTemplateDO result = alertTemplateService.saveTemplate(templateReq);

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(alertTemplateService, times(1)).saveOrUpdate(any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertRuleMapper, times(1)).selectById(anyLong());
        verify(alertTemplateRuleMapper, times(1)).insert(any(AlertTemplateRuleDO.class));
        verify(alertRuleItemMapper, times(1)).selectList(any());
        verify(alertTemplateRuleItemMapper, times(1)).insert(any(AlertTemplateRuleItemDO.class));
        assertEquals(templateReq.getTemplateName(), result.getTemplateName());
    }

    @Test
    public void testSaveTemplate2() {
        AlertTemplateQuery templateReq = new AlertTemplateQuery();
        templateReq.setTemplateName("name");
        List<AlertTemplateRuleQuery> templateRuleReqList = new ArrayList<>();
        AlertTemplateRuleQuery ruleReq2 = new AlertTemplateRuleQuery();
        ruleReq2.setRuleId(2L);
        templateRuleReqList.add(ruleReq2);
        templateReq.setTemplateRuleReqList(templateRuleReqList);

        List<AlertTemplateRuleDO> alertTemplateRuleDOS = new ArrayList<>();
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRuleDOS);

        for (AlertTemplateRuleQuery alertTemplateRuleQuery : templateRuleReqList) {
            AlertRuleDO alertRuleDO = new AlertRuleDO().setId(1L);
            when(alertRuleMapper.selectById(alertTemplateRuleQuery.getRuleId())).thenReturn(alertRuleDO);
            when(alertTemplateRuleMapper.insert(any(AlertTemplateRuleDO.class))).thenReturn(1);

            AlertRuleItemDO alertRuleItemDO1 = new AlertRuleItemDO().setRuleId(1L).setId(1L);
            List<AlertRuleItemDO> alertRuleItemDOS = new ArrayList<>();
            alertRuleItemDOS.add(alertRuleItemDO1);
            when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItemDOS);
            when(alertTemplateRuleItemMapper.insert(any(AlertTemplateRuleItemDO.class))).thenReturn(1);
        }

        AlertTemplateDO result = alertTemplateService.saveTemplate(templateReq);

        verify(alertTemplateService, times(1)).saveOrUpdate(any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertRuleMapper, times(1)).selectById(anyLong());
        verify(alertTemplateRuleMapper, times(1)).insert(any(AlertTemplateRuleDO.class));
        verify(alertRuleItemMapper, times(1)).selectList(any());
        verify(alertTemplateRuleItemMapper, times(1)).insert(any(AlertTemplateRuleItemDO.class));
        assertEquals(templateReq.getTemplateName(), result.getTemplateName());
    }

    @Test
    public void testSaveTemplate3() {
        AlertTemplateQuery templateReq = new AlertTemplateQuery();
        templateReq.setId(1L);
        templateReq.setTemplateName("name");
        List<AlertTemplateRuleQuery> templateRuleReqList = new ArrayList<>();
        AlertTemplateRuleQuery ruleReq1 = new AlertTemplateRuleQuery();
        ruleReq1.setRuleId(1L);
        ruleReq1.setTemplateRuleId(1L);
        templateRuleReqList.add(ruleReq1);
        templateReq.setTemplateRuleReqList(templateRuleReqList);

        AlertTemplateDO alertTemplateDO = new AlertTemplateDO();
        when(baseMapper.selectById(templateReq.getId())).thenReturn(alertTemplateDO);

        AlertTemplateRuleDO templateRule = new AlertTemplateRuleDO().setId(3L);
        List<AlertTemplateRuleDO> alertTemplateRuleDOS = new ArrayList<>();
        alertTemplateRuleDOS.add(templateRule);
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRuleDOS);
        when(alertTemplateRuleMapper.updateById(any(AlertTemplateRuleDO.class))).thenReturn(1);
        when(alertTemplateRuleItemMapper.update(any(), any())).thenReturn(1);

        AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO().setTemplateId(1L).setId(1L);
        when(alertTemplateRuleMapper.selectById(alertTemplateRuleDO.getTemplateId()))
            .thenReturn(alertTemplateRuleDO);

        AlertTemplateDO result = alertTemplateService.saveTemplate(templateReq);

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(alertTemplateService, times(1)).saveOrUpdate(any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertTemplateRuleMapper, times(1)).updateById(any(AlertTemplateRuleDO.class));
        verify(alertTemplateRuleItemMapper, times(1)).update(any(), any());
        verify(alertTemplateRuleMapper, times(1)).selectById(anyLong());
        assertEquals(templateReq.getTemplateName(), result.getTemplateName());
    }

    @Test
    public void testGetTemplateRuleListByIdNull() {
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(templateRuleList);
        List<AlertTemplateRuleDO> templateRuleList0 = alertTemplateService.getTemplateRuleListById(anyLong());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        assertEquals(0, templateRuleList0.size());
    }

    @Test
    public void testGetTemplateRuleListById() {
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        AlertTemplateRuleDO templateRule = new AlertTemplateRuleDO().setId(1L);
        templateRuleList.add(templateRule);
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(templateRuleList);

        AlertTemplateRuleItemDO ruleItem1 = new AlertTemplateRuleItemDO().setId(1L).setTemplateRuleId(1L);
        List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = new ArrayList<>();
        alertTemplateRuleItemDOS.add(ruleItem1);
        when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertTemplateRuleItemDOS);

        List<AlertTemplateRuleDO> templateRuleList0 = alertTemplateService.getTemplateRuleListById(anyLong());

        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
        assertEquals(1, templateRuleList0.size());
    }

    @Test(expected = ServiceException.class)
    public void testDelTemplateThrowException() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            AlertClusterNodeConfDO nodeConf = new AlertClusterNodeConfDO();
            List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
            nodeConfList.add(nodeConf);
            when(nodeConfService.list(any())).thenReturn(nodeConfList);
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("templateIsUsed");
            alertTemplateService.delTemplate(anyLong());
            verify(nodeConfService, times(1)).list(any());
        }
    }

    @Test
    public void testDelTemplate1() {
        List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
        when(nodeConfService.list(any())).thenReturn(nodeConfList);

        when(baseMapper.update(any(), any())).thenReturn(1);

        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(templateRuleList);
        when(alertTemplateRuleMapper.update(any(), any())).thenReturn(1);

        alertTemplateService.delTemplate(anyLong());
        verify(nodeConfService, times(1)).list(any());
        verify(baseMapper, times(1)).update(any(), any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertTemplateRuleMapper, times(1)).update(any(), any());
    }

    @Test
    public void testDelTemplate2() {
        List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
        when(nodeConfService.list(any())).thenReturn(nodeConfList);

        when(baseMapper.update(any(), any())).thenReturn(1);

        AlertTemplateRuleDO templateRule = new AlertTemplateRuleDO().setId(1L);
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        templateRuleList.add(templateRule);
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(templateRuleList);
        when(alertTemplateRuleMapper.update(any(), any())).thenReturn(1);
        when(alertTemplateRuleItemMapper.update(any(), any())).thenReturn(1);

        alertTemplateService.delTemplate(anyLong());
        verify(nodeConfService, times(1)).list(any());
        verify(baseMapper, times(1)).update(any(), any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertTemplateRuleMapper, times(1)).update(any(), any());
        verify(alertTemplateRuleMapper, times(1)).update(any(), any());
    }
}

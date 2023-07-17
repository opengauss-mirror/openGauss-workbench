/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.dto.AlertTemplateDto;
import com.nctigba.alert.monitor.dto.AlertTemplateRuleDto;
import com.nctigba.alert.monitor.entity.AlertClusterNodeConf;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.entity.AlertRuleItem;
import com.nctigba.alert.monitor.entity.AlertRuleItemParam;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItemParam;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemParamMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemParamMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.model.AlertTemplateReq;
import com.nctigba.alert.monitor.model.AlertTemplateRuleReq;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemParamService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
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
import static org.mockito.ArgumentMatchers.anyList;
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
    private AlertRuleItemParamMapper ruleItemParamMapper;
    @Mock
    private AlertTemplateRuleMapper alertTemplateRuleMapper;
    @Mock
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;
    @Mock
    private AlertTemplateRuleService templateRuleService;
    @Mock
    private AlertTemplateRuleItemParamMapper tRuleItemParamMapper;
    @Mock
    private AlertTemplateRuleItemParamService templateRuleItemParamService;
    @Mock
    private AlertClusterNodeConfService nodeConfService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertTemplate.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertTemplateRuleItem.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertTemplateRule.class);
    }

    @Test
    public void testGetTemplatePage() {
        Page page = new Page(1, 10);
        Page<AlertTemplate> templatePage = new Page<>(1, 10);
        when(baseMapper.selectPage(eq(page), any())).thenReturn(templatePage);

        Page<AlertTemplate> result = alertTemplateService.getTemplatePage("", page);

        verify(baseMapper, times(1)).selectPage(eq(page), any());
        assertEquals(0, result.getTotal());
    }

    @Test
    public void testGetTemplateRulePageNull() {
        Page page = new Page(1, 10);
        Page<AlertTemplateRule> rulePage = new Page<>(1, 10);
        when(alertTemplateRuleMapper.selectPage(eq(page), any())).thenReturn(rulePage);

        Page<AlertTemplateRuleDto> result = alertTemplateService.getTemplateRulePage(1L, "", page);

        verify(alertTemplateRuleMapper, times(1)).selectPage(eq(page), any());
        assertEquals(0, result.getTotal());
    }

    @Test
    public void testGetTemplateRulePage() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            Page page = new Page(1, 10);
            List<AlertTemplateRule> templateRuleList = new ArrayList<>();
            AlertTemplateRule templateRule = new AlertTemplateRule().setId(1L);
            templateRuleList.add(templateRule);
            Page<AlertTemplateRule> rulePage = new Page<>(1, 10);
            rulePage.setTotal(1L).setRecords(templateRuleList);
            when(alertTemplateRuleMapper.selectPage(eq(page), any())).thenReturn(rulePage);

            AlertTemplateRuleItem ruleItem1 = new AlertTemplateRuleItem().setId(1L).setTemplateRuleId(1L).setAction(
                    "normal")
                .setOperate(">=").setLimitValue("50").setRuleMark("A").setRuleExpName("cpu").setUnit("%");
            AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setId(2L).setTemplateRuleId(1L).setAction(
                "increase").setOperate(">=").setLimitValue("50").setRuleMark("B").setRuleExpName("cpu").setUnit("");
            AlertTemplateRuleItem ruleItem3 = new AlertTemplateRuleItem().setId(3L).setTemplateRuleId(1L).setAction(
                "decrease").setOperate(">=").setLimitValue("50").setRuleMark("C").setRuleExpName("cpu").setUnit("");
            AlertTemplateRuleItem ruleItem4 = new AlertTemplateRuleItem().setId(4L).setTemplateRuleId(1L).setAction(
                "normal").setOperate(">=").setLimitValue("50").setRuleMark("D").setRuleExpName("cpu").setUnit("");
            List<AlertTemplateRuleItem> alertTemplateRuleItems = new ArrayList<>();
            alertTemplateRuleItems.add(ruleItem1);
            alertTemplateRuleItems.add(ruleItem2);
            alertTemplateRuleItems.add(ruleItem3);
            alertTemplateRuleItems.add(ruleItem4);
            when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertTemplateRuleItems);

            AlertTemplateRuleItemParam ruleItemParam1 =
                new AlertTemplateRuleItemParam().setId(1L).setItemId(1L).setParamValue("val1");
            AlertTemplateRuleItemParam ruleItemParam2 =
                new AlertTemplateRuleItemParam().setId(2L).setItemId(2L).setParamValue("val2");
            List<AlertTemplateRuleItemParam> itemParamList = new ArrayList<>();
            itemParamList.add(ruleItemParam1);
            itemParamList.add(ruleItemParam2);
            when(tRuleItemParamMapper.selectList(any())).thenReturn(itemParamList);

            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn("name");

            Page<AlertTemplateRuleDto> result = alertTemplateService.getTemplateRulePage(1L, "", page);

            verify(alertTemplateRuleMapper, times(1)).selectPage(eq(page), any());
            verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
            verify(tRuleItemParamMapper, times(1)).selectList(any());
            assertEquals(1, result.getTotal());
        }

    }

    @Test
    public void testGetTemplate() {
        AlertTemplate alertTemplate = new AlertTemplate().setId(1L);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplate);

        List<AlertTemplateRuleDto> ruleDtoList = new ArrayList<>();
        when(templateRuleService.getDtoListByTemplateId(anyLong())).thenReturn(ruleDtoList);

        AlertTemplateDto template = alertTemplateService.getTemplate(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(templateRuleService, times(1)).getDtoListByTemplateId(anyLong());
        assertEquals(alertTemplate.getId(), template.getId());
    }

    @Test
    public void testGetTemplateList() {
        List<AlertTemplate> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);

        List<AlertTemplate> templateList = alertTemplateService.getTemplateList();

        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list, templateList);
    }

    @Test
    public void testSaveTemplate1() {
        AlertTemplateReq templateReq = new AlertTemplateReq();
        templateReq.setId(1L);
        templateReq.setTemplateName("name");
        List<AlertTemplateRuleReq> templateRuleReqList = new ArrayList<>();
        AlertTemplateRuleReq ruleReq1 = new AlertTemplateRuleReq();
        ruleReq1.setRuleId(2L);
        templateRuleReqList.add(ruleReq1);
        templateReq.setTemplateRuleReqList(templateRuleReqList);
        AlertTemplate alertTemplate = new AlertTemplate();
        when(baseMapper.selectById(templateReq.getId())).thenReturn(alertTemplate);

        List<AlertTemplateRule> alertTemplateRules = new ArrayList<>();
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRules);

        for (AlertTemplateRuleReq alertTemplateRuleReq : templateRuleReqList) {
            AlertRule alertRule = new AlertRule().setId(1L);
            when(alertRuleMapper.selectById(alertTemplateRuleReq.getRuleId())).thenReturn(alertRule);
            when(alertTemplateRuleMapper.insert(any(AlertTemplateRule.class))).thenReturn(1);
            AlertRuleItem alertRuleItem1 = new AlertRuleItem().setRuleId(1L).setId(1L);
            List<AlertRuleItem> alertRuleItems = new ArrayList<>();
            alertRuleItems.add(alertRuleItem1);
            when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);
            when(alertTemplateRuleItemMapper.insert(any(AlertTemplateRuleItem.class))).thenReturn(1);
            AlertRuleItemParam ruleItemParam = new AlertRuleItemParam().setItemId(1L);
            List<AlertRuleItemParam> itemParamList = new ArrayList<>();
            itemParamList.add(ruleItemParam);
            when(ruleItemParamMapper.selectList(any())).thenReturn(itemParamList);
            when(templateRuleItemParamService.saveBatch(anyList())).thenReturn(true);
        }

        AlertTemplate result = alertTemplateService.saveTemplate(templateReq);

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(alertTemplateService, times(1)).saveOrUpdate(any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertRuleMapper, times(1)).selectById(anyLong());
        verify(alertTemplateRuleMapper, times(1)).insert(any(AlertTemplateRule.class));
        verify(alertRuleItemMapper, times(1)).selectList(any());
        verify(alertTemplateRuleItemMapper, times(1)).insert(any(AlertTemplateRuleItem.class));
        verify(ruleItemParamMapper, times(1)).selectList(any());
        verify(templateRuleItemParamService, times(1)).saveBatch(anyList());
        assertEquals(templateReq.getTemplateName(), result.getTemplateName());
    }

    @Test
    public void testSaveTemplate2() {
        AlertTemplateReq templateReq = new AlertTemplateReq();
        templateReq.setTemplateName("name");
        List<AlertTemplateRuleReq> templateRuleReqList = new ArrayList<>();
        AlertTemplateRuleReq ruleReq2 = new AlertTemplateRuleReq();
        ruleReq2.setRuleId(2L);
        templateRuleReqList.add(ruleReq2);
        templateReq.setTemplateRuleReqList(templateRuleReqList);

        List<AlertTemplateRule> alertTemplateRules = new ArrayList<>();
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRules);

        for (AlertTemplateRuleReq alertTemplateRuleReq : templateRuleReqList) {
            AlertRule alertRule = new AlertRule().setId(1L);
            when(alertRuleMapper.selectById(alertTemplateRuleReq.getRuleId())).thenReturn(alertRule);
            when(alertTemplateRuleMapper.insert(any(AlertTemplateRule.class))).thenReturn(1);

            AlertRuleItem alertRuleItem1 = new AlertRuleItem().setRuleId(1L).setId(1L);
            List<AlertRuleItem> alertRuleItems = new ArrayList<>();
            alertRuleItems.add(alertRuleItem1);
            when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);
            when(alertTemplateRuleItemMapper.insert(any(AlertTemplateRuleItem.class))).thenReturn(1);

            List<AlertRuleItemParam> itemParamList = new ArrayList<>();
            when(ruleItemParamMapper.selectList(any())).thenReturn(itemParamList);
        }

        AlertTemplate result = alertTemplateService.saveTemplate(templateReq);

        verify(alertTemplateService, times(1)).saveOrUpdate(any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertRuleMapper, times(1)).selectById(anyLong());
        verify(alertTemplateRuleMapper, times(1)).insert(any(AlertTemplateRule.class));
        verify(alertRuleItemMapper, times(1)).selectList(any());
        verify(alertTemplateRuleItemMapper, times(1)).insert(any(AlertTemplateRuleItem.class));
        verify(ruleItemParamMapper, times(1)).selectList(any());
        assertEquals(templateReq.getTemplateName(), result.getTemplateName());
    }

    @Test
    public void testSaveTemplate3() {
        AlertTemplateReq templateReq = new AlertTemplateReq();
        templateReq.setId(1L);
        templateReq.setTemplateName("name");
        List<AlertTemplateRuleReq> templateRuleReqList = new ArrayList<>();
        AlertTemplateRuleReq ruleReq1 = new AlertTemplateRuleReq();
        ruleReq1.setRuleId(1L);
        ruleReq1.setTemplateRuleId(1L);
        templateRuleReqList.add(ruleReq1);
        templateReq.setTemplateRuleReqList(templateRuleReqList);

        AlertTemplate alertTemplate = new AlertTemplate();
        when(baseMapper.selectById(templateReq.getId())).thenReturn(alertTemplate);

        AlertTemplateRule templateRule = new AlertTemplateRule().setId(3L);
        List<AlertTemplateRule> alertTemplateRules = new ArrayList<>();
        alertTemplateRules.add(templateRule);
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRules);
        when(alertTemplateRuleMapper.updateById(any(AlertTemplateRule.class))).thenReturn(1);
        when(alertTemplateRuleItemMapper.update(any(), any())).thenReturn(1);

        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setTemplateId(1L).setId(1L);
        when(alertTemplateRuleMapper.selectById(alertTemplateRule.getTemplateId()))
            .thenReturn(alertTemplateRule);

        AlertTemplate result = alertTemplateService.saveTemplate(templateReq);

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(alertTemplateService, times(1)).saveOrUpdate(any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertTemplateRuleMapper, times(2)).updateById(any(AlertTemplateRule.class));
        verify(alertTemplateRuleItemMapper, times(1)).update(any(), any());
        verify(alertTemplateRuleMapper, times(1)).selectById(anyLong());
        assertEquals(templateReq.getTemplateName(), result.getTemplateName());
    }

    @Test
    public void testGetTemplateRuleListByIdNull() {
        List<AlertTemplateRule> templateRuleList = new ArrayList<>();
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(templateRuleList);
        List<AlertTemplateRuleDto> templateRuleDtoList = alertTemplateService.getTemplateRuleListById(anyLong());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        assertEquals(0, templateRuleDtoList.size());
    }

    @Test
    public void testGetTemplateRuleListById() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            List<AlertTemplateRule> templateRuleList = new ArrayList<>();
            AlertTemplateRule templateRule = new AlertTemplateRule().setId(1L);
            templateRuleList.add(templateRule);
            when(alertTemplateRuleMapper.selectList(any())).thenReturn(templateRuleList);

            AlertTemplateRuleItem ruleItem1 = new AlertTemplateRuleItem().setId(1L).setTemplateRuleId(1L).setAction(
                "normal").setOperate(">=").setLimitValue("50").setRuleMark("A").setRuleExpName("cpu").setUnit("%");
            AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setId(2L).setTemplateRuleId(1L).setAction(
                "increase").setOperate(">=").setLimitValue("50").setRuleMark("B").setRuleExpName("cpu").setUnit("");
            AlertTemplateRuleItem ruleItem3 = new AlertTemplateRuleItem().setId(3L).setTemplateRuleId(1L).setAction(
                "decrease").setOperate(">=").setLimitValue("50").setRuleMark("C").setRuleExpName("cpu").setUnit("");
            AlertTemplateRuleItem ruleItem4 = new AlertTemplateRuleItem().setId(4L).setTemplateRuleId(1L).setAction(
                "normal").setOperate(">=").setLimitValue("50").setRuleMark("D").setRuleExpName("cpu").setUnit("");
            List<AlertTemplateRuleItem> alertTemplateRuleItems = new ArrayList<>();
            alertTemplateRuleItems.add(ruleItem1);
            alertTemplateRuleItems.add(ruleItem2);
            alertTemplateRuleItems.add(ruleItem3);
            alertTemplateRuleItems.add(ruleItem4);
            when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertTemplateRuleItems);

            AlertTemplateRuleItemParam ruleItemParam1 =
                new AlertTemplateRuleItemParam().setId(1L).setItemId(1L).setParamValue("val1");
            AlertTemplateRuleItemParam ruleItemParam2 =
                new AlertTemplateRuleItemParam().setId(2L).setItemId(2L).setParamValue("val2");
            List<AlertTemplateRuleItemParam> itemParamList = new ArrayList<>();
            itemParamList.add(ruleItemParam1);
            itemParamList.add(ruleItemParam2);
            when(tRuleItemParamMapper.selectList(any())).thenReturn(itemParamList);

            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn("name");

            List<AlertTemplateRuleDto> templateRuleDtoList = alertTemplateService.getTemplateRuleListById(anyLong());

            verify(alertTemplateRuleMapper, times(1)).selectList(any());
            verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
            verify(tRuleItemParamMapper, times(1)).selectList(any());
            assertEquals(1, templateRuleDtoList.size());
        }
    }

    @Test(expected = ServiceException.class)
    public void testDelTemplateThrowException() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            AlertClusterNodeConf nodeConf = new AlertClusterNodeConf();
            List<AlertClusterNodeConf> nodeConfList = new ArrayList<>();
            nodeConfList.add(nodeConf);
            when(nodeConfService.list(any())).thenReturn(nodeConfList);
            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn("templateIsUsed");
            alertTemplateService.delTemplate(anyLong());
            verify(nodeConfService, times(1)).list(any());
        }
    }
    @Test
    public void testDelTemplate1() {
        List<AlertClusterNodeConf> nodeConfList = new ArrayList<>();
        when(nodeConfService.list(any())).thenReturn(nodeConfList);

        when(baseMapper.update(any(),any())).thenReturn(1);

        List<AlertTemplateRule> templateRuleList = new ArrayList<>();
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(templateRuleList);
        when(alertTemplateRuleMapper.update(any(),any())).thenReturn(1);

        alertTemplateService.delTemplate(anyLong());
        verify(nodeConfService, times(1)).list(any());
        verify(baseMapper, times(1)).update(any(),any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertTemplateRuleMapper, times(1)).update(any(),any());
    }
    @Test
    public void testDelTemplate2() {
        List<AlertClusterNodeConf> nodeConfList = new ArrayList<>();
        when(nodeConfService.list(any())).thenReturn(nodeConfList);

        when(baseMapper.update(any(),any())).thenReturn(1);

        AlertTemplateRule templateRule = new AlertTemplateRule().setId(1L);
        List<AlertTemplateRule> templateRuleList = new ArrayList<>();
        templateRuleList.add(templateRule);
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(templateRuleList);
        when(alertTemplateRuleMapper.update(any(),any())).thenReturn(1);
        when(alertTemplateRuleItemMapper.update(any(),any())).thenReturn(1);

        alertTemplateService.delTemplate(anyLong());
        verify(nodeConfService, times(1)).list(any());
        verify(baseMapper, times(1)).update(any(),any());
        verify(alertTemplateRuleMapper, times(1)).selectList(any());
        verify(alertTemplateRuleMapper, times(1)).update(any(),any());
        verify(alertTemplateRuleMapper, times(1)).update(any(),any());
    }
}

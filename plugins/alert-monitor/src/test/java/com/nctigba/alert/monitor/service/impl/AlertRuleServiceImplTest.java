/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.config.properties.AlertProperty;
import com.nctigba.alert.monitor.config.properties.RuleItemProperty;
import com.nctigba.alert.monitor.dto.AlertRuleDto;
import com.nctigba.alert.monitor.dto.RuleItemPropertyDto;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.entity.AlertRuleItem;
import com.nctigba.alert.monitor.entity.AlertRuleItemParam;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemParamMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.model.RuleReq;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the AlertRuleServiceImpl
 *
 * @since 2023/7/13 09:21
 */
@RunWith(SpringRunner.class)
public class AlertRuleServiceImplTest {
    @InjectMocks
    @Spy
    private AlertRuleServiceImpl alertRuleService;
    @Mock
    private AlertProperty alertProperty;
    @Mock
    private AlertRuleItemMapper alertRuleItemMapper;
    @Mock
    private AlertRuleItemParamMapper ruleItemParamMapper;
    @Mock
    private AlertRuleMapper baseMapper;


    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertRule.class);
    }

    @Test
    public void testGetRulePageNull() {
        Page page = new Page(1, 10);
        Page<AlertRule> alertRulePage = new Page<>(1, 10);
        when(baseMapper.selectPage(eq(page), any())).thenReturn(alertRulePage);

        RuleReq ruleReq = new RuleReq();
        Page<AlertRuleDto> ruleDtoPage = alertRuleService.getRulePage(ruleReq, page);
        verify(baseMapper, times(1)).selectPage(eq(page), any());
        assertEquals(0L, ruleDtoPage.getTotal());
    }

    @Test
    public void testGetRulePage() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            Page page = new Page(1, 10);
            AlertRule alertRule = new AlertRule().setId(1L);
            List<AlertRule> alertRuleList = new ArrayList<>();
            alertRuleList.add(alertRule);
            Page<AlertRule> alertRulePage = new Page<>(1, 10);
            alertRulePage.setRecords(alertRuleList).setTotal(1);
            when(baseMapper.selectPage(eq(page), any())).thenReturn(alertRulePage);

            AlertRuleItem ruleItem1 = new AlertRuleItem().setId(1L).setRuleId(1L).setAction("normal")
                .setOperate(">=").setLimitValue("50").setRuleMark("A").setRuleExpName("cpu").setUnit("%");
            AlertRuleItem ruleItem2 = new AlertRuleItem().setId(2L).setRuleId(1L).setAction(
                "increase").setOperate(">=").setLimitValue("50").setRuleMark("B").setRuleExpName("cpu").setUnit("");
            AlertRuleItem ruleItem3 = new AlertRuleItem().setId(3L).setRuleId(1L).setAction(
                "decrease").setOperate(">=").setLimitValue("50").setRuleMark("C").setRuleExpName("cpu").setUnit("");
            AlertRuleItem ruleItem4 = new AlertRuleItem().setId(4L).setRuleId(1L).setAction(
                "normal").setOperate(">=").setLimitValue("50").setRuleMark("D").setRuleExpName("cpu").setUnit("");
            List<AlertRuleItem> alertRuleItems = new ArrayList<>();
            alertRuleItems.add(ruleItem1);
            alertRuleItems.add(ruleItem2);
            alertRuleItems.add(ruleItem3);
            alertRuleItems.add(ruleItem4);
            when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);

            List<AlertRuleItemParam> itemParamList = new ArrayList<>();
            AlertRuleItemParam ruleItemParam1 =
                new AlertRuleItemParam().setId(1L).setItemId(1L).setParamValue("val1");
            AlertRuleItemParam ruleItemParam2 =
                new AlertRuleItemParam().setId(2L).setItemId(2L).setParamValue("val2");
            itemParamList.add(ruleItemParam1);
            itemParamList.add(ruleItemParam2);
            when(ruleItemParamMapper.selectList(any())).thenReturn(itemParamList);

            RuleReq ruleReq = new RuleReq();
            Page<AlertRuleDto> ruleDtoPage = alertRuleService.getRulePage(ruleReq, page);
            verify(baseMapper, times(1)).selectPage(eq(page), any());
            verify(alertRuleItemMapper, times(1)).selectList(any());
            verify(ruleItemParamMapper, times(1)).selectList(any());
            assertEquals(1L, ruleDtoPage.getTotal());
        }
    }

    @Test(expected = ServiceException.class)
    public void testGetRuleByIdThrowException() {
        AlertRule alertRule = null;
        when(baseMapper.selectById(anyLong())).thenReturn(alertRule);
        alertRuleService.getRuleById(anyLong());
    }

    @Test
    public void testGetRuleById() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            AlertRule alertRule = new AlertRule().setId(1L).setRuleName("ruleName");
            when(baseMapper.selectById(anyLong())).thenReturn(alertRule);

            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn("ruleName");

            List<AlertRuleItem> alertRuleItems = new ArrayList<>();
            AlertRuleItem alertRuleItem = new AlertRuleItem().setId(1L).setRuleId(1L);
            alertRuleItems.add(alertRuleItem);
            when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);

            List<AlertRuleItemParam> itemParamList = new ArrayList<>();
            AlertRuleItemParam ruleItemParam1 =
                new AlertRuleItemParam().setId(1L).setItemId(1L).setParamValue("val1");
            itemParamList.add(ruleItemParam1);
            when(ruleItemParamMapper.selectList(any())).thenReturn(itemParamList);

            AlertRule result = alertRuleService.getRuleById(anyLong());

            verify(baseMapper, times(1)).selectById(anyLong());
            verify(alertRuleItemMapper, times(1)).selectList(any());
            verify(ruleItemParamMapper, times(1)).selectList(any());
            assertEquals(alertRule.getId(), result.getId());
        }
    }

    @Test
    public void testGetRuleItemProperties() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            List<RuleItemProperty> ruleItems = new ArrayList<>();
            RuleItemProperty ruleItemProperty = new RuleItemProperty();
            ruleItemProperty.setName("name");
            ruleItems.add(ruleItemProperty);
            when(alertProperty.getRuleItems()).thenReturn(ruleItems);

            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn("name");

            List<RuleItemPropertyDto> ruleItemProperties = alertRuleService.getRuleItemProperties();

            verify(alertProperty).getRuleItems();
            assertEquals(ruleItems.size(), ruleItemProperties.size());
            assertEquals("name", ruleItemProperties.get(0).getI18nName());
        }
    }

    @Test
    public void testGetRuleList() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            List<AlertRule> alertRules = new ArrayList<>();
            AlertRule alertRule = new AlertRule().setId(1L);
            alertRules.add(alertRule);
            when(baseMapper.selectList(any())).thenReturn(alertRules);

            AlertRuleItem ruleItem1 = new AlertRuleItem().setId(1L).setRuleId(1L).setAction("normal")
                .setOperate(">=").setLimitValue("50").setRuleMark("A").setRuleExpName("cpu").setUnit("%");
            AlertRuleItem ruleItem2 = new AlertRuleItem().setId(2L).setRuleId(1L).setAction(
                "increase").setOperate(">=").setLimitValue("50").setRuleMark("B").setRuleExpName("cpu").setUnit("");
            AlertRuleItem ruleItem3 = new AlertRuleItem().setId(3L).setRuleId(1L).setAction(
                "decrease").setOperate(">=").setLimitValue("50").setRuleMark("C").setRuleExpName("cpu").setUnit("");
            AlertRuleItem ruleItem4 = new AlertRuleItem().setId(4L).setRuleId(1L).setAction(
                "normal").setOperate(">=").setLimitValue("50").setRuleMark("D").setRuleExpName("cpu").setUnit("");
            List<AlertRuleItem> alertRuleItems = new ArrayList<>();
            alertRuleItems.add(ruleItem1);
            alertRuleItems.add(ruleItem2);
            alertRuleItems.add(ruleItem3);
            alertRuleItems.add(ruleItem4);
            when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);

            List<AlertRuleItemParam> itemParamList = new ArrayList<>();
            AlertRuleItemParam ruleItemParam1 =
                new AlertRuleItemParam().setId(1L).setItemId(1L).setParamValue("val1");
            AlertRuleItemParam ruleItemParam2 =
                new AlertRuleItemParam().setId(2L).setItemId(2L).setParamValue("val2");
            itemParamList.add(ruleItemParam1);
            itemParamList.add(ruleItemParam2);
            when(ruleItemParamMapper.selectList(any())).thenReturn(itemParamList);

            String name = "ruleExpName";
            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn(name);

            List<AlertRuleDto> ruleList = alertRuleService.getRuleList();

            verify(baseMapper, times(1)).selectList(any());
            verify(alertRuleItemMapper, times(1)).selectList(any());
            verify(ruleItemParamMapper, times(1)).selectList(any());
            assertEquals(alertRules.size(), ruleList.size());
        }
    }
}

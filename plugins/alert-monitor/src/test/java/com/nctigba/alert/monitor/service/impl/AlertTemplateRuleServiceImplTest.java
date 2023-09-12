/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
import com.nctigba.alert.monitor.service.PrometheusService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the AlertTemplateRuleServiceImpl
 *
 * @since 2023/7/13 16:40
 */
@RunWith(SpringRunner.class)
public class AlertTemplateRuleServiceImplTest {
    @InjectMocks
    @Spy
    private AlertTemplateRuleServiceImpl alertTemplateRuleService;
    @Mock
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;
    @Mock
    private AlertTemplateRuleItemService templateRuleItemService;
    @Mock
    private AlertTemplateRuleMapper baseMapper;
    @Mock
    private PrometheusService prometheusService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertTemplateRule.class);
    }

    @Test(expected = ServiceException.class)
    public void testGetTemplateRuleThrowException() {
        AlertTemplateRule alertTemplateRule = null;
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        alertTemplateRuleService.getTemplateRule(anyLong());
        verify(baseMapper, times(1)).selectById(anyLong());
    }

    @Test
    public void testGetTemplateRule() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setTemplateId(1L).setId(1L).setRuleId(1L);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRule);

        List<AlertTemplateRuleItem> alertTemplateRuleItems = new ArrayList<>();
        AlertTemplateRuleItem ruleItem = new AlertTemplateRuleItem().setId(1L).setTemplateRuleId(1L);
        alertTemplateRuleItems.add(ruleItem);
        when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertTemplateRuleItems);

        AlertTemplateRule templateRule = alertTemplateRuleService.getTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
        assertEquals(alertTemplateRule.getId(), templateRule.getId());
    }

    @Test
    public void testSaveIndexTemplateRule() {
        AlertTemplateRuleItem ruleItem = new AlertTemplateRuleItem().setId(1L).setTemplateRuleId(1L)
            .setAction("normal").setOperate(">=").setLimitValue("50").setRuleMark("A").setRuleExpName("cpu")
            .setUnit("%");
        AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setTemplateRuleId(1L).setAction(
            "normal").setOperate(">=").setLimitValue("50").setRuleMark("B").setRuleExpName("cpu").setUnit("");
        AlertTemplateRuleItem ruleItem3 = new AlertTemplateRuleItem().setId(3L).setTemplateRuleId(1L).setAction(
            "normal").setOperate(">=").setLimitValue("50").setRuleMark("C").setRuleExpName("cpu").setUnit("");
        List<AlertTemplateRuleItem> templateRuleItemList = new ArrayList<>();
        templateRuleItemList.add(ruleItem);
        templateRuleItemList.add(ruleItem2);
        templateRuleItemList.add(ruleItem3);
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setRuleType(CommonConstants.INDEX_RULE)
            .setTemplateId(1L);
        alertTemplateRule.setAlertRuleItemList(templateRuleItemList);

        when(baseMapper.insert(alertTemplateRule)).thenReturn(1);
        when(templateRuleItemService.saveOrUpdateBatch(templateRuleItemList)).thenReturn(true);
        doNothing().when(prometheusService).updateRuleByTemplateRule(alertTemplateRule);

        AlertTemplateRule alertTemplateRule0 = alertTemplateRuleService.saveTemplateRule(alertTemplateRule);

        verify(baseMapper, times(1)).insert(alertTemplateRule);
        verify(templateRuleItemService, times(1)).saveOrUpdateBatch(templateRuleItemList);
        verify(prometheusService, times(1)).updateRuleByTemplateRule(any());
        assertEquals(3, alertTemplateRule0.getAlertRuleItemList().size());
    }

    @Test
    public void testSaveIndexTemplateRuleWithoutTemplateId() {
        AlertTemplateRuleItem ruleItem = new AlertTemplateRuleItem().setId(1L).setTemplateRuleId(1L)
            .setAction("normal").setOperate(">=").setLimitValue("50").setRuleMark("A").setRuleExpName("cpu")
            .setUnit("%");
        AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setTemplateRuleId(1L).setAction(
            "normal").setOperate(">=").setLimitValue("50").setRuleMark("B").setRuleExpName("cpu").setUnit("");
        AlertTemplateRuleItem ruleItem3 = new AlertTemplateRuleItem().setId(3L).setTemplateRuleId(1L).setAction(
            "normal").setOperate(">=").setLimitValue("50").setRuleMark("C").setRuleExpName("cpu").setUnit("");
        List<AlertTemplateRuleItem> templateRuleItemList = new ArrayList<>();
        templateRuleItemList.add(ruleItem);
        templateRuleItemList.add(ruleItem2);
        templateRuleItemList.add(ruleItem3);
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setRuleType(CommonConstants.INDEX_RULE)
            .setId(1L);
        alertTemplateRule.setAlertRuleItemList(templateRuleItemList);

        when(baseMapper.selectById(alertTemplateRule.getId())).thenReturn(alertTemplateRule);
        when(baseMapper.updateById(alertTemplateRule)).thenReturn(1);
        when(templateRuleItemService.saveOrUpdateBatch(templateRuleItemList)).thenReturn(true);

        AlertTemplateRule alertTemplateRule0 = alertTemplateRuleService.saveTemplateRule(alertTemplateRule);

        verify(baseMapper, times(1)).selectById(alertTemplateRule.getId());
        verify(baseMapper, times(1)).updateById(alertTemplateRule);
        verify(templateRuleItemService, times(1)).saveOrUpdateBatch(templateRuleItemList);
        assertEquals(3, alertTemplateRule0.getAlertRuleItemList().size());
    }

    @Test
    public void testSaveLogTemplateRule() {
        AlertTemplateRuleItem ruleItem = new AlertTemplateRuleItem().setId(1L).setTemplateRuleId(1L);
        AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setTemplateRuleId(1L).setAction("normal");
        AlertTemplateRuleItem ruleItem3 = new AlertTemplateRuleItem().setId(3L).setTemplateRuleId(1L).setRuleMark("C");
        List<AlertTemplateRuleItem> templateRuleItemList = new ArrayList<>();
        templateRuleItemList.add(ruleItem);
        templateRuleItemList.add(ruleItem2);
        templateRuleItemList.add(ruleItem3);
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setRuleType(CommonConstants.LOG_RULE)
            .setTemplateId(1L);
        alertTemplateRule.setAlertRuleItemList(templateRuleItemList);

        when(baseMapper.insert(alertTemplateRule)).thenReturn(1);
        when(templateRuleItemService.saveOrUpdateBatch(templateRuleItemList)).thenReturn(true);

        AlertTemplateRule alertTemplateRule0 = alertTemplateRuleService.saveTemplateRule(alertTemplateRule);

        verify(baseMapper, times(1)).insert(alertTemplateRule);
        verify(templateRuleItemService, times(1)).saveOrUpdateBatch(templateRuleItemList);
        assertEquals(3, alertTemplateRule0.getAlertRuleItemList().size());
    }

    @Test
    public void testGetListByTemplateId() {
        List<AlertTemplateRule> alertRules = new ArrayList<>();
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setId(1L);
        alertRules.add(alertTemplateRule);
        when(baseMapper.selectList(any())).thenReturn(alertRules);

        List<AlertTemplateRuleItem> alertRuleItems = new ArrayList<>();
        AlertTemplateRuleItem ruleItem1 = new AlertTemplateRuleItem().setId(1L).setTemplateRuleId(1L);
        AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setId(2L).setTemplateRuleId(1L);
        AlertTemplateRuleItem ruleItem3 = new AlertTemplateRuleItem().setId(3L).setTemplateRuleId(1L);
        AlertTemplateRuleItem ruleItem4 = new AlertTemplateRuleItem().setId(4L).setTemplateRuleId(1L);
        alertRuleItems.add(ruleItem1);
        alertRuleItems.add(ruleItem2);
        alertRuleItems.add(ruleItem3);
        alertRuleItems.add(ruleItem4);
        when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);

        List<AlertTemplateRule> list = alertTemplateRuleService.getListByTemplateId(anyLong());

        verify(baseMapper, times(1)).selectList(any());
        verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
        assertEquals(1, list.size());
    }

    @Test
    public void testEnableIndexTemplateRule() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setId(1L).setRuleType(CommonConstants.INDEX_RULE);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        when(baseMapper.updateById(alertTemplateRule)).thenReturn(1);
        doNothing().when(prometheusService).updateRuleByTemplateRule(alertTemplateRule);

        alertTemplateRuleService.enableTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(baseMapper, times(1)).updateById(any());
        verify(prometheusService, times(1)).updateRuleByTemplateRule(any());
    }

    @Test
    public void testEnableLogTemplateRule() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setId(1L).setRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        when(baseMapper.updateById(alertTemplateRule)).thenReturn(1);

        alertTemplateRuleService.enableTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(baseMapper, times(1)).updateById(any());
    }

    @Test
    public void testDisableIndexTemplateRule() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setId(1L).setRuleType(CommonConstants.INDEX_RULE);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        when(baseMapper.updateById(alertTemplateRule)).thenReturn(1);
        doNothing().when(prometheusService).updateRuleByTemplateRule(alertTemplateRule);

        alertTemplateRuleService.disableTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(baseMapper, times(1)).updateById(any());
        verify(prometheusService, times(1)).removeRuleByTemplateRule(any());
    }

    @Test
    public void testDisableLogTemplateRule() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setId(1L).setRuleType(CommonConstants.LOG_RULE);
        when(baseMapper.selectById(anyLong())).thenReturn(alertTemplateRule);
        when(baseMapper.updateById(alertTemplateRule)).thenReturn(1);

        alertTemplateRuleService.disableTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(baseMapper, times(1)).updateById(any());
    }
}

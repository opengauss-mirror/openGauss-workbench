/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.dto.AlertTemplateRuleDto;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItemParam;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemParamMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemParamService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleItemService;
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
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
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
    private AlertTemplateRuleItemParamMapper ruleItemParamMapper;
    @Mock
    private AlertTemplateRuleItemParamService ruleItemParamService;
    @Mock
    private AlertTemplateRuleMapper baseMapper;

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

        List<AlertTemplateRuleItemParam> itemParamList = new ArrayList<>();
        AlertTemplateRuleItemParam ruleItemParam = new AlertTemplateRuleItemParam();
        ruleItemParam.setId(1L).setItemId(1L);
        itemParamList.add(ruleItemParam);
        when(ruleItemParamMapper.selectList(any())).thenReturn(itemParamList);

        AlertTemplateRule templateRule = alertTemplateRuleService.getTemplateRule(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
        verify(ruleItemParamMapper, times(1)).selectList(any());
        assertEquals(alertTemplateRule.getId(), templateRule.getId());
    }

    @Test
    public void testSaveTemplateRuleWithoutRuleItem() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule();
        AlertTemplateRuleDto alertTemplateRuleDto = alertTemplateRuleService.saveTemplateRule(alertTemplateRule);
        verify(alertTemplateRuleService, times(1)).saveOrUpdate(alertTemplateRule);
        assertNull(alertTemplateRuleDto.getAlertRuleItemList());
    }

    @Test
    public void testSaveTemplateRule() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setId(1L);
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
            alertTemplateRule.setAlertRuleItemList(templateRuleItemList);
            List<AlertTemplateRuleItemParam> itemParamList = new ArrayList<>();
            AlertTemplateRuleItemParam ruleItemParam = new AlertTemplateRuleItemParam().setItemId(1L).setId(1L);
            itemParamList.add(ruleItemParam);
            ruleItem.setItemParamList(itemParamList);
            ruleItem2.setItemParamList(new ArrayList<>());
            AlertTemplateRuleItemParam ruleItemParam3 = new AlertTemplateRuleItemParam().setItemId(3L);
            List<AlertTemplateRuleItemParam> itemParamList3 = new ArrayList<>();
            itemParamList3.add(ruleItemParam3);
            ruleItem3.setItemParamList(itemParamList3);

            when(templateRuleItemService.saveOrUpdate(ruleItem)).thenReturn(true);
            when(templateRuleItemService.saveOrUpdate(ruleItem2)).thenReturn(true);

            when(ruleItemParamService.saveOrUpdateBatch(itemParamList)).thenReturn(true);

            mockedStatic.when(() -> MessageSourceUtil.get(any())).thenReturn("info");

            AlertTemplateRuleDto alertTemplateRuleDto = alertTemplateRuleService.saveTemplateRule(alertTemplateRule);

            verify(alertTemplateRuleService, times(1)).saveOrUpdate(alertTemplateRule);
            verify(templateRuleItemService, times(3)).saveOrUpdate(any());
            verify(ruleItemParamService, times(1)).saveOrUpdateBatch(itemParamList);
            assertEquals(3, alertTemplateRuleDto.getAlertRuleItemList().size());
            assertEquals(1, alertTemplateRuleDto.getAlertRuleItemList().get(0).getItemParamList().size());
        }
    }

    @Test
    public void testGetDtoListByTemplateId() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            List<AlertTemplateRule> alertRules = new ArrayList<>();
            AlertTemplateRule alertTemplateRule = new AlertTemplateRule().setId(1L);
            alertRules.add(alertTemplateRule);
            when(baseMapper.selectList(any())).thenReturn(alertRules);

            List<AlertTemplateRuleItem> alertRuleItems = new ArrayList<>();
            AlertTemplateRuleItem ruleItem1 = new AlertTemplateRuleItem().setId(1L).setTemplateRuleId(1L).setAction(
                "normal").setOperate(">=").setLimitValue("50").setRuleMark("A").setRuleExpName("cpu").setUnit("%");
            AlertTemplateRuleItem ruleItem2 = new AlertTemplateRuleItem().setId(2L).setTemplateRuleId(1L).setAction(
                "increase").setOperate(">=").setLimitValue("50").setRuleMark("B").setRuleExpName("cpu").setUnit("");
            AlertTemplateRuleItem ruleItem3 = new AlertTemplateRuleItem().setId(3L).setTemplateRuleId(1L).setAction(
                "decrease").setOperate(">=").setLimitValue("50").setRuleMark("C").setRuleExpName("cpu").setUnit("");
            AlertTemplateRuleItem ruleItem4 = new AlertTemplateRuleItem().setId(4L).setTemplateRuleId(1L).setAction(
                "normal").setOperate(">=").setLimitValue("50").setRuleMark("D").setRuleExpName("cpu").setUnit("");
            alertRuleItems.add(ruleItem1);
            alertRuleItems.add(ruleItem2);
            alertRuleItems.add(ruleItem3);
            alertRuleItems.add(ruleItem4);
            when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);

            List<AlertTemplateRuleItemParam> itemParamList = new ArrayList<>();
            AlertTemplateRuleItemParam ruleItemParam = new AlertTemplateRuleItemParam().setItemId(1L).setId(1L);
            itemParamList.add(ruleItemParam);
            when(ruleItemParamMapper.selectList(any())).thenReturn(itemParamList);

            List<AlertTemplateRuleDto> dtoList = alertTemplateRuleService.getDtoListByTemplateId(anyLong());

            verify(baseMapper, times(1)).selectList(any());
            verify(alertTemplateRuleItemMapper, times(1)).selectList(any());
            verify(ruleItemParamMapper, times(1)).selectList(any());
            assertEquals(1, dtoList.size());
        }

    }
}

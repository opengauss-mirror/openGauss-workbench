/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.config.properties.AlertProperty;
import com.nctigba.alert.monitor.config.properties.RuleItemProperty;
import com.nctigba.alert.monitor.dto.RuleItemPropertyDto;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.entity.AlertRuleItem;
import com.nctigba.alert.monitor.entity.AlertRuleItemExpSrc;
import com.nctigba.alert.monitor.entity.AlertRuleItemSrc;
import com.nctigba.alert.monitor.mapper.AlertRuleItemExpSrcMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleItemSrcMapper;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.model.RuleReq;
import com.nctigba.alert.monitor.service.AlertRuleItemService;
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
    private AlertRuleMapper baseMapper;
    @Mock
    private AlertRuleItemSrcMapper ruleItemSrcMapper;
    @Mock
    private AlertRuleItemExpSrcMapper ruleItemExpSrcMapper;
    @Mock
    private AlertRuleItemService ruleItemService;


    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertRule.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertRuleItem.class);
    }

    @Test
    public void testGetRulePageNull() {
        Page page = new Page(1, 10);
        Page<AlertRule> alertRulePage = new Page<>(1, 10);
        when(baseMapper.selectPage(eq(page), any())).thenReturn(alertRulePage);

        RuleReq ruleReq = new RuleReq();
        Page<AlertRule> ruleDtoPage = alertRuleService.getRulePage(ruleReq, page);
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

            AlertRuleItem ruleItem = new AlertRuleItem().setId(1L).setRuleId(1L).setAction("normal")
                .setOperate(">=").setLimitValue("50").setRuleMark("A").setRuleExpName("cpu").setUnit("%");
            List<AlertRuleItem> alertRuleItems = new ArrayList<>();
            alertRuleItems.add(ruleItem);
            when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);

            RuleReq ruleReq = new RuleReq();
            Page<AlertRule> ruleDtoPage = alertRuleService.getRulePage(ruleReq, page);

            verify(baseMapper, times(1)).selectPage(eq(page), any());
            verify(alertRuleItemMapper, times(1)).selectList(any());
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
        AlertRule alertRule = new AlertRule().setId(1L).setRuleName("ruleName");
        when(baseMapper.selectById(anyLong())).thenReturn(alertRule);
        List<AlertRuleItem> alertRuleItems = new ArrayList<>();
        AlertRuleItem alertRuleItem = new AlertRuleItem().setId(1L).setRuleId(1L);
        alertRuleItems.add(alertRuleItem);
        when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);

        AlertRule result = alertRuleService.getRuleById(anyLong());

        verify(baseMapper, times(1)).selectById(anyLong());
        verify(alertRuleItemMapper, times(1)).selectList(any());
        assertEquals(alertRule.getId(), result.getId());
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
        List<AlertRule> alertRules = new ArrayList<>();
        AlertRule alertRule = new AlertRule().setId(1L);
        alertRules.add(alertRule);
        when(baseMapper.selectList(any())).thenReturn(alertRules);

        AlertRuleItem ruleItem1 = new AlertRuleItem().setId(1L).setRuleId(1L).setAction("normal")
            .setOperate(">=").setLimitValue("50").setRuleMark("A").setRuleExpName("cpu").setUnit("%");
        List<AlertRuleItem> alertRuleItems = new ArrayList<>();
        alertRuleItems.add(ruleItem1);
        when(alertRuleItemMapper.selectList(any())).thenReturn(alertRuleItems);

        List<AlertRule> ruleList = alertRuleService.getRuleList();

        verify(baseMapper, times(1)).selectList(any());
        verify(alertRuleItemMapper, times(1)).selectList(any());
        assertEquals(alertRules.size(), ruleList.size());
    }

    @Test
    public void testGetRuleItemSrcList() {
        List<AlertRuleItemSrc> list = new ArrayList<>();
        when(ruleItemSrcMapper.selectList(any())).thenReturn(list);
        List<AlertRuleItemSrc> ruleItemSrcList = alertRuleService.getRuleItemSrcList();
        verify(ruleItemSrcMapper, times(1)).selectList(any());
        assertEquals(list, ruleItemSrcList);
    }

    @Test
    public void testGetRuleItemExpSrcListByRuleItemSrcId() {
        List<AlertRuleItemExpSrc> list = new ArrayList<>();
        when(ruleItemExpSrcMapper.selectList(any())).thenReturn(list);
        List<AlertRuleItemExpSrc> result = alertRuleService.getRuleItemExpSrcListByRuleItemSrcId(anyLong());
        verify(ruleItemExpSrcMapper, times(1)).selectList(any());
        assertEquals(list, result);
    }

    @Test
    public void testSaveRuleWithoutDel() {
        when(baseMapper.insert(any())).thenReturn(1);
        List<AlertRuleItem> delItemList = new ArrayList<>();
        when(ruleItemService.list(any())).thenReturn(delItemList);
        when(ruleItemService.saveOrUpdateBatch(anyList())).thenReturn(true);

        AlertRuleItem ruleItem = new AlertRuleItem();
        List<AlertRuleItem> ruleItemList = new ArrayList<>();
        ruleItemList.add(ruleItem);
        AlertRule alertRule = new AlertRule().setAlertRuleItemList(ruleItemList);
        alertRuleService.saveRule(alertRule);

        verify(baseMapper, times(1)).insert(any());
        verify(ruleItemService, times(1)).list(any());
        verify(ruleItemService, times(1)).saveOrUpdateBatch(anyList());
    }

    @Test
    public void testSaveRuleWithDel() {
        AlertRuleItem alertRuleItem = new AlertRuleItem();
        List<AlertRuleItem> delItemList = new ArrayList<>();
        delItemList.add(alertRuleItem);
        when(ruleItemService.list(any())).thenReturn(delItemList);
        when(ruleItemService.update(any())).thenReturn(true);
        when(ruleItemService.saveOrUpdateBatch(anyList())).thenReturn(true);

        AlertRuleItem ruleItem = new AlertRuleItem();
        List<AlertRuleItem> ruleItemList = new ArrayList<>();
        ruleItemList.add(ruleItem);
        AlertRule alertRule = new AlertRule().setAlertRuleItemList(ruleItemList).setId(1L);
        alertRuleService.saveRule(alertRule);

        verify(ruleItemService, times(1)).list(any());
        verify(ruleItemService, times(1)).update(any());
        verify(ruleItemService, times(1)).saveOrUpdateBatch(anyList());
    }

    @Test
    public void testDelRuleById() {
        when(baseMapper.update(any(), any())).thenReturn(1);
        when(ruleItemService.update(any())).thenReturn(true);
        alertRuleService.delRuleById(anyLong());
        verify(baseMapper, times(1)).update(any(), any());
        verify(ruleItemService, times(1)).update(any());
    }
}

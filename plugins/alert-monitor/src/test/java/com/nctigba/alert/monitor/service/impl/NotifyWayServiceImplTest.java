/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.dto.NotifyWayDto;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.service.AlertRuleService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the NotifyWayServiceImpl
 *
 * @since 2023/7/14 19:20
 */
@RunWith(SpringRunner.class)
public class NotifyWayServiceImplTest {
    @InjectMocks
    @Spy
    private NotifyWayServiceImpl notifyWayService;
    @Mock
    private AlertRuleService ruleService;
    @Mock
    private AlertTemplateRuleService templateRuleService;
    @Mock
    private NotifyWayMapper baseMapper;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            NotifyWay.class);
    }

    @Test
    public void testGetListPage() {
        Page page = new Page(1, 10);
        Page<NotifyWayDto> notifyWayDtoPage = new Page<>(1, 10);
        when(baseMapper.selectDtoPage(eq(page), any())).thenReturn(notifyWayDtoPage);

        Page result = notifyWayService.getListPage("", "", page);

        verify(baseMapper, times(1)).selectDtoPage(eq(page), any());
        assertEquals(notifyWayDtoPage, result);
    }

    @Test
    public void testGetList() {
        List<NotifyWay> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);
        List<NotifyWay> resultList = notifyWayService.getList(anyString());
        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list, resultList);
    }

    @Test
    public void testSaveNotifyWay1() {
        NotifyWay notifyWay = new NotifyWay();
        when(baseMapper.insert(any(NotifyWay.class))).thenReturn(1);
        notifyWayService.saveNotifyWay(notifyWay);
        verify(baseMapper, times(1)).insert(any(NotifyWay.class));
    }

    @Test
    public void testSaveNotifyWay2() {
        NotifyWay notifyWay = new NotifyWay().setId(1L);
        when(baseMapper.updateById(any(NotifyWay.class))).thenReturn(1);
        notifyWayService.saveNotifyWay(notifyWay);
        verify(baseMapper, times(1)).insert(any(NotifyWay.class));
    }

    @Test(expected = ServiceException.class)
    public void testDelByIdThrowException1() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            List<AlertRule> ruleList = new ArrayList<>();
            AlertRule alertRule = new AlertRule();
            ruleList.add(alertRule);
            List<AlertTemplateRule> templateRuleList = new ArrayList<>();
            when(ruleService.list(any())).thenReturn(ruleList);
            when(templateRuleService.list(any())).thenReturn(templateRuleList);
            notifyWayService.delById(anyLong());
            verify(ruleService, times(1)).list(any());
            verify(templateRuleService, times(1)).list(any());
        }
    }

    @Test(expected = ServiceException.class)
    public void testDelByIdThrowException2() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            List<AlertRule> ruleList = new ArrayList<>();
            List<AlertTemplateRule> templateRuleList = new ArrayList<>();
            AlertTemplateRule alertTemplateRule = new AlertTemplateRule();
            templateRuleList.add(alertTemplateRule);
            when(ruleService.list(any())).thenReturn(ruleList);
            when(templateRuleService.list(any())).thenReturn(templateRuleList);
            notifyWayService.delById(anyLong());
            verify(ruleService, times(1)).list(any());
            verify(templateRuleService, times(1)).list(any());
        }
    }

    @Test
    public void testDelById() {
        List<AlertRule> ruleList = new ArrayList<>();
        List<AlertTemplateRule> templateRuleList = new ArrayList<>();
        when(ruleService.list(any())).thenReturn(ruleList);
        when(templateRuleService.list(any())).thenReturn(templateRuleList);
        when(baseMapper.update(any(), any())).thenReturn(1);

        notifyWayService.delById(anyLong());
        verify(ruleService, times(1)).list(any());
        verify(templateRuleService, times(1)).list(any());
        verify(baseMapper, times(1)).update(any(), any());
    }
}

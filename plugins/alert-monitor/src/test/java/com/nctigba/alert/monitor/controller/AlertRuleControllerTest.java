/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.dto.AlertRuleDto;
import com.nctigba.alert.monitor.dto.RuleItemPropertyDto;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.model.RuleReq;
import com.nctigba.alert.monitor.service.AlertRuleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.utils.ServletUtils;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test AlertRuleController
 *
 * @since 2023/7/16 10:37
 */
@RunWith(SpringRunner.class)
public class AlertRuleControllerTest {
    @InjectMocks
    private AlertRuleController alertRuleController;
    @Mock
    private AlertRuleService alertRuleService;

    @Test
    public void testGetRuleListPage() {
        try (MockedStatic<ServletUtils> mockedStatic = mockStatic(ServletUtils.class)) {
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageNum")).thenReturn(1);
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageSize")).thenReturn(10);
            Page<AlertRuleDto> page = new Page<>(1, 10);
            when(alertRuleService.getRulePage(any(RuleReq.class), any(Page.class))).thenReturn(page);
            RuleReq ruleReq = new RuleReq();
            TableDataInfo result = alertRuleController.getRuleListPage(ruleReq);
            verify(alertRuleService, times(1)).getRulePage(any(RuleReq.class), any(Page.class));
            assertEquals(page.getRecords(), result.getRows());
            assertEquals(page.getTotal(), result.getTotal());
        }
    }

    @Test
    public void testGetRuleList() {
        List<AlertRuleDto> list = new ArrayList<>();
        when(alertRuleService.getRuleList()).thenReturn(list);
        AjaxResult result = alertRuleController.getRuleList();
        verify(alertRuleService, times(1)).getRuleList();
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetRuleById() {
        AlertRule alertRule = new AlertRule();
        when(alertRuleService.getRuleById(anyLong())).thenReturn(alertRule);
        Long id = 1L;
        AjaxResult result = alertRuleController.getRuleById(id);
        verify(alertRuleService, times(1)).getRuleById(anyLong());
        assertEquals(alertRule, result.get("data"));
    }

    @Test
    public void testGetRuleItemProperties() {
        List<RuleItemPropertyDto> list = new ArrayList<>();
        when(alertRuleService.getRuleItemProperties()).thenReturn(list);
        AjaxResult result = alertRuleController.getRuleItemProperties();
        verify(alertRuleService, times(1)).getRuleItemProperties();
        assertEquals(list, result.get("data"));
    }
}
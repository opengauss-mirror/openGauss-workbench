/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.dto.AlertTemplateDto;
import com.nctigba.alert.monitor.dto.AlertTemplateRuleDto;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.model.AlertTemplateReq;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test AlertTemplateController
 *
 * @since 2023/7/16 11:22
 */
@RunWith(SpringRunner.class)
public class AlertTemplateControllerTest {
    @InjectMocks
    private AlertTemplateController alertTemplateController;
    @Mock
    private AlertTemplateService templateService;
    @Mock
    private AlertTemplateRuleService templateRuleService;

    @Test
    public void testGetTemplatePage() {
        try (MockedStatic<ServletUtils> mockedStatic = mockStatic(ServletUtils.class)) {
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageNum")).thenReturn(1);
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageSize")).thenReturn(10);
            Page<AlertTemplate> page = new Page<>(1, 10);
            when(templateService.getTemplatePage(anyString(), any(Page.class))).thenReturn(page);
            TableDataInfo result = alertTemplateController.getTemplatePage("");
            verify(templateService, times(1)).getTemplatePage(anyString(), any(Page.class));
            assertEquals(page.getRecords(), result.getRows());
            assertEquals(page.getTotal(), result.getTotal());
        }
    }

    @Test
    public void testGetTemplateList() {
        List<AlertTemplate> list = new ArrayList<>();
        when(templateService.getTemplateList()).thenReturn(list);
        AjaxResult result = alertTemplateController.getTemplateList();
        verify(templateService, times(1)).getTemplateList();
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetTemplateDto() {
        AlertTemplateDto templateDto = new AlertTemplateDto();
        when(templateService.getTemplate(anyLong())).thenReturn(templateDto);
        AjaxResult result = alertTemplateController.getTemplateDto(1L);
        verify(templateService, times(1)).getTemplate(anyLong());
        assertEquals(templateDto, result.get("data"));
    }

    @Test
    public void testGetTemplateRulePage() {
        try (MockedStatic<ServletUtils> mockedStatic = mockStatic(ServletUtils.class)) {
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageNum")).thenReturn(1);
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageSize")).thenReturn(10);
            Page<AlertTemplateRuleDto> page = new Page<>(1, 10);
            when(templateService.getTemplateRulePage(anyLong(), anyString(), any(Page.class))).thenReturn(page);
            TableDataInfo result = alertTemplateController.getTemplateRulePage(1L, "");
            verify(templateService, times(1))
                .getTemplateRulePage(anyLong(), anyString(), any(Page.class));
            assertEquals(page.getRecords(), result.getRows());
            assertEquals(page.getTotal(), result.getTotal());
        }
    }

    @Test
    public void testGetTemplateRuleListById() {
        List<AlertTemplateRuleDto> list = new ArrayList<>();
        when(templateService.getTemplateRuleListById(anyLong())).thenReturn(list);
        AjaxResult result = alertTemplateController.getTemplateRuleListById(1L);
        verify(templateService, times(1)).getTemplateRuleListById(anyLong());
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetTemplateRule() {
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule();
        when(templateRuleService.getTemplateRule(anyLong())).thenReturn(alertTemplateRule);
        AjaxResult result = alertTemplateController.getTemplateRule(1L);
        verify(templateRuleService, times(1)).getTemplateRule(anyLong());
        assertEquals(alertTemplateRule, result.get("data"));
    }

    @Test
    public void testSaveTemplate() {
        AlertTemplate alertTemplate = new AlertTemplate();
        when(templateService.saveTemplate(any(AlertTemplateReq.class))).thenReturn(alertTemplate);
        AlertTemplateReq templateReq = new AlertTemplateReq();
        AjaxResult result = alertTemplateController.saveTemplate(templateReq);
        verify(templateService, times(1)).saveTemplate(any(AlertTemplateReq.class));
        assertEquals(alertTemplate, result.get("data"));
    }

    @Test
    public void testDelTemplate() {
        doNothing().when(templateService).delTemplate(anyLong());
        AjaxResult result = alertTemplateController.delTemplate(1L);
        assertEquals(AjaxResult.success(), result);
    }

    @Test
    public void testSaveTemplateRule() {
        AlertTemplateRuleDto templateRuleDto = new AlertTemplateRuleDto();
        when(templateRuleService.saveTemplateRule(any(AlertTemplateRule.class))).thenReturn(templateRuleDto);
        AlertTemplateRule alertTemplateRule = new AlertTemplateRule();
        AjaxResult result = alertTemplateController.saveTemplateRule(alertTemplateRule);
        verify(templateRuleService, times(1)).saveTemplateRule(any(AlertTemplateRule.class));
        assertEquals(templateRuleDto, result.get("data"));
    }
}

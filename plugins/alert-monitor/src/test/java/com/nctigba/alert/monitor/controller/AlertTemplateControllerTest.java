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
 *  AlertTemplateControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/AlertTemplateControllerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.dto.AlertTemplateDTO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.query.AlertTemplateQuery;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.utils.ServletUtils;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
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
            Page<AlertTemplateDO> page = new Page<>(1, 10);
            when(templateService.getTemplatePage(anyString(), any(Page.class))).thenReturn(page);
            TableDataInfo result = alertTemplateController.getTemplatePage("");
            verify(templateService, times(1)).getTemplatePage(anyString(), any(Page.class));
            assertEquals(page.getRecords(), result.getRows());
            assertEquals(page.getTotal(), result.getTotal());
        }
    }

    @Test
    public void testGetTemplateList() {
        List<AlertTemplateDO> list = new ArrayList<>();
        when(templateService.getTemplateList(CommonConstants.INSTANCE)).thenReturn(list);
        AjaxResult result = alertTemplateController.getTemplateList(CommonConstants.INSTANCE);
        verify(templateService, times(1)).getTemplateList(CommonConstants.INSTANCE);
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetTemplateDto() {
        AlertTemplateDTO templateDto = new AlertTemplateDTO();
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
            Page<AlertTemplateRuleDO> page = new Page<>(1, 10);
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
        List<AlertTemplateRuleDO> list = new ArrayList<>();
        when(templateService.getTemplateRuleListById(anyLong())).thenReturn(list);
        AjaxResult result = alertTemplateController.getTemplateRuleListById(1L);
        verify(templateService, times(1)).getTemplateRuleListById(anyLong());
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetTemplateRule() {
        AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO();
        when(templateRuleService.getTemplateRule(anyLong())).thenReturn(alertTemplateRuleDO);
        AjaxResult result = alertTemplateController.getTemplateRule(1L);
        verify(templateRuleService, times(1)).getTemplateRule(anyLong());
        assertEquals(alertTemplateRuleDO, result.get("data"));
    }

    @Test
    public void testSaveTemplate() {
        AlertTemplateDO alertTemplateDO = new AlertTemplateDO();
        when(templateService.saveTemplate(any(AlertTemplateQuery.class))).thenReturn(alertTemplateDO);
        AlertTemplateQuery templateReq = new AlertTemplateQuery();
        AjaxResult result = alertTemplateController.saveTemplate(templateReq);
        verify(templateService, times(1)).saveTemplate(any(AlertTemplateQuery.class));
        assertEquals(alertTemplateDO, result.get("data"));
    }

    @Test
    public void testDelTemplate() {
        doNothing().when(templateService).delTemplate(anyLong());
        AjaxResult result = alertTemplateController.delTemplate(1L);
        assertEquals(AjaxResult.success(), result);
    }

    @Test
    public void testSaveIndexTemplateRuleWithCheckFail() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertTemplateRuleDO templateRule = new AlertTemplateRuleDO().setRuleType(CommonConstants.INDEX_RULE);
            alertTemplateController.saveTemplateRule(templateRule);
        }
    }

    @Test
    public void testSaveSilenceIndexTemplateRuleWithCheckFail() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertTemplateRuleDO templateRule = new AlertTemplateRuleDO().setRuleType(CommonConstants.INDEX_RULE)
                .setIsSilence(CommonConstants.IS_SILENCE).setIsRepeat(CommonConstants.IS_NOT_REPEAT);
            alertTemplateController.saveTemplateRule(templateRule);
        }
    }

    @Test
    public void testSaveLogTemplateRuleWithCheckFail() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertTemplateRuleDO templateRule = new AlertTemplateRuleDO().setRuleType(CommonConstants.LOG_RULE);
            alertTemplateController.saveTemplateRule(templateRule);
        }
    }

    @Test
    public void testSaveLogTemplateRule() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertTemplateRuleDO templateRule =
                new AlertTemplateRuleDO().setRuleType(CommonConstants.LOG_RULE).setCheckFrequency(1)
                    .setCheckFrequencyUnit("m");
            when(templateRuleService.saveTemplateRule(any(AlertTemplateRuleDO.class))).thenReturn(templateRule);
            AjaxResult result = alertTemplateController.saveTemplateRule(templateRule);
            verify(templateRuleService, times(1)).saveTemplateRule(any(AlertTemplateRuleDO.class));
            assertEquals(templateRule, result.get("data"));
        }
    }

    @Test
    public void testSaveIndexTemplateRule() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertTemplateRuleDO templateRule =
                new AlertTemplateRuleDO().setRuleType(CommonConstants.INDEX_RULE).setIsRepeat(CommonConstants.IS_REPEAT)
                    .setIsSilence(CommonConstants.IS_NOT_SILENCE);
            when(templateRuleService.saveTemplateRule(any(AlertTemplateRuleDO.class))).thenReturn(templateRule);
            AjaxResult result = alertTemplateController.saveTemplateRule(templateRule);
            verify(templateRuleService, times(1)).saveTemplateRule(any(AlertTemplateRuleDO.class));
            assertEquals(templateRule, result.get("data"));
        }
    }

    @Test
    public void testSaveIndexSilenceTemplateRule() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertTemplateRuleDO templateRule =
                new AlertTemplateRuleDO().setRuleType(CommonConstants.INDEX_RULE).setIsRepeat(CommonConstants.IS_REPEAT)
                    .setIsSilence(CommonConstants.IS_SILENCE).setSilenceStartTime(LocalDateTime.now().minusHours(1))
                    .setSilenceEndTime(LocalDateTime.now());
            when(templateRuleService.saveTemplateRule(any(AlertTemplateRuleDO.class))).thenReturn(templateRule);
            AjaxResult result = alertTemplateController.saveTemplateRule(templateRule);
            verify(templateRuleService, times(1)).saveTemplateRule(any(AlertTemplateRuleDO.class));
            assertEquals(templateRule, result.get("data"));
        }
    }

    @Test
    public void testEnableTemplateRule() {
        doNothing().when(templateRuleService).enableTemplateRule(anyLong());
        alertTemplateController.enableTemplateRule(anyLong());
        verify(templateRuleService, times(1)).enableTemplateRule(anyLong());
    }

    @Test
    public void testDisableTemplateRule() {
        doNothing().when(templateRuleService).disableTemplateRule(anyLong());
        alertTemplateController.disableTemplateRule(anyLong());
        verify(templateRuleService, times(1)).disableTemplateRule(anyLong());
    }
}

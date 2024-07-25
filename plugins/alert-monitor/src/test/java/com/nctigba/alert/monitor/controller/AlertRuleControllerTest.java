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
 *  AlertRuleControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/AlertRuleControllerTest.java
 *
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.dto.AlertRuleParamDTO;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemExpSrcDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemSrcDO;
import com.nctigba.alert.monitor.model.query.RuleQuery;
import com.nctigba.alert.monitor.service.AlertRuleService;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
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
            Page<AlertRuleDO> page = new Page<>(1, 10);
            when(alertRuleService.getRulePage(any(RuleQuery.class), any(Page.class))).thenReturn(page);
            RuleQuery ruleQuery = new RuleQuery();
            TableDataInfo result = alertRuleController.getRuleListPage(ruleQuery);
            verify(alertRuleService, times(1)).getRulePage(any(RuleQuery.class), any(Page.class));
            assertEquals(page.getRecords(), result.getRows());
            assertEquals(page.getTotal(), result.getTotal());
        }
    }

    @Test
    public void testGetRuleList() {
        List<AlertRuleDO> list = new ArrayList<>();
        when(alertRuleService.getRuleList(Arrays.asList("index", "log"))).thenReturn(list);
        AjaxResult result = alertRuleController.getRuleList("index.log");
        verify(alertRuleService, times(1)).getRuleList(Arrays.asList("index", "log"));
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetRuleById() {
        AlertRuleDO alertRuleDO = new AlertRuleDO();
        when(alertRuleService.getRuleById(anyLong())).thenReturn(alertRuleDO);
        Long id = 1L;
        AjaxResult result = alertRuleController.getRuleById(id);
        verify(alertRuleService, times(1)).getRuleById(anyLong());
        assertEquals(alertRuleDO, result.get("data"));
    }

    @Test
    public void testGetRuleItemSrcList() {
        List<AlertRuleItemSrcDO> list = new ArrayList<>();
        when(alertRuleService.getRuleItemSrcList()).thenReturn(list);
        AjaxResult result = alertRuleController.getRuleItemSrcList();
        verify(alertRuleService, times(1)).getRuleItemSrcList();
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetRuleItemExpSrcListByRuleItemSrcId() {
        List<AlertRuleItemExpSrcDO> list = new ArrayList<>();
        when(alertRuleService.getRuleItemExpSrcListByRuleItemSrcId(anyLong())).thenReturn(list);
        AjaxResult result = alertRuleController.getRuleItemExpSrcListByRuleItemSrcId(anyLong());
        verify(alertRuleService, times(1)).getRuleItemExpSrcListByRuleItemSrcId(anyLong());
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testSaveIndexRuleWithCheckFail() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertRuleParamDTO alertRule = new AlertRuleParamDTO().setRuleType(CommonConstants.INDEX_RULE);
            alertRuleController.saveRule(alertRule);
        }
    }

    @Test
    public void testSaveSilenceIndexRuleWithCheckFail() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertRuleParamDTO alertRule = new AlertRuleParamDTO().setRuleType(CommonConstants.INDEX_RULE)
                .setIsSilence(CommonConstants.IS_SILENCE).setIsRepeat(CommonConstants.IS_NOT_REPEAT);
            alertRuleController.saveRule(alertRule);
        }
    }

    @Test
    public void testSaveLogRuleWithCheckFail() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertRuleParamDTO alertRule = new AlertRuleParamDTO().setRuleType(CommonConstants.LOG_RULE);
            alertRuleController.saveRule(alertRule);
        }
    }

    @Test
    public void testSaveLogRule() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertRuleParamDTO alertRule =
                new AlertRuleParamDTO().setRuleType(CommonConstants.LOG_RULE).setCheckFrequency(1)
                    .setCheckFrequencyUnit("m");
            doNothing().when(alertRuleService).saveRule(any(AlertRuleParamDTO.class));
            AjaxResult result = alertRuleController.saveRule(alertRule);
            verify(alertRuleService, times(1)).saveRule(any(AlertRuleParamDTO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveIndexRule() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertRuleParamDTO alertRule =
                new AlertRuleParamDTO().setRuleType(CommonConstants.INDEX_RULE).setIsRepeat(CommonConstants.IS_REPEAT)
                    .setIsSilence(CommonConstants.IS_NOT_SILENCE);
            doNothing().when(alertRuleService).saveRule(alertRule);
            AjaxResult result = alertRuleController.saveRule(alertRule);
            verify(alertRuleService, times(1)).saveRule(any(AlertRuleParamDTO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveIndexSilenceRule() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("error");
            AlertRuleParamDTO alertRule =
                new AlertRuleParamDTO().setRuleType(CommonConstants.INDEX_RULE).setIsRepeat(CommonConstants.IS_REPEAT)
                    .setIsSilence(CommonConstants.IS_SILENCE).setSilenceStartTime(LocalDateTime.now().minusHours(1))
                    .setSilenceEndTime(LocalDateTime.now());
            doNothing().when(alertRuleService).saveRule(alertRule);
            AjaxResult result = alertRuleController.saveRule(alertRule);
            verify(alertRuleService, times(1)).saveRule(any(AlertRuleParamDTO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testDelRuleById() {
        doNothing().when(alertRuleService).delRuleById(anyLong());
        AjaxResult result = alertRuleController.delRuleById(anyLong());
        verify(alertRuleService, times(1)).delRuleById(anyLong());
        assertEquals(AjaxResult.success(), result);
    }
}

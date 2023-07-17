/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.dto.AlertRecordDto;
import com.nctigba.alert.monitor.dto.AlertRelationDto;
import com.nctigba.alert.monitor.dto.AlertStatisticsDto;
import com.nctigba.alert.monitor.model.AlertRecordReq;
import com.nctigba.alert.monitor.model.AlertStatisticsReq;
import com.nctigba.alert.monitor.service.AlertRecordService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test AlertRecordController
 *
 * @since 2023/7/16 03:19
 */
@RunWith(SpringRunner.class)
public class AlertRecordControllerTest {
    @InjectMocks
    @Spy
    private AlertRecordController alertRecordController;
    @Mock
    private AlertRecordService alertRecordService;

    @Test
    public void testAlertRecordListPage() {
        try (MockedStatic<ServletUtils> mockedStatic = mockStatic(ServletUtils.class)) {
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageNum")).thenReturn(1);
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageSize")).thenReturn(10);
            Page<AlertRecordDto> page = new Page<>(1, 10);
            when(alertRecordService.getListPage(any(AlertRecordReq.class), any(Page.class))).thenReturn(page);
            AlertRecordReq recordReq = new AlertRecordReq();
            TableDataInfo result = alertRecordController.alertRecordListPage(recordReq);
            verify(alertRecordService, times(1)).getListPage(any(AlertRecordReq.class), any(Page.class));
            assertEquals(page.getRecords(), result.getRows());
            assertEquals(page.getTotal(), result.getTotal());
        }
    }

    @Test
    public void testAlertRecordStatistics() {
        AlertStatisticsDto statisticsDto = new AlertStatisticsDto();
        when(alertRecordService.alertRecordStatistics(any(AlertStatisticsReq.class))).thenReturn(statisticsDto);
        AlertStatisticsReq alertStatisticsReq = new AlertStatisticsReq();
        AjaxResult result = alertRecordController.alertRecordStatistics(alertStatisticsReq);
        verify(alertRecordService, times(1))
            .alertRecordStatistics(any(AlertStatisticsReq.class));
        assertEquals(statisticsDto, result.get("data"));
    }

    @Test
    public void testGetById() {
        AlertRecordDto alertRecordDto = new AlertRecordDto();
        when(alertRecordService.getById(anyLong())).thenReturn(alertRecordDto);
        Long id = 1L;
        AjaxResult result = alertRecordController.getById(id);
        verify(alertRecordService, times(1)).getById(anyLong());
        assertEquals(alertRecordDto, result.get("data"));
    }

    @Test
    public void testMarkAsUnread() {
        when(alertRecordService.markAsUnread(anyString())).thenReturn("success");
        AjaxResult result = alertRecordController.markAsUnread("1");
        verify(alertRecordService, times(1)).markAsUnread(anyString());
        assertEquals("success", result.get("msg"));
    }

    @Test
    public void testMarkAsRead() {
        when(alertRecordService.markAsRead(anyString())).thenReturn("success");
        AjaxResult result = alertRecordController.markAsRead("1");
        verify(alertRecordService, times(1)).markAsRead(anyString());
        assertEquals("success", result.get("msg"));
    }

    @Test
    public void testGetRelationData() {
        List<AlertRelationDto> relationData = new ArrayList<>();
        when(alertRecordService.getRelationData(anyLong())).thenReturn(relationData);
        AjaxResult result = alertRecordController.getRelationData(1L);
        verify(alertRecordService, times(1)).getRelationData(anyLong());
        assertEquals(relationData, result.get("data"));
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.dto.AlertRecordDto;
import com.nctigba.alert.monitor.dto.AlertRelationDto;
import com.nctigba.alert.monitor.dto.AlertStatisticsDto;
import com.nctigba.alert.monitor.dto.LogInfoDTO;
import com.nctigba.alert.monitor.model.AlertRecordReq;
import com.nctigba.alert.monitor.model.AlertStatisticsReq;
import com.nctigba.alert.monitor.service.AlertRecordService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.apache.poi.ss.usermodel.Workbook;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

    @Test
    public void testGetRelationLog() {
        LogInfoDTO logInfoDTO = new LogInfoDTO();
        when(alertRecordService.getRelationLog(anyLong(), anyBoolean(), anyString())).thenReturn(logInfoDTO);
        AjaxResult result = alertRecordController.getRelationLog(anyLong(), anyBoolean(), anyString());
        verify(alertRecordService, times(1)).getRelationLog(anyLong(), anyBoolean(), anyString());
        assertEquals(logInfoDTO, result.get("data"));
    }

    @Test
    public void testExport() throws IOException {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            Workbook workbook = mock(Workbook.class);
            when(alertRecordService.exportWorkbook(any(AlertStatisticsReq.class))).thenReturn(workbook);
            HttpServletResponse response = mock(HttpServletResponse.class);
            ServletOutputStream outputStream = mock(ServletOutputStream.class);
            when(response.getOutputStream()).thenReturn(outputStream);
            mockedStatic.when(() -> MessageSourceUtil.get("alertRecord")).thenReturn("alertRecord");
            alertRecordController.export(new AlertStatisticsReq(), response);
            verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            verify(response).setHeader(eq("Content-Disposition"), contains("alertRecord.xlsx"));
            verify(alertRecordService).exportWorkbook(any(AlertStatisticsReq.class));
            verify(workbook).write(outputStream);
            verify(workbook).close();
        }
    }

    @Test
    public void testExportReport() throws IOException {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            HttpServletResponse response = mock(HttpServletResponse.class);
            ServletOutputStream outputStream = mock(ServletOutputStream.class);
            when(response.getOutputStream()).thenReturn(outputStream);
            mockedStatic.when(() -> MessageSourceUtil.get("alertRecord")).thenReturn("alertRecord");
            when(alertRecordService.exportReport(any(AlertStatisticsReq.class))).thenReturn("html");
            alertRecordController.exportReport(new AlertStatisticsReq(), response);
            verify(alertRecordService).exportReport(any(AlertStatisticsReq.class));
        }
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.entity.NotifyTemplate;
import com.nctigba.alert.monitor.service.NotifyTemplateService;
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
 * test NotifyTemplateController
 *
 * @since 2023/7/16 12:22
 */
@RunWith(SpringRunner.class)
public class NotifyTemplateControllerTest {
    @InjectMocks
    private NotifyTemplateController notifyTemplateController;
    @Mock
    private NotifyTemplateService notifyTemplateService;

    @Test
    public void testGetListPage() {
        try (MockedStatic<ServletUtils> mockedStatic = mockStatic(ServletUtils.class)) {
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageNum")).thenReturn(1);
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageSize")).thenReturn(10);
            Page page = new Page(1, 10);
            when(notifyTemplateService.getListPage(anyString(), anyString(), any(Page.class))).thenReturn(page);
            TableDataInfo result = notifyTemplateController.getListPage("", "");
            verify(notifyTemplateService, times(1))
                .getListPage(anyString(), anyString(), any(Page.class));
            assertEquals(page.getRecords(), result.getRows());
            assertEquals(page.getTotal(), result.getTotal());
        }
    }

    @Test
    public void testGetList() {
        List<NotifyTemplate> list = new ArrayList<>();
        when(notifyTemplateService.getList(anyString())).thenReturn(list);
        AjaxResult result = notifyTemplateController.getList("");
        verify(notifyTemplateService, times(1)).getList(anyString());
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetById() {
        NotifyTemplate notifyTemplate = new NotifyTemplate();
        when(notifyTemplateService.getById(anyLong())).thenReturn(notifyTemplate);
        AjaxResult result = notifyTemplateController.getById(1L);
        verify(notifyTemplateService, times(1)).getById(anyLong());
        assertEquals(notifyTemplate, result.get("data"));
    }

    @Test
    public void testSaveTemplate() {
        doNothing().when(notifyTemplateService).saveTemplate(any(NotifyTemplate.class));
        NotifyTemplate notifyTemplate = new NotifyTemplate();
        AjaxResult result = notifyTemplateController.saveTemplate(notifyTemplate);
        verify(notifyTemplateService, times(1)).saveTemplate(any(NotifyTemplate.class));
        assertEquals(AjaxResult.success(), result);
    }
    @Test
    public void testDelById() {
        doNothing().when(notifyTemplateService).delById(anyLong());
        AjaxResult result = notifyTemplateController.delById(1L);
        verify(notifyTemplateService, times(1)).delById(anyLong());
        assertEquals(AjaxResult.success(), result);
    }
}

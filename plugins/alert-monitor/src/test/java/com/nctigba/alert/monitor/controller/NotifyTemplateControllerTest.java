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
 *  NotifyTemplateControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/NotifyTemplateControllerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
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
        List<NotifyTemplateDO> list = new ArrayList<>();
        when(notifyTemplateService.getList(anyString())).thenReturn(list);
        AjaxResult result = notifyTemplateController.getList("");
        verify(notifyTemplateService, times(1)).getList(anyString());
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetById() {
        NotifyTemplateDO notifyTemplateDO = new NotifyTemplateDO();
        when(notifyTemplateService.getById(anyLong())).thenReturn(notifyTemplateDO);
        AjaxResult result = notifyTemplateController.getById(1L);
        verify(notifyTemplateService, times(1)).getById(anyLong());
        assertEquals(notifyTemplateDO, result.get("data"));
    }

    @Test
    public void testSaveTemplate() {
        doNothing().when(notifyTemplateService).saveTemplate(any(NotifyTemplateDO.class));
        NotifyTemplateDO notifyTemplateDO = new NotifyTemplateDO();
        AjaxResult result = notifyTemplateController.saveTemplate(notifyTemplateDO);
        verify(notifyTemplateService, times(1)).saveTemplate(any(NotifyTemplateDO.class));
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

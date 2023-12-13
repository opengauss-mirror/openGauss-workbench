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
 *  BaseControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/BaseControllerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.utils.ServletUtils;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

/**
 * test BaseController
 *
 * @since 2023/7/16 11:51
 */
@RunWith(SpringRunner.class)
public class BaseControllerTest {
    @InjectMocks
    private BaseController baseController;

    @Test
    public void testStartPageNull() {
        try (MockedStatic<ServletUtils> mockedStatic = mockStatic(ServletUtils.class)) {
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageNum")).thenReturn(any());
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageSize")).thenReturn(any());
            Page page = baseController.startPage();
            assertEquals(1, page.getCurrent());
            assertEquals(10, page.getSize());
        }
    }

    @Test
    public void testStartPageNull2() {
        try (MockedStatic<ServletUtils> mockedStatic = mockStatic(ServletUtils.class)) {
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageNum")).thenReturn(2);
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageSize")).thenReturn(any());
            Page page = baseController.startPage();
            assertEquals(1, page.getCurrent());
            assertEquals(10, page.getSize());
        }
    }
    @Test
    public void testStartPage() {
        try (MockedStatic<ServletUtils> mockedStatic = mockStatic(ServletUtils.class)) {
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageNum")).thenReturn(2);
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageSize")).thenReturn(100);
            Page page = baseController.startPage();
            assertEquals(2, page.getCurrent());
            assertEquals(100, page.getSize());
        }
    }

    @Test
    public void testGetDataTable() {
        Page page = new Page();
        TableDataInfo dataTable = baseController.getDataTable(page);
        assertEquals(page.getTotal(), dataTable.getTotal());
        assertEquals(page.getRecords(), dataTable.getRows());
    }
}

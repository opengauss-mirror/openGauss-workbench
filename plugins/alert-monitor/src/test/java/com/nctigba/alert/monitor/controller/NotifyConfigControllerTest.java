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
 *  NotifyConfigControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/NotifyConfigControllerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.model.entity.NotifyConfigDO;
import com.nctigba.alert.monitor.model.query.NotifyConfigQuery;
import com.nctigba.alert.monitor.service.NotifyConfigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test NotifyConfigController
 *
 * @since 2023/7/16 12:10
 */
@RunWith(SpringRunner.class)
public class NotifyConfigControllerTest {
    @InjectMocks
    private NotifyConfigController notifyConfigController;
    @Mock
    private NotifyConfigService notifyConfigService;

    @Test
    public void testGetAllList() {
        List<NotifyConfigDO> list = new ArrayList<>();
        when(notifyConfigService.getAllList()).thenReturn(list);
        AjaxResult result = notifyConfigController.getAllList();
        verify(notifyConfigService, times(1)).getAllList();
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testSaveList() {
        doNothing().when(notifyConfigService).saveList(anyList());
        AjaxResult result = notifyConfigController.saveList(anyList());
        verify(notifyConfigService, times(1)).saveList(anyList());
        assertEquals(AjaxResult.success(), result);
    }

    @Test
    public void testTestConfig1() {
        when(notifyConfigService.testConfig(any(NotifyConfigQuery.class))).thenReturn(true);
        NotifyConfigQuery notifyConfigReq = new NotifyConfigQuery();
        AjaxResult result = notifyConfigController.testConfig(notifyConfigReq);
        verify(notifyConfigService, times(1)).testConfig(any(NotifyConfigQuery.class));
        assertEquals(AjaxResult.success(), result);
    }

    @Test
    public void testTestConfig2() {
        when(notifyConfigService.testConfig(any(NotifyConfigQuery.class))).thenReturn(false);
        NotifyConfigQuery notifyConfigReq = new NotifyConfigQuery();
        AjaxResult result = notifyConfigController.testConfig(notifyConfigReq);
        verify(notifyConfigService, times(1)).testConfig(any(NotifyConfigQuery.class));
        assertEquals(AjaxResult.error(), result);
    }
}

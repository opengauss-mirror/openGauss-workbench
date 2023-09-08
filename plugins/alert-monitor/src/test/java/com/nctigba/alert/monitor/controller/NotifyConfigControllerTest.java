/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.entity.NotifyConfig;
import com.nctigba.alert.monitor.model.NotifyConfigReq;
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
        List<NotifyConfig> list = new ArrayList<>();
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
        when(notifyConfigService.testConfig(any(NotifyConfigReq.class))).thenReturn(true);
        NotifyConfigReq notifyConfigReq = new NotifyConfigReq();
        AjaxResult result = notifyConfigController.testConfig(notifyConfigReq);
        verify(notifyConfigService, times(1)).testConfig(any(NotifyConfigReq.class));
        assertEquals(AjaxResult.success(), result);
    }

    @Test
    public void testTestConfig2() {
        when(notifyConfigService.testConfig(any(NotifyConfigReq.class))).thenReturn(false);
        NotifyConfigReq notifyConfigReq = new NotifyConfigReq();
        AjaxResult result = notifyConfigController.testConfig(notifyConfigReq);
        verify(notifyConfigService, times(1)).testConfig(any(NotifyConfigReq.class));
        assertEquals(AjaxResult.error(), result);
    }
}

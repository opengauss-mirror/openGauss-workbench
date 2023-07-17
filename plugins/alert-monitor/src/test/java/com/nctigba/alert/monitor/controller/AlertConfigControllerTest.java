/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.entity.AlertConfig;
import com.nctigba.alert.monitor.service.AlertConfigService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test AlertConfigController
 *
 * @since 2023/7/16 03:08
 */
@RunWith(SpringRunner.class)
public class AlertConfigControllerTest {
    @InjectMocks
    private AlertConfigController alertConfigController;
    @Mock
    private AlertConfigService alertConfigService;

    @Test
    public void testGetAlertConfWithNull() {
        List<AlertConfig> list = new ArrayList<>();
        when(alertConfigService.list()).thenReturn(list);
        AjaxResult result = alertConfigController.getAlertConf();
        verify(alertConfigService, times(1)).list();
        assertEquals(new AlertConfig(), result.get("data"));
    }
    @Test
    public void testGetAlertConf() {
        List<AlertConfig> list = new ArrayList<>();
        AlertConfig alertConfig = new AlertConfig();
        list.add(alertConfig);
        when(alertConfigService.list()).thenReturn(list);
        AjaxResult result = alertConfigController.getAlertConf();
        verify(alertConfigService, times(1)).list();
        assertEquals(list.get(0), result.get("data"));
    }

    @Test
    public void testSaveAlertConf() {
        doNothing().when(alertConfigService).saveAlertConf(any(AlertConfig.class));
        AlertConfig alertConfig = new AlertConfig();
        AjaxResult result = alertConfigController.saveAlertConf(alertConfig);
        assertEquals(AjaxResult.success(), result);
    }
}

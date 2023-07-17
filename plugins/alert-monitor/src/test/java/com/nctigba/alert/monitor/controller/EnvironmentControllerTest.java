/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.service.EnvironmentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the EnvironmentController
 *
 * @since 2023/7/3 23:45
 */
@RunWith(SpringRunner.class)
public class EnvironmentControllerTest {
    @InjectMocks
    private EnvironmentController environmentController;
    @Mock
    private EnvironmentService environmentService;

    @Test
    public void testCluster() {
        List cluster = new ArrayList<>();
        when(environmentService.cluster()).thenReturn(cluster);
        AjaxResult result = environmentController.cluster();
        verify(environmentService, times(1)).cluster();
        assertEquals(cluster, result.get("data"));
    }

    @Test
    public void testCheckPrometheus() {
        doNothing().when(environmentService).checkPrometheus();
        AjaxResult result = environmentController.checkPrometheus();
        verify(environmentService, times(1)).checkPrometheus();
        assertEquals(AjaxResult.success(), result);
    }

    @Test
    public void testGetAlertContentParam() {
        Map<String, Map<String, String>> map = new HashMap<>();
        when(environmentService.getAlertContentParam(anyString())).thenReturn(map);
        AjaxResult result = environmentController.getAlertContentParam(anyString());
        verify(environmentService, times(1)).getAlertContentParam(anyString());
        assertEquals(map, result.get("data"));
    }
}

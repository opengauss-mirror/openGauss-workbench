/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.controller;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.service.impl.MonitoringServiceImpl;
import com.nctigba.observability.instance.service.impl.SessionServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.AjaxResult;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opengauss.admin.common.core.domain.AjaxResult.DATA_TAG;

/**
 * Test
 *
 * @author liupengfei
 * @since 2023/6/30 
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionControllerTest {

    @InjectMocks
    SessionController sessionController;
    @Mock
    SessionServiceImpl sessionService;
    @Mock
    MetricsService metricsService;
    @Mock
    MonitoringServiceImpl monitoringService;

    @Test
    public void sessionStatistic() {
        String id = "123";
        Long start = 111L;
        Long end = 222L;
        Integer step = 15;
        when(monitoringService.getRangeMonitoringData(any())).thenReturn(List.of("waitEven"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("simple", "val");
        when(sessionService.simpleStatistic(id)).thenReturn(jsonObject);
        HashMap<String, Object> metric = new HashMap<>();
        metric.put("metric", "val");
        when(metricsService.listBatch(any(), any(), any(), any(), any())).thenReturn(metric);

        AjaxResult appResult = sessionController.sessionStatistic(id, start, end, step);
        assertNotNull(appResult.get(DATA_TAG));
        verify(sessionService, times(1)).simpleStatistic(any());
    }

}
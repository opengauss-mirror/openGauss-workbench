/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.dto.AlertClusterNodeConfDto;
import com.nctigba.alert.monitor.entity.AlertClusterNodeConf;
import com.nctigba.alert.monitor.model.AlertClusterNodeAndTemplateReq;
import com.nctigba.alert.monitor.model.AlertClusterNodeConfReq;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test AlertClusterNodeConfController
 *
 * @since 2023/7/16 02:54
 */
@RunWith(SpringRunner.class)
public class AlertClusterNodeConfControllerTest {
    @InjectMocks
    private AlertClusterNodeConfController alertClusterNodeConfController;
    @Mock
    private AlertClusterNodeConfService alertClusterNodeConfService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetList() {
        List<AlertClusterNodeConfDto> list = new ArrayList<>();
        when(alertClusterNodeConfService.getList()).thenReturn(list);
        AjaxResult result = alertClusterNodeConfController.getList();
        verify(alertClusterNodeConfService, times(1)).getList();
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetByClusterNodeId() {
        AlertClusterNodeConf alertClusterNodeConf = new AlertClusterNodeConf();
        when(alertClusterNodeConfService.getByClusterNodeId(anyString())).thenReturn(alertClusterNodeConf);
        String clusterNodeId = "node1";
        AjaxResult result = alertClusterNodeConfController.getByClusterNodeId(clusterNodeId);
        verify(alertClusterNodeConfService, times(1)).getByClusterNodeId(anyString());
        assertEquals(alertClusterNodeConf, result.get("data"));
    }

    @Test
    public void testSaveClusterNodeConf() {
        doNothing().when(alertClusterNodeConfService).saveClusterNodeConf(any(AlertClusterNodeConfReq.class));
        AlertClusterNodeConfReq alertClusterNodeConfReq = new AlertClusterNodeConfReq();
        AjaxResult result = alertClusterNodeConfController.saveClusterNodeConf(alertClusterNodeConfReq);
        verify(alertClusterNodeConfService, times(1))
            .saveClusterNodeConf(any(AlertClusterNodeConfReq.class));
        assertEquals(AjaxResult.success(), result);
    }

    @Test
    public void testSaveAlertTemplateAndConfig() {
        doNothing().when(alertClusterNodeConfService)
            .saveAlertTemplateAndConfig(any(AlertClusterNodeAndTemplateReq.class));
        AlertClusterNodeAndTemplateReq clusterNodeAndTemplateReq = new AlertClusterNodeAndTemplateReq();
        AjaxResult result = alertClusterNodeConfController.saveAlertTemplateAndConfig(clusterNodeAndTemplateReq);
        verify(alertClusterNodeConfService, times(1))
            .saveAlertTemplateAndConfig(any(AlertClusterNodeAndTemplateReq.class));
        assertEquals(AjaxResult.success(), result);
    }
}

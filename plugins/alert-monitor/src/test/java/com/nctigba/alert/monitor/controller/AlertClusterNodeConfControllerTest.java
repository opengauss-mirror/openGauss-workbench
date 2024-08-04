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
 *  AlertClusterNodeConfControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/AlertClusterNodeConfControllerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.dto.AlertClusterNodeConfDTO;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeAndTemplateQuery;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeConfQuery;
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
        List<AlertClusterNodeConfDTO> list = new ArrayList<>();
        when(alertClusterNodeConfService.getList(CommonConstants.INSTANCE)).thenReturn(list);
        AjaxResult result = alertClusterNodeConfController.getList(CommonConstants.INSTANCE);
        verify(alertClusterNodeConfService, times(1)).getList(CommonConstants.INSTANCE);
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetByClusterNodeId() {
        AlertClusterNodeConfDO alertClusterNodeConfDO = new AlertClusterNodeConfDO();
        when(alertClusterNodeConfService.getByClusterNodeId(anyString(), CommonConstants.INSTANCE)).thenReturn(alertClusterNodeConfDO);
        String clusterNodeId = "node1";
        AjaxResult result = alertClusterNodeConfController.getByClusterNodeId(clusterNodeId, CommonConstants.INSTANCE);
        verify(alertClusterNodeConfService, times(1)).getByClusterNodeId(anyString(), CommonConstants.INSTANCE);
        assertEquals(alertClusterNodeConfDO, result.get("data"));
    }

    @Test
    public void testSaveClusterNodeConf() {
        doNothing().when(alertClusterNodeConfService).saveClusterNodeConf(any(AlertClusterNodeConfQuery.class));
        AlertClusterNodeConfQuery alertClusterNodeConfQuery = new AlertClusterNodeConfQuery();
        AjaxResult result = alertClusterNodeConfController.saveClusterNodeConf(alertClusterNodeConfQuery);
        verify(alertClusterNodeConfService, times(1))
            .saveClusterNodeConf(any(AlertClusterNodeConfQuery.class));
        assertEquals(AjaxResult.success(), result);
    }

    @Test
    public void testSaveAlertTemplateAndConfig() {
        doNothing().when(alertClusterNodeConfService)
            .saveAlertTemplateAndConfig(any(AlertClusterNodeAndTemplateQuery.class));
        AlertClusterNodeAndTemplateQuery clusterNodeAndTemplateReq = new AlertClusterNodeAndTemplateQuery();
        AjaxResult result = alertClusterNodeConfController.saveAlertTemplateAndConfig(clusterNodeAndTemplateReq);
        verify(alertClusterNodeConfService, times(1))
            .saveAlertTemplateAndConfig(any(AlertClusterNodeAndTemplateQuery.class));
        assertEquals(AjaxResult.success(), result);
    }
}

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
 *  EnvironmentControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/EnvironmentControllerTest.java
 *
 */

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.service.EnvironmentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
}

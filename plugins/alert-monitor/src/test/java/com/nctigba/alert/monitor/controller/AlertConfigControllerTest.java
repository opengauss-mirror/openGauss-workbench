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
 *  AlertConfigControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/AlertConfigControllerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.nctigba.alert.monitor.model.entity.AlertConfigDO;
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
        List<AlertConfigDO> list = new ArrayList<>();
        when(alertConfigService.list()).thenReturn(list);
        AjaxResult result = alertConfigController.getAlertConf();
        verify(alertConfigService, times(1)).list();
        assertEquals(new AlertConfigDO(), result.get("data"));
    }
    @Test
    public void testGetAlertConf() {
        List<AlertConfigDO> list = new ArrayList<>();
        AlertConfigDO alertConfigDO = new AlertConfigDO();
        list.add(alertConfigDO);
        when(alertConfigService.list()).thenReturn(list);
        AjaxResult result = alertConfigController.getAlertConf();
        verify(alertConfigService, times(1)).list();
        assertEquals(list.get(0), result.get("data"));
    }

    @Test
    public void testSaveAlertConf() {
        doNothing().when(alertConfigService).saveAlertConf(any(AlertConfigDO.class));
        AlertConfigDO alertConfigDO = new AlertConfigDO();
        AjaxResult result = alertConfigController.saveAlertConf(alertConfigDO);
        assertEquals(AjaxResult.success(), result);
    }
}

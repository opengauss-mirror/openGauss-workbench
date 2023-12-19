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
 *  AlertApiControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/AlertApiControllerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import cn.hutool.json.JSONObject;
import com.nctigba.alert.monitor.service.impl.AlertApiServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * test AlertApiController
 *
 * @since 2023/7/16 02:31
 */
@RunWith(SpringRunner.class)
public class AlertApiControllerTest {
    @InjectMocks
    private AlertApiController alertApiController;
    @Mock
    private AlertApiServiceImpl alertApiServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAlerts() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("startsAt", "2023-04-29T17:38:17.71694477Z");
        jsonObject.put("endsAt", "2023-04-29T17:42:17.71694477Z");
        jsonObject.put("generatorURL", "url");
        JSONObject labels = new JSONObject();
        labels.put("alertname", "rule_1");
        labels.put("instance", "node1");
        labels.put("level", "warn");
        labels.put("templateId", 1);
        labels.put("templateRuleId", 1);
        jsonObject.put("labels", labels);
        JSONObject annotations = new JSONObject();
        annotations.put("summary", "summary");
        annotations.put("description", "description");
        jsonObject.put("annotations", annotations);
        List<JSONObject> paramList = new ArrayList<>();
        paramList.add(jsonObject);

        doNothing().when(alertApiServiceImpl).alerts(anyList());

        AjaxResult result = alertApiController.alerts(paramList);
        verify(alertApiServiceImpl, times(1)).alerts(anyList());
        assertEquals(AjaxResult.success(), result);
    }
}

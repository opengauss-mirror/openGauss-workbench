/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import cn.hutool.json.JSONObject;
import com.nctigba.alert.monitor.service.AlertApiService;
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
    private AlertApiService alertApiService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAlerts() {
        List<JSONObject> paramList = new ArrayList<>();
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
        paramList.add(jsonObject);

        doNothing().when(alertApiService).alerts(anyList());

        AjaxResult result = alertApiController.alerts(paramList);
        verify(alertApiService, times(1)).alerts(anyList());
        assertEquals(AjaxResult.success(), result);
    }
}

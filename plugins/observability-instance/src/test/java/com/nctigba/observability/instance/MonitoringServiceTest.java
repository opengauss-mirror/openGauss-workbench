/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.model.monitoring.MonitoringMetric;
import com.nctigba.observability.instance.service.MonitoringService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
public class MonitoringServiceTest {

    @Mock
    private MonitoringService monitoringService;

    @Test
    public void testGetPointMonitoringAction() {
        Mockito.doReturn(mockPointMonitoringData()).when(monitoringService).getPointMonitoringData(any());
        Mockito.doReturn(mockMonitoringMetricData()).when(monitoringService).getCurrentMonitoringData(any());
        Mockito.doReturn(mockRangeMonitoringData()).when(monitoringService).getRangeMonitoringData(any());
    }

    private List<MonitoringMetric> mockMonitoringMetricData() {
        List<MonitoringMetric> monitoringMetricList = new ArrayList<>();
        MonitoringMetric monitoringMetric1 = new MonitoringMetric();
        monitoringMetric1.setValue(new JSONArray());
        monitoringMetric1.setMetric(new JSONObject());
        MonitoringMetric monitoringMetric2 = new MonitoringMetric();
        monitoringMetric2.setValue(new JSONArray());
        monitoringMetric2.setMetric(new JSONObject());
        monitoringMetricList.add(monitoringMetric1);
        monitoringMetricList.add(monitoringMetric2);
        return monitoringMetricList;
    }

    private List<Object> mockRangeMonitoringData() {
        List<Object> objectList = new ArrayList<>();
        objectList.add(new Object());
        objectList.add(new Object());
        return objectList;
    }

    private Map<String, Object> mockPointMonitoringData() {
        Map<String, Object> map = new HashMap<>();
        JSONObject metric = new JSONObject();
        map.put(metric.getString("__name__"), metric);
        map.put("value", null);
        return map;
    }
}

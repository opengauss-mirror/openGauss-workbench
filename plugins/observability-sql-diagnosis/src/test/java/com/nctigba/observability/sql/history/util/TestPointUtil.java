/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.mapper.history.HisThresholdMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.history.point.ExecPlanDetailDTO;
import com.nctigba.observability.sql.model.history.point.Plan;
import com.nctigba.observability.sql.util.PointUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestPointUtil
 *
 * @author luomeng
 * @since 2023/7/13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPointUtil {
    @Mock
    private HisThresholdMapper hisThresholdMapper;

    @InjectMocks
    private PointUtil util;

    private List<PrometheusData> list;

    private HisDiagnosisThreshold threshold;

    @Before
    public void before() {
        list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        JSONArray objects = new JSONArray();
        for (int i = 0; i <= 10; i++) {
            JSONArray array = new JSONArray();
            array.add("100000");
            objects.add(array);
        }
        prometheusData.setValues(objects);
        list.add(prometheusData);
        threshold = new HisDiagnosisThreshold();
        threshold.setThreshold("cpu");
        threshold.setThresholdValue("10");
        threshold.setThresholdDetail("");
        threshold.setThresholdName("");
    }

    @Test
    public void testAspTimeSlot() {
        List<AspAnalysisDTO> result = util.aspTimeSlot(list);
        assertNotNull(result);
    }

    @Test
    public void testDataToObject() {
        List<PrometheusData> result = util.dataToObject(list);
        assertNotNull(result);
    }

    @Test
    public void testThresholdMap() {
        List<HisDiagnosisThreshold> thresholds = new ArrayList<>();
        thresholds.add(threshold);
        List<HisDiagnosisThreshold> thresholdList = new ArrayList<>();
        thresholdList.add(threshold);
        when(hisThresholdMapper.selectList(any())).thenReturn(thresholdList);
        HashMap<String, String> result = util.thresholdMap(thresholds);
        assertNotNull(result);
    }

    @Test
    public void testGetCpuCoreNum() {
        int result = util.getCpuCoreNum(list);
        assertEquals(8, result);
    }

    @Test
    public void testGetExecPlan() {
        ExecPlanDetailDTO result = util.getExecPlan(mock(Plan.class));
        assertNotNull(result);
    }
}

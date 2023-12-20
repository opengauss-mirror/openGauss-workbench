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
 *  TestPointUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/util/TestPointUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.mapper.HisThresholdMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.dto.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.vo.point.ExecPlanDetailVO;
import com.nctigba.observability.sql.model.vo.point.PlanVO;
import com.nctigba.observability.sql.util.PointUtils;
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
public class TestPointUtils {
    @Mock
    private HisThresholdMapper hisThresholdMapper;

    @InjectMocks
    private PointUtils util;

    private List<PrometheusVO> list;

    private DiagnosisThresholdDO threshold;

    @Before
    public void before() {
        list = new ArrayList<>();
        PrometheusVO prometheusVO = new PrometheusVO();
        prometheusVO.setMetric(new JSONObject());
        JSONArray objects = new JSONArray();
        for (int i = 0; i <= 10; i++) {
            JSONArray array = new JSONArray();
            array.add("100000");
            objects.add(array);
        }
        prometheusVO.setValues(objects);
        list.add(prometheusVO);
        threshold = new DiagnosisThresholdDO();
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
        List<PrometheusVO> result = util.dataToObject(list);
        assertNotNull(result);
    }

    @Test
    public void testThresholdMap() {
        List<DiagnosisThresholdDO> thresholds = new ArrayList<>();
        thresholds.add(threshold);
        List<DiagnosisThresholdDO> thresholdList = new ArrayList<>();
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
        ExecPlanDetailVO result = util.getExecPlan(mock(PlanVO.class));
        assertNotNull(result);
    }
}

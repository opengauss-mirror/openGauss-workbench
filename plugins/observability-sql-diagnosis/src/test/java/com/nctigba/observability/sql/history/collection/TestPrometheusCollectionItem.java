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
 *  TestPrometheusCollectionItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/collection/TestPrometheusCollectionItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.service.impl.collection.metric.ActivityNumHighItem;
import com.nctigba.observability.sql.service.impl.collection.metric.PrometheusCollectionItem;
import com.nctigba.observability.sql.util.HostUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TestPrometheusCollectionItem
 *
 * @author luomeng
 * @since 2023/7/12
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPrometheusCollectionItem {
    @Mock
    private PrometheusUtils prometheusUtils;
    @Mock
    private HostUtils hostUtils;
    @Mock
    private ActivityNumHighItem item;
    private List<DiagnosisThresholdDO> threshold;
    @InjectMocks
    private PrometheusCollectionItem collectionItem = new PrometheusCollectionItem() {
        @Override
        public String getPrometheusParam(List<DiagnosisThresholdDO> thresholds) {
            return item.getPrometheusParam(threshold);
        }
    };

    private DiagnosisTaskDO diagnosisTaskDO;

    @Before
    public void before() {
        OptionVO optionVO = new OptionVO();
        optionVO.setOption(String.valueOf(OptionEnum.IS_CPU));
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold(ThresholdConstants.ACTIVITY_NUM);
        diagnosisThreshold.setThresholdValue("20");
        threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        diagnosisTaskDO = new DiagnosisTaskDO();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        diagnosisTaskDO.setNodeId(nodeId);
        Date sTime = new Date();
        Date eTime = new Date(sTime.getTime() + 5 * 60 * 60 * 1000);
        diagnosisTaskDO.setHisDataStartTime(sTime);
        diagnosisTaskDO.setHisDataEndTime(eTime);
        List<OptionVO> config = new ArrayList<>() {{
            add(optionVO);
        }};
        diagnosisTaskDO.setConfigs(config);
        diagnosisTaskDO.setThresholds(threshold);
        diagnosisTaskDO.setSpan("50s");
    }

    @Test
    public void testCollectData() {
        when(item.getPrometheusParam(threshold)).thenReturn(MetricConstants.ACTIVITY_NUM);
        List<?> dataList = new ArrayList<>();
        when(prometheusUtils.rangeQuery(
                any(), any(), any(), any(), any())).thenReturn(dataList);
        Object data = collectionItem.collectData(diagnosisTaskDO);
        assertNotNull(data);
    }

    @Test
    public void testCollectData_noEndDate() {
        when(item.getPrometheusParam(threshold)).thenReturn(MetricConstants.ACTIVITY_NUM);
        diagnosisTaskDO.setHisDataEndTime(null);
        Object data = collectionItem.collectData(diagnosisTaskDO);
        assertNull(data);
    }


    @Test
    public void testQueryData() {
        when(item.getPrometheusParam(threshold)).thenReturn(MetricConstants.ACTIVITY_NUM);
        Object data = collectionItem.queryData(diagnosisTaskDO);
        assertNull(data);
    }

    @Test
    public void testQueryData_noEndDate() {
        when(item.getPrometheusParam(threshold)).thenReturn(MetricConstants.ACTIVITY_NUM);
        diagnosisTaskDO.setHisDataEndTime(null);
        Object data = collectionItem.queryData(diagnosisTaskDO);
        assertNull(data);
    }
}

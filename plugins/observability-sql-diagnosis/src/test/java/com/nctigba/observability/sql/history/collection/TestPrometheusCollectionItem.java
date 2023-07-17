/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.PrometheusConstants;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.collection.metric.ActivityNumItem;
import com.nctigba.observability.sql.service.history.collection.metric.PrometheusCollectionItem;
import com.nctigba.observability.sql.util.PrometheusUtil;
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
    private PrometheusUtil prometheusUtil;
    @Mock
    private ActivityNumItem item;
    private List<HisDiagnosisThreshold> threshold;
    @InjectMocks
    private PrometheusCollectionItem collectionItem = new PrometheusCollectionItem() {
        @Override
        public String getPrometheusParam(List<HisDiagnosisThreshold> thresholds) {
            return item.getPrometheusParam(threshold);
        }
    };

    private HisDiagnosisTask hisDiagnosisTask;


    @Before
    public void before() {
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date(sTime.getTime() + 5 * 60 * 60 * 1000);
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption(String.valueOf(OptionCommon.IS_CPU));
        optionQuery.setIsCheck(true);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold(ThresholdCommon.ACTIVITY_NUM);
        diagnosisThreshold.setThresholdValue("20");
        threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        hisDiagnosisTask = new HisDiagnosisTask();
        hisDiagnosisTask.setNodeId(nodeId);
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(eTime);
        hisDiagnosisTask.setConfigs(config);
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
    }

    @Test
    public void testCollectData() {
        when(item.getPrometheusParam(threshold)).thenReturn(MetricCommon.ACTIVITY_NUM);
        List<?> dataList = new ArrayList<>();
        when(prometheusUtil.rangeQuery(
                any(), any(), any(), any(), any())).thenReturn(dataList);
        Object data = collectionItem.collectData(hisDiagnosisTask);
        assertNotNull(data);
    }

    @Test
    public void testCollectData_noEndDate() {
        when(item.getPrometheusParam(threshold)).thenReturn(MetricCommon.ACTIVITY_NUM);
        hisDiagnosisTask.setHisDataEndTime(null);
        Object data = collectionItem.collectData(hisDiagnosisTask);
        assertNull(data);
    }


    @Test
    public void testQueryData() {
        when(item.getPrometheusParam(threshold)).thenReturn(MetricCommon.ACTIVITY_NUM);
        Object data = collectionItem.queryData(hisDiagnosisTask);
        assertNull(data);
    }

    @Test
    public void testQueryData_noEndDate() {
        when(item.getPrometheusParam(threshold)).thenReturn(MetricCommon.ACTIVITY_NUM);
        hisDiagnosisTask.setHisDataEndTime(null);
        Object data = collectionItem.queryData(hisDiagnosisTask);
        assertNull(data);
    }
}

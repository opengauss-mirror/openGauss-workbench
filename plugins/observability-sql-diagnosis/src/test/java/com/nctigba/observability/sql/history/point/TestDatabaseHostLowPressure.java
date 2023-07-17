/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.MetricDataDTO;
import com.nctigba.observability.sql.model.history.point.PrometheusDataDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.metric.ActivityNumItem2;
import com.nctigba.observability.sql.service.history.collection.metric.ThreadPoolUsageItem2;
import com.nctigba.observability.sql.service.history.point.DatabaseHostLowPressure;
import com.nctigba.observability.sql.util.PrometheusUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestDatabaseHostLowPressure
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDatabaseHostLowPressure {
    @Mock
    private PrometheusUtil util;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private ActivityNumItem2 activityNumItem2;
    @Mock
    private ThreadPoolUsageItem2 threadPoolUsageItem2;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private DatabaseHostLowPressure databaseHostLowPressure;
    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption(String.valueOf(OptionCommon.IS_IO));
        optionQuery.setIsCheck(true);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold(ThresholdCommon.THREAD_POOL_USAGE_RATE);
        diagnosisThreshold.setThresholdValue("20");
        List<HisDiagnosisThreshold> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        hisDiagnosisTask = new HisDiagnosisTask();
        hisDiagnosisTask.setNodeId(nodeId);
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(eTime);
        hisDiagnosisTask.setConfigs(config);
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
        hisDiagnosisTask.setClusterId(nodeId);
        hisDiagnosisTask.setRemarks("test");
        hisDiagnosisTask.setCreateTime(new Date());
        hisDiagnosisTask.setDbName("test");
        hisDiagnosisTask.setNodeVOSub(new OpsClusterVO());
        hisDiagnosisTask.setIsDeleted(1);
        hisDiagnosisTask.setId(1);
        hisDiagnosisTask.setUpdateTime(new Date());
    }

    @Test
    public void testGetOption() {
        List<String> list = databaseHostLowPressure.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = databaseHostLowPressure.getSourceDataKeys();
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(activityNumItem2, result.get(0));
        Assertions.assertEquals(threadPoolUsageItem2, result.get(1));
    }

    @Test
    public void testAnalysis_NoPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(activityNumItem2)).thenReturn(config);
        when(dataStoreService.getData(threadPoolUsageItem2)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = databaseHostLowPressure.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(activityNumItem2);
        config.setCount(1);
        when(dataStoreService.getData(activityNumItem2)).thenReturn(config);
        DataStoreConfig dbConfig = mock(DataStoreConfig.class);
        dbConfig.setCollectionItem(threadPoolUsageItem2);
        dbConfig.setCount(1);
        when(dataStoreService.getData(threadPoolUsageItem2)).thenReturn(config);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        prometheusData.setValues(new JSONArray());
        list.add(prometheusData);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = databaseHostLowPressure.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.SUGGESTIONS, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> databaseHostLowPressure.getShowData(taskId));
    }

    @Test
    public void testGetShowData() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        List<PrometheusData> numList = new ArrayList<>();
        PrometheusData numData = new PrometheusData();
        numData.setMetric(new JSONObject());
        numData.setValues(new JSONArray());
        numList.add(numData);
        when(activityNumItem2.queryData(hisDiagnosisTask)).thenReturn(numList);
        List<PrometheusData> poolList = new ArrayList<>();
        PrometheusData poolData = new PrometheusData();
        poolData.setMetric(new JSONObject());
        poolData.setValues(new JSONArray());
        poolList.add(poolData);
        when(threadPoolUsageItem2.queryData(hisDiagnosisTask)).thenReturn(poolList);
        PrometheusDataDTO numDataDTO = new PrometheusDataDTO();
        numDataDTO.setChartName(MetricCommon.ACTIVITY_NUM);
        List<MetricDataDTO> numDataDTOS = new ArrayList<>();
        MetricDataDTO numMetricData = new MetricDataDTO();
        numMetricData.setData(new ArrayList<>());
        numMetricData.setName(MetricCommon.ACTIVITY_NUM);
        numDataDTOS.add(numMetricData);
        numDataDTO.setDatas(numDataDTOS);
        when(util.metricToLine(numList)).thenReturn(numDataDTO);
        PrometheusDataDTO poolDataDTO = new PrometheusDataDTO();
        poolDataDTO.setChartName(MetricCommon.THREAD_POOL_USAGE_RATE);
        List<MetricDataDTO> poolDataDTOS = new ArrayList<>();
        MetricDataDTO poolMetricData = new MetricDataDTO();
        poolMetricData.setData(new ArrayList<>());
        poolMetricData.setName(MetricCommon.THREAD_POOL_USAGE_RATE);
        poolDataDTOS.add(poolMetricData);
        poolDataDTO.setDatas(poolDataDTOS);
        when(util.metricToLine(poolList)).thenReturn(poolDataDTO);
        List<PrometheusDataDTO> dataDTOList = databaseHostLowPressure.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}

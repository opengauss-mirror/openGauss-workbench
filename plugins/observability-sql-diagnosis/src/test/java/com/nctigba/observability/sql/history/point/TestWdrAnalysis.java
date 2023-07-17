/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.MetricCommon;
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
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.service.history.point.WdrAnalysis;
import com.nctigba.observability.sql.util.PointUtil;
import com.nctigba.observability.sql.util.PrometheusUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestWdrAnalysis
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWdrAnalysis {
    @Mock
    private PrometheusUtil util;
    @Mock
    private PointUtil pointUtil;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private DbAvgCpuItem dbAvgCpuItem;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private WdrAnalysis wdrAnalysis;
    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption("IS_LOCK");
        optionQuery.setIsCheck(true);
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold("cpuUsageRate");
        diagnosisThreshold.setThresholdValue("20");
        hisDiagnosisTask = new HisDiagnosisTask();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        hisDiagnosisTask.setNodeId(nodeId);
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(eTime);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        hisDiagnosisTask.setConfigs(config);
        List<HisDiagnosisThreshold> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
    }

    @Test
    public void testGetOption() {
        List<String> list = wdrAnalysis.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = wdrAnalysis.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dbAvgCpuItem, result.get(0));
    }

    @Test
    public void testAnalysis_NoPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = wdrAnalysis.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        JSONArray objects = new JSONArray();
        for (int i = 0; i <= 240; i++) {
            JSONArray array = new JSONArray();
            array.add("100000");
            objects.add(array);
        }
        prometheusData.setValues(objects);
        list.add(prometheusData);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = wdrAnalysis.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> wdrAnalysis.getShowData(taskId));
    }

    @Test
    public void testGetShowData() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        prometheusData.setValues(new JSONArray());
        list.add(prometheusData);
        when(dbAvgCpuItem.queryData(hisDiagnosisTask)).thenReturn(list);
        List<PrometheusData> prometheusDataList = new ArrayList<>();
        prometheusDataList.add(prometheusData);
        when(pointUtil.dataToObject(list)).thenReturn(prometheusDataList);
        PrometheusDataDTO prometheusDataDTO = new PrometheusDataDTO();
        prometheusDataDTO.setData(new ArrayList<>());
        prometheusDataDTO.setChartName(MetricCommon.DB_AVG_CPU_USAGE_RATE);
        List<MetricDataDTO> dataDTOS = new ArrayList<>();
        MetricDataDTO metricDataDTO = new MetricDataDTO();
        metricDataDTO.setData(new ArrayList<>());
        metricDataDTO.setName(MetricCommon.DB_AVG_CPU_USAGE_RATE);
        dataDTOS.add(metricDataDTO);
        prometheusDataDTO.setDatas(dataDTOS);
        when(util.metricToLine(prometheusDataList)).thenReturn(prometheusDataDTO);
        List<PrometheusDataDTO> dataDTOList = wdrAnalysis.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}

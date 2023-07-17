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
import com.nctigba.observability.sql.service.history.collection.metric.AvgCpuItem;
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.service.history.point.OtherProcessAvgCpuUsage;
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
 * TestOtherProcessAvgCpuUsage
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestOtherProcessAvgCpuUsage {
    @Mock
    private PrometheusUtil util;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private AvgCpuItem avgCpuItem;
    @Mock
    private DbAvgCpuItem dbAvgCpuItem;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private OtherProcessAvgCpuUsage processAvgCpuUsage;
    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption(String.valueOf(OptionCommon.IS_IO));
        optionQuery.setIsCheck(true);
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold(ThresholdCommon.CPU_USAGE_RATE);
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
        List<String> list = processAvgCpuUsage.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = processAvgCpuUsage.getSourceDataKeys();
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(avgCpuItem, result.get(0));
        Assertions.assertEquals(dbAvgCpuItem, result.get(1));
    }

    @Test
    public void testAnalysis_NoPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(avgCpuItem)).thenReturn(config);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = processAvgCpuUsage.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(avgCpuItem);
        config.setCount(1);
        when(dataStoreService.getData(avgCpuItem)).thenReturn(config);
        DataStoreConfig dbConfig = mock(DataStoreConfig.class);
        dbConfig.setCollectionItem(dbAvgCpuItem);
        dbConfig.setCount(1);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        prometheusData.setValues(new JSONArray());
        list.add(prometheusData);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = processAvgCpuUsage.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> processAvgCpuUsage.getShowData(taskId));
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
        when(avgCpuItem.queryData(hisDiagnosisTask)).thenReturn(numList);
        List<PrometheusData> poolList = new ArrayList<>();
        PrometheusData poolData = new PrometheusData();
        poolData.setMetric(new JSONObject());
        poolData.setValues(new JSONArray());
        poolList.add(poolData);
        when(dbAvgCpuItem.queryData(hisDiagnosisTask)).thenReturn(poolList);
        PrometheusDataDTO numDataDTO = new PrometheusDataDTO();
        numDataDTO.setChartName(MetricCommon.AVG_CPU_USAGE_RATE);
        List<MetricDataDTO> numDataDTOS = new ArrayList<>();
        MetricDataDTO numMetricData = new MetricDataDTO();
        numMetricData.setData(new ArrayList<>());
        numMetricData.setName(MetricCommon.AVG_CPU_USAGE_RATE);
        numDataDTOS.add(numMetricData);
        numDataDTO.setDatas(numDataDTOS);
        when(util.metricToLine(numList)).thenReturn(numDataDTO);
        PrometheusDataDTO poolDataDTO = new PrometheusDataDTO();
        poolDataDTO.setChartName(MetricCommon.DB_AVG_CPU_USAGE_RATE);
        List<MetricDataDTO> poolDataDTOS = new ArrayList<>();
        MetricDataDTO poolMetricData = new MetricDataDTO();
        poolMetricData.setData(new ArrayList<>());
        poolMetricData.setName(MetricCommon.DB_AVG_CPU_USAGE_RATE);
        poolDataDTOS.add(poolMetricData);
        poolDataDTO.setDatas(poolDataDTOS);
        when(util.metricToLine(poolList)).thenReturn(poolDataDTO);
        PrometheusDataDTO dataDTOList = processAvgCpuUsage.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}

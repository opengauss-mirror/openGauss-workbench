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
import com.nctigba.observability.sql.model.history.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.history.point.MetricDataDTO;
import com.nctigba.observability.sql.model.history.point.PrometheusDataDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.metric.BusinessConnCountItem;
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.service.history.point.BusinessConnCount;
import com.nctigba.observability.sql.util.PointUtil;
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
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestBusinessConnCount
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestBusinessConnCount {
    @Mock
    private PrometheusUtil util;
    @Mock
    private PointUtil pointUtil;
    @Mock
    private BusinessConnCountItem countItem;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private DbAvgCpuItem dbAvgCpuItem;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private BusinessConnCount businessConnCount;
    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption(String.valueOf(OptionCommon.IS_MEMORY));
        optionQuery.setIsCheck(true);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold(ThresholdCommon.CONNECTION_NUM);
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
        List<String> list = businessConnCount.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = businessConnCount.getSourceDataKeys();
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(dbAvgCpuItem, result.get(0));
        Assertions.assertEquals(countItem, result.get(1));
    }

    @Test
    public void testAnalysis_NoPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = businessConnCount.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasProData_NoAsp() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        list.add(prometheusData);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = businessConnCount.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasProData_hasAsp() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        JSONArray jsonArray=new JSONArray();
        List<String> stringList=new ArrayList<>();
        stringList.add("100");
        stringList.add("200");
        jsonArray.add(stringList);
        prometheusData.setValues(jsonArray);
        list.add(prometheusData);
        when(config.getCollectionData()).thenReturn(list);
        List<AspAnalysisDTO> analysisDTOList = new ArrayList<>();
        AspAnalysisDTO analysisDTO = new AspAnalysisDTO(1, 2);
        analysisDTOList.add(analysisDTO);
        when(pointUtil.aspTimeSlot(list)).thenReturn(analysisDTOList);
        HashMap<String, String> map = new HashMap<>();
        map.put(ThresholdCommon.CONNECTION_NUM, "10");
        when(pointUtil.thresholdMap(hisDiagnosisTask.getThresholds())).thenReturn(map);
        when(countItem.queryData(any())).thenReturn(list);
        AnalysisDTO result = businessConnCount.analysis(hisDiagnosisTask, dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasData_NoPro() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        List<PrometheusData> list = new ArrayList<>();
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = businessConnCount.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> businessConnCount.getShowData(taskId));
    }

    @Test
    public void testGetShowData() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        List<PrometheusData> cpuList = new ArrayList<>();
        PrometheusData cpuData = new PrometheusData();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("cpu","50");
        cpuData.setMetric(jsonObject);
        JSONArray jsonArray=new JSONArray();
        jsonArray.add("cpu");
        cpuData.setValues(jsonArray);
        cpuList.add(cpuData);
        when(dbAvgCpuItem.queryData(hisDiagnosisTask)).thenReturn(cpuList);
        List<PrometheusData> countList = new ArrayList<>();
        PrometheusData countData = new PrometheusData();
        countData.setMetric(new JSONObject());
        countData.setValues(new JSONArray());
        countList.add(countData);
        when(countItem.queryData(hisDiagnosisTask)).thenReturn(countList);
        List<PrometheusData> cpuDataList = new ArrayList<>();
        cpuDataList.add(cpuData);
        when(pointUtil.dataToObject(cpuList)).thenReturn(cpuDataList);
        List<PrometheusData> countDataList = new ArrayList<>();
        countDataList.add(countData);
        when(pointUtil.dataToObject(countList)).thenReturn(countDataList);
        PrometheusDataDTO cpuDataDTO = new PrometheusDataDTO();
        cpuDataDTO.setData(new ArrayList<>());
        cpuDataDTO.setChartName(MetricCommon.DB_AVG_CPU_USAGE_RATE);
        List<MetricDataDTO> cpuDataDTOS = new ArrayList<>();
        MetricDataDTO cpuMetricDataDTO = new MetricDataDTO();
        cpuMetricDataDTO.setData(new ArrayList<>());
        cpuMetricDataDTO.setName(MetricCommon.DB_AVG_CPU_USAGE_RATE);
        cpuDataDTOS.add(cpuMetricDataDTO);
        cpuDataDTO.setDatas(cpuDataDTOS);
        when(util.metricToLine(cpuDataList)).thenReturn(cpuDataDTO);
        PrometheusDataDTO countDataDTO = new PrometheusDataDTO();
        countDataDTO.setData(new ArrayList<>());
        countDataDTO.setChartName(MetricCommon.BUSINESS_CONN_COUNT);
        List<MetricDataDTO> countDataDTOS = new ArrayList<>();
        MetricDataDTO countMetricDataDTO = new MetricDataDTO();
        countMetricDataDTO.setData(new ArrayList<>());
        countMetricDataDTO.setName(MetricCommon.BUSINESS_CONN_COUNT);
        countDataDTOS.add(countMetricDataDTO);
        countDataDTO.setDatas(countDataDTOS);
        when(util.metricToLine(countDataList)).thenReturn(countDataDTO);
        Object dataDTOList = businessConnCount.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}

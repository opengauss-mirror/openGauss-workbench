/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

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
import com.nctigba.observability.sql.service.history.point.AvgCpuUsageRate;
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
 * TestAvgCpuUsageRate
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAvgCpuUsageRate {
    @Mock
    private PrometheusUtil util;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private AvgCpuItem avgCpuItem;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private AvgCpuUsageRate avgCpuUsageRate;
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
        diagnosisThreshold.setThreshold(ThresholdCommon.CPU_USAGE_RATE);
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
        List<String> list = avgCpuUsageRate.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = avgCpuUsageRate.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(avgCpuItem, result.get(0));
    }

    @Test
    public void testAnalysis_NoPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(avgCpuItem)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = avgCpuUsageRate.analysis(mock(HisDiagnosisTask.class), dataStoreService);
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
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        list.add(prometheusData);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = avgCpuUsageRate.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.SUGGESTIONS, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> avgCpuUsageRate.getShowData(taskId));
    }

    @Test
    public void testGetShowData() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        list.add(prometheusData);
        when(avgCpuItem.queryData(hisDiagnosisTask)).thenReturn(list);
        List<PrometheusData> prometheusDataList = new ArrayList<>();
        prometheusDataList.add(prometheusData);
        PrometheusDataDTO prometheusDataDTO = new PrometheusDataDTO();
        prometheusDataDTO.setChartName(MetricCommon.AVG_CPU_USAGE_RATE);
        List<MetricDataDTO> dataDTOS = new ArrayList<>();
        MetricDataDTO metricData = new MetricDataDTO();
        metricData.setData(new ArrayList<>());
        metricData.setName(MetricCommon.AVG_CPU_USAGE_RATE);
        dataDTOS.add(metricData);
        prometheusDataDTO.setDatas(dataDTOS);
        when(util.metricToLine(prometheusDataList)).thenReturn(prometheusDataDTO);
        List<PrometheusDataDTO> dataDTOList = avgCpuUsageRate.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}

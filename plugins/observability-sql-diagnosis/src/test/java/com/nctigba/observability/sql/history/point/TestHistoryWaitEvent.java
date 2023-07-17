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
import com.nctigba.observability.sql.model.history.dto.MetricToTableDTO;
import com.nctigba.observability.sql.model.history.point.PrometheusDataDTO;
import com.nctigba.observability.sql.model.history.point.WaitEventDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.metric.WaitEventItem;
import com.nctigba.observability.sql.service.history.point.HistoryWaitEvent;
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
 * TestHistoryWaitEvent
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHistoryWaitEvent {
    @Mock
    private PrometheusUtil util;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private WaitEventItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private HistoryWaitEvent historyWaitEvent;
    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption(String.valueOf(OptionCommon.IS_CPU));
        optionQuery.setIsCheck(true);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold(ThresholdCommon.WAIT_EVENT_NUM);
        diagnosisThreshold.setThresholdValue("20");
        List<HisDiagnosisThreshold> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        hisDiagnosisTask = new HisDiagnosisTask();
        hisDiagnosisTask.setNodeId(nodeId);
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(new Date(eTime.getTime() + 20000));
        hisDiagnosisTask.setConfigs(config);
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
    }

    @Test
    public void testGetOption() {
        List<String> list = historyWaitEvent.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = historyWaitEvent.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysis_NoPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = historyWaitEvent.analysis(hisDiagnosisTask, dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasPrometheusData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        list.add(prometheusData);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = historyWaitEvent.analysis(hisDiagnosisTask, dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result);
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> historyWaitEvent.getShowData(taskId));
    }

    @Test
    public void testGetShowData_exists() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        list.add(prometheusData);
        when(item.queryData(hisDiagnosisTask)).thenReturn(list);
        PrometheusDataDTO prometheusDataDTO = new PrometheusDataDTO();
        prometheusDataDTO.setChartName(MetricCommon.WAIT_EVENT);
        prometheusDataDTO.setDatas(new ArrayList<>());
        List<MetricToTableDTO> dtoList = new ArrayList<>();
        MetricToTableDTO metricToTableDTO1 = new MetricToTableDTO();
        metricToTableDTO1.setName(MetricCommon.WAIT_EVENT);
        metricToTableDTO1.setValue("50");
        MetricToTableDTO metricToTableDTO2 = new MetricToTableDTO();
        metricToTableDTO2.setName("cpu");
        metricToTableDTO2.setValue("60");
        MetricToTableDTO metricToTableDTO3 = new MetricToTableDTO();
        metricToTableDTO3.setName(MetricCommon.WAIT_EVENT);
        metricToTableDTO3.setValue("60");
        dtoList.add(metricToTableDTO1);
        dtoList.add(metricToTableDTO2);
        dtoList.add(metricToTableDTO3);
        when(util.metricToTable(list)).thenReturn(dtoList);
        WaitEventDTO waitEventDTO = historyWaitEvent.getShowData(taskId);
        assertNotNull(waitEventDTO);
    }

    @Test
    public void testGetShowData_notExists() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        list.add(prometheusData);
        when(item.queryData(hisDiagnosisTask)).thenReturn(list);
        PrometheusDataDTO prometheusDataDTO = new PrometheusDataDTO();
        prometheusDataDTO.setChartName(MetricCommon.WAIT_EVENT);
        prometheusDataDTO.setDatas(new ArrayList<>());
        List<MetricToTableDTO> dtoList = new ArrayList<>();
        MetricToTableDTO metricToTableDTO1 = new MetricToTableDTO();
        metricToTableDTO1.setName("cpu");
        metricToTableDTO1.setValue("50");
        MetricToTableDTO metricToTableDTO2 = new MetricToTableDTO();
        metricToTableDTO2.setName("cpu");
        metricToTableDTO2.setValue("60");
        dtoList.add(metricToTableDTO1);
        dtoList.add(metricToTableDTO2);
        when(util.metricToTable(list)).thenReturn(dtoList);
        WaitEventDTO waitEventDTO = historyWaitEvent.getShowData(taskId);
        assertNotNull(waitEventDTO);
    }
}

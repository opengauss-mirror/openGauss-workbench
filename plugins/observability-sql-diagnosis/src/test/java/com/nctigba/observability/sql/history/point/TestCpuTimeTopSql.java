/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.DatabaseData;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.table.CpuTimeTopSqlItem;
import com.nctigba.observability.sql.service.history.point.CpuTimeTopSql;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestCpuTimeTopSql
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCpuTimeTopSql {
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private CpuTimeTopSqlItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private CpuTimeTopSql cpuTimeTopSql;
    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption(String.valueOf(OptionCommon.IS_MEMORY));
        optionQuery.setIsCheck(true);
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold(ThresholdCommon.SQL_NUM);
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
        List<String> list = cpuTimeTopSql.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = cpuTimeTopSql.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysis_NoDbData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = cpuTimeTopSql.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DISPLAY, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasDbData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        List<PrometheusData> list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        prometheusData.setMetric(new JSONObject());
        list.add(prometheusData);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = cpuTimeTopSql.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DISPLAY, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> cpuTimeTopSql.getShowData(taskId));
    }

    @Test
    public void testGetShowData_error() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        String error = "error:";
        when(item.queryData(hisDiagnosisTask)).thenReturn(error);
        List<?> dataDTOList = cpuTimeTopSql.getShowData(taskId);
        assertNotNull(dataDTOList);
        assertEquals(error, dataDTOList.get(0));
    }

    @Test
    public void testGetShowData_true() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        List<DatabaseData> list = new ArrayList<>();
        DatabaseData databaseData = new DatabaseData();
        databaseData.setValue(new JSONArray());
        JSONObject sqlName = new JSONObject();
        sqlName.put("__name__", SqlCommon.CPU_TIME_TOP_SQL);
        databaseData.setSqlName(sqlName);
        databaseData.setTableName("");
        list.add(databaseData);
        when(item.queryData(hisDiagnosisTask)).thenReturn(list);
        List<?> dataDTOList = cpuTimeTopSql.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}

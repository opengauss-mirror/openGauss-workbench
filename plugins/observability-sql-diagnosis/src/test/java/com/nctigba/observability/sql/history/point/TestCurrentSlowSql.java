/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.DatabaseData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.table.SlowSqlItem;
import com.nctigba.observability.sql.service.history.point.CurrentSlowSql;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestCurrentSlowSql
 *
 * @author luomeng
 * @since 2023/7/10
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCurrentSlowSql {
    @Mock
    private SlowSqlItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private CurrentSlowSql currentSlowSql;
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
        diagnosisThreshold.setThreshold(ThresholdCommon.SQL_NUM);
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
    }

    @Test
    public void testGetOption() {
        List<String> list = currentSlowSql.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = currentSlowSql.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(item, result.get(0));
    }

    @Test
    public void testGetDiagnosisType() {
        String type = currentSlowSql.getDiagnosisType();
        Assertions.assertEquals(DiagnosisTypeCommon.CURRENT, type);
    }

    @Test
    public void testAnalysis_NoDbData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = currentSlowSql.analysis(hisDiagnosisTask, dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasDbData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        List<DatabaseData> list = new ArrayList<>();
        DatabaseData databaseData = new DatabaseData();
        databaseData.setSqlName(new JSONObject());
        databaseData.setTableName("test");
        databaseData.setValue(new JSONArray());
        list.add(databaseData);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = currentSlowSql.analysis(hisDiagnosisTask, dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testGetShowData() {
        int taskId = 1;
        List<?> dataDTOList = currentSlowSql.getShowData(taskId);
        assertNull(dataDTOList);
    }
}

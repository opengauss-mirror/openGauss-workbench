/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.SqlCommon;
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
import com.nctigba.observability.sql.service.history.collection.table.ThreadWaitEventItem;
import com.nctigba.observability.sql.service.history.point.CurrentWaitEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestCurrentWaitEvent
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCurrentWaitEvent {
    @Mock
    private ThreadWaitEventItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private CurrentWaitEvent currentWaitEvent;
    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption(String.valueOf(OptionCommon.IS_CPU));
        optionQuery.setIsCheck(true);
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold(ThresholdCommon.WAIT_EVENT_NUM);
        diagnosisThreshold.setThresholdValue("20");
        hisDiagnosisTask = new HisDiagnosisTask();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        hisDiagnosisTask.setNodeId(nodeId);
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(new Date(eTime.getTime() + 20000));
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
        List<String> list = currentWaitEvent.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = currentWaitEvent.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysis_NoDbData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = currentWaitEvent.analysis(hisDiagnosisTask, dataStoreService);
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
        JSONArray array = new JSONArray();
        List<HashMap<String, String>> hashMaps = new ArrayList<>();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("count", "10");
        hashMap.put("wait_status", "hash join");
        hashMaps.add(hashMap);
        array.add(hashMaps);
        DatabaseData databaseData = new DatabaseData();
        databaseData.setValue(new JSONArray());
        JSONObject sqlName = new JSONObject();
        sqlName.put("__name__", SqlCommon.WAIT_EVENT);
        databaseData.setSqlName(sqlName);
        databaseData.setValue(array);
        List<DatabaseData> list = new ArrayList<>();
        list.add(databaseData);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = currentWaitEvent.analysis(hisDiagnosisTask, dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result);
    }

    @Test
    public void testGetShowData() {
        assertNull(currentWaitEvent.getShowData(1));
    }
}

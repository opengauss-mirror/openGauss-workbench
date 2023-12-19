/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TestCurrentWaitEvent.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/TestCurrentWaitEvent.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.vo.collection.DatabaseVO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.table.ThreadWaitEventItem;
import com.nctigba.observability.sql.service.impl.point.history.CurrentWaitEvent;
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
    private DiagnosisTaskDO diagnosisTaskDO;

    @Before
    public void before() {
        OptionVO optionVO = new OptionVO();
        optionVO.setOption(String.valueOf(OptionEnum.IS_CPU));
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold(ThresholdConstants.WAIT_EVENT_NUM);
        diagnosisThreshold.setThresholdValue("20");
        diagnosisTaskDO = new DiagnosisTaskDO();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        diagnosisTaskDO.setNodeId(nodeId);
        diagnosisTaskDO.setHisDataStartTime(sTime);
        diagnosisTaskDO.setHisDataEndTime(new Date(eTime.getTime() + 20000));
        List<OptionVO> config = new ArrayList<>() {{
            add(optionVO);
        }};
        diagnosisTaskDO.setConfigs(config);
        List<DiagnosisThresholdDO> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        diagnosisTaskDO.setThresholds(threshold);
        diagnosisTaskDO.setSpan("50s");
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
        DataStoreVO config = mock(DataStoreVO.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = currentWaitEvent.analysis(diagnosisTaskDO, dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasDbData() {
        DataStoreVO config = mock(DataStoreVO.class);
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
        DatabaseVO databaseVO = new DatabaseVO();
        databaseVO.setValue(new JSONArray());
        JSONObject sqlName = new JSONObject();
        sqlName.put("__name__", SqlConstants.WAIT_EVENT);
        databaseVO.setSqlName(sqlName);
        databaseVO.setValue(array);
        List<DatabaseVO> list = new ArrayList<>();
        list.add(databaseVO);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = currentWaitEvent.analysis(diagnosisTaskDO, dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result);
    }

    @Test
    public void testGetShowData() {
        assertNull(currentWaitEvent.getShowData(1));
    }
}

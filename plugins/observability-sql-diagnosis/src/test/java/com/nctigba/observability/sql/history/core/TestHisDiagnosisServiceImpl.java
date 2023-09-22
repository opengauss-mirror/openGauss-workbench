/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.core;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.history.HisThresholdMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.model.history.result.HisTreeNode;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.core.HisDiagnosisServiceImpl;
import com.nctigba.observability.sql.service.history.point.AspAnalysis;
import com.nctigba.observability.sql.service.history.point.LockTimeout;
import com.nctigba.observability.sql.util.LocaleString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestHisDiagnosisServiceImpl
 *
 * @author luomeng
 * @since 2023/7/4
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHisDiagnosisServiceImpl {
    @Mock
    private AspAnalysis aspAnalysis;
    @Mock
    private LockTimeout lockTimeout;
    @Mock
    private LocaleString localeToString;
    @Mock
    private HisThresholdMapper hisThresholdMapper;
    @Mock
    private HisDiagnosisResultMapper resultMapper;
    @Spy
    private List<HisDiagnosisPointService<?>> pointServiceList = new ArrayList<>();
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @InjectMocks
    private HisDiagnosisServiceImpl hisDiagnosisService;
    private final int taskId = 1;
    private final String pointName = "AspAnalysis";

    @Before
    public void before() {
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption("IS_LOCK");
        optionQuery.setIsCheck(true);
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold("cpuUsageRate");
        diagnosisThreshold.setThresholdValue("20");
        HisDiagnosisTask hisDiagnosisTask = new HisDiagnosisTask();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        hisDiagnosisTask.setNodeId(nodeId);
        Date sTime = new Date();
        hisDiagnosisTask.setHisDataStartTime(sTime);
        Date eTime = new Date();
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
        hisDiagnosisTask.setDiagnosisType(DiagnosisTypeCommon.HISTORY);
        HisDiagnosisResult hisDiagnosisResult = new HisDiagnosisResult();
        hisDiagnosisResult.setNodeId("37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345");
        hisDiagnosisResult.setPointName(pointName);
        hisDiagnosisResult.setTaskId(taskId);
        hisDiagnosisResult.setPointSuggestion("test");
    }

    @Test
    public void testGetTopologyMap() {
        HisDiagnosisResult result = mock(HisDiagnosisResult.class);
        List<HisDiagnosisResult> resultList = new ArrayList<>();
        resultList.add(result);
        when(resultMapper.selectList(any())).thenReturn(resultList);
        HisDiagnosisTask task = new HisDiagnosisTask();
        task.setDiagnosisType(DiagnosisTypeCommon.SQL);
        String topologyMap = "-SqlTaskInfo ROOT"
                + System.getProperty("line.separator")
                + "--ParamTuning CENTER"
                + System.getProperty("line.separator")
                + "---OsParam CENTER"
                + System.getProperty("line.separator")
                + "---DatabaseParam CENTER";
        task.setTopologyMap(topologyMap);
        when(taskMapper.selectById(taskId)).thenReturn(task);
        boolean isAll = true;
        String diagnosisType = "sql";
        HisTreeNode treeNode = hisDiagnosisService.getTopologyMap(1, isAll, diagnosisType);
        assertNotNull(treeNode);
    }

    @Test
    public void testGetNodeDetail_Exception() {
        String diagnosisType = "sql";
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(
                HisDiagnosisException.class, () -> hisDiagnosisService.getNodeDetail(taskId, pointName, diagnosisType));
        when(taskMapper.selectById(taskId)).thenReturn(new HisDiagnosisTask().setThresholds(new ArrayList<>()));
        assertThrows(
                HisDiagnosisException.class, () -> hisDiagnosisService.getNodeDetail(taskId, pointName, diagnosisType));
    }

    @Test
    public void testGetNodeDetail() {
        HisDiagnosisTask task = new HisDiagnosisTask();
        List<LinkedHashMap<String, String>> thresholds = new ArrayList<>();
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("cpuUsageRate", "20");
        thresholds.add(linkedHashMap);
        task.setThresholds((List<HisDiagnosisThreshold>) (List<?>) thresholds);
        when(taskMapper.selectById(taskId)).thenReturn(task);
        when(resultMapper.selectOne(any())).thenReturn(mock(HisDiagnosisResult.class));
        List<HisDiagnosisThreshold> thresholdList = new ArrayList<>();
        when(hisThresholdMapper.selectList(any())).thenReturn(thresholdList);
        String diagnosisType = "sql";
        Object branch1 = hisDiagnosisService.getNodeDetail(taskId, pointName, diagnosisType);
        assertNull(branch1);
        HisDiagnosisResult result = new HisDiagnosisResult();
        result.setData("test");
        result.setPointData(new JSONObject());
        result.setPointSuggestion("test");
        when(resultMapper.selectOne(any())).thenReturn(result);
        List<HisDiagnosisThreshold> thresholdList2 = new ArrayList<>();
        HisDiagnosisThreshold threshold = new HisDiagnosisThreshold();
        threshold.setThresholdValue("20");
        threshold.setThreshold("cpuUsageRate");
        thresholdList2.add(threshold);
        when(hisThresholdMapper.selectList(any())).thenReturn(thresholdList2);
        when(localeToString.trapLanguage(result)).thenReturn(result);
        Object branch2 = hisDiagnosisService.getNodeDetail(taskId, pointName, diagnosisType);
        assertNotNull(branch2);
    }
}

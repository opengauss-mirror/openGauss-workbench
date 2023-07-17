/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.model.history.result.HisTreeNode;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.Impl.HisDiagnosisServiceImpl;
import com.nctigba.observability.sql.service.history.point.AspAnalysis;
import com.nctigba.observability.sql.service.history.point.LockTimeout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    private HisDiagnosisResultMapper resultMapper;
    @Spy
    private List<HisDiagnosisPointService<?>> pointServiceList = new ArrayList<>();
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @InjectMocks
    private HisDiagnosisServiceImpl hisDiagnosisService;
    private HisDiagnosisResult hisDiagnosisResult;
    private final int taskId = 1;
    private final String pointName = "AspAnalysis";

    @Before
    public void before() {
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption("IS_LOCK");
        optionQuery.setIsCheck(true);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold("cpuUsageRate");
        diagnosisThreshold.setThresholdValue("20");
        List<HisDiagnosisThreshold> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        HisDiagnosisTask hisDiagnosisTask = new HisDiagnosisTask();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        hisDiagnosisTask.setNodeId(nodeId);
        Date sTime = new Date();
        hisDiagnosisTask.setHisDataStartTime(sTime);
        Date eTime = new Date();
        hisDiagnosisTask.setHisDataEndTime(eTime);
        hisDiagnosisTask.setConfigs(config);
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
        hisDiagnosisResult = new HisDiagnosisResult();
        hisDiagnosisResult.setNodeId("37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345");
        hisDiagnosisResult.setPointName(pointName);
        hisDiagnosisResult.setTaskId(taskId);
        hisDiagnosisResult.setPointSuggestion("test");
    }

    @Test
    public void testGetTopologyMap() {
        boolean isAll = true;
        HisTreeNode treeNode = hisDiagnosisService.getTopologyMap(taskId, isAll);
        assertNotNull(treeNode);
    }

    @Test
    public void testGetTopologyMap_hasData() {
        boolean isAll = true;
        List<HisDiagnosisResult> results = new ArrayList<>();
        results.add(hisDiagnosisResult);
        when(resultMapper.selectList(any())).thenReturn(results);
        HisTreeNode treeNode = hisDiagnosisService.getTopologyMap(taskId, isAll);
        assertNotNull(treeNode);
    }

    @Test
    public void testGetNodeDetail_Exception() {
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> hisDiagnosisService.getNodeDetail(taskId, pointName));
        when(taskMapper.selectById(taskId)).thenReturn(new HisDiagnosisTask().setThresholds(new ArrayList<>()));
        assertThrows(HisDiagnosisException.class, () -> hisDiagnosisService.getNodeDetail(taskId, pointName));
    }

    @Test
    public void testGetAllPoint() {
        Object object = hisDiagnosisService.getAllPoint(taskId);
        assertNotNull(object);
    }
}

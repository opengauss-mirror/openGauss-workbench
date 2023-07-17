/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.history.HisThresholdMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.dto.HisDiagnosisTaskDTO;
import com.nctigba.observability.sql.model.history.dto.HisThresholdDTO;
import com.nctigba.observability.sql.model.history.dto.LogInfoDTO;
import com.nctigba.observability.sql.model.history.dto.OptionDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.model.history.result.TaskState;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.Impl.TaskServiceImpl;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.elastic.LockTimeoutItem;
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * TaskServiceImplTest
 *
 * @author luomeng
 * @since 2023/6/30
 */
@RunWith(MockitoJUnitRunner.class)
public class TaskServiceImplTest {
    @Mock
    private DbAvgCpuItem dbAvgCpuItem;
    @Mock
    private LockTimeoutItem lockTimeoutItem;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private AspAnalysis aspAnalysis;
    @Mock
    private LockTimeout lockTimeout;
    @Mock
    private DataStoreService dataStoreService;
    @Mock
    private HisThresholdMapper hisThresholdMapper;
    @Mock
    private HisDiagnosisResultMapper resultMapper;
    @Mock
    private HisDiagnosisPointService<?> pointService;
    @Spy
    private List<HisDiagnosisPointService<?>> pointServiceList = new ArrayList<>();
    @InjectMocks
    private TaskServiceImpl taskService;
    private HisDiagnosisTaskDTO hisDiagnosisTaskDTO;
    private HisDiagnosisTask hisDiagnosisTask;


    @Before
    public void before() {
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        List<OptionDTO> configs = new ArrayList<>() {{
            add(new OptionDTO().setOption("IS_LOCK").setIsCheck(true));
        }};
        List<HisThresholdDTO> thresholds = new ArrayList<>() {{
            add(new HisThresholdDTO().setThreshold("cpuUsageRate").setThresholdValue("20"));
        }};
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
        hisDiagnosisTaskDTO = new HisDiagnosisTaskDTO();
        hisDiagnosisTaskDTO.setNodeId(nodeId);
        hisDiagnosisTaskDTO.setHisDataStartTime(sTime);
        hisDiagnosisTaskDTO.setHisDataEndTime(eTime);
        hisDiagnosisTaskDTO.setConfigs(configs);
        hisDiagnosisTaskDTO.setThresholds(thresholds);
        hisDiagnosisTask = new HisDiagnosisTask();
        hisDiagnosisTask.setNodeId(nodeId);
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(eTime);
        hisDiagnosisTask.setConfigs(config);
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
    }

    @Test
    public void testAdd() throws NullPointerException {
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        List<HisDiagnosisThreshold> thresholdList = new ArrayList<>();
        HisDiagnosisThreshold threshold = new HisDiagnosisThreshold();
        threshold.setThreshold("cpuUsageRate");
        threshold.setThresholdValue("50");
        thresholdList.add(threshold);
        when(hisThresholdMapper.selectList(Wrappers.emptyWrapper())).thenReturn(thresholdList);
        Integer result = taskService.add(hisDiagnosisTaskDTO);
        assertNull(result);
    }

    @Test
    public void testStartNormal() throws NullPointerException {
        hisDiagnosisTask.addRemarks(TaskState.CREATE);
        int taskId = 1;
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        HisDiagnosisTaskDTO taskDTO = hisDiagnosisTaskDTO;
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        when(aspAnalysis.getDiagnosisType()).thenReturn(DiagnosisTypeCommon.HISTORY);
        when(lockTimeout.getDiagnosisType()).thenReturn(DiagnosisTypeCommon.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(dbAvgCpuItem);
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(items);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        hisDiagnosisTask.addRemarks("test");
        hisDiagnosisTask.addRemarks("RUNNING", new Throwable());
        hisDiagnosisTask.addRemarks(TaskState.WAITING);
        hisDiagnosisTask.addRemarks(TaskState.DATA, new Throwable());
        hisDiagnosisTask.addRemarks(TaskState.FINISH);
        when(dbAvgCpuItem.queryData(hisDiagnosisTask)).thenReturn(null);
        when(lockTimeoutItem.queryData(hisDiagnosisTask)).thenReturn(null);
        taskService.start(taskId, taskDTO);
    }

    @Test
    public void testStartHistory() throws NullPointerException {
        int taskId = 1;
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        HisDiagnosisTaskDTO taskDTO = hisDiagnosisTaskDTO;
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        when(aspAnalysis.getDiagnosisType()).thenReturn(DiagnosisTypeCommon.HISTORY);
        when(lockTimeout.getDiagnosisType()).thenReturn(DiagnosisTypeCommon.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(null);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        when(lockTimeoutItem.queryData(hisDiagnosisTask)).thenReturn("true");
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        when(aspAnalysis.analysis(hisDiagnosisTask, dataStoreService)).thenReturn(analysisDTO);
        taskService.start(taskId, taskDTO);
    }

    @Test
    public void testStartCurrent() throws NullPointerException {
        int taskId = 1;
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        HisDiagnosisTaskDTO taskDTO = hisDiagnosisTaskDTO;
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        when(aspAnalysis.getDiagnosisType()).thenReturn(DiagnosisTypeCommon.CURRENT);
        when(lockTimeout.getDiagnosisType()).thenReturn(DiagnosisTypeCommon.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(dbAvgCpuItem);
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(items);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        when(dbAvgCpuItem.queryData(hisDiagnosisTask)).thenReturn("error");
        when(lockTimeoutItem.queryData(hisDiagnosisTask)).thenReturn(null);
        taskService.start(taskId, taskDTO);
    }

    @Test
    public void testStartAbnormal() throws NullPointerException {
        int taskId = 1;
        HisDiagnosisTaskDTO taskDTO = hisDiagnosisTaskDTO;
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        when(aspAnalysis.getDiagnosisType()).thenReturn(DiagnosisTypeCommon.CURRENT);
        when(lockTimeout.getDiagnosisType()).thenReturn(DiagnosisTypeCommon.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(dbAvgCpuItem);
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(items);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        when(dbAvgCpuItem.queryData(hisDiagnosisTask)).thenReturn("error");
        when(lockTimeoutItem.queryData(hisDiagnosisTask)).thenReturn(new LogInfoDTO());
        taskService.start(taskId, taskDTO);
    }

}

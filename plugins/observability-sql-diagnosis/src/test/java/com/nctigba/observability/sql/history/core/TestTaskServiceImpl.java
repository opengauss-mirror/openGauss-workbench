/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.core;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.PointTypeCommon;
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
import com.nctigba.observability.sql.model.history.query.TaskQuery;
import com.nctigba.observability.sql.model.history.result.TaskState;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.EbpfCollectionItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.OsParamItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.ProfileItem;
import com.nctigba.observability.sql.service.history.collection.elastic.LockTimeoutItem;
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.service.history.collection.table.DatabaseItem;
import com.nctigba.observability.sql.service.history.collection.table.ExplainItem;
import com.nctigba.observability.sql.service.history.core.SqlValidator;
import com.nctigba.observability.sql.service.history.core.TaskServiceImpl;
import com.nctigba.observability.sql.service.history.point.AspAnalysis;
import com.nctigba.observability.sql.service.history.point.LockTimeout;
import com.nctigba.observability.sql.service.history.point.sql.BioLatency;
import com.nctigba.observability.sql.service.history.point.sql.BioSnoop;
import com.nctigba.observability.sql.service.history.point.sql.DatabaseParam;
import com.nctigba.observability.sql.service.history.point.sql.IndexAdvisor;
import com.nctigba.observability.sql.service.history.point.sql.ObjectInfoCheck;
import com.nctigba.observability.sql.service.history.point.sql.OnCpu;
import com.nctigba.observability.sql.service.history.point.sql.OsParam;
import com.nctigba.observability.sql.util.ParamJDBCUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TaskServiceImplTest
 *
 * @author luomeng
 * @since 2023/6/30
 */
@RunWith(MockitoJUnitRunner.class)
public class TestTaskServiceImpl {
    @Mock
    private DbAvgCpuItem dbAvgCpuItem;
    @Mock
    private LockTimeoutItem lockTimeoutItem;
    @Mock
    private ProfileItem profileItem;
    @Mock
    private DatabaseItem databaseItem;
    @Mock
    private ExplainItem explainItem;
    @Mock
    private OsParamItem osParamItem;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private SqlValidator sqlValidator;
    @Mock
    private BioLatency bioLatency;
    @Mock
    private BioSnoop bioSnoop;
    @Mock
    private AspAnalysis aspAnalysis;
    @Mock
    private LockTimeout lockTimeout;
    @Mock
    private OnCpu onCpu;
    @Mock
    private IndexAdvisor indexAdvisor;
    @Mock
    private DatabaseParam databaseParam;
    @Mock
    private ObjectInfoCheck objectInfoCheck;
    @Mock
    private OsParam osParam;
    @Mock
    private DataStoreService dataStoreService;
    @Mock
    private HisThresholdMapper hisThresholdMapper;
    @Mock
    private HisDiagnosisResultMapper resultMapper;
    @Mock
    private ParamJDBCUtil paramJDBCUtil;
    @Mock
    private ClusterManager clusterManager;
    @Spy
    private List<HisDiagnosisPointService<?>> pointServiceList = new ArrayList<>();
    @Spy
    private List<EbpfCollectionItem> ebpfItemList = new ArrayList<>();
    @InjectMocks
    private TaskServiceImpl taskService;
    private HisDiagnosisTaskDTO hisDiagnosisTaskDTO;
    private HisDiagnosisTask hisDiagnosisTask;
    private HisDiagnosisTask sqlTask;


    @Before
    public void before() {
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption("IS_LOCK");
        optionQuery.setIsCheck(true);
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold("cpuUsageRate");
        diagnosisThreshold.setThresholdValue("20");
        hisDiagnosisTaskDTO = new HisDiagnosisTaskDTO();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        hisDiagnosisTaskDTO.setNodeId(nodeId);
        Date sTime = new Date();
        hisDiagnosisTaskDTO.setHisDataStartTime(sTime);
        Date eTime = new Date();
        hisDiagnosisTaskDTO.setHisDataEndTime(eTime);
        List<OptionDTO> configs = new ArrayList<>() {{
            add(new OptionDTO().setOption("IS_LOCK").setIsCheck(true));
        }};
        hisDiagnosisTaskDTO.setConfigs(configs);
        List<HisThresholdDTO> thresholds = new ArrayList<>() {{
            add(new HisThresholdDTO().setThreshold("cpuUsageRate").setThresholdValue("20"));
        }};
        hisDiagnosisTaskDTO.setThresholds(thresholds);
        hisDiagnosisTaskDTO.setDiagnosisType("sql");
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        List<HisDiagnosisThreshold> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        hisDiagnosisTask = new HisDiagnosisTask(hisDiagnosisTaskDTO, config, threshold);
        hisDiagnosisTask.setSpan("50s");
        hisDiagnosisTask.setDiagnosisType(DiagnosisTypeCommon.HISTORY);
        List<LinkedHashMap<String, String>> sqlThreshold = new ArrayList<>();
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("cpuUsageRate", "20");
        sqlThreshold.add(linkedHashMap);
        List<LinkedHashMap<String, Object>> sqlOption = new ArrayList<>();
        LinkedHashMap<String, Object> linkedHashMap2 = new LinkedHashMap<>();
        linkedHashMap2.put("option", "IS_ON_CPU");
        linkedHashMap2.put("isCheck", true);
        sqlOption.add(linkedHashMap2);
        sqlTask = new HisDiagnosisTask(
                hisDiagnosisTaskDTO, (List<OptionQuery>) (List<?>) sqlOption,
                (List<HisDiagnosisThreshold>) (List<?>) sqlThreshold);
        sqlTask.setSpan("50s");
        sqlTask.setDiagnosisType(DiagnosisTypeCommon.SQL);
        sqlTask.setSql("select 1");
        sqlTask.setDbName("postgres");
        sqlTask.setTaskName("test");
        sqlTask.setState(TaskState.CREATE);
    }

    @Test
    public void testAdd() throws NullPointerException {
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        pointServiceList.add(indexAdvisor);
        pointServiceList.add(onCpu);
        List<String> options = new ArrayList<>();
        options.add("IS_ON_CPU");
        when(onCpu.getOption()).thenReturn(options);
        when(clusterManager.getOpsClusterIdByNodeId(hisDiagnosisTaskDTO.getNodeId())).thenReturn(any());
        Integer result = taskService.add(hisDiagnosisTaskDTO);
        assertNull(result);
        Integer result2 = taskService.add(mock(HisDiagnosisTaskDTO.class));
        assertNull(result2);
    }

    @Test
    public void testStartNormal_history() throws NullPointerException {
        hisDiagnosisTask.addRemarks(TaskState.CREATE);
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(any())).thenReturn(hisDiagnosisTask);
        when(aspAnalysis.getDiagnosisType()).thenReturn(PointTypeCommon.HISTORY);
        when(lockTimeout.getDiagnosisType()).thenReturn(PointTypeCommon.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(dbAvgCpuItem);
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(items);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        hisDiagnosisTask.addRemarks("test");
        hisDiagnosisTask.addRemarks("RUNNING", new Throwable());
        hisDiagnosisTask.addRemarks(TaskState.WAITING);
        hisDiagnosisTask.addRemarks(TaskState.RECEIVING, new Throwable());
        hisDiagnosisTask.addRemarks(TaskState.FINISH);
        taskService.start(1);
    }

    @Test
    public void testStartNormal_sql() throws NullPointerException {
        try (Connection conn = mock(Connection.class)) {
            hisDiagnosisTask.addRemarks(TaskState.CREATE);
            List<String> options = new ArrayList<>();
            options.add("IS_ON_CPU");
            when(onCpu.getOption()).thenReturn(options);
            pointServiceList.add(onCpu);
            pointServiceList.add(indexAdvisor);
            List<String> paramOption = new ArrayList<>();
            paramOption.add("IS_PARAM");
            when(databaseParam.getOption()).thenReturn(paramOption);
            pointServiceList.add(databaseParam);
            List<CollectionItem<?>> items = new ArrayList<>();
            items.add(profileItem);
            when(onCpu.getSourceDataKeys()).thenReturn(items);
            List<CollectionItem<?>> paramItem = new ArrayList<>();
            paramItem.add(databaseItem);
            when(databaseParam.getSourceDataKeys()).thenReturn(paramItem);
            when(profileItem.getCollectionType()).thenReturn(CollectionTypeCommon.AFTER);
            hisDiagnosisTask.addRemarks("test");
            hisDiagnosisTask.addRemarks("RUNNING", new Throwable());
            hisDiagnosisTask.addRemarks(TaskState.WAITING);
            hisDiagnosisTask.addRemarks(TaskState.RECEIVING, new Throwable());
            hisDiagnosisTask.addRemarks(TaskState.FINISH);
            when(clusterManager.getConnectionByNodeId(hisDiagnosisTask.getNodeId())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(123);
            when(resultSet.getString(1)).thenReturn("on");
            when(taskMapper.selectById(any())).thenReturn(sqlTask);
            taskService.start(1);
            List<LinkedHashMap<String, Object>> sqlOption = new ArrayList<>();
            LinkedHashMap<String, Object> linkedHashMap2 = new LinkedHashMap<>();
            linkedHashMap2.put("option", "IS_PARAM");
            linkedHashMap2.put("isCheck", true);
            sqlOption.add(linkedHashMap2);
            sqlTask.setConfigs((List<OptionQuery>) (List<?>) sqlOption);
            when(taskMapper.selectById(any())).thenReturn(sqlTask);
            taskService.start(1);
        } catch (SQLException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }

    @Test
    public void testStartHistory() throws NullPointerException {
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(any())).thenReturn(hisDiagnosisTask);
        when(aspAnalysis.getDiagnosisType()).thenReturn(PointTypeCommon.HISTORY);
        when(lockTimeout.getDiagnosisType()).thenReturn(PointTypeCommon.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(null);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        taskService.start(1);
    }

    @Test
    public void testStartCurrent() throws NullPointerException {
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(any())).thenReturn(hisDiagnosisTask);
        when(aspAnalysis.getDiagnosisType()).thenReturn(PointTypeCommon.CURRENT);
        when(lockTimeout.getDiagnosisType()).thenReturn(PointTypeCommon.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(dbAvgCpuItem);
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(items);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        taskService.start(1);
    }

    @Test
    public void testStartAbnormal() throws NullPointerException {
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(any())).thenReturn(hisDiagnosisTask);
        when(aspAnalysis.getDiagnosisType()).thenReturn(PointTypeCommon.CURRENT);
        when(lockTimeout.getDiagnosisType()).thenReturn(PointTypeCommon.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(dbAvgCpuItem);
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(items);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        when(dbAvgCpuItem.queryData(hisDiagnosisTask)).thenReturn("error");
        when(lockTimeoutItem.queryData(hisDiagnosisTask)).thenReturn(new LogInfoDTO());
        taskService.start(1);
    }

    @Test
    public void testGetOption() throws NullPointerException {
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        pointServiceList.add(bioLatency);
        pointServiceList.add(bioSnoop);
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionCommon.IS_BCC));
        when(bioLatency.getOption()).thenReturn(option);
        String diagnosisType = "sql";
        List<OptionQuery> optionList = taskService.getOption(diagnosisType);
        assertNotNull(optionList);
    }

    @Test
    public void testSelectByPage() {
        taskService.selectByPage(mock(TaskQuery.class));
        verify(taskMapper, times(1)).selectPage(any(), any());
    }

    @Test
    public void testDelete() {
        taskService.delete(1);
        verify(taskMapper, times(1)).deleteById(anyInt());
    }

    @Test
    public void testExplainAfter() {
        try (Connection conn = mock(Connection.class)) {
            when(clusterManager.getConnectionByNodeId(hisDiagnosisTask.getNodeId())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getString(1)).thenReturn("on");
            when(taskMapper.selectById(any())).thenReturn(sqlTask);
            List<String> options = new ArrayList<>();
            options.add("IS_EXPLAIN");
            when(objectInfoCheck.getOption()).thenReturn(options);
            pointServiceList.add(objectInfoCheck);
            taskService.explainAfter(sqlTask, mock(ArrayList.class));
            List<LinkedHashMap<String, Object>> sqlOption = new ArrayList<>();
            LinkedHashMap<String, Object> linkedHashMap2 = new LinkedHashMap<>();
            linkedHashMap2.put("option", "IS_EXPLAIN");
            linkedHashMap2.put("isCheck", true);
            sqlOption.add(linkedHashMap2);
            sqlTask.setConfigs((List<OptionQuery>) (List<?>) sqlOption);
            List<CollectionItem<?>> list = new ArrayList<>();
            list.add(explainItem);
            when(objectInfoCheck.getSourceDataKeys()).thenReturn(list);
            taskService.explainAfter(sqlTask, mock(ArrayList.class));
        } catch (SQLException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }

    @Test
    public void testBccResult() {
        ebpfItemList.add(osParamItem);
        when(taskMapper.selectById(any())).thenReturn(sqlTask);
        pointServiceList.add(osParam);
        when(osParamItem.getHttpParam()).thenReturn(AgentParamCommon.OSPARAM);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(osParamItem);
        when(osParam.getSourceDataKeys()).thenReturn(items);
        MultipartFile file = mock(MultipartFile.class);
        taskService.bccResult("1", "osParam", file);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1L);
        when(osParam.analysis(any(), any())).thenReturn(mock(AnalysisDTO.class));
        taskService.bccResult("1", "osParam", file);
    }
}

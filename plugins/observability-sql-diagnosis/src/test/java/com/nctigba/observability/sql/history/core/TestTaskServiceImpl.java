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
 *  TestTaskServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/core/TestTaskServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.core;

import com.nctigba.observability.sql.constant.AgentParamConstants;
import com.nctigba.observability.sql.constant.CollectionTypeConstants;
import com.nctigba.observability.sql.constant.DiagnosisTypeConstants;
import com.nctigba.observability.sql.constant.PointTypeConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.enums.TaskStateEnum;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.DiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.HisThresholdMapper;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.mapper.param.ParamInfoMapper;
import com.nctigba.observability.sql.model.dto.DiagnosisTaskDTO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.query.TaskQuery;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.vo.point.LogInfoVO;
import com.nctigba.observability.sql.model.vo.point.ThresholdVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.collection.ebpf.EbpfCollectionItem;
import com.nctigba.observability.sql.service.impl.collection.ebpf.OsParamItem;
import com.nctigba.observability.sql.service.impl.collection.ebpf.ProfileItem;
import com.nctigba.observability.sql.service.impl.collection.elastic.LockTimeoutItem;
import com.nctigba.observability.sql.service.impl.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.service.impl.collection.table.DatabaseItem;
import com.nctigba.observability.sql.service.impl.collection.table.ExplainItem;
import com.nctigba.observability.sql.service.impl.core.SqlValidator;
import com.nctigba.observability.sql.service.impl.core.TaskServiceImpl;
import com.nctigba.observability.sql.service.impl.point.history.AspAnalysis;
import com.nctigba.observability.sql.service.impl.point.history.LockTimeout;
import com.nctigba.observability.sql.service.impl.point.sql.BioLatency;
import com.nctigba.observability.sql.service.impl.point.sql.BioSnoop;
import com.nctigba.observability.sql.service.impl.point.sql.DatabaseParam;
import com.nctigba.observability.sql.service.impl.point.sql.IndexAdvisor;
import com.nctigba.observability.sql.service.impl.point.sql.ObjectInfoCheck;
import com.nctigba.observability.sql.service.impl.point.sql.OnCpu;
import com.nctigba.observability.sql.service.impl.point.sql.OsParam;
import lombok.SneakyThrows;
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
    private DiagnosisTaskMapper taskMapper;
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
    private DiagnosisResultMapper resultMapper;
    @Mock
    private ClusterManager clusterManager;
    @Spy
    private List<DiagnosisPointService<?>> pointServiceList = new ArrayList<>();
    @Spy
    private List<EbpfCollectionItem> ebpfItemList = new ArrayList<>();
    @InjectMocks
    private TaskServiceImpl taskService;
    private DiagnosisTaskDTO diagnosisTaskDTO;
    private DiagnosisTaskDO diagnosisTaskDO;
    private DiagnosisTaskDO sqlTask;
    @Mock
    private ParamInfoMapper paramInfoMapper;
    @Mock
    private NctigbaEnvMapper envMapper;


    @Before
    public void before() {
        OptionVO optionVO = new OptionVO();
        optionVO.setOption("IS_LOCK");
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold("cpuUsageRate");
        diagnosisThreshold.setThresholdValue("20");
        diagnosisTaskDTO = new DiagnosisTaskDTO();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        diagnosisTaskDTO.setNodeId(nodeId);
        Date sTime = new Date();
        diagnosisTaskDTO.setHisDataStartTime(sTime);
        Date eTime = new Date();
        diagnosisTaskDTO.setHisDataEndTime(eTime);
        List<com.nctigba.observability.sql.model.vo.point.OptionVO> configs = new ArrayList<>() {{
            add(new com.nctigba.observability.sql.model.vo.point.OptionVO().setOption("IS_LOCK").setIsCheck(true));
        }};
        diagnosisTaskDTO.setConfigs(configs);
        List<ThresholdVO> thresholds = new ArrayList<>() {{
            add(new ThresholdVO().setThreshold("cpuUsageRate").setThresholdValue("20"));
        }};
        diagnosisTaskDTO.setThresholds(thresholds);
        diagnosisTaskDTO.setDiagnosisType("sql");
        List<OptionVO> config = new ArrayList<>() {{
            add(optionVO);
        }};
        List<DiagnosisThresholdDO> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        diagnosisTaskDO = new DiagnosisTaskDO(diagnosisTaskDTO, config, threshold);
        diagnosisTaskDO.setSpan("50s");
        diagnosisTaskDO.setDiagnosisType(DiagnosisTypeConstants.HISTORY);
        List<LinkedHashMap<String, String>> sqlThreshold = new ArrayList<>();
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("cpuUsageRate", "20");
        sqlThreshold.add(linkedHashMap);
        List<LinkedHashMap<String, Object>> sqlOption = new ArrayList<>();
        LinkedHashMap<String, Object> linkedHashMap2 = new LinkedHashMap<>();
        linkedHashMap2.put("option", "IS_ON_CPU");
        linkedHashMap2.put("isCheck", true);
        sqlOption.add(linkedHashMap2);
        sqlTask = new DiagnosisTaskDO(
                diagnosisTaskDTO, (List<OptionVO>) (List<?>) sqlOption,
                (List<DiagnosisThresholdDO>) (List<?>) sqlThreshold);
        sqlTask.setSpan("50s");
        sqlTask.setDiagnosisType(DiagnosisTypeConstants.SQL);
        sqlTask.setSql("select 1");
        sqlTask.setDbName("postgres");
        sqlTask.setTaskName("test");
        sqlTask.setState(TaskStateEnum.CREATE);
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
        when(clusterManager.getOpsClusterIdByNodeId(diagnosisTaskDTO.getNodeId())).thenReturn(any());
        Integer result = taskService.add(diagnosisTaskDTO);
        assertNull(result);
        Integer result2 = taskService.add(mock(DiagnosisTaskDTO.class));
        assertNull(result2);
    }

    @SneakyThrows
    @Test
    public void testStartNormal_history() throws NullPointerException {
        diagnosisTaskDO.addRemarks(TaskStateEnum.CREATE);
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(any())).thenReturn(diagnosisTaskDO);
        when(aspAnalysis.getDiagnosisType()).thenReturn(PointTypeConstants.HISTORY);
        when(lockTimeout.getDiagnosisType()).thenReturn(PointTypeConstants.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(dbAvgCpuItem);
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(items);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        diagnosisTaskDO.addRemarks("test");
        diagnosisTaskDO.addRemarks("RUNNING", new Throwable());
        diagnosisTaskDO.addRemarks(TaskStateEnum.WAITING);
        diagnosisTaskDO.addRemarks(TaskStateEnum.RECEIVING, new Throwable());
        diagnosisTaskDO.addRemarks(TaskStateEnum.FINISH);
        Connection conn = mock(Connection.class);
        when(clusterManager.getConnectionByNodeId(any())).thenReturn(conn);
        Statement statement = mock(Statement.class);
        when(conn.createStatement()).thenReturn(statement);
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(envMapper.selectOne(any())).thenReturn(any());
        taskService.start(1);
    }

    @Test
    public void testStartNormal_sql() throws NullPointerException {
        try (Connection conn = mock(Connection.class)) {
            diagnosisTaskDO.addRemarks(TaskStateEnum.CREATE);
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
            when(profileItem.getCollectionType()).thenReturn(CollectionTypeConstants.AFTER);
            diagnosisTaskDO.addRemarks("test");
            diagnosisTaskDO.addRemarks("RUNNING", new Throwable());
            diagnosisTaskDO.addRemarks(TaskStateEnum.WAITING);
            diagnosisTaskDO.addRemarks(TaskStateEnum.RECEIVING, new Throwable());
            diagnosisTaskDO.addRemarks(TaskStateEnum.FINISH);
            when(clusterManager.getConnectionByNodeId(diagnosisTaskDO.getNodeId())).thenReturn(conn);
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
            sqlTask.setConfigs((List<OptionVO>) (List<?>) sqlOption);
            when(taskMapper.selectById(any())).thenReturn(sqlTask);
            taskService.start(1);
        } catch (SQLException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }

    @SneakyThrows
    @Test
    public void testStartHistory() throws NullPointerException {
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(any())).thenReturn(diagnosisTaskDO);
        when(aspAnalysis.getDiagnosisType()).thenReturn(PointTypeConstants.HISTORY);
        when(lockTimeout.getDiagnosisType()).thenReturn(PointTypeConstants.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(null);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        Connection conn = mock(Connection.class);
        when(clusterManager.getConnectionByNodeId(any())).thenReturn(conn);
        Statement statement = mock(Statement.class);
        when(conn.createStatement()).thenReturn(statement);
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(envMapper.selectOne(any())).thenReturn(any());
        taskService.start(1);
    }

    @SneakyThrows
    @Test
    public void testStartCurrent() throws NullPointerException {
        List<String> options = new ArrayList<>();
        options.add("IS_LOCK");
        when(aspAnalysis.getOption()).thenReturn(options);
        when(lockTimeout.getOption()).thenReturn(options);
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(any())).thenReturn(diagnosisTaskDO);
        when(aspAnalysis.getDiagnosisType()).thenReturn(PointTypeConstants.CURRENT);
        when(lockTimeout.getDiagnosisType()).thenReturn(PointTypeConstants.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(dbAvgCpuItem);
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(items);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        Connection conn = mock(Connection.class);
        when(clusterManager.getConnectionByNodeId(any())).thenReturn(conn);
        Statement statement = mock(Statement.class);
        when(conn.createStatement()).thenReturn(statement);
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(envMapper.selectOne(any())).thenReturn(any());
        taskService.start(1);
    }

    @SneakyThrows
    @Test
    public void testStartAbnormal() throws NullPointerException {
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        when(taskMapper.selectById(any())).thenReturn(diagnosisTaskDO);
        when(aspAnalysis.getDiagnosisType()).thenReturn(PointTypeConstants.CURRENT);
        when(lockTimeout.getDiagnosisType()).thenReturn(PointTypeConstants.HISTORY);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(dbAvgCpuItem);
        items.add(lockTimeoutItem);
        when(aspAnalysis.getSourceDataKeys()).thenReturn(items);
        when(lockTimeout.getSourceDataKeys()).thenReturn(items);
        when(dbAvgCpuItem.queryData(diagnosisTaskDO)).thenReturn("error");
        when(lockTimeoutItem.queryData(diagnosisTaskDO)).thenReturn(new LogInfoVO());
        Connection conn = mock(Connection.class);
        when(clusterManager.getConnectionByNodeId(any())).thenReturn(conn);
        Statement statement = mock(Statement.class);
        when(conn.createStatement()).thenReturn(statement);
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(envMapper.selectOne(any())).thenReturn(any());
        taskService.start(1);
    }

    @Test
    public void testGetOption() throws NullPointerException {
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        pointServiceList.add(bioLatency);
        pointServiceList.add(bioSnoop);
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionEnum.IS_BCC));
        when(bioLatency.getOption()).thenReturn(option);
        String diagnosisType = "sql";
        List<OptionVO> optionList = taskService.getOption(diagnosisType);
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
            when(clusterManager.getConnectionByNodeId(diagnosisTaskDO.getNodeId())).thenReturn(conn);
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
            sqlTask.setConfigs((List<OptionVO>) (List<?>) sqlOption);
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
        when(osParamItem.getHttpParam()).thenReturn(AgentParamConstants.OS_PARAM);
        List<CollectionItem<?>> items = new ArrayList<>();
        items.add(osParamItem);
        when(osParam.getSourceDataKeys()).thenReturn(items);
        MultipartFile file = mock(MultipartFile.class);
        taskService.bccResult("1", "osParam", file);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1L);
        taskService.bccResult("1", "osParam", file);
    }
}

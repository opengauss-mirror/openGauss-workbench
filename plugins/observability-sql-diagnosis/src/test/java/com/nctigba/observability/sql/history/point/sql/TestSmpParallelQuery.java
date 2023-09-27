/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.TestTxtCommon;
import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.data.AgentData;
import com.nctigba.observability.sql.model.history.dto.AgentDTO;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.agent.CurrentCpuUsageItem;
import com.nctigba.observability.sql.service.history.point.sql.SmpParallelQuery;
import com.nctigba.observability.sql.util.PointUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestSmpParallelQuery
 *
 * @author luomeng
 * @since 2023/9/19
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSmpParallelQuery {
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private PointUtil util;
    @Mock
    private CurrentCpuUsageItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private SmpParallelQuery pointService;

    @Test
    public void testGetOption() {
        List<String> list = pointService.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = pointService.getSourceDataKeys();
        assertEquals(1, result.size());
        assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysis_exception() {
        try {
            DataStoreConfig config = mock(DataStoreConfig.class);
            config.setCollectionItem(item);
            config.setCount(1);
            when(dataStoreService.getData(item)).thenReturn(config);
            Connection conn = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false, true, false, true, false, true, false);
            String sql = TestTxtCommon.SQL_EXPLAIN;
            when(resultSet.getString(1)).thenReturn(sql, sql);
            HisDiagnosisTask task = new HisDiagnosisTask();
            task.setSql("SELECT l_returnflag, l_linestatus, count(*) AS count_order FROM tpch.lineitem"
                    + " GROUP BY l_returnflag, l_linestatus"
                    + " ORDER BY l_returnflag, l_linestatus");
            AnalysisDTO result = pointService.analysis(task, dataStoreService);
            assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
            assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
            assertNotNull(result.getPointData());
        } catch (SQLException | HisDiagnosisException e) {
            assertEquals("execute sql failed!", e.getMessage());
        }
    }

    @Test
    public void testAnalysis() {
        try {
            DataStoreConfig config = mock(DataStoreConfig.class);
            config.setCollectionItem(item);
            config.setCount(1);
            when(dataStoreService.getData(item)).thenReturn(config);
            AgentData agentData = new AgentData();
            agentData.setParamName("test");
            List<AgentDTO> dbValue = new ArrayList<>();
            AgentDTO agentDTO = new AgentDTO();
            agentDTO.setCpu("10");
            dbValue.add(agentDTO);
            agentData.setDbValue(dbValue);
            agentData.setSysValue(dbValue);
            when(config.getCollectionData()).thenReturn(agentData);
            Connection conn = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false, true, false, true, false, true, false);
            String sql = TestTxtCommon.SQL_EXPLAIN;
            when(resultSet.getString(1)).thenReturn(sql, sql);
            HisDiagnosisTask task = new HisDiagnosisTask();
            task.setSql("SELECT l_returnflag, l_linestatus, count(*) AS count_order FROM tpch.lineitem"
                    + " GROUP BY l_returnflag, l_linestatus"
                    + " ORDER BY l_returnflag, l_linestatus");
            AnalysisDTO result = pointService.analysis(task, dataStoreService);
            assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
            assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
            assertNotNull(result.getPointData());
        } catch (SQLException e) {
            throw new HisDiagnosisException("execute sql failed!");
        }
    }


    @Test
    public void testGetShowData() {
        Object result = pointService.getShowData(1);
        assertNull(result);
    }
}
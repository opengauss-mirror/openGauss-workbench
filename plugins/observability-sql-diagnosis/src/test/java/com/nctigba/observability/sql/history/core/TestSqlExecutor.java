/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.core;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.TaskService;
import com.nctigba.observability.sql.service.history.core.SqlExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestSqlExecutor
 *
 * @author luomeng
 * @since 2023/8/29
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSqlExecutor {
    @Mock
    private HisDiagnosisTaskMapper mapper;
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private TaskService taskService;
    @InjectMocks
    private SqlExecutor sqlExecutor;

    @Test
    public void testExecuteSql() {
        try {
            Connection conn = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any(), any())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            HisDiagnosisTask task = mock(HisDiagnosisTask.class);
            sqlExecutor.executeSql(task);
        } catch (SQLException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }

    @Test
    public void testExecuteSql_noSession() {
        try {
            Connection conn = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any(), any())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);
            HisDiagnosisTask task = mock(HisDiagnosisTask.class);
            sqlExecutor.executeSql(task);
        } catch (SQLException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }

    @Test
    public void testExecuteSql_exception() {
        try {
            Connection conn = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any(), any())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            when(statement.executeQuery(any())).thenThrow(new SQLException());
            HisDiagnosisTask task = mock(HisDiagnosisTask.class);
            sqlExecutor.executeSql(task);
        } catch (SQLException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }
}

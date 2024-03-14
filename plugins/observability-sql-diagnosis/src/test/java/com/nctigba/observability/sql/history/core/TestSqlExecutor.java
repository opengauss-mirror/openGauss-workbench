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
 *  TestSqlExecutor.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/core/TestSqlExecutor.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.core;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.service.TaskService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.core.SqlExecutor;
import com.nctigba.observability.sql.util.EbpfUtils;
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
    private DiagnosisTaskMapper mapper;
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private TaskService taskService;
    @Mock
    private EbpfUtils ebpfUtils;
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
            DiagnosisTaskDO task = mock(DiagnosisTaskDO.class);
            when(task.getSql()).thenReturn("select 1");
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
            DiagnosisTaskDO task = mock(DiagnosisTaskDO.class);
            when(task.getSql()).thenReturn("select 1");
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
            DiagnosisTaskDO task = mock(DiagnosisTaskDO.class);
            sqlExecutor.executeSql(task);
        } catch (SQLException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }
}

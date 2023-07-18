/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.handler.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.dto.session.DetailStatisticDto;
import com.nctigba.observability.instance.model.InstanceNodeInfo;

/**
 * OpenGaussSessionHandlerTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpenGaussSessionHandlerTest {
    @InjectMocks
    private OpenGaussSessionHandler sessionHandler;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSetMetaData mockResultSetMetaData;

    @BeforeEach
    public void setUp() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @Test
    public void testDetailGeneral() throws SQLException {
        default1Row3ColumnMockResult();
        when(mockResultSet.next()).thenReturn(true, false, true, false, true, false);
        JSONObject detailGeneral = sessionHandler.detailGeneral(mockConnection, "123");
        assertNotNull(detailGeneral);
        assertEquals(3, detailGeneral.size());
        verify(mockPreparedStatement, times(3)).executeQuery();
    }

    private void default1Row3ColumnMockResult() throws SQLException {
        when(mockResultSet.getMetaData()).thenReturn(mockResultSetMetaData);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(3);
        when(mockResultSetMetaData.getColumnLabel(eq(1))).thenReturn("key1");
        when(mockResultSetMetaData.getColumnLabel(eq(2))).thenReturn("key2");
        when(mockResultSetMetaData.getColumnLabel(eq(3))).thenReturn("count");
        when(mockResultSet.getString(eq(1))).thenReturn("value1");
        when(mockResultSet.getString(eq(2))).thenReturn("value2");
        when(mockResultSet.getString(eq(3))).thenReturn("3");
    }

    @Test
    public void testDetailGeneralException() throws SQLException {
        String sessionId = "123";
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query error"));
        assertThrows(Exception.class, () -> {
            sessionHandler.detailGeneral(mockConnection, sessionId);
        });
    }

    @Test
    public void testDetailStatistic() throws SQLException {
        String sessionId = "123";
        when(mockResultSet.next()).thenReturn(true, false, true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockResultSetMetaData);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(3);
        when(mockResultSetMetaData.getColumnLabel(eq(1))).thenReturn("stat_name");
        when(mockResultSetMetaData.getColumnLabel(eq(2))).thenReturn("statname");
        when(mockResultSetMetaData.getColumnLabel(eq(3))).thenReturn("value");
        when(mockResultSet.getString(eq(1))).thenReturn("stat_name_1");
        when(mockResultSet.getString(eq(2))).thenReturn("statname_2");
        when(mockResultSet.getString(eq(3))).thenReturn("value");
        List<DetailStatisticDto> statistic = sessionHandler.detailStatistic(mockConnection, sessionId);
        assertNotNull(statistic);
        assertEquals(2, statistic.size());
        verify(mockPreparedStatement, times(2)).executeQuery();
    }

    @Test
    public void testDetailWaiting() throws SQLException {
        String sessionId = "123";
        default1Row3ColumnMockResult();
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        List<JSONObject> waiting = sessionHandler.detailWaiting(mockConnection, sessionId);
        assertNotNull(waiting);
    }

    @Test
    public void testDetailBlockTree() throws SQLException {
        when(mockResultSet.getMetaData()).thenReturn(mockResultSetMetaData);
        when(mockResultSetMetaData.getColumnCount()).thenReturn(4);
        when(mockResultSetMetaData.getColumnLabel(eq(1))).thenReturn("id");
        when(mockResultSetMetaData.getColumnLabel(eq(2))).thenReturn("pathid");
        when(mockResultSetMetaData.getColumnLabel(eq(3))).thenReturn("parentid");
        when(mockResultSetMetaData.getColumnLabel(eq(4))).thenReturn("tree_id");
        when(mockResultSet.getString(eq(1))).thenReturn("1").thenReturn("2").thenReturn("3");
        when(mockResultSet.getString(eq(2))).thenReturn("/1").thenReturn("/1/2").thenReturn("/1/2/3");
        when(mockResultSet.getString(eq(3))).thenReturn("0").thenReturn("1").thenReturn("2");
        when(mockResultSet.getString(eq(4))).thenReturn("1");
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        List<JSONObject> blockTree = sessionHandler.detailBlockTree(mockConnection, "1");
        assertNotNull(blockTree);
        assertEquals(1, blockTree.size());
        assertEquals("1", blockTree.get(0).get("id"));
        verify(mockPreparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testSimpleStatistic() throws SQLException {
        default1Row3ColumnMockResult();
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        JSONObject statistic = sessionHandler.simpleStatistic(mockConnection);
        assertNotNull(statistic);
        assertEquals(3, statistic.size());
        verify(mockPreparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testLongTxc() throws SQLException {
        default1Row3ColumnMockResult();
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        List<JSONObject> longTxc = sessionHandler.longTxc(mockConnection);
        assertNotNull(longTxc);
        assertEquals(3, longTxc.get(0).size());
        verify(mockPreparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testGetConnection() {
        try (MockedStatic<DriverManager> mockedStatic = mockStatic(DriverManager.class)) {
            InstanceNodeInfo nodeInfo = new InstanceNodeInfo();
            nodeInfo.setIp("127.0.0.0");
            nodeInfo.setPort(8080);
            nodeInfo.setDbName("dbNameTest");
            nodeInfo.setDbUser("");
            nodeInfo.setDbUserPassword("");
            mockedStatic.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            sessionHandler.getConnection(nodeInfo);
        }
    }

    @Test
    public void testClose() throws SQLException {
        sessionHandler.close(mockConnection);
        sessionHandler.getDatabaseType();
        verify(mockConnection, times(1)).close();
    }
}
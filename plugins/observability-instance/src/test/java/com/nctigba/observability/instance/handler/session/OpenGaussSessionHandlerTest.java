/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.handler.session;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.InstanceException;
import com.nctigba.observability.instance.dto.session.DetailStatisticDto;
import com.nctigba.observability.instance.model.InstanceNodeInfo;
import com.nctigba.observability.instance.service.ClusterManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test
 *
 * @author liupengfei
 * @since 2023/6/30
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenGaussSessionHandlerTest {

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

    @Mock
    private ClusterManager clusterManager;

    @Before
    public void setUp() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

    }

    @Test
    public void testDetailGeneral() throws SQLException {
        String sessionId = "123";
        default1Row3ColumnMockResult();
        when(mockResultSet.next()).thenReturn(true, false, true, false, true, false);
        JSONObject detailGeneral = sessionHandler.detailGeneral(mockConnection, sessionId);
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

    @Test(expected = InstanceException.class)
    public void testDetailGeneralException() throws SQLException {
        String sessionId = "123";
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query error"));
        sessionHandler.detailGeneral(mockConnection, sessionId);
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
        assertEquals(3, waiting.size());
        verify(mockPreparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testDetailBlockTree() throws SQLException {
        String sessionId = "1";
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
        List<JSONObject> blockTree = sessionHandler.detailBlockTree(mockConnection, sessionId);
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
        InstanceNodeInfo nodeInfo = new InstanceNodeInfo();
        nodeInfo.setIp("127.0.0.0");
        nodeInfo.setPort(8080);
        nodeInfo.setDbName("dbNameTest");
        when(clusterManager.getConnectionByNodeInfo(nodeInfo)).thenReturn(mockConnection);
        Connection connection = sessionHandler.getConnection(nodeInfo);
        assertEquals(mockConnection, connection);
    }

    @Test
    public void testClose() throws SQLException {
        sessionHandler.close(mockConnection);
        verify(mockConnection, times(1)).close();
    }

}

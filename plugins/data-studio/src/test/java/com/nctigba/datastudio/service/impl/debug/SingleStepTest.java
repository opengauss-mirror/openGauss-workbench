/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * SingleStepTest
 *
 * @since 2023-07-12
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class SingleStepTest {
    private final String str = "{" + LF
            + "  \"operation\": \"singleStep\"," + LF
            + "  \"oid\":\"201839\"," + LF
            + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
            + "  \"rootWindowName\": \"postgres\"," + LF
            + "  \"oldWindowName\": \"\"," + LF
            + "  \"windowName\": \"postgres\"" + LF
            + "}";
    private final Map<String, Object> map = new HashMap<>();
    @InjectMocks
    private SingleStepImpl singleStep;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private ResultSetMetaData mockMetaData;
    @Mock
    private WebSocketServer webSocketServer;
    @Mock
    private OperateStatusDO operateStatusDO;
    @Mock
    private MessageSource messageSource;
    @Spy
    private LocaleString localeString;

    @Before
    public void setUp() {
        map.put(STATEMENT, mockStatement);
        map.put(OID, "201839");
        map.put(TYPE, "f");
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        localeString.setMessageSource(messageSource);
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        map.put(STATEMENT, null);
        singleStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");

        singleStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false).thenReturn(true, false);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201839");

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");

        singleStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false).thenReturn(true, false);
        when(mockResultSet.getInt(LINE_NO)).thenReturn(0);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201839");

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("key2");

        singleStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate5() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true, true, false);
        when(mockResultSet.getInt(LINE_NO)).thenReturn(0);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201800");

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("key2");

        singleStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate6() throws SQLException, IOException {
        map.put(TYPE, "p");
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true, true, false);
        when(mockResultSet.getInt(LINE_NO)).thenReturn(1);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201839").thenReturn("201800");

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("key2");

        singleStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate7() throws SQLException, IOException {
        map.put(TYPE, "p");
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true, true, false);
        when(mockResultSet.getInt(LINE_NO)).thenReturn(0);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201800").thenReturn("201839");

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("key2");

        singleStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperateException() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenThrow(new SQLException(""));

        try {
            singleStep.operate(webSocketServer, str);
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }
}

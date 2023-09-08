/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
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
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_MODES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_NAMES;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * InitStepTest
 *
 * @since 2023-07-11
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class InitStepTest {
    private final String str = "{\n"
            + "  \"operation\": \"initStep\",\n"
            + "  \"oid\":\"279436\",\n"
            + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\",\n"
            + "  \"rootWindowName\": \"postgres\",\n"
            + "  \"oldWindowName\": \"postgres\",\n"
            + "  \"windowName\": \"postgres1\",\n"
            + "  \"sql\": \"CREATE OR REPLACE FUNCTION zwr.test(i integer, OUT result integer)\\n "
            + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\r\\n"
            + "DECLARE\\r\\n\\r\\n\\r\\nBEGIN\\r\\n result = i + 1;\\r\\n result = result + 2;\\r\\n "
            + "result = result + 3;\\r\\n result = result + 4;\\r\\n result = result + 5;\\r\\n\\r\\n"
            + "RETURN NEXT;\\r\\nEND;$$;\\n/\"\n"
            + "}";
    @InjectMocks
    private InitStepImpl initStep;
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
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeString.setMessageSource(messageSource);
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        initStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, true, true, true, true, true, false).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(3);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("vartype");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("typname");
        when(mockMetaData.getColumnName(eq(3))).thenReturn("test");
        when(mockResultSet.getString(PRO_ARG_NAMES)).thenReturn("{a,b,c,d,e}");
        when(mockResultSet.getString(PRO_ARG_MODES)).thenReturn("{i,o,b,v,f}");
        when(mockResultSet.getString("vartype")).thenReturn("1");
        when(mockResultSet.getString("typname")).thenReturn("2");
        when(mockResultSet.getString("test")).thenReturn("3");

        initStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(3);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("vartype");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("typname");
        when(mockMetaData.getColumnName(eq(3))).thenReturn("test");
        when(mockResultSet.getString(PRO_ARG_NAMES)).thenReturn("{a}");
        when(mockResultSet.getString(PRO_ARG_MODES)).thenReturn("");
        when(mockResultSet.getString("vartype")).thenReturn("1");
        when(mockResultSet.getString("typname")).thenReturn("2");
        when(mockResultSet.getString("test")).thenReturn("3");

        initStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(3);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("vartype");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("typname");
        when(mockMetaData.getColumnName(eq(3))).thenReturn("test");
        when(mockResultSet.getString(PRO_ARG_NAMES)).thenReturn("");
        when(mockResultSet.getString("vartype")).thenReturn("1");
        when(mockResultSet.getString("typname")).thenReturn("2");
        when(mockResultSet.getString("test")).thenReturn("3");

        initStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperateException() throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException(""));

        try {
            initStep.operate(webSocketServer, str);
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }
}

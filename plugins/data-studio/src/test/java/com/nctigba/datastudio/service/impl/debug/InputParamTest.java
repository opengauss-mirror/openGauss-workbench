/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.utils.LocaleStringUtils;
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

import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * InputParamTest
 *
 * @since 2023-07-04
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class InputParamTest {
    private final String str = "{" + LF
            + "  \"operation\": \"inputParam\"," + LF
            + "  \"oid\":\"201839\"," + LF
            + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
            + "  \"rootWindowName\": \"postgres\"," + LF
            + "  \"oldWindowName\": \"\"," + LF
            + "  \"windowName\": \"postgres\"," + LF
            + "  \"sql\": \"CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)\\n "
            + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\nDECLARE\\n\\n\\n"
            + "BEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  if result < 10\\n  then\\n    "
            + "result = test(result);\\n  else\\n    result = result + 3;\\n  end if;\\n  "
            + "result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;\\n/\"," + LF
            + "  \"inputParams\": [" + LF
            + "    {" + LF
            + "      \"integer\": \"1\"" + LF
            + "    }," + LF
            + "    {" + LF
            + "      \"number\": \"2\"" + LF
            + "    }," + LF
            + "    {" + LF
            + "      \"float\": \"3\"" + LF
            + "    }," + LF
            + "    {" + LF
            + "      \"double\": \"4\"" + LF
            + "    }," + LF
            + "    {" + LF
            + "      \"real\": \"5\"" + LF
            + "    }," + LF
            + "    {" + LF
            + "      \"serial\": \"6\"" + LF
            + "    }," + LF
            + "    {" + LF
            + "      \"string\": \"a\"" + LF
            + "    }" + LF
            + "  ]," + LF
            + "  \"breakPoints\": []" + LF
            + "}";
    private final String str2 = "{" + LF
            + "  \"operation\": \"inputParam\"," + LF
            + "  \"oid\":\"201839\"," + LF
            + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
            + "  \"rootWindowName\": \"postgres\"," + LF
            + "  \"oldWindowName\": \"\"," + LF
            + "  \"windowName\": \"postgres\"," + LF
            + "  \"sql\": \"CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)\\n "
            + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\nDECLARE\\n\\n\\n"
            + "BEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  if result < 10\\n  then\\n    "
            + "result = test(result);\\n  else\\n    result = result + 3;\\n  end if;\\n  "
            + "result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;\\n/\"," + LF
            + "  \"inputParams\": [" + LF
            + "    {" + LF
            + "      \"integer\": \"2\"," + LF
            + "      \"string\": \"a\"" + LF
            + "    }" + LF
            + "  ]," + LF
            + "  \"breakPoints\": [10]" + LF
            + "}";
    @InjectMocks
    private InputParamImpl inputParam;
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
    private AsyncHelper asyncHelper;
    @Mock
    private AddBreakPointImpl addBreakPoint;
    @Mock
    private MessageSource messageSource;
    @Spy
    private LocaleStringUtils localeStringUtils;

    @Before
    public void setUp() {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeStringUtils.setMessageSource(messageSource);
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockResultSet.getObject("key1")).thenReturn("111");

        inputParam.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("key2");
        when(mockResultSet.getObject("key1")).thenReturn("111");
        when(mockResultSet.getObject("key2")).thenReturn("222");

        inputParam.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockResultSet.getObject("key1")).thenReturn("111");

        inputParam.operate(webSocketServer, str2);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        when(operateStatusDO.isDebug()).thenReturn(true);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false);

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");

        inputParam.operate(webSocketServer, str);
    }

    @Test
    public void testOperate5() throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        when(operateStatusDO.isDebug()).thenReturn(true);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false);

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");

        inputParam.operate(webSocketServer, str2);
    }

    @Test
    public void testOperate6() throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        when(operateStatusDO.isDebug()).thenReturn(true);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false);

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockResultSet.getString(LINE_NO)).thenReturn("6");
        when(mockResultSet.getString(CAN_BREAK)).thenReturn("t");

        inputParam.operate(webSocketServer, str2);
    }

    @Test
    public void testOperateException() throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        when(operateStatusDO.isDebug()).thenReturn(true);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenThrow(new SQLException(""));

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");

        try {
            inputParam.operate(webSocketServer, str2);
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }
}

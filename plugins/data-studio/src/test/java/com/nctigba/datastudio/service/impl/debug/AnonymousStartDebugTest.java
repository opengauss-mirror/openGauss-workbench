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

import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.NODE_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PORT;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AnonymousStartDebugTest
 *
 * @since 2023-08-29
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AnonymousStartDebugTest {
    private static final String STR = "{" + LF
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

    private static final String STR_2 = "{" + LF
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
            + "  ]" + LF
            + "}";

    @InjectMocks
    private AnonymousStartDebugImpl anonymousStartDebug;
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
    private LocaleString localeString;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());
        localeString.setMessageSource(messageSource);

        Map<String, Object> map = new HashMap<>();
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, true, true, false).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201800");
        when(mockResultSet.getString(NODE_NAME)).thenReturn("single");
        when(mockResultSet.getString(PORT)).thenReturn("1").thenReturn("2");
        when(mockResultSet.getString(LINE_NO)).thenReturn("").thenReturn("6");
        when(mockResultSet.getString(CAN_BREAK)).thenReturn("f").thenReturn("t");

        anonymousStartDebug.operate(webSocketServer, STR);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, true, true, false).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201800");
        when(mockResultSet.getString(NODE_NAME)).thenReturn("single");
        when(mockResultSet.getString(PORT)).thenReturn("1").thenReturn("2");
        when(mockResultSet.getString(LINE_NO)).thenReturn("").thenReturn("7");
        when(mockResultSet.getString(CAN_BREAK)).thenReturn("f").thenReturn("t");

        anonymousStartDebug.operate(webSocketServer, STR);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, true, false).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(1)).thenReturn(CAN_BREAK);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201839");
        when(mockResultSet.getString(NODE_NAME)).thenReturn("single");
        when(mockResultSet.getString(PORT)).thenReturn("1").thenReturn("2");
        when(mockResultSet.getString(LINE_NO)).thenReturn("10").thenReturn("11");
        when(mockResultSet.getString(CAN_BREAK)).thenReturn("t").thenReturn("f");

        anonymousStartDebug.operate(webSocketServer, STR_2);
    }
}
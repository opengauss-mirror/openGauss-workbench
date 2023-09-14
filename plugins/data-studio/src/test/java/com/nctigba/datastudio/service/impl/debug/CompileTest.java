/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static com.nctigba.datastudio.constants.CommonConstants.NSP_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_TYPES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * CompileTest
 *
 * @since 2023-07-04
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class CompileTest {
    private final String str = "{" + LF
            + "  \"operation\": \"compile\"," + LF
            + "  \"schema\": \"zwr\"," + LF
            + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
            + "  \"rootWindowName\": \"postgres\"," + LF
            + "  \"oldWindowName\": \"\"," + LF
            + "  \"windowName\": \"postgres\"," + LF
            + "  \"sql\": \"CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)\\n "
            + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\nDECLARE\\n\\n\\n"
            + "BEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  if result < 10\\n  then\\n    "
            + "result = test(result);\\n  else\\n    result = result + 3;\\n  end if;\\n  "
            + "result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;\\n/\"" + LF
            + "}";
    @InjectMocks
    private CompileImpl compile;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private WebSocketServer webSocketServer;
    @Mock
    private MessageSource messageSource;
    @Spy
    private LocaleString localeString;

    @Before
    public void setUp() {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeString.setMessageSource(messageSource);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn("111").thenReturn("111");

        String str1 = "{" + LF
                + "  \"operation\": \"compile\"," + LF
                + "  \"schema\": \"zwr\"," + LF
                + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"," + LF
                + "  \"sql\": \"CREATE OR REPLACE FUNCTION step_in(i integer, OUT result integer)\\n "
                + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\nDECLARE\\n\\n\\n"
                + "BEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  if result < 10\\n  then\\n    "
                + "result = test(result);\\n  else\\n    result = result + 3;\\n  end if;\\n  "
                + "result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;\\n/\"" + LF
                + "}";
        compile.operate(webSocketServer, str1);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(OID)).thenReturn("111").thenReturn("222");
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.getString(NSP_NAME)).thenReturn("scott");
        when(mockResultSet.getString(PRO_NAME)).thenReturn("test");
        when(mockResultSet.getString(PRO_ARG_TYPES)).thenReturn("10 20");

        compile.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(OID)).thenReturn("111").thenReturn("222");
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.getString(NSP_NAME)).thenReturn("scott");
        when(mockResultSet.getString(PRO_NAME)).thenReturn("test");
        when(mockResultSet.getString(PRO_ARG_TYPES)).thenReturn("");
        when((webSocketServer.getLanguage())).thenReturn("zh-CN");

        compile.operate(webSocketServer, str);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);

        String str1 = "{" + LF
                + "  \"operation\": \"compile\"," + LF
                + "  \"schema\": \"zwr\"," + LF
                + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"," + LF
                + "  \"isPackage\": \"true\"," + LF
                + "  \"sql\": \"CREATE OR REPLACE FUNCTION step_in(i integer, OUT result integer)\\n "
                + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\nDECLARE\\n\\n\\n"
                + "BEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  if result < 10\\n  then\\n    "
                + "result = test(result);\\n  else\\n    result = result + 3;\\n  end if;\\n  "
                + "result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;\\n/\"" + LF
                + "}";
        compile.operate(webSocketServer, str1);
    }

    @Test
    public void testOperateException() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet).thenReturn(mockResultSet)
                .thenThrow(new SQLException(""));
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(OID)).thenReturn("111").thenReturn("222");
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        compile.operate(webSocketServer, str);
    }
}

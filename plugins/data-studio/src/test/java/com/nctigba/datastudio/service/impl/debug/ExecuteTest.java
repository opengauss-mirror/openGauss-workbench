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
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_NAMES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_TYPES;
import static com.nctigba.datastudio.constants.CommonConstants.TYP_NAME;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ExecuteTest
 *
 * @since 2023-07-04
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ExecuteTest {
    private static final String SQL = "CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)" + LF
            + " RETURNS SETOF integer" + LF
            + " LANGUAGE plpgsql" + LF
            + " NOT FENCED NOT SHIPPABLE" + LF
            + "AS $$" + LF
            + "DECLARE" + LF
            + "" + LF
            + "" + LF
            + "BEGIN" + LF
            + "  result = i + 1;" + LF
            + "  result = result + 2;" + LF
            + "  if result < 10" + LF
            + "  then" + LF
            + "    result = test(result);" + LF
            + "  else" + LF
            + "    result = result + 3;" + LF
            + "  end if;" + LF
            + "  result = result + 4;" + LF
            + "" + LF
            + "RETURN NEXT;" + LF
            + "END;$$;";

    @InjectMocks
    private ExecuteImpl execute;
    @Mock
    private InputParamImpl inputParam;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private WebSocketServer webSocketServer;
    @Mock
    private OperateStatusDO operateStatusDO;
    @Mock
    private MessageSource messageSource;
    @Spy
    private LocaleString localeString;
    private String str = "{" + LF
            + "  \"operation\": \"execute\"," + LF
            + "  \"isContinue\": false," + LF
            + "  \"oid\":\"201839\"," + LF
            + "  \"rootWindowName\": \"postgres\"," + LF
            + "  \"oldWindowName\": \"\"," + LF
            + "  \"windowName\": \"postgres\"," + LF
            + "  \"sql\": \"CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)\\n "
            + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\nDECLARE\\n\\n\\n"
            + "BEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  if result < 10\\n  then\\n    "
            + "result = test(result);\\n  else\\n    result = result + 3;\\n  end if;\\n  "
            + "result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;/\"" + LF
            + "}";

    @Before
    public void setUp() {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeString.setMessageSource(messageSource);
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn("");

        try {
            execute.operate(webSocketServer, str);
        } catch (CustomException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false)
                .thenReturn(true, false).thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn(SQL);
        when(mockResultSet.getInt(anyString())).thenReturn(1);
        when(mockResultSet.getString(PRO_ARG_TYPES)).thenReturn("{23,20}");
        when(mockResultSet.getString(PRO_ARG_NAMES)).thenReturn("{i,result}");

        execute.operate(webSocketServer, str);
        str = "{" + LF
                + "  \"operation\": \"execute\"," + LF
                + "  \"isContinue\": false," + LF
                + "  \"oid\":\"201839\"," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"," + LF
                + "  \"sql\": \"CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)\\n "
                + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\nDECLARE\\n\\n\\n"
                + "BEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  if result < 100\\n  then\\n    "
                + "result = test(result);\\n  else\\n    result = result + 3;\\n  end if;\\n  "
                + "result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;/\"" + LF
                + "}";
        execute.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false)
                .thenReturn(true, false).thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn(SQL);
        when(mockResultSet.getInt(anyString())).thenReturn(0);

        str = "{" + LF
                + "  \"operation\": \"execute\"," + LF
                + "  \"isContinue\": true," + LF
                + "  \"oid\":\"201839\"," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"," + LF
                + "  \"sql\": \"CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)\\n "
                + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\nDECLARE\\n\\n\\n"
                + "BEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  if result < 10\\n  then\\n    "
                + "result = test(result);\\n  else\\n    result = result + 3;\\n  end if;\\n  "
                + "result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;/\"" + LF
                + "}";
        execute.operate(webSocketServer, str);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false)
                .thenReturn(true, false).thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn(SQL);
        when(mockResultSet.getInt(anyString())).thenReturn(1);
        when(mockResultSet.getString(PRO_ARG_TYPES)).thenReturn("");
        when(mockResultSet.getString(PRO_ARG_NAMES)).thenReturn("");
        when(mockResultSet.getString(TYP_NAME)).thenReturn("int4");

        str = "{" + LF
                + "  \"operation\": \"execute\"," + LF
                + "  \"isContinue\": true," + LF
                + "  \"oid\":\"201839\"," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"," + LF
                + "  \"sql\": \"CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)\\n "
                + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\nDECLARE\\n\\n\\n"
                + "BEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  if result < 100\\n  then\\n    "
                + "result = test(result);\\n  else\\n    result = result + 3;\\n  end if;\\n  "
                + "result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;/\"" + LF
                + "}";
        execute.operate(webSocketServer, str);
    }
}

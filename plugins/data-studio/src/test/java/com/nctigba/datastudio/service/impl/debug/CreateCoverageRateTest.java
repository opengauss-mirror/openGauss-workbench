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
 *  CreateCoverageRateTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/service/impl/debug/CreateCoverageRateTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * CreateCoverageRateTest
 *
 * @since 2023-07-12
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class CreateCoverageRateTest {
    private final String strFalse = "{" + LF
            + "  \"operation\": \"inputParam\"," + LF
            + "  \"oid\":\"201839\"," + LF
            + "  \"isCoverage\":\"false\"," + LF
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
    private final String strTrue = "{" + LF
            + "  \"operation\": \"inputParam\"," + LF
            + "  \"oid\":\"201839\"," + LF
            + "  \"isCoverage\":\"true\"," + LF
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
    private CreateCoverageRateImpl createCoverageRate;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private WebSocketServer webSocketServer;
    @Mock
    private AsyncHelper asyncHelper;
    @Mock
    private MessageSource messageSource;
    @Spy
    private LocaleStringUtils localeStringUtils;

    @Before
    public void setUp() throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        localeStringUtils.setMessageSource(messageSource);

        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        when(mockResultSet.next()).thenReturn(true, false);

        createCoverageRate.operate(webSocketServer, strFalse);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, true, false).thenReturn(true, true, false)
                .thenReturn(true, true, false);
        when(mockResultSet.getString(CAN_BREAK)).thenReturn("t").thenReturn("f");
        when(mockResultSet.getInt(LINE_NO)).thenReturn(1).thenReturn(2)
                .thenReturn(3).thenReturn(4).thenReturn(0);

        createCoverageRate.operate(webSocketServer, strTrue);
    }

    @Test
    public void testOperate5() throws SQLException, IOException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, true, false).thenReturn(false);
        when(mockResultSet.getString(CAN_BREAK)).thenReturn("t").thenReturn("f");
        when(mockResultSet.getInt(LINE_NO)).thenReturn(1);

        createCoverageRate.operate(webSocketServer, strTrue);
    }

    @Test
    public void testOperateException() throws SQLException, IOException {
        when(mockResultSet.next()).thenThrow(new SQLException(""));

        try {
            createCoverageRate.operate(webSocketServer, strFalse);
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }
}

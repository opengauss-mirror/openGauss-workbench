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
 *  StepOutTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/service/impl/debug/StepOutTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * StepOutTest
 *
 * @since 2023-07-12
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StepOutTest {
    private final String str = "{" + LF
            + "  \"operation\": \"stepOut\"," + LF
            + "  \"oid\":\"201839\"," + LF
            + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
            + "  \"rootWindowName\": \"postgres\"," + LF
            + "  \"oldWindowName\": \"\"," + LF
            + "  \"windowName\": \"postgres\"" + LF
            + "}";
    private final Map<String, Object> map = new HashMap<>();
    @InjectMocks
    private StepOutImpl stepOut;
    @Mock
    private SingleStepImpl singleStep;
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
    private LocaleStringUtils localeStringUtils;

    @Before
    public void setUp() {
        map.put(STATEMENT, mockStatement);
        map.put(OID, "201839");
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        localeStringUtils.setMessageSource(messageSource);
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        map.put(STATEMENT, null);
        stepOut.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false).thenReturn(true, false);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201839");

        stepOut.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true, false);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201839").thenReturn("201800");

        stepOut.operate(webSocketServer, str);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        map.put(String.valueOf(201800), operateStatusDO);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true, false);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201800").thenReturn("201800");

        stepOut.operate(webSocketServer, str);
    }

    @Test
    public void testOperate5() throws SQLException, IOException {
        map.put(String.valueOf(201800), "postgres");
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true, false);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201800").thenReturn("201800");

        stepOut.operate(webSocketServer, str);
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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

import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ContinueStepTest
 *
 * @since 2023-07-12
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ContinueStepTest {
    private final String str = "{" + LF
            + "  \"operation\": \"continueStep\"," + LF
            + "  \"oid\":\"201839\"," + LF
            + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
            + "  \"rootWindowName\": \"postgres\"," + LF
            + "  \"oldWindowName\": \"\"," + LF
            + "  \"windowName\": \"postgres\"" + LF
            + "}";
    private final Map<String, Object> map = new HashMap<>();
    @InjectMocks
    private ContinueStepImpl continueStep;
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
        map.put(TYPE, "f");
        map.put(DIFFER, 4);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        localeStringUtils.setMessageSource(messageSource);
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        map.put(STATEMENT, null);
        continueStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        continueStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true, true, false);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201839").thenReturn("201800")
                .thenReturn("201839");

        continueStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true, false);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201839").thenReturn("201800");

        continueStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperate5() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true, false);
        when(mockResultSet.getString(FUNC_OID)).thenReturn("201800").thenReturn("201839");

        continueStep.operate(webSocketServer, str);
    }

    @Test
    public void testOperateException() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException(""));
        try {
            continueStep.operate(webSocketServer, str);
        } catch (SQLException | IOException e) {
            log.info(e.getMessage());
        }
    }
}

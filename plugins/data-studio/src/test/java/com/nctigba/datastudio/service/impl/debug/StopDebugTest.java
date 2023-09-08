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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * StopDebugTest
 *
 * @since 2023-07-04
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StopDebugTest {
    @InjectMocks
    private StopDebugImpl stopDebug;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private OperateStatusDO operateStatusDO;
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
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        String str = "{\n"
                + "  \"operation\": \"stopDebug\",\n"
                + "  \"oid\": \"0\",\n"
                + "  \"isInPackage\": \"false\",\n"
                + "  \"rootWindowName\": \"postgres\",\n"
                + "  \"oldWindowName\": \"\",\n"
                + "  \"windowName\": \"postgres\"\n"
                + "}";
        stopDebug.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        String str = "{\n"
                + "  \"operation\": \"stopDebug\",\n"
                + "  \"oid\": \"201839\",\n"
                + "  \"isInPackage\": \"false\",\n"
                + "  \"rootWindowName\": \"postgres\",\n"
                + "  \"oldWindowName\": \"\",\n"
                + "  \"windowName\": \"postgres\"\n"
                + "}";
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(CONNECTION, mockConnection);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);

        stopDebug.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        String str = "{\n"
                + "  \"operation\": \"stopDebug\",\n"
                + "  \"oid\": \"0\",\n"
                + "  \"isInPackage\": \"true\",\n"
                + "  \"rootWindowName\": \"postgres\",\n"
                + "  \"oldWindowName\": \"\",\n"
                + "  \"windowName\": \"postgres\"\n"
                + "}";
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(CONNECTION, mockConnection);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);

        stopDebug.operate(webSocketServer, str);
    }

    @Test
    public void testOperate34() throws SQLException, IOException {
        String str = "{\n"
                + "  \"operation\": \"stopDebug\",\n"
                + "  \"oid\": \"201839\",\n"
                + "  \"isInPackage\": \"true\",\n"
                + "  \"rootWindowName\": \"postgres\",\n"
                + "  \"oldWindowName\": \"\",\n"
                + "  \"windowName\": \"postgres\"\n"
                + "}";
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(CONNECTION, mockConnection);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);

        stopDebug.operate(webSocketServer, str);
    }
}

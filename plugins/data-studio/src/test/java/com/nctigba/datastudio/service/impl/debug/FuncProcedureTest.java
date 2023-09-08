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

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * FuncProcedureTest
 *
 * @since 2023-07-04
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class FuncProcedureTest {
    @InjectMocks
    private FuncProcedureImpl funcProcedure;

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

    private String str = "{\n"
            + "  \"operation\": \"funcProcedure\",\n"
            + "  \"oid\":\"201839\",\n"
            + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\",\n"
            + "  \"isInPackage\": \"false\",\n"
            + "  \"rootWindowName\": \"postgres\",\n"
            + "  \"oldWindowName\": \"\",\n"
            + "  \"windowName\": \"postgres\"\n"
            + "}";

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeString.setMessageSource(messageSource);
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        str = "{\n"
                + "  \"operation\": \"funcProcedure\",\n"
                + "  \"oid\":\"279436\",\n"
                + "  \"isInPackage\": \"true\",\n"
                + "  \"rootWindowName\": \"postgres\",\n"
                + "  \"oldWindowName\": \"postgres\",\n"
                + "  \"windowName\": \"postgres\"\n"
                + "}";
        funcProcedure.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn("");

        try {
            funcProcedure.operate(webSocketServer, str);
        } catch (CustomException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.isClosed()).thenReturn(true);

        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn("111");

        mockConnection.close();
        funcProcedure.operate(webSocketServer, str);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(mockConnection.isClosed()).thenReturn(true);

        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn("111");

        mockConnection.close();
        funcProcedure.operate(webSocketServer, str);
    }

    @Test
    public void testOperate5() throws SQLException, IOException {
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn("111");

        mockConnection.close();
        funcProcedure.operate(webSocketServer, str);
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.utils.LocaleStringUtils;
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

import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ShowPackageTest
 *
 * @since 2023-09-08
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ShowPackageTest {
    @InjectMocks
    private ShowPackageImpl showPackage;

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
    private LocaleStringUtils localeStringUtils;

    private String str = "{" + LF
            + "  \"operation\": \"showPackage\"," + LF
            + "  \"schema\":\"wyl\"," + LF
            + "  \"oid\":\"1301349\"," + LF
            + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
            + "  \"windowName\": \"postgres\"" + LF
            + "}";

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeStringUtils.setMessageSource(messageSource);
    }

    @Test
    public void testOperateException() throws SQLException, IOException {
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("pkgname")).thenReturn("");
        when(mockResultSet.getString("pkgspecsrc")).thenReturn("test");
        when(mockResultSet.getString("pkgbodydeclsrc")).thenReturn("test");

        try {
            showPackage.operate(webSocketServer, str);
        } catch (CustomException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(null);

        showPackage.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.isClosed()).thenReturn(true);
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("pkgname")).thenReturn("test");
        when(mockResultSet.getString("pkgspecsrc")).thenReturn("test");
        when(mockResultSet.getString("pkgbodydeclsrc")).thenReturn("test");

        showPackage.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.isClosed()).thenReturn(false);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("pkgname")).thenReturn("test");
        when(mockResultSet.getString("pkgspecsrc")).thenReturn("test");
        when(mockResultSet.getString("pkgbodydeclsrc")).thenReturn("test");

        showPackage.operate(webSocketServer, str);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(mockConnection.isClosed()).thenReturn(true);
        when(webSocketServer.createConnection(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("pkgname")).thenReturn("test");
        when(mockResultSet.getString("pkgspecsrc")).thenReturn("test");
        when(mockResultSet.getString("pkgbodydeclsrc")).thenReturn("test");

        showPackage.operate(webSocketServer, str);
    }

    @Test
    public void testOperate5() throws SQLException, IOException {
        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(mockConnection.isClosed()).thenReturn(false);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("pkgname")).thenReturn("test");
        when(mockResultSet.getString("pkgspecsrc")).thenReturn("test");
        when(mockResultSet.getString("pkgbodydeclsrc")).thenReturn("test");

        showPackage.operate(webSocketServer, str);
    }
}


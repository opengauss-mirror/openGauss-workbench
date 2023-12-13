/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.utils.ConnectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ExportServiceTest
 *
 * @since 2023-07-04
 */
@RunWith(MockitoJUnitRunner.class)
public class MetaDataByJdbcServiceTest {
    @InjectMocks
    private MetaDataByJdbcServiceImpl metaDataByJdbcService;

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSetMetaData mockMetaData;

    @Mock
    private ConnectionConfig connectionConfig;

    @Before
    public void setUp() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
    }

    @Test
    public void testTestQuerySQL() {
        MockedStatic<ConnectionUtils> staticUtilsMockedStatic = Mockito.mockStatic(ConnectionUtils.class);
        staticUtilsMockedStatic.when(() -> ConnectionUtils.connectGet(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
        metaDataByJdbcService.testQuerySQL("1", "3", "3", "4");
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testVersionSQLParenthesis() throws SQLException {
        when(mockResultSet.getNString(1)).thenReturn("(value1 3.0.0)");
        MockedStatic<ConnectionUtils> staticUtilsMockedStatic = Mockito.mockStatic(ConnectionUtils.class);
        staticUtilsMockedStatic.when(() -> ConnectionUtils.connectGet(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
        metaDataByJdbcService.versionSQL("1", "3", "3", "4");
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testVersionSQL() throws SQLException {
        when(mockResultSet.getNString(1)).thenReturn("value1");
        MockedStatic<ConnectionUtils> staticUtilsMockedStatic = Mockito.mockStatic(ConnectionUtils.class);
        staticUtilsMockedStatic.when(() -> ConnectionUtils.connectGet(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
        metaDataByJdbcService.versionSQL("1", "3", "3", "4");
        staticUtilsMockedStatic.close();
    }
}

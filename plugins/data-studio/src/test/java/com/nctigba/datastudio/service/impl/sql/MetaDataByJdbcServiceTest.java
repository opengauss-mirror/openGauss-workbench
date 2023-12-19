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
 *  MetaDataByJdbcServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/service/impl/sql/MetaDataByJdbcServiceTest.java
 *
 *  -------------------------------------------------------------------------
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

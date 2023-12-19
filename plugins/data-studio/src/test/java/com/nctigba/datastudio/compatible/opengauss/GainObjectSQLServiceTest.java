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
 *  GainObjectSQLServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/compatible/opengauss/GainObjectSQLServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArraySchemaQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * GainObjectSQLServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class GainObjectSQLServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private GainObjectSQLServiceImpl gainObjectSQLService;
    @Mock
    private Connection mockConnection;
    @Mock
    private ConnectionConfig connectionConfig;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setType("openGauss");
        conMap.put(UUID, connectionDTO);
        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString(anyString())).thenReturn("1");
    }


    @Test
    public void testSchemaList() throws SQLException {
        DatabaseMetaArraySchemaQuery databaseMetaarraySchemaQuery = new DatabaseMetaArraySchemaQuery();
        databaseMetaarraySchemaQuery.setUuid(UUID);
        databaseMetaarraySchemaQuery.setConnectionName("a");
        databaseMetaarraySchemaQuery.setWebUser("s");
        gainObjectSQLService.schemaList(databaseMetaarraySchemaQuery);
    }


    @Test
    public void testObjectListAll() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false)
                .thenReturn(true, false).thenReturn(true, false);
        DatabaseMetaArrayQuery databaseMetaarrayQuery = new DatabaseMetaArrayQuery();
        databaseMetaarrayQuery.setUuid(UUID);
        databaseMetaarrayQuery.setConnectionName("a");
        databaseMetaarrayQuery.setWebUser("s");
        databaseMetaarrayQuery.setObjectType("ALL");
        databaseMetaarrayQuery.setSchema("s");
        gainObjectSQLService.objectList(databaseMetaarrayQuery);
    }

    @Test
    public void testObjectListFunPro() throws SQLException {
        DatabaseMetaArrayQuery databaseMetaarrayQuery = new DatabaseMetaArrayQuery();
        databaseMetaarrayQuery.setUuid(UUID);
        databaseMetaarrayQuery.setConnectionName("a");
        databaseMetaarrayQuery.setWebUser("s");
        databaseMetaarrayQuery.setObjectType("FUN_PRO");
        databaseMetaarrayQuery.setSchema("s");
        gainObjectSQLService.objectList(databaseMetaarrayQuery);
    }

    @Test
    public void testObjectListView() throws SQLException {
        DatabaseMetaArrayQuery databaseMetaarrayQuery = new DatabaseMetaArrayQuery();
        databaseMetaarrayQuery.setUuid(UUID);
        databaseMetaarrayQuery.setConnectionName("a");
        databaseMetaarrayQuery.setWebUser("s");
        databaseMetaarrayQuery.setObjectType("VIEW");
        databaseMetaarrayQuery.setSchema("s");
        gainObjectSQLService.objectList(databaseMetaarrayQuery);
    }

    @Test
    public void testObjectListOther() throws SQLException {
        DatabaseMetaArrayQuery databaseMetaarrayQuery = new DatabaseMetaArrayQuery();
        databaseMetaarrayQuery.setUuid(UUID);
        databaseMetaarrayQuery.setConnectionName("a");
        databaseMetaarrayQuery.setWebUser("s");
        databaseMetaarrayQuery.setObjectType("other");
        databaseMetaarrayQuery.setSchema("s");
        gainObjectSQLService.objectList(databaseMetaarrayQuery);
    }

    @Test
    public void testTableColumnList() throws SQLException {
        DatabaseMetaArrayColumnQuery databaseMetaarrayColumnQuery = new DatabaseMetaArrayColumnQuery();
        databaseMetaarrayColumnQuery.setUuid(UUID);
        databaseMetaarrayColumnQuery.setConnectionName("a");
        databaseMetaarrayColumnQuery.setWebUser("s");
        databaseMetaarrayColumnQuery.setObjectName("other");
        databaseMetaarrayColumnQuery.setSchema("s");
        gainObjectSQLService.tableColumnList(databaseMetaarrayColumnQuery);
    }
}

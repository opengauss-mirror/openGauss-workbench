/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.GainObjectSQLService;
import com.nctigba.datastudio.compatible.opengauss.GainObjectSQLServiceImpl;
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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ExportServiceTest
 *
 * @since 2023-07-04
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryMetaArrayServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private QueryMetaArrayServiceImpl queryMetaArrayService;
    @Mock
    private Map<String, GainObjectSQLService> gainObjectSQLServiceMap;
    @Mock
    private Connection mockConnection;
    @Mock
    private ConnectionConfig connectionConfig;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private ResultSetMetaData mockMetaData;
    @Mock
    private GainObjectSQLServiceImpl gainObjectSQLService;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setType("openGauss");
        conMap.put(UUID, connectionDTO);
        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
    }

    @Test
    public void testSchemaList() throws SQLException {
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        when(gainObjectSQLService.type()).thenReturn("openGauss");
        serviceArrayList.add(gainObjectSQLService);
        queryMetaArrayService.setGainObjectSQLService(serviceArrayList);
        DatabaseMetaArraySchemaQuery schemaQuery = new DatabaseMetaArraySchemaQuery();
        schemaQuery.setUuid(UUID);
        schemaQuery.setWebUser("u1");
        schemaQuery.setConnectionName("rrr");
        queryMetaArrayService.schemaList(schemaQuery);
    }

    @Test
    public void testDatabaseList() {
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new GainObjectSQLServiceImpl());
        queryMetaArrayService.setGainObjectSQLService(serviceArrayList);
        queryMetaArrayService.databaseList(UUID);
    }

    @Test
    public void testObjectList() throws SQLException {
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        when(gainObjectSQLService.type()).thenReturn("openGauss");
        serviceArrayList.add(gainObjectSQLService);
        queryMetaArrayService.setGainObjectSQLService(serviceArrayList);
        DatabaseMetaArrayQuery metaarrayQuery = new DatabaseMetaArrayQuery();
        metaarrayQuery.setUuid(UUID);
        metaarrayQuery.setWebUser("u1");
        metaarrayQuery.setConnectionName("rrr");
        metaarrayQuery.setObjectType("");
        metaarrayQuery.setSchema("s1");
        queryMetaArrayService.objectList(metaarrayQuery);
    }

    @Test
    public void testTableColumnList() throws SQLException {
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        when(gainObjectSQLService.type()).thenReturn("openGauss");
        serviceArrayList.add(gainObjectSQLService);
        queryMetaArrayService.setGainObjectSQLService(serviceArrayList);
        DatabaseMetaArrayColumnQuery metaarrayColumnQuery = new DatabaseMetaArrayColumnQuery();
        metaarrayColumnQuery.setUuid(UUID);
        metaarrayColumnQuery.setWebUser("u1");
        metaarrayColumnQuery.setConnectionName("rrr");
        metaarrayColumnQuery.setObjectName("w");
        metaarrayColumnQuery.setSchema("s1");
        queryMetaArrayService.tableColumnList(metaarrayColumnQuery);
    }

    @Test
    public void testBaseTypeList() throws SQLException {
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new GainObjectSQLServiceImpl());
        queryMetaArrayService.setGainObjectSQLService(serviceArrayList);
        queryMetaArrayService.baseTypeList(UUID);
    }

    @Test
    public void testTablespaceList() throws SQLException {
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new GainObjectSQLServiceImpl());
        queryMetaArrayService.setGainObjectSQLService(serviceArrayList);
        queryMetaArrayService.tablespaceList(UUID);
    }
}

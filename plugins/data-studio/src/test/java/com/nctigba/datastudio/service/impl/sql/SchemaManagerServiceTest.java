/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.SchemaObjectSQLService;
import com.nctigba.datastudio.compatible.opengauss.SchemaObjectSQLServiceImpl;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.query.SchemaManagerQuery;
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

import static com.nctigba.datastudio.constants.CommonConstants.NSP_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.ROL_NAME;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ExportServiceTest
 *
 * @since 2023-07-04
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaManagerServiceTest {
    @InjectMocks
    private SchemaManagerServiceImpl schemaManagerService;

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

    @Mock
    private SchemaObjectSQLService schemaObjectSQLService;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setType("openGauss");
        conMap.put("111", connectionDTO);

        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString(NSP_NAME)).thenReturn("value1");
        when(mockResultSet.getString(ROL_NAME)).thenReturn("value2");
    }

    @Test
    public void testQueryAllUsers() throws SQLException {
        List<SchemaObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SchemaObjectSQLServiceImpl());
        schemaManagerService.setSchemaObjectSQLService(serviceArrayList);

        SchemaManagerQuery request = new SchemaManagerQuery();
        request.setUuid("111");
        schemaManagerService.queryAllUsers(request);
    }

    @Test
    public void testQuerySchema() throws SQLException {
        List<SchemaObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SchemaObjectSQLServiceImpl());
        schemaManagerService.setSchemaObjectSQLService(serviceArrayList);

        SchemaManagerQuery request = new SchemaManagerQuery();
        request.setUuid("111");
        request.setOid("123");
        schemaManagerService.querySchema(request);
    }

    @Test
    public void testCreateSchema() throws SQLException {
        List<SchemaObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SchemaObjectSQLServiceImpl());
        schemaManagerService.setSchemaObjectSQLService(serviceArrayList);

        SchemaManagerQuery request = new SchemaManagerQuery();
        request.setUuid("111");
        request.setSchemaName("ss");
        request.setOwner("scott");
        schemaManagerService.createSchema(request);
        request.setDescription("description");
        schemaManagerService.createSchema(request);
    }

    @Test
    public void testUpdateSchema() throws SQLException {
        List<SchemaObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SchemaObjectSQLServiceImpl());
        schemaManagerService.setSchemaObjectSQLService(serviceArrayList);

        SchemaManagerQuery request = new SchemaManagerQuery();
        request.setUuid("111");
        request.setSchemaName("ss");
        request.setOwner("scott");
        request.setDescription("description");
        schemaManagerService.updateSchema(request);
    }

    @Test
    public void testUpdateSchema2() throws SQLException {
        List<SchemaObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SchemaObjectSQLServiceImpl());
        schemaManagerService.setSchemaObjectSQLService(serviceArrayList);

        SchemaManagerQuery request = new SchemaManagerQuery();
        request.setUuid("111");
        request.setSchemaName("value1");
        request.setOwner("value2");
        request.setDescription("description");
        schemaManagerService.updateSchema(request);
    }

    @Test
    public void testDeleteSchema() throws SQLException {
        List<SchemaObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SchemaObjectSQLServiceImpl());
        schemaManagerService.setSchemaObjectSQLService(serviceArrayList);

        SchemaManagerQuery request = new SchemaManagerQuery();
        request.setUuid("111");
        request.setSchemaName("ss");
        schemaManagerService.deleteSchema(request);
    }
}

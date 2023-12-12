/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.GainObjectSQLService;
import com.nctigba.datastudio.compatible.opengauss.GainObjectSQLServiceImpl;
import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.dao.DatabaseConnectionDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.dto.GetConnectionAttributeDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayIdSchemaQuery;
import com.nctigba.datastudio.service.DataListByJdbcService;
import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.utils.ConnectionUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * DbConnectionServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class DbConnectionServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private DbConnectionServiceImpl connectionService;
    @Mock
    private Connection mockConnection;
    @Mock
    private MetaDataByJdbcService metaDataByJdbcService;
    @Mock
    private DatabaseConnectionDAO databaseConnectionDAO;
    @Mock
    private ConnectionMapDAO connectionMapDAO;
    @Mock
    private DataListByJdbcService dataListByJdbcService;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setType("openGauss");
        conMap.put(UUID, connectionDTO);
    }

    @Test
    public void testAddDatabaseConnectionExist() {
        MockedStatic<ConnectionUtils> staticUtilsMockedStatic = Mockito.mockStatic(ConnectionUtils.class);
        staticUtilsMockedStatic.when(() -> ConnectionUtils.connectGet(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
        when(metaDataByJdbcService.versionSQL(anyString(), anyString(), anyString(), anyString())).thenReturn("0");
        when(databaseConnectionDAO.getJudgeName(anyString(), anyString())).thenReturn(1);
        when(databaseConnectionDAO.getAttributeByName(anyString(), anyString())).thenReturn(new DatabaseConnectionDO());
        when(databaseConnectionDAO.getByName(anyString(), anyString())).thenReturn(new DatabaseConnectionUrlDO());
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new GainObjectSQLServiceImpl());
        connectionService.setGainObjectSQLService(serviceArrayList);
        DbConnectionCreateDTO dbConnectionCreateDTO = new DbConnectionCreateDTO();
        dbConnectionCreateDTO.setConnectionid("");
        dbConnectionCreateDTO.setPassword("");
        dbConnectionCreateDTO.setType("openGauss");
        dbConnectionCreateDTO.setDataName("");
        dbConnectionCreateDTO.setDriver("");
        dbConnectionCreateDTO.setId("");
        dbConnectionCreateDTO.setIp("");
        dbConnectionCreateDTO.setName("");
        dbConnectionCreateDTO.setPort("");
        dbConnectionCreateDTO.setUserName("");
        dbConnectionCreateDTO.setWebUser("");
        connectionService.addDatabaseConnection(dbConnectionCreateDTO);
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testAddDatabaseConnectionNotExist() {
        MockedStatic<ConnectionUtils> staticUtilsMockedStatic = Mockito.mockStatic(ConnectionUtils.class);
        staticUtilsMockedStatic.when(() -> ConnectionUtils.connectGet(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
        when(metaDataByJdbcService.versionSQL(anyString(), anyString(), anyString(), anyString())).thenReturn("1");
        when(databaseConnectionDAO.getJudgeName(anyString(), anyString())).thenReturn(0);
        when(databaseConnectionDAO.getAttributeByName(anyString(), anyString())).thenReturn(new DatabaseConnectionDO());
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new GainObjectSQLServiceImpl());
        connectionService.setGainObjectSQLService(serviceArrayList);
        DbConnectionCreateDTO dbConnectionCreateDTO = new DbConnectionCreateDTO();
        dbConnectionCreateDTO.setConnectionid("");
        dbConnectionCreateDTO.setPassword("");
        dbConnectionCreateDTO.setType("openGauss");
        dbConnectionCreateDTO.setDataName("");
        dbConnectionCreateDTO.setDriver("");
        dbConnectionCreateDTO.setId("");
        dbConnectionCreateDTO.setIp("");
        dbConnectionCreateDTO.setName("");
        dbConnectionCreateDTO.setPort("");
        dbConnectionCreateDTO.setUserName("");
        dbConnectionCreateDTO.setWebUser("");
        connectionService.addDatabaseConnection(dbConnectionCreateDTO);
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testDeleteDatabaseConnectionList() {
        connectionService.deleteDatabaseConnectionList("1");
    }

    @Test
    public void testDatabaseAttributeConnection() {
        when(databaseConnectionDAO.getAttributeById(anyString(), anyString())).thenReturn(new DatabaseConnectionDO());
        GetConnectionAttributeDTO request = new GetConnectionAttributeDTO();
        request.setId("1");
        request.setWebUser("2");
        connectionService.databaseAttributeConnection(request);
    }

    @Test
    public void testDatabaseConnectionList() {
        when(databaseConnectionDAO.selectTable(anyString())).thenReturn(new ArrayList<>());
        connectionService.databaseConnectionList("1");
    }

    @Test
    public void testUpdateDatabaseConnection() throws SQLException {
        MockedStatic<ConnectionUtils> staticUtilsMockedStatic = Mockito.mockStatic(ConnectionUtils.class);
        staticUtilsMockedStatic.when(() -> ConnectionUtils.connectGet(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
        when(metaDataByJdbcService.versionSQL(anyString(), anyString(), anyString(), anyString())).thenReturn("1");
        when(databaseConnectionDAO.getAttributeByName(anyString(), anyString())).thenReturn(new DatabaseConnectionDO());
        when(databaseConnectionDAO.getByName(anyString(), anyString())).thenReturn(new DatabaseConnectionUrlDO());
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new GainObjectSQLServiceImpl());
        connectionService.setGainObjectSQLService(serviceArrayList);
        DbConnectionCreateDTO dbConnectionCreateDTO = new DbConnectionCreateDTO();
        dbConnectionCreateDTO.setConnectionid(UUID);
        dbConnectionCreateDTO.setPassword("");
        dbConnectionCreateDTO.setType("openGauss");
        dbConnectionCreateDTO.setDataName("");
        dbConnectionCreateDTO.setDriver("");
        dbConnectionCreateDTO.setId("");
        dbConnectionCreateDTO.setIp("");
        dbConnectionCreateDTO.setName("");
        dbConnectionCreateDTO.setPort("");
        dbConnectionCreateDTO.setUserName("");
        dbConnectionCreateDTO.setWebUser("");
        connectionService.updateDatabaseConnection(dbConnectionCreateDTO);
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testSchemaObjectListFalse() {
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");
        List<GainObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new GainObjectSQLServiceImpl());
        connectionService.setGainObjectSQLService(serviceArrayList);
        DatabaseMetaArrayIdSchemaQuery databaseMetaarrayIdSchemaQuery = new DatabaseMetaArrayIdSchemaQuery();
        databaseMetaarrayIdSchemaQuery.setSchema("");
        databaseMetaarrayIdSchemaQuery.setWebUser("");
        databaseMetaarrayIdSchemaQuery.setUuid("222");
        databaseMetaarrayIdSchemaQuery.setConnectionName("");
        assertThrows(CustomException.class, () -> {
            connectionService.schemaObjectList(databaseMetaarrayIdSchemaQuery);
        });
        staticUtilsMockedStatic.close();
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.DatabaseObjectSQLService;
import com.nctigba.datastudio.compatible.opengauss.DatabaseObjectSQLServiceImpl;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.dao.DatabaseConnectionDAO;
import com.nctigba.datastudio.dao.ResultSetMapDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * CreateDatabaseServiceTest
 *
 * @since 2023-07-04
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class CreateDatabaseServiceTest {
    private static final String UUID = "111";

    private static final String DATABASE_TYPE = "openGauss";

    @InjectMocks
    private CreateDatabaseServiceImpl createDatabaseService;
    @Mock
    private ResultSetMapDAO resultSetMapDAO;
    @Mock
    private Map<String, DatabaseObjectSQLService> databaseObjectSQLService;
    @Mock
    private Connection mockConnection;
    @Mock
    private ConnectionConfig connectionConfig;
    @Mock
    private Statement mockStatement;
    @Mock
    private DatabaseConnectionDAO databaseConnectionDAO;
    @Mock
    private MetaDataByJdbcService metaDataByJdbcService;
    @Mock
    private RequestContextHolder contextHolder;
    @Mock
    private ServletRequestAttributes requestAttributes;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private ResultSetMetaData mockMetaData;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setType(DATABASE_TYPE);
        conMap.put(UUID, connectionDTO);
        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        List<DatabaseObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new DatabaseObjectSQLServiceImpl());
        createDatabaseService.setDatabaseObjectSQLService(serviceArrayList);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("qqq");
        when(mockResultSet.getObject(anyString())).thenReturn("test");
    }

    @Test
    public void testCreateDatabase() throws SQLException {
        CreateDatabaseDTO createDatabaseDTO = new CreateDatabaseDTO();
        createDatabaseDTO.setDatabaseCode("utf-8");
        createDatabaseDTO.setDatabaseName("222");
        createDatabaseDTO.setUuid("111");
        createDatabaseDTO.setCharacterType("utf-8");
        createDatabaseDTO.setCompatibleType("utf-8");
        createDatabaseDTO.setConRestrictions("111");
        createDatabaseDTO.setCollation(UUID);
        createDatabaseService.createDatabase(createDatabaseDTO);
    }

    @Test
    public void testCreateDatabaseNull() throws SQLException {
        CreateDatabaseDTO createDatabaseDTO = new CreateDatabaseDTO();
        createDatabaseDTO.setDatabaseCode("utf-8");
        createDatabaseDTO.setDatabaseName("222");
        createDatabaseDTO.setUuid(UUID);
        createDatabaseDTO.setConRestrictions("3");
        createDatabaseDTO.setCompatibleType("utf-8");
        createDatabaseService.createDatabase(createDatabaseDTO);
    }

    @Test
    public void testCreateDatabaseConRestrictions() throws SQLException {
        CreateDatabaseDTO createDatabaseDTO = new CreateDatabaseDTO();
        createDatabaseDTO.setDatabaseCode("utf-8");
        createDatabaseDTO.setDatabaseName("222");
        createDatabaseDTO.setUuid(UUID);
        createDatabaseDTO.setConRestrictions("-3");
        createDatabaseDTO.setCompatibleType("utf-8");
        try {
            createDatabaseService.createDatabase(createDatabaseDTO);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testConnectionDatabase() {
        DatabaseConnectionDO databaseConnectionDO = new DatabaseConnectionDO();
        databaseConnectionDO.setIp("0.0.0.0");
        databaseConnectionDO.setPort("1234");
        databaseConnectionDO.setUserName("test");
        databaseConnectionDO.setPassword("tset");
        databaseConnectionDO.setType(DATABASE_TYPE);
        when(databaseConnectionDAO.getByIdDatabase(any(), anyString())).thenReturn(databaseConnectionDO);
        databaseConnectionDO.setId("1");
        databaseConnectionDO.setWebUser("23");
        databaseConnectionDO.setDataName("342");
        createDatabaseService.connectionDatabase(databaseConnectionDO);
    }

    @Test
    public void testDeleteDatabase() throws SQLException {
        DatabaseNameDTO databaseNameDTO = new DatabaseNameDTO();
        databaseNameDTO.setUuid(UUID);
        databaseNameDTO.setDatabaseName("dwq");
        createDatabaseService.deleteDatabase(databaseNameDTO);
    }

    @Test
    public void testRenameDatabaseTure() throws SQLException {
        RenameDatabaseDTO renameDatabaseDTO = new RenameDatabaseDTO();
        renameDatabaseDTO.setUuid(UUID);
        renameDatabaseDTO.setOldDatabaseName("dwq");
        renameDatabaseDTO.setDatabaseName("dwqa");
        renameDatabaseDTO.setConRestrictions("2");
        createDatabaseService.renameDatabase(renameDatabaseDTO);
    }

    @Test
    public void testRenameDatabaseFalse() {
        MockedStatic<LocaleString> staticUtilsMockedStatic = Mockito.mockStatic(LocaleString.class);
        staticUtilsMockedStatic.when(() -> LocaleString.transLanguage(anyString()))
                .thenReturn("123");
        RenameDatabaseDTO renameDatabaseDTO = new RenameDatabaseDTO();
        renameDatabaseDTO.setUuid(UUID);
        renameDatabaseDTO.setOldDatabaseName("dwq");
        renameDatabaseDTO.setDatabaseName("dwq");
        renameDatabaseDTO.setConRestrictions("-3");
        assertThrows(CustomException.class, () -> {
            createDatabaseService.renameDatabase(renameDatabaseDTO);
        });
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testDatabaseAttribute() throws SQLException {
        DatabaseNameDTO databaseNameDTO = new DatabaseNameDTO();
        databaseNameDTO.setUuid(UUID);
        databaseNameDTO.setDatabaseName("dwqa");
        createDatabaseService.databaseAttribute(databaseNameDTO);
    }

    @Test
    public void testDatabaseAttributeUpdate() throws SQLException {
        DatabaseNameDTO databaseNameDTO = new DatabaseNameDTO();
        databaseNameDTO.setUuid(UUID);
        databaseNameDTO.setDatabaseName("dwqa");
        createDatabaseService.databaseAttributeUpdate(databaseNameDTO);
    }
}

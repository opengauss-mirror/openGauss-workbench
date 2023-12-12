/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ViewObjectSQLServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewObjectSQLServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private ViewObjectSQLServiceImpl viewObjectSQLService;
    @Mock
    private Connection mockConnection;
    @Mock
    private ViewObjectSQLServiceImpl viewObjectSQL;
    @Mock
    private ConnectionConfig connectionConfig;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private ResultSetMetaData mockMetaData;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setType("openGauss");
        conMap.put(UUID, connectionDTO);
        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockResultSet.getString(anyString())).thenReturn("1");
    }


    @Test
    public void testReturnDatabaseViewDDL() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockMetaData.getColumnCount()).thenReturn(4);
        when(mockMetaData.getColumnName(anyInt())).thenReturn("relkind")
                .thenReturn("schemaname").thenReturn("matviewname").thenReturn("definition");
        when(mockResultSet.getString(anyString())).thenReturn("1");
        DatabaseViewDdlDTO databaseViewDdlDTO = new DatabaseViewDdlDTO();
        databaseViewDdlDTO.setViewName("false");
        databaseViewDdlDTO.setSchema("false");
        databaseViewDdlDTO.setUuid(UUID);
        databaseViewDdlDTO.setConnectionName("false");
        databaseViewDdlDTO.setWebUser("false");
        viewObjectSQLService.returnDatabaseViewDDL(databaseViewDdlDTO);
    }

    @Test
    public void testReturnDatabaseViewDDLTrue() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockMetaData.getColumnCount()).thenReturn(4);
        when(mockMetaData.getColumnName(anyInt())).thenReturn("relkind")
                .thenReturn("schemaname").thenReturn("matviewname").thenReturn("definition");
        when(mockResultSet.getString("relkind")).thenReturn("m");
        DatabaseViewDdlDTO databaseViewDdlDTO = new DatabaseViewDdlDTO();
        databaseViewDdlDTO.setViewName("false");
        databaseViewDdlDTO.setSchema("false");
        databaseViewDdlDTO.setUuid(UUID);
        databaseViewDdlDTO.setConnectionName("false");
        databaseViewDdlDTO.setWebUser("false");
        viewObjectSQLService.returnDatabaseViewDDL(databaseViewDdlDTO);
    }

    @Test
    public void testrReturnViewDDLData() throws SQLException {
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");
        when(mockResultSet.getString("relkind")).thenReturn("");
        DatabaseViewDdlDTO databaseViewDdlDTO = new DatabaseViewDdlDTO();
        databaseViewDdlDTO.setViewName("false");
        databaseViewDdlDTO.setSchema("false");
        databaseViewDdlDTO.setUuid(UUID);
        databaseViewDdlDTO.setConnectionName("false");
        databaseViewDdlDTO.setWebUser("false");
        assertThrows(CustomException.class, () -> {
            viewObjectSQLService.returnViewDDLData(databaseViewDdlDTO);
        });
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testViewTypeData() {
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");
        DatabaseViewDdlDTO databaseViewDdlDTO = new DatabaseViewDdlDTO();
        databaseViewDdlDTO.setViewName("false");
        databaseViewDdlDTO.setSchema("false");
        databaseViewDdlDTO.setUuid(UUID);
        databaseViewDdlDTO.setConnectionName("false");
        databaseViewDdlDTO.setWebUser("false");
        assertThrows(CustomException.class, () -> {
            viewObjectSQLService.viewTypeData(databaseViewDdlDTO);
        });
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testReturnDropViewSQL() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("relkind")).thenReturn("m");
        DatabaseViewDdlDTO databaseViewDdlDTO = new DatabaseViewDdlDTO();
        databaseViewDdlDTO.setViewName("false");
        databaseViewDdlDTO.setSchema("false");
        databaseViewDdlDTO.setUuid(UUID);
        databaseViewDdlDTO.setConnectionName("false");
        databaseViewDdlDTO.setWebUser("false");
        viewObjectSQLService.returnDropViewSQL(databaseViewDdlDTO);
    }

    @Test
    public void testReturnDropViewSQLElse() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("relkind")).thenReturn("y");
        DatabaseViewDdlDTO databaseViewDdlDTO = new DatabaseViewDdlDTO();
        databaseViewDdlDTO.setViewName("false");
        databaseViewDdlDTO.setSchema("false");
        databaseViewDdlDTO.setUuid(UUID);
        databaseViewDdlDTO.setConnectionName("false");
        databaseViewDdlDTO.setWebUser("false");
        viewObjectSQLService.returnDropViewSQL(databaseViewDdlDTO);
    }
}

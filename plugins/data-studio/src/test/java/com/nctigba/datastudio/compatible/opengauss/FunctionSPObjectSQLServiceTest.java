/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * FunctionSPObjectSQLServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class FunctionSPObjectSQLServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private FunctionSPObjectSQLServiceImpl functionSPObjectSQLService;
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
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString(anyString())).thenReturn("1");
    }


    @Test
    public void testType() {
        functionSPObjectSQLService.type();
    }

    @Test
    public void testFunctionDdl() throws SQLException {
        DatabaseFunctionSPDTO databaseFunctionSPDTO = new DatabaseFunctionSPDTO();
        databaseFunctionSPDTO.setSchema("s");
        databaseFunctionSPDTO.setConnectionName("ik");
        databaseFunctionSPDTO.setFunctionSPName("hj");
        databaseFunctionSPDTO.setWebUser("j");
        databaseFunctionSPDTO.setUuid(UUID);
        databaseFunctionSPDTO.setOid("uj");
        functionSPObjectSQLService.functionDdl(databaseFunctionSPDTO);
    }

    @Test
    public void testFunctionDdlFalse() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);
        DatabaseFunctionSPDTO databaseFunctionSPDTO = new DatabaseFunctionSPDTO();
        databaseFunctionSPDTO.setSchema("s");
        databaseFunctionSPDTO.setConnectionName("ik");
        databaseFunctionSPDTO.setFunctionSPName("hj");
        databaseFunctionSPDTO.setWebUser("j");
        databaseFunctionSPDTO.setUuid(UUID);
        databaseFunctionSPDTO.setOid("uj");
        functionSPObjectSQLService.functionDdl(databaseFunctionSPDTO);
    }

    @Test
    public void testFunctionDdlElseFalse() throws SQLException {
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");
        when(mockResultSet.getString(anyString())).thenReturn("");
        DatabaseFunctionSPDTO databaseFunctionSPDTO = new DatabaseFunctionSPDTO();
        databaseFunctionSPDTO.setSchema("s");
        databaseFunctionSPDTO.setConnectionName("ik");
        databaseFunctionSPDTO.setFunctionSPName("hj");
        databaseFunctionSPDTO.setWebUser("j");
        databaseFunctionSPDTO.setUuid(UUID);
        databaseFunctionSPDTO.setOid("uj");
        assertThrows(CustomException.class, () -> {
            functionSPObjectSQLService.functionDdl(databaseFunctionSPDTO);
        });
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testDropFunctionSPFunction() throws SQLException {
        when(mockResultSet.getString(anyString())).thenReturn("f");
        DatabaseFunctionSPDTO databaseFunctionSPDTO = new DatabaseFunctionSPDTO();
        databaseFunctionSPDTO.setSchema("s");
        databaseFunctionSPDTO.setConnectionName("ik");
        databaseFunctionSPDTO.setFunctionSPName("hj");
        databaseFunctionSPDTO.setWebUser("j");
        databaseFunctionSPDTO.setUuid(UUID);
        databaseFunctionSPDTO.setOid("uj");
        functionSPObjectSQLService.dropFunctionSP(databaseFunctionSPDTO);
    }

    @Test
    public void testDropFunctionSPProcedure() throws SQLException {
        when(mockResultSet.getString(anyString())).thenReturn("p");
        DatabaseFunctionSPDTO databaseFunctionSPDTO = new DatabaseFunctionSPDTO();
        databaseFunctionSPDTO.setSchema("s");
        databaseFunctionSPDTO.setConnectionName("ik");
        databaseFunctionSPDTO.setFunctionSPName("hj");
        databaseFunctionSPDTO.setWebUser("j");
        databaseFunctionSPDTO.setUuid(UUID);
        databaseFunctionSPDTO.setOid("uj");
        functionSPObjectSQLService.dropFunctionSP(databaseFunctionSPDTO);
    }

    @Test
    public void testDropFunctionSPFalse() throws SQLException {
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");
        when(mockResultSet.next()).thenReturn(false);
        DatabaseFunctionSPDTO databaseFunctionSPDTO = new DatabaseFunctionSPDTO();
        databaseFunctionSPDTO.setSchema("s");
        databaseFunctionSPDTO.setConnectionName("ik");
        databaseFunctionSPDTO.setFunctionSPName("hj");
        databaseFunctionSPDTO.setWebUser("j");
        databaseFunctionSPDTO.setUuid(UUID);
        databaseFunctionSPDTO.setOid("uj");
        assertThrows(CustomException.class, () -> {
            functionSPObjectSQLService.dropFunctionSP(databaseFunctionSPDTO);
        });
        staticUtilsMockedStatic.close();
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.FunctionSPObjectSQLService;
import com.nctigba.datastudio.compatible.opengauss.FunctionSPObjectSQLServiceImpl;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * DatabaseFunctionSPServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseFunctionSPServiceTest {
    private static final String UUID = "111";

    @Mock
    FunctionSPObjectSQLServiceImpl functionObjectSQLService;
    @InjectMocks
    private DatabaseFunctionSPServiceImpl databaseFunctionSPService;
    @Mock
    private Map<String, FunctionSPObjectSQLService> functionSPObjectSQLService;
    @Mock
    private Connection mockConnection;
    @Mock
    private ConnectionConfig connectionConfig;
    @Mock
    private PreparedStatement mockStatement;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setType("openGauss");
        conMap.put(UUID, connectionDTO);
        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
    }


    @Test
    public void testFunctionDdl() throws SQLException {
        List<FunctionSPObjectSQLService> serviceArrayList = new ArrayList<>();
        when(functionObjectSQLService.type()).thenReturn("openGauss");
        serviceArrayList.add(functionObjectSQLService);
        databaseFunctionSPService.setFunctionSPObjectSQLService(serviceArrayList);
        DatabaseFunctionSPDTO databaseFunctionSPDTO = new DatabaseFunctionSPDTO();
        databaseFunctionSPDTO.setOid("2");
        databaseFunctionSPDTO.setFunctionSPName("t1");
        databaseFunctionSPDTO.setWebUser("user");
        databaseFunctionSPDTO.setConnectionName("one");
        databaseFunctionSPDTO.setSchema("s1");
        databaseFunctionSPDTO.setUuid(UUID);
        databaseFunctionSPService.functionDdl(databaseFunctionSPDTO);
    }

    @Test
    public void testDropFunctionSP() {
        List<FunctionSPObjectSQLService> serviceArrayList = new ArrayList<>();
        when(functionObjectSQLService.type()).thenReturn("openGauss");
        serviceArrayList.add(functionObjectSQLService);
        databaseFunctionSPService.setFunctionSPObjectSQLService(serviceArrayList);
        DatabaseFunctionSPDTO databaseFunctionSPDTO = new DatabaseFunctionSPDTO();
        databaseFunctionSPDTO.setOid("2");
        databaseFunctionSPDTO.setFunctionSPName("t1");
        databaseFunctionSPDTO.setWebUser("user");
        databaseFunctionSPDTO.setConnectionName("one");
        databaseFunctionSPDTO.setSchema("s1");
        databaseFunctionSPDTO.setUuid("111");
        databaseFunctionSPService.dropFunctionSP(databaseFunctionSPDTO);
    }
}

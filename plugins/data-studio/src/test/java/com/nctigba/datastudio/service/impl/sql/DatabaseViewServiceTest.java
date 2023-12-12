/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.SequenceObjectSQLService;
import com.nctigba.datastudio.compatible.ViewObjectSQLService;
import com.nctigba.datastudio.compatible.opengauss.ViewObjectSQLServiceImpl;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.dao.ResultSetMapDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
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
 * DatabaseViewServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseViewServiceTest {
    private static final String UUID = "111";

    @Mock
    ViewObjectSQLServiceImpl viewObjectSQLService;
    @InjectMocks
    private DatabaseViewServiceImpl viewService;
    @Mock
    private ResultSetMapDAO resultSetMapDAO;
    @Mock
    private Map<String, SequenceObjectSQLService> tableColumnSQLService;
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
    public void testCreateViewDDL() {
        List<ViewObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new ViewObjectSQLServiceImpl());
        viewService.setViewObjectSQLService(serviceArrayList);
        DatabaseCreateViewDTO createViewDTO = new DatabaseCreateViewDTO();
        createViewDTO.setUuid(UUID);
        createViewDTO.setWebUser("u1");
        createViewDTO.setSchema("w1");
        createViewDTO.setConnectionName("rrr");
        createViewDTO.setSql("selest 1");
        createViewDTO.setViewName("v1");
        createViewDTO.setViewType("MATERIALIZED");
        viewService.createViewDDL(createViewDTO);
        createViewDTO.setViewType("v");
        viewService.createViewDDL(createViewDTO);
    }

    @Test
    public void testCreateView() {
        List<ViewObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new ViewObjectSQLServiceImpl());
        viewService.setViewObjectSQLService(serviceArrayList);
        DatabaseCreateViewDTO createViewDTO = new DatabaseCreateViewDTO();
        createViewDTO.setUuid(UUID);
        createViewDTO.setWebUser("u1");
        createViewDTO.setSchema("w1");
        createViewDTO.setConnectionName("rrr");
        createViewDTO.setSql("selest 1");
        createViewDTO.setViewName("v1");
        createViewDTO.setViewType("MATERIALIZED");
        viewService.createView(createViewDTO);
        createViewDTO.setViewType("v");
        viewService.createView(createViewDTO);
    }

    @Test
    public void testReturnViewDDL() throws SQLException {
        List<ViewObjectSQLService> serviceArrayList = new ArrayList<>();
        when(viewObjectSQLService.type()).thenReturn("openGauss");
        serviceArrayList.add(viewObjectSQLService);
        viewService.setViewObjectSQLService(serviceArrayList);
        DatabaseViewDdlDTO viewDdlDTO = new DatabaseViewDdlDTO();
        viewDdlDTO.setUuid(UUID);
        viewDdlDTO.setWebUser("u1");
        viewDdlDTO.setSchema("w1");
        viewDdlDTO.setConnectionName("rrr");
        viewDdlDTO.setViewName("v1");
        viewService.returnViewDDL(viewDdlDTO);
    }

    @Test
    public void testDropView() {
        List<ViewObjectSQLService> serviceArrayList = new ArrayList<>();
        when(viewObjectSQLService.type()).thenReturn("openGauss");
        serviceArrayList.add(viewObjectSQLService);
        viewService.setViewObjectSQLService(serviceArrayList);
        DatabaseViewDdlDTO dropViewDTO = new DatabaseViewDdlDTO();
        dropViewDTO.setUuid(UUID);
        dropViewDTO.setWebUser("u1");
        dropViewDTO.setSchema("w1");
        dropViewDTO.setConnectionName("rrr");
        dropViewDTO.setViewName("v1");
        viewService.dropView(dropViewDTO);
    }
}

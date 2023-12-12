/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.query.SelectDataFiltrationQuery;
import com.nctigba.datastudio.model.query.SelectDataQuery;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * TableObjectSQLServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class TableObjectSQLServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private TableObjectSQLServiceImpl tableObjectSQLService;
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
    }

    @Test
    public void testTableAttributeSQL() throws SQLException {
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");
        when(mockResultSet.next()).thenReturn(true, false);
        tableObjectSQLService.tableAttributeSQL(UUID, "", "n");
        tableObjectSQLService.tableAttributeSQL(UUID, "", "y");
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testTableDdlErro() throws SQLException {
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("pg_get_tabledef")).thenReturn("");
        SelectDataQuery selectDataQuery = new SelectDataQuery();
        selectDataQuery.setOid("312");
        selectDataQuery.setSchema("s1");
        selectDataQuery.setUuid(UUID);
        selectDataQuery.setTableName("t1");
        assertThrows(CustomException.class, () -> {
            tableObjectSQLService.tableDdl(selectDataQuery);
        });
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testTableDdl() throws SQLException {
        when(mockResultSet.getString("pg_get_tabledef")).thenReturn("22");
        when(mockResultSet.next()).thenReturn(true, false);
        SelectDataQuery selectDataQuery = new SelectDataQuery();
        selectDataQuery.setOid("312");
        selectDataQuery.setSchema("s1");
        selectDataQuery.setUuid(UUID);
        selectDataQuery.setTableName("t1");
        tableObjectSQLService.tableDdl(selectDataQuery);
    }

    @Test
    public void testTableDataSQL() {
        SelectDataFiltrationQuery selectDataFiltrationQuery = new SelectDataFiltrationQuery();
        List<String> orderList = new ArrayList();
        List<String> filtrationList = new ArrayList();
        orderList.add("a desc");
        orderList.add("b asc");
        selectDataFiltrationQuery.setOrder(orderList);
        selectDataFiltrationQuery.setFiltration(filtrationList);
        tableObjectSQLService.tableDataSQL("", "", 1, 2, selectDataFiltrationQuery);
        filtrationList.add("a = c");
        filtrationList.add("a = b and");
        selectDataFiltrationQuery.setOrder(orderList);
        selectDataFiltrationQuery.setFiltration(filtrationList);
        tableObjectSQLService.tableDataSQL("", "", 1, 2, selectDataFiltrationQuery);
        filtrationList.add("a = b or");
        selectDataFiltrationQuery.setOrder(orderList);
        selectDataFiltrationQuery.setFiltration(filtrationList);
        tableObjectSQLService.tableDataSQL("", "", 1, 2, selectDataFiltrationQuery);
        filtrationList.add("a = b");
        selectDataFiltrationQuery.setOrder(orderList);
        selectDataFiltrationQuery.setFiltration(filtrationList);
        tableObjectSQLService.tableDataSQL("", "", 1, 2, selectDataFiltrationQuery);
    }
}

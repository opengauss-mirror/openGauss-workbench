/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.utils.ConnectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * DataListByJdbcServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class DataListByJdbcServiceTest {
    @InjectMocks
    private DataListByJdbcServiceImpl dataListByJdbcService;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    @Test
    public void testCreateViewDDL() throws SQLException, InterruptedException {
        MockedStatic<ConnectionUtils> staticUtilsMockedStatic = Mockito.mockStatic(ConnectionUtils.class);
        staticUtilsMockedStatic.when(() -> ConnectionUtils.connectGet(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
        dataListByJdbcService.dataListQuerySQL("", "", "", "", "",
                "", "", "", "", "", "");
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testCreateViewDDLTypeOne() throws SQLException, InterruptedException {
        MockedStatic<ConnectionUtils> staticUtilsMockedStatic = Mockito.mockStatic(ConnectionUtils.class);
        staticUtilsMockedStatic.when(() -> ConnectionUtils.connectGet(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
        dataListByJdbcService.dataListQuerySQL("", "", "", "", "",
                "", "", "", "", "", "");
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testCreateViewDDLTypeNull() throws SQLException, InterruptedException {
        MockedStatic<ConnectionUtils> staticUtilsMockedStatic = Mockito.mockStatic(ConnectionUtils.class);
        staticUtilsMockedStatic.when(() -> ConnectionUtils.connectGet(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
        dataListByJdbcService.dataListQuerySQL("", "", "", "", "",
                "", "", "", "", "", "");
        staticUtilsMockedStatic.close();
    }
}

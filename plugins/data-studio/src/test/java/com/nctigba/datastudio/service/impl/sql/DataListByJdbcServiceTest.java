/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  DataListByJdbcServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/service/impl/sql/DataListByJdbcServiceTest.java
 *
 *  -------------------------------------------------------------------------
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

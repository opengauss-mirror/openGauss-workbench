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
 *  SequenceObjectSQLServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/compatible/opengauss/SequenceObjectSQLServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
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
 * SequenceObjectSQLServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class SequenceObjectSQLServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private SequenceObjectSQLServiceImpl sequenceObjectSQLService;
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
    }

    @Test
    public void testUpdateSchemaCommentSQLErro() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");
        when(mockResultSet.getInt(anyString())).thenReturn(0);
        DatabaseSequenceDdlDTO databaseCreateSynonymDTO = new DatabaseSequenceDdlDTO();
        databaseCreateSynonymDTO.setSchema("");
        databaseCreateSynonymDTO.setUuid(UUID);
        databaseCreateSynonymDTO.setSequenceName("");
        databaseCreateSynonymDTO.setConnectionName("");
        databaseCreateSynonymDTO.setWebUser("");
        assertThrows(CustomException.class, () -> {
            sequenceObjectSQLService.returnSequenceDDL(databaseCreateSynonymDTO);
        });
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testUpdateSchemaCommentSQLTrue() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString(anyString())).thenReturn("qq");
        when(mockResultSet.getInt("count")).thenReturn(1);
        when(mockResultSet.getInt("minimum_value")).thenReturn(1);
        when(mockResultSet.getString("cycle_option")).thenReturn("YES");
        DatabaseSequenceDdlDTO databaseCreateSynonymDTO = new DatabaseSequenceDdlDTO();
        databaseCreateSynonymDTO.setSchema("");
        databaseCreateSynonymDTO.setUuid(UUID);
        databaseCreateSynonymDTO.setSequenceName("");
        databaseCreateSynonymDTO.setConnectionName("");
        databaseCreateSynonymDTO.setWebUser("");
        sequenceObjectSQLService.returnSequenceDDL(databaseCreateSynonymDTO);
    }

    @Test
    public void testUpdateSchemaCommentSQLFalse() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString(anyString())).thenReturn("qq");
        when(mockResultSet.getInt("count")).thenReturn(1);
        when(mockResultSet.getInt("minimum_value")).thenReturn(3);
        when(mockResultSet.getString("cycle_option")).thenReturn("NO");
        DatabaseSequenceDdlDTO databaseCreateSynonymDTO = new DatabaseSequenceDdlDTO();
        databaseCreateSynonymDTO.setSchema("");
        databaseCreateSynonymDTO.setUuid(UUID);
        databaseCreateSynonymDTO.setSequenceName("");
        databaseCreateSynonymDTO.setConnectionName("");
        databaseCreateSynonymDTO.setWebUser("");
        sequenceObjectSQLService.returnSequenceDDL(databaseCreateSynonymDTO);
    }

    @Test
    public void testSplicingSequenceDDL() {
        DatabaseCreateSequenceDTO databaseCreateSequenceDTO = new DatabaseCreateSequenceDTO();
        databaseCreateSequenceDTO.setSchema("s");
        databaseCreateSequenceDTO.setSequenceName("s");
        databaseCreateSequenceDTO.setIsCycle(false);
        databaseCreateSequenceDTO.setIncrement("123");
        sequenceObjectSQLService.splicingSequenceDDL(databaseCreateSequenceDTO);
    }
}

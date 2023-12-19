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
 *  DatabaseSequenceServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/service/impl/sql/DatabaseSequenceServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.SequenceObjectSQLService;
import com.nctigba.datastudio.compatible.opengauss.SequenceObjectSQLServiceImpl;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.dao.ResultSetMapDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
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
 * DatabaseSequenceServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseSequenceServiceTest {
    private static final String UUID = "111";

    @Mock
    SequenceObjectSQLServiceImpl sequenceObjectSQLService;
    @InjectMocks
    private DatabaseSequenceServiceImpl sequenceDataService;
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
    public void testCreateSequenceDDL() {
        List<SequenceObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SequenceObjectSQLServiceImpl());
        sequenceDataService.setSequenceObjectSQLService(serviceArrayList);
        DatabaseCreateSequenceDTO createSequenceDTO = new DatabaseCreateSequenceDTO();
        createSequenceDTO.setUuid(UUID);
        createSequenceDTO.setWebUser("u1");
        createSequenceDTO.setCache("2");
        createSequenceDTO.setSequenceName("s1");
        createSequenceDTO.setTableName("t1");
        createSequenceDTO.setSchema("w1");
        createSequenceDTO.setConnectionName("rrr");
        createSequenceDTO.setIsCycle(true);
        createSequenceDTO.setIncrement("");
        createSequenceDTO.setMaxValue("5");
        createSequenceDTO.setMinValue("1");
        createSequenceDTO.setStart("1");
        createSequenceDTO.setTableColumn("c2");
        createSequenceDTO.setTableSchema("w2");
        sequenceDataService.createSequenceDDL(createSequenceDTO);
    }

    @Test
    public void testCreateSequence() throws SQLException {
        List<SequenceObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SequenceObjectSQLServiceImpl());
        sequenceDataService.setSequenceObjectSQLService(serviceArrayList);
        DatabaseCreateSequenceDTO createSequenceDTO = new DatabaseCreateSequenceDTO();
        createSequenceDTO.setUuid(UUID);
        createSequenceDTO.setWebUser("u1");
        createSequenceDTO.setSequenceName("s1");
        createSequenceDTO.setSchema("w1");
        createSequenceDTO.setConnectionName("rrr");
        createSequenceDTO.setIsCycle(false);
        createSequenceDTO.setTableSchema("w2");
        sequenceDataService.createSequence(createSequenceDTO);
    }

    @Test
    public void testDropSequence() throws SQLException {
        List<SequenceObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SequenceObjectSQLServiceImpl());
        sequenceDataService.setSequenceObjectSQLService(serviceArrayList);
        DatabaseDropSequenceDTO dropSequenceDTO = new DatabaseDropSequenceDTO();
        dropSequenceDTO.setSequenceName("s1");
        dropSequenceDTO.setUuid(UUID);
        dropSequenceDTO.setSchema("s1");
        dropSequenceDTO.setConnectionName("c1");
        dropSequenceDTO.setWebUser("w1");
        sequenceDataService.dropSequence(dropSequenceDTO);
    }

    @Test
    public void testReturnSequenceDDL() throws SQLException {
        List<SequenceObjectSQLService> serviceArrayList = new ArrayList<>();
        when(sequenceObjectSQLService.type()).thenReturn("openGauss");
        serviceArrayList.add(sequenceObjectSQLService);
        sequenceDataService.setSequenceObjectSQLService(serviceArrayList);
        DatabaseSequenceDdlDTO sequenceDdlDTO = new DatabaseSequenceDdlDTO();
        sequenceDdlDTO.setSequenceName("s1");
        sequenceDdlDTO.setUuid(UUID);
        sequenceDdlDTO.setSchema("s1");
        sequenceDdlDTO.setConnectionName("c1");
        sequenceDdlDTO.setWebUser("w1");
        sequenceDataService.returnSequenceDDL(sequenceDdlDTO);
    }
}

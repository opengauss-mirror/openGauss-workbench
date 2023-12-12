/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.SequenceObjectSQLService;
import com.nctigba.datastudio.compatible.SynonymObjectSQLService;
import com.nctigba.datastudio.compatible.opengauss.SynonymObjectSQLServiceImpl;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.dao.ResultSetMapDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * DatabaseSynonymServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseSynonymServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private DatabaseSynonymServiceImpl synonymDataService;
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
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("qqq");
        when(mockResultSet.getString(anyString())).thenReturn("test");
    }

    @Test
    public void testCreateSynonymDDL() {
        List<SynonymObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SynonymObjectSQLServiceImpl());
        synonymDataService.setSynonymObjectSQLService(serviceArrayList);
        DatabaseCreateSynonymDTO createSynonymDTO = new DatabaseCreateSynonymDTO();
        createSynonymDTO.setUuid(UUID);
        createSynonymDTO.setWebUser("u1");
        createSynonymDTO.setSchema("w1");
        createSynonymDTO.setConnectionName("rrr");
        createSynonymDTO.setSynonymName("s1");
        createSynonymDTO.setObjectName("r1");
        createSynonymDTO.setReplace(true);
        synonymDataService.createSynonymDDL(createSynonymDTO);
    }

    @Test
    public void testCreateSynonym() throws SQLException {
        List<SynonymObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SynonymObjectSQLServiceImpl());
        synonymDataService.setSynonymObjectSQLService(serviceArrayList);
        DatabaseCreateSynonymDTO createSynonymDTO = new DatabaseCreateSynonymDTO();
        createSynonymDTO.setUuid(UUID);
        createSynonymDTO.setWebUser("u1");
        createSynonymDTO.setSchema("w1");
        createSynonymDTO.setConnectionName("rrr");
        createSynonymDTO.setSynonymName("s1");
        createSynonymDTO.setObjectName("r1");
        createSynonymDTO.setReplace(true);
        synonymDataService.createSynonym(createSynonymDTO);
    }

    @Test
    public void testSynonymAttribute() throws SQLException {
        List<SynonymObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SynonymObjectSQLServiceImpl());
        synonymDataService.setSynonymObjectSQLService(serviceArrayList);
        DatabaseSynonymAttributeDTO synonymAttribute = new DatabaseSynonymAttributeDTO();
        synonymAttribute.setUuid(UUID);
        synonymAttribute.setSynonymName("s1");
        synonymAttribute.setWebUser("");
        synonymAttribute.setConnectionName("");
        synonymDataService.synonymAttribute(synonymAttribute);
    }


    @Test
    public void testSynonymAttributeErro() throws SQLException {
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");
        List<SynonymObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SynonymObjectSQLServiceImpl());
        synonymDataService.setSynonymObjectSQLService(serviceArrayList);
        when(mockResultSet.getString(anyString())).thenReturn("");
        DatabaseSynonymAttributeDTO synonymAttribute = new DatabaseSynonymAttributeDTO();
        synonymAttribute.setUuid(UUID);
        synonymAttribute.setSynonymName("sss");
        assertThrows(CustomException.class, () -> {
            synonymDataService.synonymAttribute(synonymAttribute);
        });
        staticUtilsMockedStatic.close();
    }

    @Test
    public void testDropSynonym() throws SQLException {
        List<SynonymObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new SynonymObjectSQLServiceImpl());
        synonymDataService.setSynonymObjectSQLService(serviceArrayList);
        DatabaseDropSynonymDTO dropSynonymDTO = new DatabaseDropSynonymDTO();
        dropSynonymDTO.setUuid(UUID);
        dropSynonymDTO.setSynonymName("s1");
        dropSynonymDTO.setWebUser("");
        dropSynonymDTO.setConnectionName("");
        dropSynonymDTO.setSchema("ss1");
        synonymDataService.dropSynonym(dropSynonymDTO);
    }
}

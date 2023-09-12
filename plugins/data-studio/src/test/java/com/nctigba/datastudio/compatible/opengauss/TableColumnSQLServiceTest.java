/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.ConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateTableDTO;
import com.nctigba.datastudio.model.dto.IndexDTO;
import com.nctigba.datastudio.model.query.TablePartitionInfoQuery;
import com.nctigba.datastudio.model.query.TableUnderlyingInfoQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * TableColumnSQLServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class TableColumnSQLServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private TableColumnSQLServiceImpl tableColumnSQLService;
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
    }

    @Test
    public void testCreateTableOnlColumn() {
        TableUnderlyingInfoQuery tableUnderlyingInfoQuery = new TableUnderlyingInfoQuery();
        tableUnderlyingInfoQuery.setTableSpace("sp1");
        tableUnderlyingInfoQuery.setTableName("t1");
        tableUnderlyingInfoQuery.setStorage("row");
        tableUnderlyingInfoQuery.setFillingFactor(90);
        tableUnderlyingInfoQuery.setComment("dd");
        tableUnderlyingInfoQuery.setIsExists(true);
        tableUnderlyingInfoQuery.setIsOids(true);
        tableUnderlyingInfoQuery.setTableType("UNLOGGED");
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTO =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTO.setColumnName("qq");
        creUpdColumnDataDTO.setNewColumnName("wq");
        creUpdColumnDataDTO.setType("int");
        creUpdColumnDataDTO.setOperationType(4);
        creUpdColumnDataDTO.setComment("ss");
        creUpdColumnDataDTO.setIsEmpty(true);
        creUpdColumnDataDTO.setIsOnly(true);
        creUpdColumnDataDTO.setPrecision("");
        creUpdColumnDataDTO.setDefaultValue("");
        TablePartitionInfoQuery tablePartitionInfoQuery = new TablePartitionInfoQuery();
        List<IndexDTO> indexDTOList = new ArrayList<>();
        List<ConstraintDTO> constraintDTOList = new ArrayList<>();
        List<DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO> creUpdColumnDataDTOList = new ArrayList<>();
        creUpdColumnDataDTOList.add(creUpdColumnDataDTO);
        DatabaseCreateTableDTO databaseCreateTableDTO = new DatabaseCreateTableDTO();
        databaseCreateTableDTO.setTableInfo(tableUnderlyingInfoQuery);
        databaseCreateTableDTO.setColumn(creUpdColumnDataDTOList);
        databaseCreateTableDTO.setSchema("s1");
        databaseCreateTableDTO.setPartitionInfo(tablePartitionInfoQuery);
        databaseCreateTableDTO.setIndexs(indexDTOList);
        databaseCreateTableDTO.setConstraints(constraintDTOList);
        databaseCreateTableDTO.setUuid(UUID);
        tableColumnSQLService.createTable(databaseCreateTableDTO);
    }
}

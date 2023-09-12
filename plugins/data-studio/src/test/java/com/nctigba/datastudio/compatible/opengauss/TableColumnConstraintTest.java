/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.ConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintPkDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseIndexDTO;
import com.nctigba.datastudio.model.dto.IndexDTO;
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

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * TableColumnConstraintTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class TableColumnConstraintTest {
    private static final String UUID = "111";

    @InjectMocks
    private TableColumnSQLServiceImpl tableColumnSQLService;
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
    public void testEditConstraint() throws SQLException {
        ConstraintDTO constraintDTO = new ConstraintDTO();
        constraintDTO.setConstraintDef("");
        constraintDTO.setTbName("");
        constraintDTO.setNspName("");
        constraintDTO.setConName("");
        constraintDTO.setColName("");
        constraintDTO.setDescription("");
        constraintDTO.setAttName("");
        constraintDTO.setConDeferrable(true);
        constraintDTO.setOldConName("");
        constraintDTO.setConType("u");
        constraintDTO.setType(1);
        ConstraintDTO constraintDTOTwo = new ConstraintDTO();
        constraintDTOTwo.setConstraintDef("");
        constraintDTOTwo.setTbName("");
        constraintDTOTwo.setNspName("");
        constraintDTOTwo.setConName("");
        constraintDTOTwo.setColName("");
        constraintDTOTwo.setDescription("");
        constraintDTOTwo.setAttName("");
        constraintDTO.setConDeferrable(true);
        constraintDTOTwo.setOldConName("");
        constraintDTOTwo.setConType("1");
        constraintDTOTwo.setType(2);
        ConstraintDTO constraintDTOThree = new ConstraintDTO();
        constraintDTOThree.setConstraintDef("");
        constraintDTOThree.setTbName("");
        constraintDTOThree.setNspName("");
        constraintDTOThree.setConName("");
        constraintDTOThree.setColName("");
        constraintDTOThree.setDescription("");
        constraintDTOThree.setAttName("");
        constraintDTOThree.setOldConName("");
        constraintDTOThree.setConType("1");
        constraintDTOThree.setType(3);
        ConstraintDTO constraintDTOOther = new ConstraintDTO();
        constraintDTOOther.setConstraintDef("");
        constraintDTOOther.setTbName("");
        constraintDTOOther.setNspName("");
        constraintDTOOther.setConName("");
        constraintDTOOther.setColName("");
        constraintDTOOther.setDescription("");
        constraintDTOOther.setAttName("");
        constraintDTOOther.setOldConName("");
        constraintDTOOther.setConType("1");
        constraintDTOOther.setType(0);
        constraintDTOOther.setConDeferrable(true);
        List<ConstraintDTO> list = new ArrayList<>();
        list.add(constraintDTO);
        list.add(constraintDTOTwo);
        list.add(constraintDTOThree);
        list.add(constraintDTOOther);
        DatabaseConstraintDTO databaseConstraintDTO = new DatabaseConstraintDTO();
        databaseConstraintDTO.setConstraints(list);
        databaseConstraintDTO.setWebUser("");
        databaseConstraintDTO.setConnectionName("");
        databaseConstraintDTO.setTableName("");
        databaseConstraintDTO.setSchema("");
        databaseConstraintDTO.setUuid(UUID);
        databaseConstraintDTO.setSequenceName("w1");
        tableColumnSQLService.editConstraint(databaseConstraintDTO);
    }

    @Test
    public void testEditPkConstraint() throws SQLException {
        DatabaseConstraintPkDTO databaseConstraintPkDTO = new DatabaseConstraintPkDTO();
        databaseConstraintPkDTO.setColumn("fd");
        databaseConstraintPkDTO.setTableName("t1");
        databaseConstraintPkDTO.setSchema("s1");
        databaseConstraintPkDTO.setUuid(UUID);
        tableColumnSQLService.editPkConstraint(databaseConstraintPkDTO);
    }

    @Test
    public void testEditIndex() throws SQLException {
        IndexDTO indexDTOOne = new IndexDTO();
        indexDTOOne.setIndexName("g");
        indexDTOOne.setExpression("n");
        indexDTOOne.setUnique(true);
        indexDTOOne.setAmName("a");
        indexDTOOne.setDescription("q");
        indexDTOOne.setType(1);
        indexDTOOne.setAttName("w");
        indexDTOOne.setOldIndexName("e");
        IndexDTO indexDTOTwo = new IndexDTO();
        indexDTOTwo.setIndexName("g");
        indexDTOTwo.setExpression("n");
        indexDTOTwo.setUnique(true);
        indexDTOTwo.setAmName("a");
        indexDTOTwo.setDescription("q");
        indexDTOTwo.setType(2);
        indexDTOTwo.setAttName("w");
        indexDTOTwo.setOldIndexName("e");
        IndexDTO indexDTOThree = new IndexDTO();
        indexDTOThree.setIndexName("g");
        indexDTOThree.setExpression("n");
        indexDTOThree.setUnique(true);
        indexDTOThree.setAmName("a");
        indexDTOThree.setDescription("q");
        indexDTOThree.setType(3);
        indexDTOThree.setAttName("w");
        indexDTOThree.setOldIndexName("e");
        IndexDTO indexDTOOther = new IndexDTO();
        indexDTOOther.setIndexName("g");
        indexDTOOther.setExpression("n");
        indexDTOOther.setUnique(true);
        indexDTOOther.setAmName("a");
        indexDTOOther.setDescription("q");
        indexDTOOther.setType(0);
        indexDTOOther.setAttName("w");
        indexDTOOther.setOldIndexName("e");
        List<IndexDTO> list = new ArrayList<>();
        list.add(indexDTOOne);
        list.add(indexDTOTwo);
        list.add(indexDTOThree);
        list.add(indexDTOOther);
        DatabaseIndexDTO databaseIndexDTO = new DatabaseIndexDTO();
        databaseIndexDTO.setIndexs(list);
        databaseIndexDTO.setTableName("t1");
        databaseIndexDTO.setSchema("s1");
        databaseIndexDTO.setUuid(UUID);
        databaseIndexDTO.setWebUser("");
        databaseIndexDTO.setConnectionName("");
        databaseIndexDTO.setSequenceName("dd");
        tableColumnSQLService.editIndex(databaseIndexDTO);
    }

    @Test
    public void testEditConstraintAdd() throws SQLException {
        ConstraintDTO constraintDTOU = new ConstraintDTO();
        constraintDTOU.setConstraintDef("");
        constraintDTOU.setTbName("");
        constraintDTOU.setNspName("");
        constraintDTOU.setConName("");
        constraintDTOU.setColName("");
        constraintDTOU.setDescription("");
        constraintDTOU.setAttName("");
        constraintDTOU.setConDeferrable(false);
        constraintDTOU.setOldConName("");
        constraintDTOU.setConType("u");
        constraintDTOU.setType(1);
        ConstraintDTO constraintDTOUTwo = new ConstraintDTO();
        constraintDTOUTwo.setConstraintDef("");
        constraintDTOUTwo.setTbName("");
        constraintDTOUTwo.setNspName("");
        constraintDTOUTwo.setConName("");
        constraintDTOUTwo.setColName("");
        constraintDTOUTwo.setDescription("");
        constraintDTOUTwo.setAttName("");
        constraintDTOUTwo.setOldConName("");
        constraintDTOUTwo.setConType("u");
        constraintDTOUTwo.setType(1);
        ConstraintDTO constraintDTOPOne = new ConstraintDTO();
        constraintDTOPOne.setConstraintDef("");
        constraintDTOPOne.setTbName("");
        constraintDTOPOne.setNspName("");
        constraintDTOPOne.setConName("");
        constraintDTOPOne.setColName("");
        constraintDTOPOne.setDescription("");
        constraintDTOPOne.setAttName("");
        constraintDTOPOne.setConDeferrable(false);
        constraintDTOPOne.setOldConName("");
        constraintDTOPOne.setConType("p");
        constraintDTOPOne.setType(1);
        ConstraintDTO constraintDTOPTwo = new ConstraintDTO();
        constraintDTOPTwo.setConstraintDef("");
        constraintDTOPTwo.setTbName("");
        constraintDTOPTwo.setNspName("");
        constraintDTOPTwo.setConName("");
        constraintDTOPTwo.setColName("");
        constraintDTOPTwo.setDescription("");
        constraintDTOPTwo.setAttName("");
        constraintDTOPTwo.setConDeferrable(true);
        constraintDTOPTwo.setOldConName("");
        constraintDTOPTwo.setConType("p");
        constraintDTOPTwo.setType(1);
        ConstraintDTO constraintDTOCOne = new ConstraintDTO();
        constraintDTOCOne.setConstraintDef("ddd");
        constraintDTOCOne.setTbName("");
        constraintDTOCOne.setNspName("");
        constraintDTOCOne.setConName("");
        constraintDTOCOne.setColName("");
        constraintDTOCOne.setDescription("");
        constraintDTOCOne.setAttName("");
        constraintDTOCOne.setConDeferrable(false);
        constraintDTOCOne.setOldConName("");
        constraintDTOCOne.setConType("c");
        constraintDTOCOne.setType(1);
        ConstraintDTO constraintDTOCTwo = new ConstraintDTO();
        constraintDTOCTwo.setConstraintDef("check");
        constraintDTOCTwo.setTbName("");
        constraintDTOCTwo.setNspName("");
        constraintDTOCTwo.setConName("");
        constraintDTOCTwo.setColName("");
        constraintDTOCTwo.setDescription("");
        constraintDTOCTwo.setAttName("");
        constraintDTOCTwo.setOldConName("");
        constraintDTOCTwo.setConType("c");
        constraintDTOCTwo.setType(1);
        ConstraintDTO constraintDTOF = new ConstraintDTO();
        constraintDTOF.setConstraintDef("");
        constraintDTOF.setTbName("");
        constraintDTOF.setNspName("");
        constraintDTOF.setConName("");
        constraintDTOF.setColName("");
        constraintDTOF.setDescription("");
        constraintDTOF.setAttName("");
        constraintDTOF.setOldConName("");
        constraintDTOF.setConType("f");
        constraintDTOF.setType(1);
        ConstraintDTO constraintDTOS = new ConstraintDTO();
        constraintDTOS.setConstraintDef("");
        constraintDTOS.setTbName("");
        constraintDTOS.setNspName("");
        constraintDTOS.setConName("");
        constraintDTOS.setColName("");
        constraintDTOS.setDescription("");
        constraintDTOS.setAttName("");
        constraintDTOS.setOldConName("");
        constraintDTOS.setConType("s");
        constraintDTOS.setType(1);
        ConstraintDTO constraintDTOOther = new ConstraintDTO();
        constraintDTOOther.setConstraintDef("sddd");
        constraintDTOOther.setTbName("");
        constraintDTOOther.setNspName("");
        constraintDTOOther.setConName("");
        constraintDTOOther.setColName("");
        constraintDTOOther.setDescription("ww");
        constraintDTOOther.setAttName("");
        constraintDTOOther.setOldConName("");
        constraintDTOOther.setConType("f");
        constraintDTOOther.setType(1);
        List<ConstraintDTO> list = new ArrayList<>();
        list.add(constraintDTOCOne);
        list.add(constraintDTOF);
        list.add(constraintDTOCTwo);
        list.add(constraintDTOPOne);
        list.add(constraintDTOUTwo);
        list.add(constraintDTOU);
        list.add(constraintDTOS);
        list.add(constraintDTOPTwo);
        DatabaseConstraintDTO databaseConstraintDTO = new DatabaseConstraintDTO();
        databaseConstraintDTO.setConstraints(list);
        databaseConstraintDTO.setWebUser("");
        databaseConstraintDTO.setConnectionName("");
        databaseConstraintDTO.setTableName("");
        databaseConstraintDTO.setSchema("");
        databaseConstraintDTO.setUuid(UUID);
        databaseConstraintDTO.setSequenceName("w1");
        tableColumnSQLService.editConstraint(databaseConstraintDTO);
    }

    @Test
    public void testTableColumnUpdateSQL() throws SQLException {
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO columnDataDTO = new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        columnDataDTO.setColumnName("fd");
        columnDataDTO.setDefaultValue("t1");
        columnDataDTO.setPrecision("s1");
        columnDataDTO.setNewColumnName("fd");
        columnDataDTO.setComment("uuid");
        columnDataDTO.setIsOnly(true);
        columnDataDTO.setIsEmpty(true);
        columnDataDTO.setOperationType(1);
        columnDataDTO.setScope("uuid");
        columnDataDTO.setType("int");
        tableColumnSQLService.tableColumnUpdateSQL(columnDataDTO, "s1", "t1", UUID);
    }

    @Test
    public void testTableColumnUpdateSQLAdd() throws SQLException {
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO columnDataDTO = new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        columnDataDTO.setColumnName("fd");
        columnDataDTO.setDefaultValue("t1");
        columnDataDTO.setPrecision("s1");
        columnDataDTO.setComment("uuid");
        columnDataDTO.setIsOnly(true);
        columnDataDTO.setIsEmpty(true);
        columnDataDTO.setOperationType(1);
        columnDataDTO.setScope("uuid");
        columnDataDTO.setType("int");
        tableColumnSQLService.tableColumnUpdateSQL(columnDataDTO, "s1", "t1", UUID);
    }
}

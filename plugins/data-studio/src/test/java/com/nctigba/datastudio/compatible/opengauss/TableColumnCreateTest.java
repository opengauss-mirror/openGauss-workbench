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
 *  TableColumnCreateTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/compatible/opengauss/TableColumnCreateTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

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
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * TableColumnSQLServiceImpl
 *
 * @since 2023-09-06
 */
@RunWith(MockitoJUnitRunner.class)
public class TableColumnCreateTest {
    private static final String UUID = "111";

    @InjectMocks
    private TableColumnSQLServiceImpl tableColumnSQLService;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setType("openGauss");
        conMap.put(UUID, connectionDTO);
    }

    @Test
    public void testCreateTableIsExists() {
        TableUnderlyingInfoQuery tableUnderlyingInfoQuery = new TableUnderlyingInfoQuery();
        tableUnderlyingInfoQuery.setTableSpace("sp1");
        tableUnderlyingInfoQuery.setTableName("t1");
        tableUnderlyingInfoQuery.setStorage("row");
        tableUnderlyingInfoQuery.setFillingFactor(100);
        tableUnderlyingInfoQuery.setComment("");
        tableUnderlyingInfoQuery.setIsExists(true);
        tableUnderlyingInfoQuery.setIsOids(false);
        tableUnderlyingInfoQuery.setTableType("wwww");
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTO =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTO.setColumnName("qq");
        creUpdColumnDataDTO.setNewColumnName("wq");
        creUpdColumnDataDTO.setType("int");
        creUpdColumnDataDTO.setOperationType(4);
        creUpdColumnDataDTO.setComment("ss");
        creUpdColumnDataDTO.setIsEmpty(false);
        creUpdColumnDataDTO.setIsOnly(true);
        creUpdColumnDataDTO.setPrecision("");
        creUpdColumnDataDTO.setDefaultValue("");
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTOTwo =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTOTwo.setColumnName("qq");
        creUpdColumnDataDTOTwo.setNewColumnName("wq");
        creUpdColumnDataDTOTwo.setType("int");
        creUpdColumnDataDTOTwo.setOperationType(4);
        creUpdColumnDataDTOTwo.setComment("ss");
        creUpdColumnDataDTOTwo.setIsEmpty(false);
        creUpdColumnDataDTOTwo.setIsOnly(true);
        creUpdColumnDataDTOTwo.setPrecision("");
        creUpdColumnDataDTOTwo.setDefaultValue("");
        ConstraintDTO constraintDTO = new ConstraintDTO();
        constraintDTO.setDescription("gg");
        constraintDTO.setType(1);
        constraintDTO.setConstraintDef("h5");
        constraintDTO.setAttName("a3");
        constraintDTO.setColName("f4");
        constraintDTO.setConName("y4");
        constraintDTO.setNspName("t3");
        constraintDTO.setTbName("e");
        ConstraintDTO constraintDTOTwo = new ConstraintDTO();
        constraintDTOTwo.setDescription("");
        constraintDTOTwo.setType(1);
        constraintDTOTwo.setConstraintDef("h5");
        constraintDTOTwo.setAttName("a3");
        constraintDTOTwo.setColName("f4");
        constraintDTOTwo.setConName("y4");
        constraintDTOTwo.setNspName("t3");
        constraintDTOTwo.setTbName("e");
        IndexDTO indexDTO = new IndexDTO();
        indexDTO.setDescription("qq");
        indexDTO.setAmName("");
        indexDTO.setIndexName("h5");
        indexDTO.setUnique(true);
        indexDTO.setAttName("ss");
        indexDTO.setExpression("");
        IndexDTO indexDTOTwo = new IndexDTO();
        indexDTOTwo.setDescription("");
        indexDTOTwo.setAmName("e2");
        indexDTOTwo.setIndexName("r4");
        indexDTOTwo.setUnique(false);
        indexDTOTwo.setAttName("t7");
        indexDTOTwo.setExpression("i8");
        List<IndexDTO> indexDTOList = new ArrayList<>();
        indexDTOList.add(indexDTO);
        indexDTOList.add(indexDTOTwo);
        List<ConstraintDTO> constraintDTOList = new ArrayList<>();
        constraintDTOList.add(constraintDTO);
        constraintDTOList.add(constraintDTOTwo);
        List<DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO> creUpdColumnDataDTOList = new ArrayList<>();
        creUpdColumnDataDTOList.add(creUpdColumnDataDTO);
        creUpdColumnDataDTOList.add(creUpdColumnDataDTOTwo);
        DatabaseCreateTableDTO databaseCreateTableDTO = new DatabaseCreateTableDTO();
        databaseCreateTableDTO.setTableInfo(tableUnderlyingInfoQuery);
        databaseCreateTableDTO.setColumn(creUpdColumnDataDTOList);
        databaseCreateTableDTO.setSchema("s1");
        TablePartitionInfoQuery tablePartitionInfoQuery = new TablePartitionInfoQuery();
        databaseCreateTableDTO.setPartitionInfo(tablePartitionInfoQuery);
        databaseCreateTableDTO.setIndexs(indexDTOList);
        databaseCreateTableDTO.setConstraints(constraintDTOList);
        databaseCreateTableDTO.setUuid(UUID);
        tableColumnSQLService.createTable(databaseCreateTableDTO);
    }

    @Test
    public void testCreateTableIsOids() {
        TableUnderlyingInfoQuery tableUnderlyingInfoQuery = new TableUnderlyingInfoQuery();
        tableUnderlyingInfoQuery.setTableSpace("sp1");
        tableUnderlyingInfoQuery.setTableName("t1");
        tableUnderlyingInfoQuery.setStorage("row");
        tableUnderlyingInfoQuery.setFillingFactor(90);
        tableUnderlyingInfoQuery.setComment("");
        tableUnderlyingInfoQuery.setIsExists(false);
        tableUnderlyingInfoQuery.setIsOids(false);
        tableUnderlyingInfoQuery.setTableType("wwww");
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTO =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTO.setColumnName("qq");
        creUpdColumnDataDTO.setNewColumnName("wq");
        creUpdColumnDataDTO.setType("int");
        creUpdColumnDataDTO.setOperationType(4);
        creUpdColumnDataDTO.setComment("ss");
        creUpdColumnDataDTO.setIsEmpty(false);
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

    @Test
    public void testCreateTableExists() {
        TableUnderlyingInfoQuery tableUnderlyingInfoQuery = new TableUnderlyingInfoQuery();
        tableUnderlyingInfoQuery.setTableSpace("sp1");
        tableUnderlyingInfoQuery.setTableName("t1");
        tableUnderlyingInfoQuery.setStorage("COLUMN");
        tableUnderlyingInfoQuery.setFillingFactor(100);
        tableUnderlyingInfoQuery.setComment("");
        tableUnderlyingInfoQuery.setIsExists(false);
        tableUnderlyingInfoQuery.setIsOids(true);
        tableUnderlyingInfoQuery.setTableType("UNLOGGED");
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTO =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTO.setColumnName("qq");
        creUpdColumnDataDTO.setNewColumnName("wq");
        creUpdColumnDataDTO.setType("int");
        creUpdColumnDataDTO.setOperationType(4);
        creUpdColumnDataDTO.setComment("ss");
        creUpdColumnDataDTO.setIsEmpty(false);
        creUpdColumnDataDTO.setIsOnly(true);
        creUpdColumnDataDTO.setPrecision("");
        creUpdColumnDataDTO.setDefaultValue("");
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTOTwo =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTOTwo.setColumnName("qq");
        creUpdColumnDataDTOTwo.setNewColumnName("wq");
        creUpdColumnDataDTOTwo.setType("int");
        creUpdColumnDataDTOTwo.setOperationType(4);
        creUpdColumnDataDTOTwo.setComment("ss");
        creUpdColumnDataDTOTwo.setIsEmpty(false);
        creUpdColumnDataDTOTwo.setIsOnly(true);
        creUpdColumnDataDTOTwo.setPrecision("");
        creUpdColumnDataDTOTwo.setDefaultValue("");
        ConstraintDTO constraintDTO = new ConstraintDTO();
        constraintDTO.setDescription("gg");
        constraintDTO.setType(1);
        constraintDTO.setConstraintDef("h5");
        constraintDTO.setAttName("a3");
        constraintDTO.setColName("f4");
        constraintDTO.setConName("y4");
        constraintDTO.setNspName("t3");
        constraintDTO.setTbName("e");
        ConstraintDTO constraintDTOTwo = new ConstraintDTO();
        constraintDTOTwo.setDescription("");
        constraintDTOTwo.setType(1);
        constraintDTOTwo.setConstraintDef("h5");
        constraintDTOTwo.setAttName("a3");
        constraintDTOTwo.setColName("f4");
        constraintDTOTwo.setConName("y4");
        constraintDTOTwo.setNspName("t3");
        constraintDTOTwo.setTbName("e");
        IndexDTO indexDTO = new IndexDTO();
        indexDTO.setDescription("qq");
        indexDTO.setAmName("");
        indexDTO.setIndexName("h5");
        indexDTO.setUnique(true);
        indexDTO.setAttName("ss");
        indexDTO.setExpression("");
        IndexDTO indexDTOTwo = new IndexDTO();
        indexDTOTwo.setDescription("");
        indexDTOTwo.setAmName("e2");
        indexDTOTwo.setIndexName("r4");
        indexDTOTwo.setUnique(false);
        indexDTOTwo.setAttName("t7");
        indexDTOTwo.setExpression("i8");
        List<IndexDTO> indexDTOList = new ArrayList<>();
        indexDTOList.add(indexDTO);
        List<ConstraintDTO> constraintDTOList = new ArrayList<>();
        constraintDTOList.add(constraintDTO);
        constraintDTOList.add(constraintDTOTwo);
        List<DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO> creUpdColumnDataDTOList = new ArrayList<>();
        creUpdColumnDataDTOList.add(creUpdColumnDataDTO);
        creUpdColumnDataDTOList.add(creUpdColumnDataDTOTwo);
        DatabaseCreateTableDTO databaseCreateTableDTO = new DatabaseCreateTableDTO();
        databaseCreateTableDTO.setTableInfo(tableUnderlyingInfoQuery);
        databaseCreateTableDTO.setColumn(creUpdColumnDataDTOList);
        databaseCreateTableDTO.setSchema("s1");
        databaseCreateTableDTO.setPartitionInfo(new TablePartitionInfoQuery());
        databaseCreateTableDTO.setIndexs(indexDTOList);
        databaseCreateTableDTO.setConstraints(constraintDTOList);
        databaseCreateTableDTO.setUuid(UUID);
        tableColumnSQLService.createTable(databaseCreateTableDTO);
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

    @Test
    public void testCreateTableOnlColumnNotIsOids() {
        TableUnderlyingInfoQuery tableUnderlyingInfoQuery = new TableUnderlyingInfoQuery();
        tableUnderlyingInfoQuery.setTableSpace("sp1");
        tableUnderlyingInfoQuery.setTableName("t1");
        tableUnderlyingInfoQuery.setStorage("row");
        tableUnderlyingInfoQuery.setFillingFactor(100);
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

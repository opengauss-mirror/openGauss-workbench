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
 *  TableDataServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/service/impl/sql/TableDataServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.TableColumnSQLService;
import com.nctigba.datastudio.compatible.TableObjectSQLService;
import com.nctigba.datastudio.compatible.opengauss.TableColumnSQLServiceImpl;
import com.nctigba.datastudio.compatible.opengauss.TableObjectSQLServiceImpl;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.dao.ResultSetMapDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.ConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintPkDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateTableDTO;
import com.nctigba.datastudio.model.dto.DatabaseIndexDTO;
import com.nctigba.datastudio.model.dto.IndexDTO;
import com.nctigba.datastudio.model.dto.TableAlterDTO;
import com.nctigba.datastudio.model.dto.TableAttributeDTO;
import com.nctigba.datastudio.model.dto.TableNameDTO;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.model.query.TableDataQuery;
import com.nctigba.datastudio.model.query.TablePartitionInfoQuery;
import com.nctigba.datastudio.model.query.TableUnderlyingInfoQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * TableDataServiceTest
 *
 * @since 2023-07-04
 */
@RunWith(MockitoJUnitRunner.class)
public class TableDataServiceTest {
    private static final String UUID = "111";

    @InjectMocks
    private TableDataServiceImpl tableDataService;
    @Mock
    private ResultSetMapDAO resultSetMapDAO;
    @Mock
    private Map<String, TableColumnSQLService> tableColumnSQLService;
    @Mock
    private Map<String, TableObjectSQLService> tableObjectSQLService;
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
    @Mock
    private DebugUtils debugUtils;
    @Mock
    private TableColumnSQLServiceImpl tableColumnSQLServiceImpl;
    @Mock
    private TableObjectSQLServiceImpl tableObjectSQLServiceImpl;
    @Mock
    private MockHttpServletResponse response;
    @Mock
    private ServletOutputStream outputStream;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setType("openGauss");
        conMap.put(UUID, connectionDTO);
        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE))
                .thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("qqq");
        when(mockResultSet.getObject(anyString())).thenReturn("test");
    }

    @Test
    public void testTableColumn() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableColumnSQLServiceImpl());
        tableDataService.setTableColumnSQLService(serviceArrayList);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        SelectDataQuery selectDataQuery = new SelectDataQuery();
        selectDataQuery.setOid("2");
        selectDataQuery.setTableName("t1");
        selectDataQuery.setSchema("s1");
        selectDataQuery.setUuid(UUID);
        tableDataService.tableColumn(selectDataQuery);
    }

    @Test
    public void testTableDataClose() throws SQLException {
        tableDataService.tableDataClose("222");
        tableDataService.tableDataClose(null);
    }

    @Test
    public void testTableData() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        tableDataService.setTableObjectSQLService(serviceArrayList);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        TableDataQuery tableDataQuery = new TableDataQuery();
        tableDataQuery.setTableName("t1");
        tableDataQuery.setSchema("s1");
        tableDataQuery.setUuid(UUID);
        tableDataQuery.setPageNum(10);
        tableDataQuery.setPageSize(2);
        tableDataQuery.setWinId("w1w1w1w1");
        tableDataService.tableData(tableDataQuery);
    }

    @Test
    public void testTableIndex() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableColumnSQLServiceImpl());
        tableDataService.setTableColumnSQLService(serviceArrayList);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        SelectDataQuery selectDataQuery = new SelectDataQuery();
        selectDataQuery.setTableName("t1");
        selectDataQuery.setSchema("s1");
        selectDataQuery.setUuid(UUID);
        selectDataQuery.setOid("1");
        tableDataService.tableIndex(selectDataQuery);
    }

    @Test
    public void testTableEditIndex() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        when(tableColumnSQLServiceImpl.type()).thenReturn("openGauss");
        serviceArrayList.add(tableColumnSQLServiceImpl);
        tableDataService.setTableColumnSQLService(serviceArrayList);
        List<IndexDTO> list = new ArrayList<>();
        DatabaseIndexDTO databaseIndexDTO = new DatabaseIndexDTO();
        databaseIndexDTO.setTableName("t1");
        databaseIndexDTO.setSchema("s1");
        databaseIndexDTO.setUuid(UUID);
        databaseIndexDTO.setIndexs(list);
        databaseIndexDTO.setConnectionName("1");
        databaseIndexDTO.setSequenceName("1");
        databaseIndexDTO.setWebUser("1");
        tableDataService.tableEditIndex(databaseIndexDTO);
    }

    @Test
    public void testTableCreate() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        when(tableColumnSQLServiceImpl.type()).thenReturn("openGauss");
        serviceArrayList.add(tableColumnSQLServiceImpl);
        tableDataService.setTableColumnSQLService(serviceArrayList);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        List<IndexDTO> listIndexs = new ArrayList<>();
        TableUnderlyingInfoQuery tableUnderlyingInfoQuery = new TableUnderlyingInfoQuery();
        List<DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO> listColumn = new ArrayList<>();
        List<ConstraintDTO> listConstraints = new ArrayList<>();
        DatabaseCreateTableDTO createTableDTO = new DatabaseCreateTableDTO();
        TablePartitionInfoQuery tablePartitionInfoQuery = new TablePartitionInfoQuery();
        createTableDTO.setTableInfo(tableUnderlyingInfoQuery);
        createTableDTO.setSchema("s1");
        createTableDTO.setUuid(UUID);
        createTableDTO.setIndexs(listIndexs);
        createTableDTO.setColumn(listColumn);
        createTableDTO.setConstraints(listConstraints);
        createTableDTO.setPartitionInfo(tablePartitionInfoQuery);
        tableDataService.tableCreate(createTableDTO);
    }

    @Test
    public void testReturnTableCreate() {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        when(tableColumnSQLServiceImpl.type()).thenReturn("openGauss");
        serviceArrayList.add(tableColumnSQLServiceImpl);
        tableDataService.setTableColumnSQLService(serviceArrayList);
        List<IndexDTO> listIndexs = new ArrayList<>();
        TableUnderlyingInfoQuery tableUnderlyingInfoQuery = new TableUnderlyingInfoQuery();
        List<DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO> listColumn = new ArrayList<>();
        List<ConstraintDTO> listConstraints = new ArrayList<>();
        DatabaseCreateTableDTO createTableDTO = new DatabaseCreateTableDTO();
        TablePartitionInfoQuery tablePartitionInfoQuery = new TablePartitionInfoQuery();
        createTableDTO.setTableInfo(tableUnderlyingInfoQuery);
        createTableDTO.setSchema("s1");
        createTableDTO.setUuid(UUID);
        createTableDTO.setIndexs(listIndexs);
        createTableDTO.setColumn(listColumn);
        createTableDTO.setConstraints(listConstraints);
        createTableDTO.setPartitionInfo(tablePartitionInfoQuery);
        tableDataService.returnTableCreate(createTableDTO);
    }

    @Test
    public void testTableConstraint() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableColumnSQLServiceImpl());
        tableDataService.setTableColumnSQLService(serviceArrayList);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        SelectDataQuery selectDataQuery = new SelectDataQuery();
        selectDataQuery.setTableName("t1");
        selectDataQuery.setSchema("s1");
        selectDataQuery.setUuid(UUID);
        selectDataQuery.setOid("1");
        tableDataService.tableConstraint(selectDataQuery);
    }

    @Test
    public void testTableEditConstraint() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        when(tableColumnSQLServiceImpl.type()).thenReturn("openGauss");
        serviceArrayList.add(tableColumnSQLServiceImpl);
        tableDataService.setTableColumnSQLService(serviceArrayList);
        List<ConstraintDTO> list = new ArrayList<>();
        DatabaseConstraintDTO databaseConstraintDTO = new DatabaseConstraintDTO();
        databaseConstraintDTO.setTableName("t1");
        databaseConstraintDTO.setSchema("s1");
        databaseConstraintDTO.setUuid(UUID);
        databaseConstraintDTO.setConstraints(list);
        databaseConstraintDTO.setConnectionName("1");
        databaseConstraintDTO.setSequenceName("1");
        databaseConstraintDTO.setWebUser("1");
        tableDataService.tableEditConstraint(databaseConstraintDTO);
    }

    @Test
    public void testTableEditPkConstraint() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        when(tableColumnSQLServiceImpl.type()).thenReturn("openGauss");
        serviceArrayList.add(tableColumnSQLServiceImpl);
        tableDataService.setTableColumnSQLService(serviceArrayList);
        DatabaseConstraintPkDTO databaseConstraintDTO = new DatabaseConstraintPkDTO();
        databaseConstraintDTO.setTableName("t1");
        databaseConstraintDTO.setSchema("s1");
        databaseConstraintDTO.setUuid(UUID);
        databaseConstraintDTO.setColumn("");
        tableDataService.tableEditPkConstraint(databaseConstraintDTO);
    }

    @Test
    public void testTableDdl() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        when(tableObjectSQLServiceImpl.type()).thenReturn("openGauss");
        serviceArrayList.add(tableObjectSQLServiceImpl);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        SelectDataQuery selectDataQuery = new SelectDataQuery();
        selectDataQuery.setTableName("t1");
        selectDataQuery.setSchema("s1");
        selectDataQuery.setUuid(UUID);
        selectDataQuery.setOid("1");
        tableDataService.tableDdl(selectDataQuery);
    }

    @Test
    public void testTableColumnEdit() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableColumnSQLServiceImpl());
        tableDataService.setTableColumnSQLService(serviceArrayList);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        List<DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO> list = new ArrayList<>();
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTOOne =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTOOne.setOperationType(1);
        creUpdColumnDataDTOOne.setNewColumnName("sss");
        creUpdColumnDataDTOOne.setType("char");
        list.add(creUpdColumnDataDTOOne);
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTOTwo =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTOTwo.setOperationType(2);
        creUpdColumnDataDTOTwo.setType("char");
        creUpdColumnDataDTOTwo.setColumnName("sss");
        list.add(creUpdColumnDataDTOTwo);
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTOThre =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTOThre.setOperationType(3);
        creUpdColumnDataDTOThre.setColumnName("cc1");
        creUpdColumnDataDTOThre.setType("char");
        creUpdColumnDataDTOThre.setNewColumnName("sss");
        list.add(creUpdColumnDataDTOThre);
        DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO creUpdColumnDataDTOFour =
                new DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO();
        creUpdColumnDataDTOFour.setOperationType(4);
        creUpdColumnDataDTOFour.setColumnName("cc1");
        creUpdColumnDataDTOFour.setType("char");
        creUpdColumnDataDTOFour.setNewColumnName("sss");
        list.add(creUpdColumnDataDTOFour);
        DatabaseCreUpdColumnDTO creUpdColumnDTO = new DatabaseCreUpdColumnDTO();
        creUpdColumnDTO.setTableName("t1");
        creUpdColumnDTO.setSchema("s1");
        creUpdColumnDTO.setUuid(UUID);
        creUpdColumnDTO.setOid("1");
        creUpdColumnDTO.setData(list);
        tableDataService.tableColumnEdit(creUpdColumnDTO);
    }

    @Test
    public void testTableTruncate() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableNameDTO tableNameDTO = new TableNameDTO();
        tableNameDTO.setTableName("t1");
        tableNameDTO.setSchema("s1");
        tableNameDTO.setUuid(UUID);
        tableNameDTO.setOid("1");
        tableDataService.tableTruncate(tableNameDTO);
    }

    @Test
    public void testTableVacuum() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableNameDTO tableNameDTO = new TableNameDTO();
        tableNameDTO.setTableName("t1");
        tableNameDTO.setSchema("s1");
        tableNameDTO.setUuid(UUID);
        tableNameDTO.setOid("1");
        tableDataService.tableVacuum(tableNameDTO);
    }

    @Test
    public void testTableReindex() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableNameDTO tableNameDTO = new TableNameDTO();
        tableNameDTO.setTableName("t1");
        tableNameDTO.setSchema("s1");
        tableNameDTO.setUuid(UUID);
        tableNameDTO.setOid("1");
        tableDataService.tableReindex(tableNameDTO);
    }

    @Test
    public void testTableRename() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableAlterDTO tableAlterDTO = new TableAlterDTO();
        tableAlterDTO.setTableName("t1");
        tableAlterDTO.setSchema("s1");
        tableAlterDTO.setUuid(UUID);
        tableAlterDTO.setGeneralPurpose("1");
        tableDataService.tableRename(tableAlterDTO);
    }

    @Test
    public void testTableComment() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableAlterDTO tableAlterDTO = new TableAlterDTO();
        tableAlterDTO.setTableName("t1");
        tableAlterDTO.setSchema("s1");
        tableAlterDTO.setUuid(UUID);
        tableAlterDTO.setGeneralPurpose("1");
        tableDataService.tableComment(tableAlterDTO);
    }

    @Test
    public void testTableAlterSchema() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableAlterDTO tableAlterDTO = new TableAlterDTO();
        tableAlterDTO.setTableName("t1");
        tableAlterDTO.setSchema("s1");
        tableAlterDTO.setUuid(UUID);
        tableAlterDTO.setGeneralPurpose("1");
        tableDataService.tableAlterSchema(tableAlterDTO);
    }

    @Test
    public void testTableDrop() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableNameDTO tableNameDTO = new TableNameDTO();
        tableNameDTO.setTableName("t1");
        tableNameDTO.setSchema("s1");
        tableNameDTO.setUuid(UUID);
        tableNameDTO.setOid("1");
        tableDataService.tableDrop(tableNameDTO);
    }

    @Test
    public void testTableAlterTablespace() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableAlterDTO tableAlterDTO = new TableAlterDTO();
        tableAlterDTO.setTableName("t1");
        tableAlterDTO.setSchema("s1");
        tableAlterDTO.setUuid(UUID);
        tableAlterDTO.setGeneralPurpose("1");
        tableDataService.tableAlterTablespace(tableAlterDTO);
    }

    @Test
    public void testTableSequence() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableNameDTO tableNameDTO = new TableNameDTO();
        tableNameDTO.setTableName("t1");
        tableNameDTO.setSchema("s1");
        tableNameDTO.setUuid(UUID);
        tableNameDTO.setOid("1");
        tableDataService.tableSequence(tableNameDTO);
    }

    @Test
    public void testTableAttribute() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        when(tableObjectSQLServiceImpl.type()).thenReturn("openGauss");
        serviceArrayList.add(tableObjectSQLServiceImpl);
        tableDataService.setTableObjectSQLService(serviceArrayList);
        TableAttributeDTO tableAttributeDTO = new TableAttributeDTO();
        tableAttributeDTO.setTableName("t1");
        tableAttributeDTO.setSchema("s1");
        tableAttributeDTO.setUuid(UUID);
        tableAttributeDTO.setOid("1");
        tableAttributeDTO.setTableType("y");
        tableDataService.tableAttribute(tableAttributeDTO);
    }

    @Test
    public void testTableDataEdit() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableColumnSQLServiceImpl());
        tableDataService.setTableColumnSQLService(serviceArrayList);
        MockedStatic<LocaleStringUtils> staticUtilsMockedStatic = Mockito.mockStatic(LocaleStringUtils.class);
        staticUtilsMockedStatic.when(() -> LocaleStringUtils.transLanguage(anyString()))
                .thenReturn("123");

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        TableDataEditQuery request = new TableDataEditQuery();
        request.setUuid(UUID);
        request.setSchema("s1");
        request.setTableName("t1");
        tableDataService.tableDataEdit(request);
    }

    @Test
    public void testTableDataEdit2() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableColumnSQLServiceImpl());
        tableDataService.setTableColumnSQLService(serviceArrayList);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        TableDataEditQuery.TableDataDTO dataDTO1 = new TableDataEditQuery.TableDataDTO();
        List<TableDataEditQuery.TableDataDTOColumn> lineList1 = new ArrayList<>();
        TableDataEditQuery.TableDataDTOColumn tableDataDTOColumn1 = new TableDataEditQuery.TableDataDTOColumn();
        tableDataDTOColumn1.setTypeNum(1);
        tableDataDTOColumn1.setTypeName("varchar");
        tableDataDTOColumn1.setColumnData("aaa");
        tableDataDTOColumn1.setColumnName("column1");
        tableDataDTOColumn1.setOldColumnData("bbb");
        lineList1.add(tableDataDTOColumn1);
        dataDTO1.setLine(lineList1);
        dataDTO1.setOperationType("INSERT");
        TableDataEditQuery.TableDataDTO dataDTO2 = new TableDataEditQuery.TableDataDTO();
        List<TableDataEditQuery.TableDataDTOColumn> lineList2 = new ArrayList<>();
        TableDataEditQuery.TableDataDTOColumn tableDataDTOColumn2 = new TableDataEditQuery.TableDataDTOColumn();
        tableDataDTOColumn2.setTypeNum(1);
        tableDataDTOColumn2.setTypeName("varchar");
        tableDataDTOColumn2.setColumnData("aaa");
        tableDataDTOColumn2.setColumnName("column1");
        tableDataDTOColumn2.setOldColumnData("bbb");
        lineList2.add(tableDataDTOColumn2);
        dataDTO2.setLine(lineList2);
        dataDTO2.setOperationType("UPDATE");
        TableDataEditQuery.TableDataDTO dataDTO3 = new TableDataEditQuery.TableDataDTO();
        List<TableDataEditQuery.TableDataDTOColumn> lineList3 = new ArrayList<>();
        TableDataEditQuery.TableDataDTOColumn tableDataDTOColumn3 = new TableDataEditQuery.TableDataDTOColumn();
        tableDataDTOColumn3.setTypeNum(1);
        tableDataDTOColumn3.setTypeName("varchar");
        tableDataDTOColumn3.setColumnData("aaa");
        tableDataDTOColumn3.setColumnName("column1");
        tableDataDTOColumn3.setOldColumnData("bbb");
        lineList3.add(tableDataDTOColumn3);
        dataDTO3.setLine(lineList3);
        dataDTO3.setOperationType("DELETE");
        TableDataEditQuery.TableDataDTO dataDTO4 = new TableDataEditQuery.TableDataDTO();
        dataDTO4.setOperationType("QUERY");
        List<TableDataEditQuery.TableDataDTO> list = new ArrayList<>();
        list.add(dataDTO1);
        list.add(dataDTO2);
        list.add(dataDTO3);
        list.add(dataDTO4);

        TableDataEditQuery request = new TableDataEditQuery();
        request.setUuid(UUID);
        request.setSchema("s1");
        request.setTableName("t1");
        request.setData(list);
        tableDataService.tableDataEdit(request);
    }

    @Test
    public void testTableDataEdit3() throws SQLException {
        List<TableColumnSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableColumnSQLServiceImpl());
        tableDataService.setTableColumnSQLService(serviceArrayList);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        TableDataEditQuery.TableDataDTO dataDTO1 = new TableDataEditQuery.TableDataDTO();
        List<TableDataEditQuery.TableDataDTOColumn> lineList1 = new ArrayList<>();
        TableDataEditQuery.TableDataDTOColumn tableDataDTOColumn1 = new TableDataEditQuery.TableDataDTOColumn();
        tableDataDTOColumn1.setTypeNum(1);
        tableDataDTOColumn1.setTypeName("varchar");
        tableDataDTOColumn1.setColumnData("aaa");
        tableDataDTOColumn1.setColumnName("column1");
        tableDataDTOColumn1.setOldColumnData("bbb");
        lineList1.add(tableDataDTOColumn1);
        lineList1.add(tableDataDTOColumn1);
        dataDTO1.setLine(lineList1);
        dataDTO1.setOperationType("INSERT");
        TableDataEditQuery.TableDataDTO dataDTO2 = new TableDataEditQuery.TableDataDTO();
        List<TableDataEditQuery.TableDataDTOColumn> lineList2 = new ArrayList<>();
        TableDataEditQuery.TableDataDTOColumn tableDataDTOColumn2 = new TableDataEditQuery.TableDataDTOColumn();
        tableDataDTOColumn2.setTypeNum(1);
        tableDataDTOColumn2.setTypeName("varchar");
        tableDataDTOColumn2.setColumnData("aaa");
        tableDataDTOColumn2.setColumnName("column1");
        tableDataDTOColumn2.setOldColumnData("bbb");
        lineList2.add(tableDataDTOColumn2);
        lineList2.add(tableDataDTOColumn2);
        dataDTO2.setLine(lineList2);
        dataDTO2.setOperationType("UPDATE");
        TableDataEditQuery.TableDataDTO dataDTO3 = new TableDataEditQuery.TableDataDTO();
        List<TableDataEditQuery.TableDataDTOColumn> lineList3 = new ArrayList<>();
        TableDataEditQuery.TableDataDTOColumn tableDataDTOColumn3 = new TableDataEditQuery.TableDataDTOColumn();
        tableDataDTOColumn3.setTypeNum(1);
        tableDataDTOColumn3.setTypeName("varchar");
        tableDataDTOColumn3.setColumnData("aaa");
        tableDataDTOColumn3.setColumnName("column1");
        tableDataDTOColumn3.setOldColumnData("bbb");
        lineList3.add(tableDataDTOColumn3);
        lineList3.add(tableDataDTOColumn3);
        dataDTO3.setLine(lineList3);
        dataDTO3.setOperationType("DELETE");
        TableDataEditQuery.TableDataDTO dataDTO4 = new TableDataEditQuery.TableDataDTO();
        dataDTO4.setOperationType("QUERY");
        List<TableDataEditQuery.TableDataDTO> list = new ArrayList<>();
        list.add(dataDTO1);
        list.add(dataDTO2);
        list.add(dataDTO3);
        list.add(dataDTO4);

        TableDataEditQuery request = new TableDataEditQuery();
        request.setUuid(UUID);
        request.setSchema("s1");
        request.setTableName("t1");
        request.setData(list);
        tableDataService.tableDataEdit(request);
    }

    @Test
    public void testTableAnalyze() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        tableDataService.setTableObjectSQLService(serviceArrayList);

        when(mockConnection.createStatement()).thenReturn(mockStatement);

        TableNameDTO tableNameDTO = new TableNameDTO();
        tableNameDTO.setTableName("t1");
        tableNameDTO.setSchema("s1");
        tableNameDTO.setUuid(UUID);
        tableNameDTO.setOid("1");
        tableDataService.tableAnalyze(tableNameDTO);
    }

    @Test
    public void testTableAttributePartition() throws SQLException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        tableDataService.setTableObjectSQLService(serviceArrayList);

        when(mockConnection.createStatement()).thenReturn(mockStatement);

        TableNameDTO tableNameDTO = new TableNameDTO();
        tableNameDTO.setTableName("t1");
        tableNameDTO.setSchema("s1");
        tableNameDTO.setUuid(UUID);
        tableNameDTO.setOid("1");
        tableDataService.tableAttributePartition(tableNameDTO);
    }

    @Test
    public void testExportData() throws SQLException, IOException {
        List<TableObjectSQLService> serviceArrayList = new ArrayList<>();
        serviceArrayList.add(new TableObjectSQLServiceImpl());
        tableDataService.setTableObjectSQLService(serviceArrayList);

        when(response.getOutputStream()).thenReturn(outputStream);
        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("qqq");

        TableDataQuery request = new TableDataQuery();
        request.setUuid(UUID);
        request.setSchema("s1");
        request.setTableName("t1");
        request.setPageNum(10);
        request.setPageSize(2);
        request.setFileType("Excel(xlsx)");
        tableDataService.exportData(request, response);
        request.setFileType("Excel(xls)");
        tableDataService.exportData(request, response);
    }
}

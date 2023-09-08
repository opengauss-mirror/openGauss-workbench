/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayIdSchemaQuery;
import com.nctigba.datastudio.model.query.ExportRequest;
import com.nctigba.datastudio.service.DatabaseFunctionSPService;
import com.nctigba.datastudio.service.DatabaseSequenceService;
import com.nctigba.datastudio.service.DatabaseViewService;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.TableDataService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.copy.CopyManager;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.concurrent.ExecutionException;

import static com.nctigba.datastudio.constants.CommonConstants.IS_PACKAGE;
import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * ExportServiceTest
 *
 * @since 2023-07-03
 */
@RunWith(MockitoJUnitRunner.class)
public class ExportServiceTest {
    @InjectMocks
    private ExportServiceImpl exportService;

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSetMetaData mockMetaData;

    @Mock
    private MockHttpServletResponse response;

    @Mock
    private ConnectionConfig connectionConfig;

    @Mock
    private TableDataService tableDataService;

    @Mock
    private DatabaseFunctionSPService functionSPService;

    @Mock
    private DatabaseViewService viewService;

    @Mock
    private DatabaseSequenceService sequenceService;

    @Mock
    private DbConnectionService dbConnectionService;

    @Mock
    private ServletOutputStream outputStream;

    @Mock
    private CopyManager copyManager;

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        conMap.put("111", new ConnectionDTO());

        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, true, true, true, true, true, true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(10);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("name1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("name2");
        when(mockMetaData.getColumnName(eq(3))).thenReturn("name3");
        when(mockMetaData.getColumnName(eq(4))).thenReturn("name4");
        when(mockMetaData.getColumnName(eq(5))).thenReturn("name5");
        when(mockMetaData.getColumnName(eq(6))).thenReturn("name6");
        when(mockMetaData.getColumnName(eq(7))).thenReturn("name7");
        when(mockMetaData.getColumnName(eq(8))).thenReturn("name8");
        when(mockMetaData.getColumnName(eq(9))).thenReturn("name9");
        when(mockMetaData.getColumnName(eq(10))).thenReturn("name10");
        when(mockResultSet.getString("name1")).thenReturn("value1");
        when(mockResultSet.getString("name2")).thenReturn("value2");
        when(mockResultSet.getString("name3")).thenReturn("value3");
        when(mockResultSet.getString("name4")).thenReturn("value4");
        when(mockResultSet.getString("name5")).thenReturn("true");
        when(mockResultSet.getString("name6")).thenReturn("value6");
        when(mockResultSet.getString("name7")).thenReturn("true");
        when(mockResultSet.getString("name8")).thenReturn("false");
        when(mockResultSet.getString("name9")).thenReturn("value9");
        when(mockResultSet.getString("name10")).thenReturn("true");
        when(mockMetaData.getColumnType(eq(1))).thenReturn(0);
        when(mockMetaData.getColumnType(eq(2))).thenReturn(-5);
        when(mockMetaData.getColumnType(eq(3))).thenReturn(8);
        when(mockMetaData.getColumnType(eq(4))).thenReturn(8);
        when(mockMetaData.getColumnType(eq(5))).thenReturn(-7);
        when(mockMetaData.getColumnType(eq(6))).thenReturn(-7);
        when(mockMetaData.getColumnType(eq(7))).thenReturn(-7);
        when(mockMetaData.getColumnType(eq(8))).thenReturn(-7);
        when(mockMetaData.getColumnType(eq(9))).thenReturn(-7);
        when(mockMetaData.getColumnType(eq(10))).thenReturn(100);
        when(mockMetaData.getColumnTypeName(eq(1))).thenReturn("int");
        when(mockMetaData.getColumnTypeName(eq(2))).thenReturn("integer");
        when(mockMetaData.getColumnTypeName(eq(3))).thenReturn("money");
        when(mockMetaData.getColumnTypeName(eq(4))).thenReturn("double");
        when(mockMetaData.getColumnTypeName(eq(5))).thenReturn("bool");
        when(mockMetaData.getColumnTypeName(eq(6))).thenReturn("bool");
        when(mockMetaData.getColumnTypeName(eq(7))).thenReturn("boolean");
        when(mockMetaData.getColumnTypeName(eq(8))).thenReturn("boolean");
        when(mockMetaData.getColumnTypeName(eq(9))).thenReturn("boolean");
        when(mockMetaData.getColumnTypeName(eq(10))).thenReturn("true");
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    public void testExportTableDDL() throws SQLException, IOException {
        List<String> list = new ArrayList<>();
        list.add("tt");
        ExportRequest request = new ExportRequest();
        request.setTableList(list);
        request.setUuid("111");
        request.setSchema("aaa");
        request.setDataFlag(true);
        exportService.exportTableDdl(request, response);
        request.setDataFlag(false);
        exportService.exportTableDdl(request, response);
        when(mockResultSet.next()).thenReturn(false);
        list.add("ttt");
        request.setDataFlag(true);
        exportService.exportTableDdl(request, response);
    }

    @Test
    public void testExportTableData() throws SQLException, IOException {
        List<String> columnList = new ArrayList<>();
        columnList.add("c1");
        columnList.add("c2");
        ExportRequest request = new ExportRequest();
        request.setUuid("111");
        request.setSchema("aaa");
        request.setTableName("tt");
        request.setColumnList(columnList);
        request.setEncoding("UTF-8");
        request.setFileType("Excel(xlsx)");
        exportService.exportTableData(request, response);
        request.setFileType("Excel(xls)");
        exportService.exportTableData(request, response);
    }

    @Test
    public void testExportTableData2() throws SQLException, IOException {
        List<String> columnList = new ArrayList<>();
        columnList.add("c1");
        columnList.add("c2");
        ExportRequest request = new ExportRequest();
        request.setUuid("111");
        request.setSchema("aaa");
        request.setTableName("tt");
        request.setColumnList(columnList);
        request.setFileType("Text");
        exportService.exportTableData(request, response);
        request.setEncoding("UTF-8");
        request.setFileType("Binary");
        exportService.exportTableData(request, response);
        request.setFileType("Text");
        request.setQuote("1");
        request.setEscape("0");
        request.setDelimiter("|");
        request.setReplaceNull("666");
        request.setTitleFlag(true);
        exportService.exportTableData(request, response);
        request.setFileType("Text");
        request.setQuote("'");
        request.setEscape("'");
        request.setDelimiter("'");
        request.setReplaceNull("666");
        request.setTitleFlag(true);
        exportService.exportTableData(request, response);
    }

    @Test
    public void testExportFunctionDDL() throws SQLException, IOException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put(OID, "111");
        map1.put(IS_PACKAGE, false);
        list.add(map1);

        ExportRequest request = new ExportRequest();
        request.setFunctionMap(list);
        request.setUuid("111");
        request.setSchema("aaa");
        exportService.exportFunctionDdl(request, response);

        Map<String, Object> map2 = new HashMap<>();
        map2.put(OID, "222");
        map2.put(IS_PACKAGE, true);
        list.add(map2);
        exportService.exportFunctionDdl(request, response);
    }

    @Test
    public void testExportFunctionDDL2() throws SQLException, IOException {
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put(OID, "111");
        map1.put(IS_PACKAGE, false);
        Map<String, Object> map2 = new HashMap<>();
        map2.put(OID, 111);
        map2.put(IS_PACKAGE, "false");
        list.add(map1);
        list.add(map2);

        ExportRequest request = new ExportRequest();
        request.setFunctionMap(list);
        request.setUuid("111");
        request.setSchema("aaa");
        exportService.exportFunctionDdl(request, response);
    }

    @Test
    public void testExportViewDDL() throws SQLException, IOException {
        List<String> list = new ArrayList<>();
        list.add("vv");
        ExportRequest request = new ExportRequest();
        request.setViewList(list);
        request.setUuid("111");
        request.setSchema("aaa");
        exportService.exportViewDdl(request, response);
    }

    @Test
    public void testExportSequenceDDL() throws SQLException, IOException {
        when(mockResultSet.next()).thenReturn(true, false).thenReturn(true, false);

        List<String> list = new ArrayList<>();
        list.add("ss");
        ExportRequest request = new ExportRequest();
        request.setSequenceList(list);
        request.setUuid("111");
        request.setSchema("aaa");
        request.setDataFlag(true);
        exportService.exportSequenceDdl(request, response);
        request.setDataFlag(false);
        exportService.exportSequenceDdl(request, response);
    }

    @Test
    public void testExportSequenceDDLException() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Sequence reached maximum value"));

        List<String> list = new ArrayList<>();
        list.add("ss");
        ExportRequest request = new ExportRequest();
        request.setSequenceList(list);
        request.setUuid("111");
        request.setSchema("aaa");
        request.setDataFlag(true);
        exportService.exportSequenceDdl(request, response);
    }

    @Test
    public void testExportSequenceDDLException2() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Sequence reached minimum value"));

        List<String> list = new ArrayList<>();
        list.add("ss");
        ExportRequest request = new ExportRequest();
        request.setSequenceList(list);
        request.setUuid("111");
        request.setSchema("aaa");
        request.setDataFlag(true);
        exportService.exportSequenceDdl(request, response);
    }

    @Test
    public void testExportSequenceDDLException4() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException(""));

        List<String> list = new ArrayList<>();
        list.add("ss");
        ExportRequest request = new ExportRequest();
        request.setSequenceList(list);
        request.setUuid("111");
        request.setSchema("aaa");
        request.setDataFlag(true);
        exportService.exportSequenceDdl(request, response);
    }

    @Test
    public void testExportSchemaDDL() throws IOException, ExecutionException, InterruptedException {
        DatabaseMetaarrayIdSchemaQuery query = new DatabaseMetaarrayIdSchemaQuery();
        query.setUuid("111");
        query.setSchema("aaa");
        DataListDTO dataListDTO = new DataListDTO();
        List<Map<String, Object>> functionList = new ArrayList<>();
        Map<String, Object> functionMap = new HashMap<>();
        functionMap.put(OID, "111");
        functionMap.put(IS_PACKAGE, false);
        functionList.add(functionMap);
        dataListDTO.setFun_pro(functionList);
        List<Map<String, String>> tableList = new ArrayList<>();
        Map<String, String> tableMap = new HashMap<>();
        tableMap.put(NAME, "tt");
        tableList.add(tableMap);
        dataListDTO.setTable(tableList);
        List<Map<String, String>> viewList = new ArrayList<>();
        Map<String, String> viewMap = new HashMap<>();
        viewMap.put(NAME, "vv");
        viewList.add(viewMap);
        dataListDTO.setView(viewList);
        List<Map<String, String>> sequenceList = new ArrayList<>();
        Map<String, String> sequenceMap = new HashMap<>();
        sequenceMap.put(NAME, "ss");
        sequenceList.add(sequenceMap);
        dataListDTO.setSequence(sequenceList);
        List<DataListDTO> dtoList = new ArrayList<>();
        dtoList.add(dataListDTO);
        when(dbConnectionService.schemaObjectList(query)).thenReturn((dtoList));

        List<String> list = new ArrayList<>();
        list.add("aaa");
        ExportRequest request = new ExportRequest();
        request.setSchemaList(list);
        request.setUuid("111");
        request.setSchema("aaa");
        request.setDataFlag(true);
        exportService.exportSchemaDdl(request, response);
    }

    @Test
    public void testImportTableData() throws SQLException, IOException {
        File file = new File(System.getProperty("user.dir") + "\\data_test_20230814170806.xlsx");
        MockMultipartFile firstFile = new MockMultipartFile("file", "data_test_20230814170806.xlsx",
                MediaType.TEXT_PLAIN_VALUE, new FileInputStream(file));

        ExportRequest request = new ExportRequest();
        request.setUuid("111");
        request.setSchema("scott");
        request.setTableName("test");
        request.setColumnString("a,b,c,d,e,f");
        request.setFile(firstFile);
        request.setTitleFlag(true);
        request.setFileType("Excel");
        exportService.importTableData(request);
        request.setFileType("Text");
        exportService.importTableData(request);
        request.setFileType("Binary");
        exportService.importTableData(request);
    }
}

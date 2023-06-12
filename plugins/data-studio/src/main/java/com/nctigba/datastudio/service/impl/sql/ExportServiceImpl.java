/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayIdSchemaQuery;
import com.nctigba.datastudio.model.query.ExportRequest;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.service.DatabaseFunctionSPService;
import com.nctigba.datastudio.service.DatabaseSequenceService;
import com.nctigba.datastudio.service.DatabaseViewService;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.ExportService;
import com.nctigba.datastudio.service.TableDataService;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.opengauss.copy.CopyManager;
import org.opengauss.core.BaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nctigba.datastudio.constants.CommonConstants.FUNCTION;
import static com.nctigba.datastudio.constants.CommonConstants.IS_CALLED;
import static com.nctigba.datastudio.constants.CommonConstants.MAX_VALUE;
import static com.nctigba.datastudio.constants.CommonConstants.MIN_VALUE;
import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.NEXT_VALUE;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SCHEMA;
import static com.nctigba.datastudio.constants.CommonConstants.SEQUENCE;
import static com.nctigba.datastudio.constants.CommonConstants.TABLE;
import static com.nctigba.datastudio.constants.CommonConstants.UNDERLINE;
import static com.nctigba.datastudio.constants.CommonConstants.VIEW;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SCHEMA_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.FROM_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LEFT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.NEXT_VALUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.PROC_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES;
import static com.nctigba.datastudio.constants.SqlConstants.RIGHT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.SEQUENCE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SET_VALUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DATA_SQL;

@Slf4j
@Service
public class ExportServiceImpl implements ExportService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Autowired
    private TableDataService tableDataService;

    @Autowired
    private DatabaseFunctionSPService functionSPService;

    @Autowired
    private DatabaseViewService viewService;

    @Autowired
    private DatabaseSequenceService sequenceService;

    @Autowired
    private DbConnectionService dbConnectionService;

    @Override
    public void exportTableDdl(ExportRequest request, HttpServletResponse response) throws Exception {
        String fileName = getFileName(request.isDataFlag(), TABLE, request.getTableList());
        DebugUtils.exportFile(fileName, getTableDdl(request), response);
    }

    @Override
    public void exportTableData(ExportRequest request, HttpServletResponse response) throws Exception {
        log.info("ExportService exportTableData request: " + request);
        String fileType = request.getFileType();
        String tableName = request.getTableName();
        String fileName = Strings.EMPTY;
        if (fileType.contains("Excel")) {
            exportExcel(request, response);
            return;
        } else if (fileType.equals("Text")) {
            fileName = getDataFileName(tableName) + ".txt";
        } else if (fileType.equals("Binary")) {
            fileName = getDataFileName(tableName) + ".bin";
        }
        String path = System.getProperty("user.dir") + "\\" + fileName;
        log.info("ExportService exportTableData path: " + path);

        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
                OutputStream os = response.getOutputStream()
        ) {
            CopyManager copyManager = new CopyManager((BaseConnection) connection);
            StringBuilder sb = new StringBuilder();
            sb.append("COPY ").append(request.getSchema()).append(POINT).append(tableName);
            List<String> columnList = request.getColumnList();
            sb.append(LEFT_BRACKET);
            for (int i = 0; i < columnList.size(); i++) {
                sb.append(columnList.get(i));
                if (i != columnList.size() - 1) {
                    sb.append(COMMA);
                }
            }
            sb.append(RIGHT_BRACKET);
            sb.append(" TO STDOUT ");
            if (fileType.equals("Binary")) {
                sb.append(fileType.toUpperCase());
            } else if (fileType.equals("Text")) {
                String delimiter = request.getDelimiter();
                if (StringUtils.isNotEmpty(delimiter)) {
                    sb.append(" DELIMITERS ").append(QUOTES).append(delimiter).append(QUOTES);
                }
                String replaceNull = request.getReplaceNull();
                if (StringUtils.isNotEmpty(replaceNull)) {
                    sb.append(" NULL ").append(QUOTES).append(replaceNull).append(QUOTES);
                }
                sb.append(" CSV ");
                boolean titleFlag = request.isTitleFlag();
                if (titleFlag) {
                    sb.append(" HEADER ");
                }
                String quote = request.getQuote();
                if (StringUtils.isNotEmpty(quote)) {
                    sb.append(" QUOTE ").append(QUOTES).append(quote).append(QUOTES);
                }
                String escape = request.getEscape();
                if (StringUtils.isNotEmpty(escape)) {
                    sb.append(" ESCAPE ").append(QUOTES).append(escape).append(QUOTES);
                }
            }
            String encoding = request.getEncoding();
            if (StringUtils.isNotEmpty(encoding)) {
                sb.append(" ENCODING ").append(QUOTES).append(encoding).append(QUOTES);
            }
            sb.append(SEMICOLON);
            copyManager.copyOut(sb.toString(), fileOutputStream);
            log.info("ExportService exportTableData sb: " + sb);

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.addHeader("Response-Type", "blob");

            byte[] buffer = new byte[1024];
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        }

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        log.info("ExportService exportTableData end: ");
    }

    private void exportExcel(ExportRequest request, HttpServletResponse response) throws Exception {
        log.info("ExportService exportExcel request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
                OutputStream outputStream = response.getOutputStream()
        ) {
            StringBuilder columnSb = new StringBuilder();
            List<String> columnList = request.getColumnList();
            columnList.forEach(column -> {
                columnSb.append(column).append(COMMA);
            });
            log.info("ExportService exportExcel columnSb: " + columnSb);

            String tableName = request.getTableName();
            String sql = SELECT_KEYWORD_SQL + columnSb.deleteCharAt(columnSb.length() - 1) + FROM_KEYWORD_SQL
                    + request.getSchema() + POINT + tableName + SEMICOLON;
            ResultSet resultSet = statement.executeQuery(sql);
            log.info("ExportService exportExcel sql: " + sql);

            int index = 0;
            XSSFSheet sheet = xssfWorkbook.createSheet(tableName);
            XSSFRow row = sheet.createRow(index);
            for (int i = 0; i < columnList.size(); i++) {
                row.createCell(i).setCellValue(columnList.get(i));
            }
            while (resultSet.next()) {
                row = sheet.createRow(++index);
                for (int i = 0; i < columnList.size(); i++) {
                    row.createCell(i).setCellValue(resultSet.getString(columnList.get(i)));
                }
            }
            log.info("ExportService exportExcel hssfSheet: " + sheet.getLastRowNum());

            String fileName = getDataFileName(tableName) + ".xlsx";
            if (request.getFileType().equals("Excel(xls)")) {
                fileName = getDataFileName(tableName) + ".xls";
            }
            log.info("ExportService exportExcel fileName: " + fileName);

            response.reset();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.addHeader("Response-Type", "blob");
            response.setCharacterEncoding(request.getEncoding());
            xssfWorkbook.write(outputStream);
            outputStream.flush();
        }
        log.info("ExportService exportExcel end: ");
    }

    @Override
    public void exportFunctionDdl(ExportRequest request, HttpServletResponse response) throws Exception {
        String fileName = getFileName(false, FUNCTION, request.getFunctionMap());
        DebugUtils.exportFile(fileName, getFunctionDdl(request), response);
    }

    @Override
    public void exportViewDdl(ExportRequest request, HttpServletResponse response) throws Exception {
        String fileName = getFileName(false, VIEW, request.getViewList());
        DebugUtils.exportFile(fileName, getViewDdl(request), response);
    }

    @Override
    public void exportSequenceDdl(ExportRequest request, HttpServletResponse response) throws Exception {
        String fileName = getFileName(request.isDataFlag(), SEQUENCE, request.getSequenceList());
        DebugUtils.exportFile(fileName, getSequenceDdl(request), response);
    }

    @Override
    public void exportSchemaDdl(ExportRequest request, HttpServletResponse response) throws Exception {
        log.info("ExportService exportSchemaDdl request: " + request);
        String uuid = request.getUuid();
        boolean dataFlag = request.isDataFlag();
        List<String> schemaList = request.getSchemaList();
        StringBuilder sb = new StringBuilder();

        for (String schema : schemaList) {
            DatabaseMetaarrayIdSchemaQuery query = new DatabaseMetaarrayIdSchemaQuery();
            query.setUuid(uuid);
            query.setSchema(schema);
            DataListDTO dataList = dbConnectionService.schemaObjectList(query).get(0);
            log.info("ExportService exportSchemaDdl dataList: " + dataList);

            List<String> tableList = parseList(dataList.getTable(), NAME);
            List<String> strings = parseList(dataList.getFun_pro(), OID);
            List<Integer> functionList = strings.stream().map(Integer::valueOf).collect(Collectors.toList());
            List<String> viewList = parseList(dataList.getView(), NAME);
            List<String> sequenceList = parseList(dataList.getSequence(), NAME);

            ExportRequest exportRequest = new ExportRequest();
            exportRequest.setDataFlag(dataFlag);
            exportRequest.setSchema(schema);
            exportRequest.setUuid(uuid);
            exportRequest.setTableList(tableList);
            exportRequest.setFunctionMap(functionList);
            exportRequest.setViewList(viewList);
            exportRequest.setSequenceList(sequenceList);

            sb.append(addHeader(false, schema, SCHEMA, schema)).append(CREATE_SCHEMA_SQL).append(schema)
                    .append(getTableDdl(exportRequest)).append(getFunctionDdl(exportRequest))
                    .append(getViewDdl(exportRequest)).append(getSequenceDdl(exportRequest));
        }

        String fileName = getFileName(dataFlag, SCHEMA, schemaList);
        DebugUtils.exportFile(fileName, sb.toString(), response);
    }

    private String getTableDdl(ExportRequest request) throws Exception {
        log.info("ExportService getTableDdl request: " + request);
        StringBuilder sb = new StringBuilder();
        String uuid = request.getUuid();
        String schema = request.getSchema();

        List<String> tableList = request.getTableList();
        for (String name : tableList) {
            SelectDataQuery dto = new SelectDataQuery();
            dto.setUuid(uuid);
            dto.setSchema(schema);
            dto.setTableName(name);

            String ddl = tableDataService.tableDdl(dto).get(RESULT);
            sb.append(addHeader(false, name, TABLE, schema));
            sb.append(ddl);

            try (
                    Connection connection = connectionConfig.connectDatabase(request.getUuid());
                    Statement statement = connection.createStatement()
            ) {
                if (request.isDataFlag()) {
                    String tableData = getTableData(statement, request.getSchema(), name);
                    if (StringUtils.isNotEmpty(tableData)) {
                        sb.append(addHeader(true, name, TABLE, request.getSchema())).append(tableData);
                    }
                }
            }
        }
        log.info("ExportService getTableDdl sb: " + sb);
        return sb.toString();
    }

    private String getFunctionDdl(ExportRequest request) throws Exception {
        log.info("ExportService getFunctionDdl request: " + request);
        StringBuilder sb = new StringBuilder();
        String uuid = request.getUuid();
        String schema = request.getSchema();

        List<Integer> functionMap = request.getFunctionMap();
        for (Integer oid : functionMap) {
            DatabaseFunctionSPDTO dto = new DatabaseFunctionSPDTO();
            dto.setUuid(uuid);
            dto.setSchema(schema);
            dto.setOid(oid.toString());

            try (
                    Connection connection = connectionConfig.connectDatabase(request.getUuid());
                    Statement statement = connection.createStatement()
            ) {
                String name = Strings.EMPTY;
                ResultSet nameResultSet = statement.executeQuery(String.format(PROC_SQL, oid));
                if (nameResultSet.next()) {
                    name = nameResultSet.getString(PRO_NAME);
                }

                String ddl = functionSPService.functionDdl(dto);
                sb.append(addHeader(false, name, FUNCTION, schema));
                sb.append(ddl);
            }
        }
        log.info("ExportService getFunctionDdl sb: " + sb);
        return sb.toString();
    }

    private String getViewDdl(ExportRequest request) throws Exception {
        log.info("ExportService getViewDdl request: " + request);
        StringBuilder sb = new StringBuilder();
        String uuid = request.getUuid();
        String schema = request.getSchema();

        List<String> viewList = request.getViewList();
        for (String name : viewList) {
            DatabaseViewDdlDTO dto = new DatabaseViewDdlDTO();
            dto.setUuid(uuid);
            dto.setSchema(schema);
            dto.setViewName(name);

            String ddl = viewService.returnViewDDL(dto);
            sb.append(addHeader(false, name, VIEW, schema));
            sb.append(ddl);
        }
        log.info("ExportService getViewDdl sb: " + sb);
        return sb.toString();
    }

    private String getSequenceDdl(ExportRequest request) throws Exception {
        log.info("ExportService getSequenceDdl request: " + request);
        StringBuilder sb = new StringBuilder();
        String uuid = request.getUuid();
        String schema = request.getSchema();

        List<String> sequenceList = request.getSequenceList();
        for (String name : sequenceList) {
            DatabaseSequenceDdlDTO dto = new DatabaseSequenceDdlDTO();
            dto.setUuid(uuid);
            dto.setSchema(schema);
            dto.setSequenceName(name);

            String ddl = sequenceService.returnSequenceDDL(dto);
            sb.append(addHeader(false, name, SEQUENCE, schema));
            sb.append(ddl);

            if (request.isDataFlag()) {
                sb.append(addHeader(true, name, SEQUENCE, schema));
                sb.append(getSequenceData(request, name));
            }
        }
        log.info("ExportService getSequenceDdl sb: " + sb);
        return sb.toString();
    }

    private List<String> parseList(List<Map<String, String>> listMap, String key) {
        log.info("ExportService parseList key: " + key);
        List<String> list = new ArrayList<>();
        for (Map<String, String> map : listMap) {
            list.add(map.get(key));
        }
        log.info("ExportService parseList list: " + list);
        return list;
    }

    private String getFileName(boolean flag, String type, List list) {
        StringBuilder sb = new StringBuilder();
        if (flag) {
            sb.append("ddl_data_");
        } else {
            sb.append("ddl_");
        }
        sb.append(type).append(UNDERLINE);
        if (list.size() == 1) {
            sb.append(list.get(0)).append(UNDERLINE);
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        sb.append(format.format(new Date())).append(".sql");
        log.info("ExportService getFileName sb: " + sb);
        return sb.toString();
    }

    private String getDataFileName(String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("data_").append(tableName).append(UNDERLINE);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        sb.append(format.format(new Date()));
        log.info("ExportService getFileName sb: " + sb);
        return sb.toString();
    }

    private String addHeader(boolean flag, String name, String type, String schema) {
        String str = LF + LF + "-- ";
        if (flag) {
            str = str + "Data for ";
        }
        str = str + "Name: " + name + "; Type: " + type + "; Schema: " + schema + ";" + LF + LF;
        return str;
    }

    private String getTableData(Statement statement, String schema, String table) throws SQLException {
        log.info("ExportService getTableData schema--table: " + schema + "--" + table);
        StringBuilder columnSb = new StringBuilder();
        columnSb.append(LF).append("INSERT INTO ").append(schema).append(POINT).append(table).append(LEFT_BRACKET);

        ResultSet resultSet = statement.executeQuery(String.format(TABLE_DATA_SQL, schema, table));
        List<String> columnList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i + 1);
            columnList.add(columnName);
            columnSb.append(columnName);
            if (i != metaData.getColumnCount() - 1) {
                columnSb.append(COMMA);
            } else {
                columnSb.append(RIGHT_BRACKET).append(" VALUES ");
            }
        }
        log.info("ExportService getTableData columnList: " + columnList);

        StringBuilder sb = new StringBuilder();
        while (resultSet.next()) {
            sb.append(columnSb).append(LEFT_BRACKET);
            for (int i = 0; i < columnList.size(); i++) {
                String value = resultSet.getString(columnList.get(i));
                int columnType = metaData.getColumnType(i + 1);
                String columnTypeName = metaData.getColumnTypeName(i + 1);

                sb.append(typeChange(value, columnType, columnTypeName));
                if (i != columnList.size() - 1) {
                    sb.append(COMMA);
                } else {
                    sb.append(RIGHT_BRACKET + SEMICOLON);
                }
            }
        }
        log.info("ExportService getTableData sb: " + sb);
        return sb.toString();
    }

    private String getSequenceData(ExportRequest request, String name) throws Exception {
        log.info("ExportService getSequenceData request: " + request);
        String schema = request.getSchema();
        boolean called = false;
        long nextVal = 0;
        long maxValue = 0;
        long minValue = 0;
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            ResultSet resultSet = statement.executeQuery(String.format(SEQUENCE_SQL, schema + POINT + name));
            while (resultSet.next()) {
                maxValue = resultSet.getLong(MAX_VALUE);
                minValue = resultSet.getLong(MIN_VALUE);
                called = resultSet.getBoolean(IS_CALLED);
            }

            ResultSet nextResultSet = statement.executeQuery(String.format(NEXT_VALUE_SQL, schema + POINT + name));
            while (nextResultSet.next()) {
                nextVal = nextResultSet.getInt(NEXT_VALUE);
            }
            log.info("ExportService getSequenceData nextVal: " + nextVal);
        } catch (SQLException exception) {
            String msg = exception.getMessage();
            if (msg.contains("Sequence reached maximum value")) {
                nextVal = maxValue;
                called = true;
            }
            if (msg.contains("Sequence reached minimum value")) {
                nextVal = minValue;
                called = true;
            }
        }

        String ddl = String.format(SET_VALUE_SQL, name, nextVal, called);
        log.info("ExportService getSequenceData ddl: " + ddl);
        return ddl;
    }

    private String typeChange(String value, int columnType, String columnTypeName) {
        String newValue;
        switch (columnType) {
            case Types.NULL: {
                newValue = null;
                break;
            }
            case Types.BIGINT:
            case Types.TINYINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.FLOAT:
            case Types.REAL: {
                newValue = value;
                break;
            }
            case Types.DOUBLE: {
                if ("money".equalsIgnoreCase(columnTypeName)) {
                    newValue = QUOTES + value + QUOTES;
                } else {
                    newValue = value;
                }
                break;
            }
            case Types.BIT: {
                if ("bool".equalsIgnoreCase(columnTypeName)) {
                    Boolean boolValue = BooleanUtils.toBooleanObject(value);
                    if (boolValue != null) {
                        newValue = boolValue.toString();
                    } else {
                        newValue = value;
                    }
                } else {
                    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                        if ("true".equalsIgnoreCase(value)) {
                            newValue = QUOTES + "1" + QUOTES;
                        } else {
                            newValue = QUOTES + "0" + QUOTES;
                        }
                    } else {
                        newValue = QUOTES + value + QUOTES;
                    }
                }
                break;
            }
            default: {
                newValue = QUOTES + value + QUOTES;
                break;
            }
        }
        return newValue;
    }
}

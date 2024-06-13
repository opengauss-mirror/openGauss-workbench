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
 *  ExportServiceSqlServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/ExportServiceSqlServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.ExportServiceSqlService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayIdSchemaQuery;
import com.nctigba.datastudio.model.query.ExportQuery;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.service.DatabaseFunctionSPService;
import com.nctigba.datastudio.service.DatabaseSequenceService;
import com.nctigba.datastudio.service.DatabaseViewService;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.TableDataService;
import com.nctigba.datastudio.utils.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.opengauss.copy.CopyManager;
import org.opengauss.core.BaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CURRENT_VALUE;
import static com.nctigba.datastudio.constants.CommonConstants.FUNCTION;
import static com.nctigba.datastudio.constants.CommonConstants.IS_CALLED;
import static com.nctigba.datastudio.constants.CommonConstants.IS_PACKAGE;
import static com.nctigba.datastudio.constants.CommonConstants.MAX_VALUE;
import static com.nctigba.datastudio.constants.CommonConstants.MIN_VALUE;
import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SCHEMA;
import static com.nctigba.datastudio.constants.CommonConstants.SEQUENCE;
import static com.nctigba.datastudio.constants.CommonConstants.TABLE;
import static com.nctigba.datastudio.constants.CommonConstants.UNDERLINE;
import static com.nctigba.datastudio.constants.CommonConstants.VIEW;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COUNT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COURSE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SCHEMA_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CUR_VALUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.FETCH_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.FROM_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LEFT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.PROC_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_OID_BY_PACKAGE;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES;
import static com.nctigba.datastudio.constants.SqlConstants.RIGHT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.SEQUENCE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SET_VALUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DATA_SQL;

/**
 * ExportServiceSqlServiceImpl
 *
 * @since 2023-10-16
 */
@Slf4j
@Service
public class ExportServiceSqlServiceImpl implements ExportServiceSqlService {
    private static final String ROOT_PATH = System.getProperty("user.dir");

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
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public void exportTableDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        String fileName = getFileName(request.isDataFlag(), TABLE, request.getTableList());
        String path = ROOT_PATH + File.separator + fileName;
        log.info("ExportService exportTableDdl path: " + path);
        try (
                FileWriter fileWriter = new FileWriter(path);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            getTableDdl(bufferedWriter, request);
        }
        DebugUtils.exportFile(fileName, path, response);
    }

    @Override
    public void exportTableData(ExportQuery request, HttpServletResponse response) throws IOException, SQLException {
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
        String path = ROOT_PATH + File.separator + fileName;
        log.info("ExportService exportTableData path: " + path);

        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
                OutputStream os = response.getOutputStream()
        ) {
            String querySql = composeQuerySql(request, true);
            if (connection instanceof BaseConnection) {
                CopyManager copyManager = new CopyManager((BaseConnection) connection);
                copyManager.copyOut(querySql, fileOutputStream);
            }

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.addHeader("Response-Type", "blob");

            byte[] buffer = new byte[1024];
            int len = bis.read(buffer);
            while (len != -1) {
                os.write(buffer, 0, len);
                len = bis.read(buffer);
            }
        }

        DebugUtils.deleteFile(path);
        log.info("ExportService exportTableData end: ");
    }

    @Override
    public void importTableData(ExportQuery request) throws IOException, SQLException {
        log.info("ExportService importTableData request: " + request);
        MultipartFile multipartFile = request.getFile();
        String path = ROOT_PATH + File.separator + multipartFile.getName();
        File file = new File(path);
        multipartFile.transferTo(file);
        log.info("ExportService importTableData file: " + file);

        String fileType = request.getFileType();
        if (fileType.contains("Excel")) {
            importExcel(request, file, path);
            return;
        }

        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                FileInputStream fileInputStream = new FileInputStream((file))
        ) {
            String querySql = composeQuerySql(request, false);
            if (connection instanceof BaseConnection) {
                CopyManager copyManager = new CopyManager((BaseConnection) connection);
                copyManager.copyIn(querySql, fileInputStream);
            }
        }

        DebugUtils.deleteFile(path);
        log.info("ExportService importTableData end: ");
    }

    private void exportExcel(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        log.info("ExportService exportExcel request: " + request);

        StringBuilder columnSb = new StringBuilder();
        List<String> columnList = request.getColumnList();
        columnList.forEach(column -> columnSb.append(column).append(COMMA));
        log.info("ExportService exportExcel columnSb: " + columnSb);

        String tableName = request.getTableName();
        String sql = SELECT_KEYWORD_SQL + columnSb.deleteCharAt(columnSb.length() - 1)
                + FROM_KEYWORD_SQL + DebugUtils.needQuoteName(request.getSchema())
                + POINT + DebugUtils.needQuoteName(tableName) + SEMICOLON;

        String fileName = getDataFileName(tableName) + ".xlsx";
        if (request.getFileType().equals("Excel(xls)")) {
            fileName = getDataFileName(tableName) + ".xls";
        }

        exportExcelFile(request, sql, fileName, response);
        log.info("ExportService exportExcel end: ");
    }

    private void exportExcelFile(ExportQuery request, String sql, String fileName, HttpServletResponse response)
            throws SQLException, IOException {
        log.info("ExportService getXssfWorkBook startTime: " + System.currentTimeMillis());
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();
                OutputStream outputStream = response.getOutputStream()
        ) {
            response.reset();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.addHeader("Response-Type", "blob");
            response.setCharacterEncoding(request.getEncoding());

            int count = 0;
            String tableName = request.getTableName();
            ResultSet countResult = statement.executeQuery(String.format(COUNT_SQL,
                    DebugUtils.needQuoteName(request.getSchema()), DebugUtils.needQuoteName(tableName)));
            while (countResult.next()) {
                count = countResult.getInt("count");
            }
            log.info("ExportService getXssfWorkBook count: " + count);

            String timeStamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
            connection.setAutoCommit(false);
            statement.execute(String.format(COURSE_SQL, "DS_" + timeStamp, sql));
            log.info("ExportService getXssfWorkBook timeStamp: " + "DS_" + timeStamp);

            int page = count % 1000000 == 0 ? count / 1000000 : count / 1000000 + 1;
            log.info("ExportService getXssfWorkBook page: " + page);
            for (int p = 0; p < page; p++) {
                List<String> columnList = request.getColumnList();
                SXSSFSheet sheet = sxssfWorkbook.createSheet(tableName + "-" + p);
                SXSSFRow row = sheet.createRow(0);
                for (int i = 0; i < columnList.size(); i++) {
                    row.createCell(i).setCellValue(columnList.get(i));
                }

                int currentCount = page - 1 == p ? count - p * 1000000 : 1000000;
                int size = currentCount % 1000 == 0 ? currentCount / 1000 : currentCount / 1000 + 1;
                int index = 0;
                for (int s = 0; s < size; s++) {
                    ResultSet resultSet = statement.executeQuery(String.format(FETCH_SQL, 1000, "DS_" + timeStamp));
                    while (resultSet.next()) {
                        row = sheet.createRow(++index);
                        for (int i = 0; i < columnList.size(); i++) {
                            row.createCell(i).setCellValue(resultSet.getString(columnList.get(i)));
                        }
                    }
                }
            }

            sxssfWorkbook.write(outputStream);
            outputStream.flush();
            log.info("ExportService getXssfWorkBook endTime: " + System.currentTimeMillis());
        }
    }

    private void importExcel(ExportQuery request, File file, String path) throws SQLException, IOException {
        String columnString = request.getColumnString();
        log.info("ExportService importExcel columnString: " + columnString);

        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(file))
        ) {
            String schema = DebugUtils.needQuoteName(request.getSchema());
            String tableName = DebugUtils.needQuoteName(request.getTableName());
            ResultSet resultSet = statement.executeQuery(SELECT_KEYWORD_SQL + columnString + FROM_KEYWORD_SQL
                    + schema + POINT + tableName + " limit 1;");
            ResultSetMetaData metaData = resultSet.getMetaData();
            int size = 0;
            List<Integer> typeList = new ArrayList<>();
            List<String> typeNameList = new ArrayList<>();
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                size++;
                typeList.add(metaData.getColumnType(i + 1));
                typeNameList.add(metaData.getColumnTypeName(i + 1));
            }
            log.info("ExportService importExcel typeList: " + typeList);
            log.info("ExportService importExcel typeNameList: " + typeNameList);

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(schema).append(POINT).append(tableName).append(LEFT_BRACKET)
                    .append(columnString).append(RIGHT_BRACKET).append(" VALUES ");
            int number = xssfWorkbook.getNumberOfSheets();
            for (int i = 0; i < number; i++) {
                XSSFSheet sheet = xssfWorkbook.getSheetAt(i);
                for (int j = 0; j <= sheet.getLastRowNum(); j++) {
                    XSSFRow row = sheet.getRow(j);
                    if (j == 0 && request.isTitleFlag()) {
                        continue;
                    }
                    sb.append(LEFT_BRACKET);
                    log.info("ExportService importExcel row: " + row.getLastCellNum());
                    for (int k = 0; k < size; k++) {
                        XSSFCell cell = row.getCell(k);
                        String s = DebugUtils.typeChange(getCellValue(cell, request.getTimeFormat()),
                                typeList.get(k), typeNameList.get(k));
                        if (StringUtils.isEmpty(s)) {
                            sb.append("''");
                        } else {
                            sb.append(s);
                        }
                        if (k != size - 1) {
                            sb.append(COMMA);
                        }
                    }
                    sb.append(RIGHT_BRACKET).append(COMMA);
                }
            }

            sb.deleteCharAt(sb.length() - 1).append(SEMICOLON);
            log.info("ExportService importExcel sb: " + sb);
            statement.execute(sb.toString());
        }

        DebugUtils.deleteFile(path);
        log.info("ExportService importExcel end: ");
    }

    private String getCellValue(XSSFCell cellObj, String timeFormat) {
        log.info("ExportService getCellValue cellObj: " + cellObj);
        String cellValue = null;
        if (cellObj == null) {
            cellValue = "";
        } else if (cellObj.getCellType() == CellType.BOOLEAN) {
            cellValue = String.valueOf(cellObj.getBooleanCellValue());
        } else if (cellObj.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cellObj)) {
                SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
                if (cellObj.getDateCellValue() != null) {
                    cellValue = formatter.format(cellObj.getDateCellValue());
                }
            } else {
                cellValue = String.valueOf(cellObj.getNumericCellValue());
            }
        } else {
            cellValue = cellObj.getStringCellValue();
        }
        log.info("ExportService getCellValue cellValue: " + cellValue);
        return cellValue;
    }

    private String composeQuerySql(ExportQuery request, boolean isExport) {
        StringBuilder sb = new StringBuilder();
        sb.append("COPY ").append(DebugUtils.needQuoteName(request.getSchema())).append(POINT)
                .append(DebugUtils.needQuoteName(request.getTableName()));
        List<String> columnList = request.getColumnList();
        sb.append(LEFT_BRACKET);
        if (CollectionUtils.isEmpty(columnList)) {
            sb.append(request.getColumnString());
        } else {
            sb.append(DebugUtils.listToString(columnList, COMMA));
        }
        sb.append(RIGHT_BRACKET);
        if (isExport) {
            sb.append(" TO STDOUT");
        } else {
            sb.append(" FROM STDIN");
        }
        if (request.getFileType().equals("Binary")) {
            sb.append(" binary");
        } else {
            String delimiter = request.getDelimiter();
            if (StringUtils.isNotEmpty(delimiter)) {
                sb.append(" DELIMITERS ").append(QUOTES).append(delimiter).append(QUOTES);
                if (delimiter.equals("'")) {
                    sb.append(QUOTES).append(delimiter).append(QUOTES);
                }
            }
            String replaceNull = request.getReplaceNull();
            if (StringUtils.isNotEmpty(replaceNull)) {
                sb.append(" NULL ").append(QUOTES).append(replaceNull).append(QUOTES);
            }
            boolean isTitleFlag = request.isTitleFlag();
            if (isTitleFlag) {
                sb.append(" HEADER ");
            }
            sb.append(" CSV ");
            String quote = request.getQuote();
            if (StringUtils.isNotEmpty(quote)) {
                sb.append(" QUOTE ").append(QUOTES).append(quote).append(QUOTES);
                if (quote.equals("'")) {
                    sb.append(QUOTES).append(quote).append(QUOTES);
                }
            }
            String escape = request.getEscape();
            if (StringUtils.isNotEmpty(escape)) {
                sb.append(" ESCAPE ").append(QUOTES).append(escape).append(QUOTES);
                if (escape.equals("'")) {
                    sb.append(QUOTES).append(escape).append(QUOTES);
                }
            }
        }
        String encoding = request.getEncoding();
        if (StringUtils.isNotEmpty(encoding)) {
            sb.append(" ENCODING ").append(QUOTES).append(encoding).append(QUOTES);
        }
        sb.append(SEMICOLON);
        log.info("ExportService composeQuerySql sb: " + sb);
        return sb.toString();
    }

    @Override
    public void exportFunctionDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        List<Object> list = new ArrayList<>();
        List<Map<String, Object>> functionMap = request.getFunctionMap();
        functionMap.forEach(item -> {
            list.add(item.get(NAME) + UNDERLINE + item.get(OID));
        });
        String fileName = getFileName(false, FUNCTION, list);
        String path = ROOT_PATH + File.separator + fileName;
        log.info("ExportService exportFunctionDdl path: " + path);
        try (
                FileWriter fileWriter = new FileWriter(path);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            getFunctionDdl(bufferedWriter, request);
        }
        DebugUtils.exportFile(fileName, path, response);
    }

    @Override
    public void exportViewDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        String fileName = getFileName(false, VIEW, request.getViewList());
        String path = ROOT_PATH + File.separator + fileName;
        log.info("ExportService exportViewDdl path: " + path);
        try (
                FileWriter fileWriter = new FileWriter(path);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            getViewDdl(bufferedWriter, request);
        }
        DebugUtils.exportFile(fileName, path, response);
    }

    @Override
    public void exportSequenceDdl(ExportQuery request, HttpServletResponse response)
            throws SQLException, IOException {
        String fileName = getFileName(request.isDataFlag(), SEQUENCE, request.getSequenceList());
        String path = ROOT_PATH + File.separator + fileName;
        log.info("ExportService exportSequenceDdl path: " + path);
        try (
                FileWriter fileWriter = new FileWriter(path);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            getSequenceDdl(bufferedWriter, request);
        }
        DebugUtils.exportFile(fileName, path, response);
    }

    @Override
    public void exportSchemaDdl(
            ExportQuery request,
            HttpServletResponse response) throws IOException, SQLException {
        log.info("ExportService exportSchemaDdl request: " + request);
        boolean isDataFlag = request.isDataFlag();
        List<String> schemaList = request.getSchemaList();
        String fileName = getFileName(isDataFlag, SCHEMA, schemaList);
        String path = ROOT_PATH + File.separator + fileName;
        log.info("ExportService exportSequenceDdl path: " + path);

        try (
                FileWriter fileWriter = new FileWriter(path);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            for (String schema : schemaList) {
                DatabaseMetaArrayIdSchemaQuery query = new DatabaseMetaArrayIdSchemaQuery();
                String uuid = request.getUuid();
                query.setUuid(uuid);
                query.setSchema(schema);
                DataListDTO dataList = dbConnectionService.schemaObjectList(query).get(0);
                log.info("ExportService exportSchemaDdl dataList: " + dataList);

                List<String> tableList = parseList(dataList.getTable());
                List<Map<String, Object>> funProList = dataList.getFun_pro();
                List<String> viewList = parseList(dataList.getView());
                List<String> sequenceList = parseList(dataList.getSequence());

                ExportQuery exportQuery = new ExportQuery();
                exportQuery.setDataFlag(isDataFlag);
                exportQuery.setSchema(schema);
                exportQuery.setUuid(uuid);
                exportQuery.setTableList(tableList);
                exportQuery.setFunctionMap(funProList);
                exportQuery.setViewList(viewList);
                exportQuery.setSequenceList(sequenceList);

                bufferedWriter.write(addHeader(false, schema, SCHEMA, schema));
                bufferedWriter.write(String.format(CREATE_SCHEMA_SQL, DebugUtils.needQuoteName(schema)));
                bufferedWriter.flush();

                getTableDdl(bufferedWriter, exportQuery);
                getFunctionDdl(bufferedWriter, exportQuery);
                getViewDdl(bufferedWriter, exportQuery);
                getSequenceDdl(bufferedWriter, exportQuery);
                log.info("ExportService exportSchemaDdl end: ");
            }
        }
        DebugUtils.exportFile(fileName, path, response);
    }

    private void getTableDdl(BufferedWriter bufferedWriter, ExportQuery request) throws SQLException, IOException {
        log.info("ExportService getTableDdl request: " + request);
        String uuid = request.getUuid();
        String schema = request.getSchema();

        List<String> tableList = request.getTableList();
        for (String name : tableList) {
            SelectDataQuery dto = new SelectDataQuery();
            dto.setUuid(uuid);
            dto.setSchema(schema);
            dto.setTableName(name);

            String ddl = tableDataService.tableDdl(dto).get(RESULT);
            bufferedWriter.write(addHeader(false, name, TABLE, schema) + ddl);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            if (request.isDataFlag()) {
                getTableData(bufferedWriter, uuid, schema, name);
            }
        }
    }

    private void getFunctionDdl(BufferedWriter bufferedWriter, ExportQuery request) throws SQLException, IOException {
        log.info("ExportService getFunctionDdl request: " + request);
        String uuid = request.getUuid();
        String schema = request.getSchema();

        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            List<String> list = new ArrayList<>();
            StringBuilder packageSb = new StringBuilder();
            List<Map<String, Object>> functionMap = request.getFunctionMap();
            functionMap.forEach(map -> {
                boolean isPackage = false;
                if (map.get(IS_PACKAGE) instanceof Boolean) {
                    isPackage = (boolean) map.get(IS_PACKAGE);
                }
                if (isPackage) {
                    packageSb.append(map.get(OID)).append(COMMA);
                } else {
                    if (map.get(OID) instanceof String) {
                        list.add((String) map.get(OID));
                    }
                }
            });

            if (StringUtils.isNotEmpty(packageSb)) {
                ResultSet resultSet = statement.executeQuery(
                        String.format(QUERY_OID_BY_PACKAGE, packageSb.deleteCharAt(packageSb.length() - 1)));
                while (resultSet.next()) {
                    list.add(resultSet.getString(OID));
                }
            }

            log.info("ExportService getFunctionDdl list: " + list);
            for (String oid : list) {
                DatabaseFunctionSPDTO dto = new DatabaseFunctionSPDTO();
                dto.setUuid(uuid);
                dto.setSchema(schema);
                dto.setOid(oid);

                String name = Strings.EMPTY;
                ResultSet nameResultSet = statement.executeQuery(String.format(PROC_SQL, oid));
                if (nameResultSet.next()) {
                    name = nameResultSet.getString(PRO_NAME);
                }

                String ddl = functionSPService.functionDdl(dto);
                bufferedWriter.write(addHeader(false, name, FUNCTION, schema) + ddl);
                bufferedWriter.flush();
            }
        }
        log.info("ExportService getFunctionDdl end: ");
    }

    private void getViewDdl(BufferedWriter bufferedWriter, ExportQuery request) throws SQLException, IOException {
        log.info("ExportService getViewDdl request: " + request);
        String uuid = request.getUuid();
        String schema = request.getSchema();

        List<String> viewList = request.getViewList();
        for (String name : viewList) {
            DatabaseViewDdlDTO dto = new DatabaseViewDdlDTO();
            dto.setUuid(uuid);
            dto.setSchema(schema);
            dto.setViewName(name);

            String ddl = viewService.returnViewDDL(dto);
            bufferedWriter.write(addHeader(false, name, VIEW, schema) + ddl);
            bufferedWriter.flush();
        }
        log.info("ExportService getViewDdl end: ");
    }

    private void getSequenceDdl(BufferedWriter bufferedWriter, ExportQuery request) throws SQLException, IOException {
        log.info("ExportService getSequenceDdl request: " + request);
        String uuid = request.getUuid();
        String schema = request.getSchema();

        List<String> sequenceList = request.getSequenceList();
        for (String name : sequenceList) {
            DatabaseSequenceDdlDTO dto = new DatabaseSequenceDdlDTO();
            dto.setUuid(uuid);
            dto.setSchema(schema);
            dto.setSequenceName(name);

            String ddl = sequenceService.returnSequenceDDL(dto);
            bufferedWriter.write(addHeader(false, name, SEQUENCE, schema) + ddl);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            if (request.isDataFlag()) {
                bufferedWriter.write(addHeader(true, name, SEQUENCE, schema));
                bufferedWriter.write(getSequenceData(request, name));
                bufferedWriter.flush();
            }
        }
        log.info("ExportService getSequenceDdl end: ");
    }

    private List<String> parseList(List<Map<String, String>> listMap) {
        log.info("ExportService parseList listMap: " + listMap);
        List<String> list = new ArrayList<>();
        for (Map<String, String> map : listMap) {
            list.add(map.get(NAME));
        }
        log.info("ExportService parseList list: " + list);
        return list;
    }

    private String getFileName(boolean isData, String type, List list) {
        StringBuilder sb = new StringBuilder();
        if (isData) {
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

    private String addHeader(boolean isData, String name, String type, String schema) {
        String str = LF + LF + "-- ";
        if (isData) {
            str = str + "Data for ";
        }
        str = str + "Name: " + name + "; Type: " + type + "; Schema: " + schema + ";" + LF + LF;
        return str;
    }

    private void getTableData(
            BufferedWriter bufferedWriter, String uuid, String schema, String table) throws SQLException, IOException {
        log.info("ExportService getTableData schema--table: " + schema + "--" + table);
        StringBuilder columnSb = new StringBuilder();
        String sch = DebugUtils.needQuoteName(schema);
        String tab = DebugUtils.needQuoteName(table);
        columnSb.append("INSERT INTO ").append(sch).append(POINT).append(tab).append(LEFT_BRACKET);

        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
        ) {
            int count = 0;
            ResultSet countResult = statement.executeQuery(String.format(COUNT_SQL, sch, tab));
            while (countResult.next()) {
                count = countResult.getInt("count");
            }
            log.info("ExportService getTableData count: " + count);
            if (count == 0) {
                return;
            }
            bufferedWriter.write(addHeader(true, table, TABLE, schema));

            ResultSet columnResultSet = statement.executeQuery(String.format(TABLE_DATA_SQL + " limit 1;", sch, tab));
            List<String> columnList = new ArrayList<>();
            ResultSetMetaData metaData = columnResultSet.getMetaData();
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

            connection.setAutoCommit(false);
            String timeStamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
            log.info("ExportService getTableData startTime: " + System.currentTimeMillis());
            statement.execute(String.format(COURSE_SQL, "DS_" + timeStamp, String.format(TABLE_DATA_SQL, sch, tab)));

            int size = count % 1000 == 0 ? count / 1000 : count / 1000 + 1;
            log.info("ExportService getTableData size: " + size);
            for (int s = 0; s < size; s++) {
                StringBuilder sb = new StringBuilder();
                ResultSet resultSet = statement.executeQuery(String.format(FETCH_SQL, 1000, "DS_" + timeStamp));
                while (resultSet.next()) {
                    sb.append(columnSb).append(LEFT_BRACKET);
                    for (int i = 0; i < columnList.size(); i++) {
                        String value = resultSet.getString(columnList.get(i));
                        int columnType = metaData.getColumnType(i + 1);
                        String columnTypeName = metaData.getColumnTypeName(i + 1);

                        sb.append(DebugUtils.typeChange(value, columnType, columnTypeName));
                        if (i != columnList.size() - 1) {
                            sb.append(COMMA);
                        } else {
                            sb.append(RIGHT_BRACKET + SEMICOLON).append(LF);
                        }
                    }
                }

                bufferedWriter.write(sb.toString());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            log.info("ExportService getTableData endTime: " + System.currentTimeMillis());
        }
    }

    private String getSequenceData(ExportQuery request, String name) {
        log.info("ExportService getSequenceData request: " + request);
        String schema = DebugUtils.needQuoteName(request.getSchema());
        String sequence = DebugUtils.needQuoteName(name);
        boolean isCalled = false;
        long currentVal = 0;
        long maxValue = 0;
        long minValue = 0;
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            ResultSet resultSet = statement.executeQuery(String.format(SEQUENCE_SQL, schema + POINT + sequence));
            while (resultSet.next()) {
                maxValue = resultSet.getLong(MAX_VALUE);
                minValue = resultSet.getLong(MIN_VALUE);
                isCalled = resultSet.getBoolean(IS_CALLED);
            }

            ResultSet nextResultSet = statement.executeQuery(String.format(CUR_VALUE_SQL, schema + POINT + sequence));
            while (nextResultSet.next()) {
                currentVal = nextResultSet.getInt(CURRENT_VALUE);
            }
            log.info("ExportService getSequenceData nextVal: " + currentVal);
        } catch (SQLException e) {
            String msg = e.getMessage();
            log.info("ExportService getSequenceData msg: " + msg);
            if (msg.contains("Sequence reached maximum value")) {
                currentVal = maxValue;
                isCalled = true;
            }
            if (msg.contains("Sequence reached minimum value")) {
                currentVal = minValue;
                isCalled = true;
            }
        }

        String ddl = String.format(SET_VALUE_SQL, sequence, currentVal, isCalled);
        log.info("ExportService getSequenceData ddl: " + ddl);
        return ddl;
    }
}

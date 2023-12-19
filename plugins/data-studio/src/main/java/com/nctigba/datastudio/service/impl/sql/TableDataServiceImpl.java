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
 *  TableDataServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/TableDataServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.TableColumnSQLService;
import com.nctigba.datastudio.compatible.TableObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.dao.ResultSetMapDAO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintPkDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateTableDTO;
import com.nctigba.datastudio.model.dto.DatabaseIndexDTO;
import com.nctigba.datastudio.model.dto.TableAlterDTO;
import com.nctigba.datastudio.model.dto.TableAttributeDTO;
import com.nctigba.datastudio.model.dto.TableDataDTO;
import com.nctigba.datastudio.model.dto.TableNameDTO;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.model.query.TableDataQuery;
import com.nctigba.datastudio.model.query.TablePKDataQuery;
import com.nctigba.datastudio.service.TableDataService;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.SqlConstants.COUNT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COURSE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.FETCH_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DATA_LIMIT_SQL;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;
import static java.lang.Math.ceil;

/**
 * TableDataServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class TableDataServiceImpl implements TableDataService {
    @Autowired
    private ResultSetMapDAO resultSetMapDAO;
    @Autowired
    private ConnectionConfig connectionConfig;
    private Map<String, TableObjectSQLService> tableObjectSQLService;
    private Map<String, TableColumnSQLService> tableColumnSQLService;


    /**
     * set table object sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setTableObjectSQLService(List<TableObjectSQLService> SQLServiceList) {
        tableObjectSQLService = new HashMap<>();
        for (TableObjectSQLService service : SQLServiceList) {
            tableObjectSQLService.put(service.type(), service);
        }
    }

    /**
     * set table column sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setTableColumnSQLService(List<TableColumnSQLService> SQLServiceList) {
        tableColumnSQLService = new HashMap<>();
        for (TableColumnSQLService service : SQLServiceList) {
            tableColumnSQLService.put(service.type(), service);
        }
    }

    @Override
    public TableDataDTO tableData(TableDataQuery request) throws SQLException {
        log.info("tableData request is: " + request);
        String schema = DebugUtils.needQuoteName(request.getSchema());
        String tableName = DebugUtils.needQuoteName(request.getTableName());
        try (
                Connection connectionCount = connectionConfig.connectDatabase(request.getUuid());
                Statement statementCount = connectionCount.createStatement()
        ) {
            statementCount.executeUpdate(tableObjectSQLService.get(
                    comGetUuidType(request.getUuid())).tableAnalyseSQL(schema, tableName));
        } catch (SQLException e) {
            log.info(e.toString());
        }
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                Statement statementCount = connection.createStatement();
                ResultSet resultSetCount = statementCount.executeQuery(
                        tableObjectSQLService.get(comGetUuidType(request.getUuid()))
                                .tableDataCountSQL(schema, request.getTableName()))

        ) {
            resultSetCount.next();
            Integer count = resultSetCount.getInt(1);
            TableDataDTO tableDataDTO = new TableDataDTO();
            tableDataDTO.setTableDataDTO(request);
            tableDataDTO.setDataSize(count);
            tableDataDTO.setPageTotal((int) ceil((double) count / request.getPageSize()));
            int start;
            if (request.getPageNum() != 1) {
                start = request.getPageSize() * (request.getPageNum() - 1);
            } else {
                start = 0;
            }
            String sql = tableObjectSQLService.get(
                    comGetUuidType(request.getUuid())).tableDataSQL(schema,
                    tableName, start, request.getPageSize(), request.getExpansion());
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                Map<String, Object> resultMap = DebugUtils.parseResultSetType(resultSet);
                tableDataDTO.setSql(sql);
                tableDataDTO.setData(resultMap);
                return tableDataDTO;
            }
        }
    }

    @Override
    public Map<String, Object> tableColumn(SelectDataQuery request) throws SQLException {
        log.info("tableColumn request is: " + request);
        TableColumnSQLService tableColumnSQLService1 = tableColumnSQLService.get(
                comGetUuidType(request.getUuid()));
        String ddl = tableColumnSQLService1.tableColumnSQL(request.getOid(),
                DebugUtils.needQuoteName(request.getSchema()),
                request.getTableName());
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();

                ResultSet resultSet = statement.executeQuery(ddl)
        ) {
            log.info("tableColumn resultSet is: " + resultSet);
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("tableColumn resultMap is: " + resultMap);
            return resultMap;
        }
    }

    @Override
    public Map<String, Object> tableIndex(SelectDataQuery request) throws SQLException {
        log.info("tableIndex request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(tableColumnSQLService.get(
                        comGetUuidType(request.getUuid())).tableIndexSQL(request))
        ) {
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("tableIndex resultMap is: " + resultMap);
            return resultMap;
        }
    }

    @Override
    public void tableEditIndex(DatabaseIndexDTO request) throws SQLException {
        log.info("tableEditIndex request is: " + request);
        tableColumnSQLService.get(comGetUuidType(request.getUuid())).editIndex(request);
    }

    @Override
    public void tableCreate(DatabaseCreateTableDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            statement.execute(tableColumnSQLService.get(comGetUuidType(request.getUuid())).createTable(request));
        }
    }

    @Override
    public String returnTableCreate(DatabaseCreateTableDTO request) {
        log.info("DatabaseCreateTableDTO request is: " + request);
        return tableColumnSQLService.get(comGetUuidType(request.getUuid())).createTable(request);
    }

    @Override
    public Map<String, Object> tableConstraint(SelectDataQuery request) throws SQLException {
        log.info("tableConstraint request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(tableColumnSQLService.get(
                        comGetUuidType(request.getUuid())).tableConstraintSQL(request))
        ) {
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("tableConstraint resultMap is: " + resultMap);
            return resultMap;
        }
    }

    @Override
    public void tableEditConstraint(DatabaseConstraintDTO request) throws SQLException {
        log.info("tableEditConstraint request is: " + request);
        tableColumnSQLService.get(comGetUuidType(request.getUuid())).editConstraint(request);
    }

    @Override
    public void tableEditPkConstraint(DatabaseConstraintPkDTO request) throws SQLException {
        log.info("tableEditPkConstraint request is: {}", request);
        tableColumnSQLService.get(comGetUuidType(request.getUuid())).editPkConstraint(request);
    }

    @Override
    public Map<String, String> tableDdl(SelectDataQuery request) throws SQLException {
        log.info("tableDdl request is: " + request);
        return tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableDdl(request);
    }

    @Override
    public Map<String, Object> tableColumnEdit(DatabaseCreUpdColumnDTO request) throws SQLException {
        log.info("tableColumnEdit request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            for (DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data : request.getData()) {
                TableColumnSQLService aa = tableColumnSQLService.get(comGetUuidType(request.getUuid()));
                switch (data.getOperationType()) {
                    case 1:
                        for (String list : aa.tableColumnAddSQL(data,
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getSchema())),
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getTableName())))) {
                            statement.execute(list);
                        }
                        break;
                    case 2:
                        statement.execute(aa.tableColumnDropSQL(data,
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getSchema())),
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getTableName()))));
                        break;
                    case 3:
                        for (String list : aa.tableColumnUpdateSQL(data,
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getSchema())),
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getTableName())),
                                request.getUuid())) {
                            statement.execute(list);
                        }
                        break;
                    default:
                        break;
                }
            }
            try (
                    ResultSet resultSet = statement.executeQuery(tableColumnSQLService.get(
                            comGetUuidType(request.getUuid()))
                            .tableColumnSQL(request.getOid(), DebugUtils.needQuoteName(request.getSchema()),
                            request.getTableName()))
            ) {
                Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("tableColumn resultMap is: " + resultMap);
                return resultMap;
            }
        }
    }

    @Override
    public void tableDataClose(String winId) {
        log.info("tableDataClose request is: " + winId);
        if (StringUtils.isNotEmpty(winId)) {
            resultSetMapDAO.overtimeCloseWin(winId);
        }
    }

    @Override
    public TablePKDataQuery tableDataEdit(TableDataEditQuery request) throws SQLException {
        log.info("tableColumnEdit request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            TablePKDataQuery tablePKDataQuery = new TablePKDataQuery();
            boolean isTableEdit = tableDataEdit(request, connection);
            if (!isTableEdit) {
                tablePKDataQuery.setMsg(LocaleStringUtils.transLanguage("2017"));
                tablePKDataQuery.setIsPKCreate(true);
                log.info("TablePKDataQuery is: {}", tablePKDataQuery);
                return tablePKDataQuery;
            }
            connection.setAutoCommit(false);
            for (TableDataEditQuery.TableDataDTO data : request.getData()) {
                TableColumnSQLService aa = tableColumnSQLService.get(comGetUuidType(request.getUuid()));
                switch (data.getOperationType()) {
                    case "INSERT":
                        String addSQL = aa.tableDataAddSQL(data,
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getSchema())),
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getTableName())));
                        statement.execute(addSQL);
                        break;
                    case "DELETE":
                        String dropSQL = aa.tableDataDropSQL(data,
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getSchema())),
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getTableName())));
                        statement.execute(dropSQL);
                        break;
                    case "UPDATE":
                        String updateSQL = aa.tableDataUpdateSQL(data,
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getSchema())),
                                DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getTableName())));
                        statement.execute(updateSQL);
                        break;
                    default:
                        break;
                }
            }
            connection.commit();
            tablePKDataQuery.setMsg("");
            tablePKDataQuery.setIsPKCreate(false);
            return tablePKDataQuery;
        }
    }

    private Boolean tableDataEdit(TableDataEditQuery request, Connection connection) throws SQLException {
        log.info("tableDataEdit request is: {}", request);
        String sql =
                tableColumnSQLService.get(comGetUuidType(request.getUuid()))
                        .tablePKConstraintSQL(request.getSchema(), request.getTableName());
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSetUnique = statement.executeQuery(sql)
        ) {
            if (resultSetUnique.next()) {
                log.info("resultSetUnique is: {}", true);
                return true;
            } else {
                log.info("resultSetUnique is: {}", false);
                return false;
            }
        }
    }

    @Override
    public void tableTruncate(TableNameDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            statement.execute(tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableTruncateSQL(
                    request.getSchema(), request.getTableName()));
        }
    }

    @Override
    public void tableVacuum(TableNameDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableVacuumSQL(
                    request.getSchema(), request.getTableName()));
        }
    }

    @Override
    public void tableReindex(TableNameDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableReindexSQL(
                    request.getSchema(), request.getTableName()));
        }
    }

    @Override
    public void tableRename(TableAlterDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableRenameSQL(
                    request.getSchema(), request.getTableName(), request.getGeneralPurpose()));
        }
    }

    @Override
    public void tableComment(TableAlterDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableCommentSQL(
                    request.getSchema(), request.getTableName(), request.getGeneralPurpose()));
        }
    }

    @Override
    public void tableAlterSchema(TableAlterDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableAlterSchemaSQL(
                    request.getSchema(), request.getTableName(), request.getGeneralPurpose()));
        }
    }

    @Override
    public void tableDrop(TableNameDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(
                    tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableDropSQL(request.getSchema(),
                            request.getTableName()));
        }
    }

    @Override
    public void tableAlterTablespace(TableAlterDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(
                    tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableAlterTablespaceSQL(
                            request.getSchema(), request.getTableName(), request.getGeneralPurpose()));
        }
    }

    public Map<String, Object> tableSequence(TableNameDTO request) throws SQLException {
        log.info("tableSequence request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(tableObjectSQLService.get(
                    comGetUuidType(request.getUuid()))
                    .tableSequenceSQL(request.getSchema(), request.getTableName()))) {
                Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("tableSequence resultMap is: " + resultMap);
                return resultMap;
            }
        }
    }

    public List<Map<String, Object>> tableAttribute(TableAttributeDTO request) throws SQLException {
        return tableObjectSQLService.get(conMap.get(request.getUuid())
                .getType()).tableAttributeSQL(request.getUuid(), request.getOid(), request.getTableType());
    }

    public void tableAnalyze(TableNameDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(comGetUuidType(request.getUuid())).tableAnalyzeSQL(
                    request.getSchema(), DebugUtils.needQuoteName(request.getTableName())));
        }
    }

    public Map<String, Object> tableAttributePartition(TableNameDTO request) throws SQLException {
        log.info("tableSequence request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(tableObjectSQLService.get(
                    comGetUuidType(request.getUuid())).tableAttributePartitionSQL(request.getOid()))) {
                Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("tableSequence resultMap is: " + resultMap);
                return resultMap;
            }
        }
    }

    @Override
    public void exportData(TableDataQuery request, HttpServletResponse response) throws SQLException, IOException {
        log.info("TableDataServiceImpl exportData request: " + request);
        String sql = tableObjectSQLService.get(comGetUuidType(request.getUuid())).exportTableData(
                request.getSchema(), request.getTableName(), request.getPageNum(), request.getPageSize(),
                request.getExpansion());

        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(TABLE_DATA_LIMIT_SQL,
                        DebugUtils.needQuoteName(request.getSchema()),
                        DebugUtils.needQuoteName(request.getTableName()), 1, 1))
        ) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<String> columnList = new ArrayList<>();
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                columnList.add(metaData.getColumnName(i + 1));
            }
            log.info("TableDataServiceImpl exportData columnList: " + columnList);

            String tableName = request.getTableName();
            String fileName = tableName + ".xlsx";
            if (request.getFileType().equals("Excel(xls)")) {
                fileName = tableName + ".xls";
            }

            exportDataFile(request, columnList, sql, fileName, response);
            log.info("TableDataServiceImpl exportData end: ");
        }
    }

    private void exportDataFile(
            TableDataQuery request, List<String> columnList, String sql, String fileName,
            HttpServletResponse response) throws SQLException, IOException {
        log.info("TableDataServiceImpl exportDataFile startTime: " + System.currentTimeMillis());

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

            int count = 0;
            String tableName = request.getTableName();
            ResultSet countResult = statement.executeQuery(String.format(COUNT_SQL,
                    DebugUtils.needQuoteName(request.getSchema()), DebugUtils.needQuoteName(tableName)));
            while (countResult.next()) {
                count = countResult.getInt("count");
            }
            log.info("TableDataServiceImpl exportDataFile count: " + count);

            String timeStamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
            connection.setAutoCommit(false);
            statement.execute(String.format(COURSE_SQL, "DS_" + timeStamp, sql));
            log.info("TableDataServiceImpl exportDataFile timeStamp: " + "DS_" + timeStamp);

            int index = 0;
            SXSSFSheet sheet = sxssfWorkbook.createSheet(tableName);
            SXSSFRow row = sheet.createRow(index);
            for (int i = 0; i < columnList.size(); i++) {
                row.createCell(i).setCellValue(columnList.get(i));
            }

            int size = count % 1000 == 0 ? count / 1000 : count / 1000 + 1;
            log.info("TableDataServiceImpl exportDataFile size: " + size);
            for (int s = 0; s < size; s++) {
                ResultSet resultSet = statement.executeQuery(String.format(FETCH_SQL, 1000, "DS_" + timeStamp));
                while (resultSet.next()) {
                    row = sheet.createRow(++index);
                    for (int i = 0; i < columnList.size(); i++) {
                        row.createCell(i).setCellValue(resultSet.getString(columnList.get(i)));
                    }
                }
            }

            sxssfWorkbook.write(outputStream);
            outputStream.flush();
            log.info("TableDataServiceImpl exportDataFile endTime: " + System.currentTimeMillis());
        }
    }
}

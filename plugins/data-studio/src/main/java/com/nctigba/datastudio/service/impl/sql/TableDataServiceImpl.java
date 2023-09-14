/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static java.lang.Math.ceil;

/**
 * TableDataServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class TableDataServiceImpl implements TableDataService {
    private final Map<String, ResultSet> resultSetMap = new HashMap<>();

    private final Map<String, Connection> connectionMap = new HashMap<>();

    private final Map<String, Statement> statementMap = new HashMap<>();
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
            statementCount.executeUpdate(tableObjectSQLService.get(conMap.get(request.getUuid())
                    .getType()).tableAnalyseSQL(schema, tableName));
        } catch (SQLException e) {
            log.info(e.toString());
        }
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                Statement statementCount = connection.createStatement();
                ResultSet resultSetCount = statementCount.executeQuery(
                        tableObjectSQLService.get(conMap.get(request.getUuid())
                                .getType()).tableDataCountSQL(schema, request.getTableName()));

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
                    conMap.get(request.getUuid()).getType()).tableDataSQL(schema,
                    tableName, start, request.getPageSize(), request.getExpansion());
            try (ResultSet resultSet = statement.executeQuery(sql);) {
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
                conMap.get(request.getUuid()).getType());
        String ddl = tableColumnSQLService1.tableColumnSQL(request.getOid(),
                DebugUtils.needQuoteName(request.getSchema()),
                request.getTableName());
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();

                ResultSet resultSet = statement.executeQuery(ddl);
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
                ResultSet resultSet = statement.executeQuery(tableColumnSQLService.get(conMap.get(request.getUuid())
                        .getType()).tableIndexSQL(request))
        ) {
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("tableIndex resultMap is: " + resultMap);
            return resultMap;
        }
    }

    @Override
    public void tableEditIndex(DatabaseIndexDTO request) throws SQLException {
        log.info("tableEditIndex request is: " + request);
        tableColumnSQLService.get(conMap.get(request.getUuid()).getType()).editIndex(request);
    }

    @Override
    public void tableCreate(DatabaseCreateTableDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            statement.execute(tableColumnSQLService.get(conMap.get(request.getUuid()).getType()).createTable(request));
        }
    }

    @Override
    public String returnTableCreate(DatabaseCreateTableDTO request) {
        log.info("DatabaseCreateTableDTO request is: " + request);
        return tableColumnSQLService.get(conMap.get(request.getUuid()).getType()).createTable(request);
    }

    @Override
    public Map<String, Object> tableConstraint(SelectDataQuery request) throws SQLException {
        log.info("tableConstraint request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(tableColumnSQLService.get(conMap.get(request.getUuid())
                        .getType()).tableConstraintSQL(request))
        ) {
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("tableConstraint resultMap is: " + resultMap);
            return resultMap;
        }
    }

    @Override
    public void tableEditConstraint(DatabaseConstraintDTO request) throws SQLException {
        log.info("tableEditConstraint request is: " + request);
        tableColumnSQLService.get(conMap.get(request.getUuid()).getType()).editConstraint(request);
    }

    @Override
    public void tableEditPkConstraint(DatabaseConstraintPkDTO request) throws SQLException {
        log.info("tableEditPkConstraint request is: {}", request);
        tableColumnSQLService.get(conMap.get(request.getUuid()).getType()).editPkConstraint(request);
    }

    @Override
    public Map<String, String> tableDdl(SelectDataQuery request) throws SQLException {
        log.info("tableDdl request is: " + request);
        return tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableDdl(request);
    }

    @Override
    public Map<String, Object> tableColumnEdit(DatabaseCreUpdColumnDTO request) throws SQLException {
        log.info("tableColumnEdit request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            for (DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data : request.getData()) {
                TableColumnSQLService aa = tableColumnSQLService.get(conMap.get(request.getUuid()).getType());
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
                    ResultSet resultSet = statement.executeQuery(tableColumnSQLService.get(conMap.get(request.getUuid())
                            .getType()).tableColumnSQL(request.getOid(), DebugUtils.needQuoteName(request.getSchema()),
                            request.getTableName()))
            ) {
                Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("tableColumn resultMap is: " + resultMap);
                return resultMap;
            }
        }
    }

    @Override
    public void tableDataClose(String winId) throws SQLException {
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
                tablePKDataQuery.setMsg(LocaleString.transLanguage("2017"));
                tablePKDataQuery.setIsPKCreate(true);
                log.info("TablePKDataQuery is: {}", tablePKDataQuery);
                return tablePKDataQuery;
            }
            connection.setAutoCommit(false);
            for (TableDataEditQuery.TableDataDTO data : request.getData()) {
                TableColumnSQLService aa = tableColumnSQLService.get(conMap.get(request.getUuid()).getType());
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
                tableColumnSQLService.get(conMap.get(request.getUuid()).getType())
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
            statement.execute(tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableTruncateSQL(
                    request.getSchema(), request.getTableName()));
        }
    }

    @Override
    public void tableVacuum(TableNameDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableVacuumSQL(
                    request.getSchema(), request.getTableName()));
        }
    }

    @Override
    public void tableReindex(TableNameDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableReindexSQL(
                    request.getSchema(), request.getTableName()));
        }
    }

    @Override
    public void tableRename(TableAlterDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableRenameSQL(
                    request.getSchema(), request.getTableName(), request.getGeneralPurpose()));
        }
    }

    @Override
    public void tableComment(TableAlterDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableCommentSQL(
                    request.getSchema(), request.getTableName(), request.getGeneralPurpose()));
        }
    }

    @Override
    public void tableAlterSchema(TableAlterDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableAlterSchemaSQL(
                    request.getSchema(), request.getTableName(), request.getGeneralPurpose()));
        }
    }

    @Override
    public void tableDrop(TableNameDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(
                    tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableDropSQL(request.getSchema(),
                            request.getTableName()));
        }
    }

    @Override
    public void tableAlterTablespace(TableAlterDTO request) throws SQLException {
        log.info("DatabaseCreateTableDTO request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            statement.execute(
                    tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableAlterTablespaceSQL(
                            request.getSchema(), request.getTableName(), request.getGeneralPurpose()));
        }
    }

    public Map<String, Object> tableSequence(TableNameDTO request) throws SQLException {
        log.info("tableSequence request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(tableObjectSQLService.get(conMap.get(request.getUuid())
                    .getType()).tableSequenceSQL(request.getSchema(), request.getTableName()))) {
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
            statement.execute(tableObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableAnalyzeSQL(
                    request.getSchema(), DebugUtils.needQuoteName(request.getTableName())));
        }
    }

    public Map<String, Object> tableAttributePartition(TableNameDTO request) throws SQLException {
        log.info("tableSequence request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(tableObjectSQLService.get(conMap.get(request.getUuid())
                    .getType()).tableAttributePartitionSQL(request.getOid()))) {
                Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("tableSequence resultMap is: " + resultMap);
                return resultMap;
            }
        }
    }

    @Override
    public void exportData(TableDataQuery request, HttpServletResponse response) throws SQLException, IOException {
        log.info("TableDataServiceImpl exportData request: " + request);
        try (
                XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
                OutputStream outputStream = response.getOutputStream();
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(tableObjectSQLService.get(
                        conMap.get(request.getUuid()).getType()).exportTableData(request.getSchema(),
                        request.getTableName(), request.getPageNum(), request.getPageSize(), request.getExpansion()))
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
            log.info("TableDataServiceImpl exportData hssfSheet: " + sheet.getLastRowNum());

            response.reset();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.addHeader("Response-Type", "blob");
            xssfWorkbook.write(outputStream);
            outputStream.flush();
        }
    }
}

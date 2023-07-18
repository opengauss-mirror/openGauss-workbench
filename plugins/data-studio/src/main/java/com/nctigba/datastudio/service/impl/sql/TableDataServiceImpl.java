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
import com.nctigba.datastudio.model.dto.WinInfoDTO;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.model.query.TableDataQuery;
import com.nctigba.datastudio.model.query.TablePKDataQuery;
import com.nctigba.datastudio.service.TableDataService;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.dao.ResultSetMapDAO.setWinMap;
import static com.nctigba.datastudio.dao.ResultSetMapDAO.winMap;
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

    public void setResultSetMap(String winId, ResultSet resultSet) {
        this.resultSetMap.put(winId, resultSet);
    }

    public void setConnectionMap(String winId, Connection connection) {
        this.connectionMap.put(winId, connection);
    }

    public void setStatementMap(String winId, Statement statement) {
        this.statementMap.put(winId, statement);
    }

    public ResultSet getResultSetMap(String winId) {
        return this.resultSetMap.get(winId);
    }

    public Connection getConnectionMap(String winId) {
        return this.connectionMap.get(winId);
    }

    public Statement getStatementMap(String winId) {
        return this.statementMap.get(winId);
    }

    public void removeResultSetMap(String winId) {
        this.resultSetMap.remove(winId);
    }

    public void removeConnectionMap(String winId) {
        this.connectionMap.remove(winId);
    }

    public void removeStatementMap(String winId) {
        this.statementMap.remove(winId);
    }

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
        Connection connection;
        Statement statement;
        ResultSet resultSet;
        if (resultSetMap.containsKey(request.getWinId())) {
            connection = getConnectionMap(request.getWinId());
            statement = getStatementMap(request.getWinId());
            resultSet = getResultSetMap(request.getWinId());
        } else {
            connection = connectionConfig.connectDatabase(request.getUuid());
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            resultSet = statement.executeQuery(tableObjectSQLService.get(conMap.get(request.getUuid())
                    .getType()).tableDataSQL(request.getSchema(), request.getTableName()));
            setConnectionMap(request.getWinId(), connection);
            setStatementMap(request.getWinId(), statement);
            setResultSetMap(request.getWinId(), resultSet);
        }
        TableDataDTO tableDataDTO = new TableDataDTO();
        try (
                Connection connectionCount = connectionConfig.connectDatabase(request.getUuid());
                Statement statementCount = connectionCount.createStatement()
        ) {
            statementCount.executeUpdate(tableObjectSQLService.get(conMap.get(request.getUuid())
                    .getType()).tableAnalyseSQL(request.getSchema(), request.getTableName()));
        } catch (SQLException e) {
            log.info(e.toString());
        }
        try (
                Statement statementCount = connection.createStatement();
                ResultSet resultSetCount = statementCount.executeQuery(
                        tableObjectSQLService.get(conMap.get(request.getUuid())
                                .getType()).tableDataCountSQL(request.getSchema(), request.getTableName()))
        ) {
            resultSetCount.next();
            Integer count = resultSetCount.getInt(1);
            tableDataDTO.setTableDataDTO(request);
            tableDataDTO.setDataSize(count);
            tableDataDTO.setPageTotal((int) ceil((double) count / request.getPageSize()));
            statement.setFetchSize(request.getPageSize());
            resultSet.setFetchSize(request.getPageSize());
            WinInfoDTO winInfoDTO = new WinInfoDTO();
            winInfoDTO.setWinInfoDTO(request.getUuid(), request.getSchema(), request.getTableName());
            setWinMap(request.getWinId(), winInfoDTO);
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet, request.getPageSize(),
                    request.getPageNum());
            tableDataDTO.setData(resultMap);
            return tableDataDTO;
        }
    }

    @Override
    public Map<String, Object> tableColumn(SelectDataQuery request) throws SQLException {
        log.info("tableColumn request is: " + request);
        TableColumnSQLService tableColumnSQLService1 = tableColumnSQLService.get(
                conMap.get(request.getUuid()).getType());
        String ddl = tableColumnSQLService1.tableColumnSQL(request.getOid(), request.getSchema(),
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
                                DebugUtils.containsSqlInjection(request.getSchema()),
                                DebugUtils.containsSqlInjection(request.getTableName()))) {
                            statement.execute(list);
                        }
                        break;
                    case 2:
                        statement.execute(aa.tableColumnDropSQL(data,
                                DebugUtils.containsSqlInjection(request.getSchema()),
                                DebugUtils.containsSqlInjection(request.getTableName())));
                        break;
                    case 3:
                        for (String list : aa.tableColumnUpdateSQL(data,
                                DebugUtils.containsSqlInjection(request.getSchema()),
                                DebugUtils.containsSqlInjection(request.getTableName()),
                                request.getUuid())) {
                            statement.execute(list);
                        }
                        break;
                }
            }
            try (
                    ResultSet resultSet = statement.executeQuery(tableColumnSQLService.get(conMap.get(request.getUuid())
                            .getType()).tableColumnSQL(request.getOid(), request.getSchema(),
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
        log.info("tableDataEdit request is: {}", request);
        log.info("winMap is: {}", winMap);
        Connection connection = getConnectionMap(request.getWinId());
        ResultSet resultSet = getResultSetMap(request.getWinId());
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
            data.getType().rs(resultSet, data);
        }
        connection.commit();
        tablePKDataQuery.setMsg("");
        tablePKDataQuery.setIsPKCreate(false);
        return tablePKDataQuery;
    }

    private Boolean tableDataEdit(TableDataEditQuery request, Connection connection) throws SQLException {
        log.info("tableDataEdit request is: {}", request);
        log.info("winMap is: {}", winMap);
        if (!winMap.containsKey(request.getWinId())) {
            throw new CustomException(LocaleString.transLanguage("1009"));
        }
        String uuid = winMap.get(request.getWinId()).getUuid();
        String schema = winMap.get(request.getWinId()).getSchema();
        String tableName = winMap.get(request.getWinId()).getTableName();
        String sql = tableColumnSQLService.get(conMap.get(uuid).getType()).tablePKConstraintSQL(schema, tableName);
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
}

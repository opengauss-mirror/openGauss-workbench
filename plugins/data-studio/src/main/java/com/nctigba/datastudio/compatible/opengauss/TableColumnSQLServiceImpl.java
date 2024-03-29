/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.TableColumnSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.constants.CommonConstants;
import com.nctigba.datastudio.constants.SqlConstants;
import com.nctigba.datastudio.model.dto.ConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintPkDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateTableDTO;
import com.nctigba.datastudio.model.dto.DatabaseIndexDTO;
import com.nctigba.datastudio.model.dto.IndexDTO;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.model.query.TablePartitionInfoQuery;
import com.nctigba.datastudio.model.query.TableUnderlyingInfoQuery;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_DEFAULT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_TABLE_COLUMN_ADD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_TABLE_COLUMN_DROPQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CHECK_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COLUMN_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COLUMN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMENT_ON_COLUMN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COMMENT_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CONSTRAINT_COMMENT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CONSTRAINT_DROP_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CONSTRAINT_TABLE_COLUMN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CONSTRAINT_UNIQUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_TABLE_EXISTS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_UNLOGGED_TABLE_EXISTS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_UNLOGGED_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DEFAULT_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_DEFAULT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.FILLFACTOR_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.FOREIGN_KEY_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_COLUMN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.HASH_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INTERVAL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LEFT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.LIST_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NOT_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NULL_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.OIDS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PARTIAL_CLUSTER_KEY_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PARTITION_BY_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.PRIMARY_KEY_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_LF_COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.RANGE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.RENAME_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.RIGHT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.SET_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.TABLESPACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TO_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TYPE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.UNIQUE_IMMEDIATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.UNIQUE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.UNIQUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.WITH_DOUBLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.WITH_SQL;

/**
 * TableColumnSQLService achieve
 *
 * @since 2023-06-26
 */
@Slf4j
@Service
public class TableColumnSQLServiceImpl implements TableColumnSQLService {
    private final String[] arrayRefVar = {
            "bigint", "smallint", "tinyint", "integer", "int16", "real", "double precision"};
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String tableConstraintSQL(SelectDataQuery request) {
        String ddl = String.format(SqlConstants.CONSTRAINT_TABLE_SQL, request.getSchema(), request.getTableName());
        log.info("tableConstraintSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tablePKConstraintSQL(String schema, String tableName) {
        String ddl = String.format(SqlConstants.UNIQUE_CONSTRAINT_TABLE_SQL, schema, tableName);
        log.info("tableConstraintSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public void editConstraint(DatabaseConstraintDTO request) throws SQLException {
        List<ConstraintDTO> list = request.getConstraints();
        log.info("List<ConstraintDTO> request is: " + list.toString());
        Connection connection = null;
        Statement statement = null;
        try {
            connection = connectionConfig.connectDatabase(request.getUuid());
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            for (ConstraintDTO obj : list) {
                if (obj.getType() == 1) {
                    this.addConstraint(request, obj, statement);
                } else if (obj.getType() == 2) {
                    statement.addBatch(String.format(SqlConstants.CONSTRAINT_DROP_SQL, request.getSchema(),
                            request.getTableName(), obj.getConName()));
                } else if (obj.getType() == 3) {
                    this.updateConstraint(request, obj, statement);
                }
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw new SQLException(e);
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Override
    public void editPkConstraint(DatabaseConstraintPkDTO request) throws SQLException {
        log.info("DatabaseConstraintPkDTO request is: {}", request);
        String attName = request.getTableName() + "_PK";
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sqlPk = String.format(SqlConstants.CONSTRAINT_PRIMARY_SQL, request.getSchema(),
                    request.getTableName(),
                    attName, request.getColumn());
            log.info("sqlPK request is: {}", sqlPk);
            statement.execute(sqlPk);
        }
    }

    @Override
    public String tableIndexSQL(SelectDataQuery request) {
        String ddl = String.format(SqlConstants.INDEX_TABLE_SQL, request.getTableName(), request.getSchema());
        log.info("tableIndexSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public void editIndex(DatabaseIndexDTO request) throws SQLException {
        List<IndexDTO> list = request.getIndexs();
        log.info("List<IndexDTO> request is: " + list.toString());
        Connection connection = null;
        Statement statement = null;
        try {
            connection = connectionConfig.connectDatabase(request.getUuid());
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            for (IndexDTO obj : list) {
                if (obj.getType() == 1) {
                    this.addIndex(request, obj, statement);
                } else if (obj.getType() == 2) {
                    statement.addBatch(
                            String.format(SqlConstants.INDEX_DROP_SQL, request.getSchema(), obj.getIndexName()));
                } else if (obj.getType() == 3) {
                    this.updateIndex(request, obj, statement);
                }
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw new SQLException(e);
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Override
    public String tableColumnSQL(String oid, String schema, String tableName) {
        String ddl = String.format(GET_COLUMN_SQL, oid, oid, schema, tableName);
        log.info("tableColumnSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public List<String> tableColumnAddSQL(
            DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data, String schema,
            String tableName) {
        List<String> list = new ArrayList<>();
        StringBuilder ddl = new StringBuilder(String.format(ALTER_TABLE_COLUMN_ADD_SQL, schema, tableName,
                data.getNewColumnName()));
        log.info("tableColumnAddSQL response is: " + ddl);
        list.add(String.valueOf(ddl.append(columnAddSQL(data)).append(SEMICOLON)));
        list.add(commentAddSQL(data, schema, tableName));
        log.info("tableColumnAddSQL response is: " + ddl);
        return list;

    }

    public String columnAddSQL(DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data) {
        log.info("columnAddSQL response is: {}", data);
        StringBuilder ddl = new StringBuilder();
        StringBuilder precision = new StringBuilder();
        if (!StringUtils.isEmpty(data.getPrecision())) {
            log.info("1columnAddSQL response is: {}", data.getPrecision());
            precision.append(LEFT_BRACKET).append(DebugUtils.containsSqlInjection(data.getPrecision()));
            log.info("2columnAddSQL response is: {}", data.getScope());
            if (!StringUtils.isEmpty(data.getScope())) {
                precision.append(COMMA).append(DebugUtils.containsSqlInjection(data.getScope()));
            }
            precision.append(RIGHT_BRACKET);
        }
        if (data.getType().endsWith("[]")) {
            ddl.append(data.getType(), 0, data.getType().length() - 2);
            ddl.append(precision).append("[]");
        } else {
            ddl.append(data.getType());
            ddl.append(precision);
        }
        if (data.getIsEmpty() != null && data.getIsEmpty()) {
            ddl.append(NOT_KEYWORD_SQL).append(NULL_KEYWORD_SQL);
        }
        if (!StringUtils.isEmpty(data.getDefaultValue())) {
            if (Arrays.asList(arrayRefVar).contains(data.getType())) {
                ddl.append(DEFAULT_KEYWORD_SQL).append(DebugUtils.containsSqlInjection(data.getDefaultValue()));
            } else {
                ddl.append(DEFAULT_KEYWORD_SQL).append(QUOTES).append(
                        DebugUtils.containsSqlInjection(data.getDefaultValue())).append(QUOTES);
            }
        }
        if (data.getIsOnly() != null && data.getIsOnly()) {
            ddl.append(UNIQUE_KEYWORD_SQL);
        }
        return ddl.toString();
    }

    public String commentAddSQL(
            DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data, String schema,
            String tableName) {
        if (StringUtils.isNotEmpty(data.getComment())) {
            String ddl = String.format(COMMENT_ON_COLUMN_SQL, schema, tableName, data.getNewColumnName(),
                    data.getComment());
            log.info("commentAddSQL response is: " + ddl);
            return ddl;
        }
        return "";
    }


    @Override
    public String tableColumnDropSQL(
            DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data, String schema,
            String tableName) {
        String ddl = String.format(ALTER_TABLE_COLUMN_DROPQL, schema, tableName, data.getColumnName());
        log.info("tableColumnDropSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public List<String> tableColumnUpdateSQL(
            DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data,
            String schema, String tableName, String uuid) throws SQLException {
        List<String> list = new ArrayList<>();
        StringBuilder ddl = new StringBuilder(ALTER_TABLE_SQL).append(schema).append(POINT)
                .append(tableName);
        String column;
        if (!StringUtils.isEmpty(data.getNewColumnName()) && !data.getNewColumnName().equals(data.getColumnName())) {
            String rename = ddl + RENAME_KEYWORD_SQL + COLUMN_KEYWORD_SQL
                    + DebugUtils.containsSqlInjection(data.getColumnName()) + TO_KEYWORD_SQL
                    + DebugUtils.containsSqlInjection(data.getNewColumnName()) + SEMICOLON;
            list.add(rename);
            column = DebugUtils.containsSqlInjection(data.getNewColumnName());
        } else {
            column = DebugUtils.containsSqlInjection(data.getColumnName());
        }
        ddl.append(SPACE).append(ALTER_SQL).append(COLUMN_KEYWORD_SQL).append(column).append(SPACE);
        if (!StringUtils.isEmpty(data.getType())) {
            StringBuilder alter = new StringBuilder(ddl);
            StringBuilder precision = new StringBuilder();
            if (!StringUtils.isEmpty(data.getPrecision())) {
                precision.append(LEFT_BRACKET).append(DebugUtils.containsSqlInjection(data.getPrecision()));
                if (StringUtils.isNotEmpty(data.getScope())) {
                    precision.append(COMMA).append(DebugUtils.containsSqlInjection(data.getScope()));
                }
                precision.append(RIGHT_BRACKET);
            }
            alter.append(TYPE_KEYWORD_SQL);
            if (data.getType().endsWith("[]")) {
                alter.append(data.getType(), 0, data.getType().length() - 2)
                        .append(precision).append("[]").append(SEMICOLON);
            } else {
                alter.append(data.getType());
                alter.append(precision).append(SEMICOLON);
            }
            list.add(alter.toString());
        }
        if (data.getIsEmpty() != null) {
            StringBuilder alter = new StringBuilder(ddl);
            if (data.getIsEmpty()) {
                alter.append(SET_KEYWORD_SQL).append(NOT_KEYWORD_SQL).append(NULL_KEYWORD_SQL).append(SEMICOLON);
            } else {
                alter.append(SPACE).append(DROP_SQL).append(NOT_KEYWORD_SQL).append(NULL_KEYWORD_SQL).append(SEMICOLON);
            }
            list.add(alter.toString());
        }
        if (StringUtils.isNotEmpty(data.getDefaultValue()) && !data.getDefaultValue().equals("")) {
            String value;
            if (Arrays.asList(arrayRefVar).contains(data.getType())) {
                value = DebugUtils.containsSqlInjection(data.getDefaultValue());
            } else {
                value = QUOTES + DebugUtils.containsSqlInjection(data.getDefaultValue()) + QUOTES;
            }
            String alterDrop = String.format(ALTER_DEFAULT_SQL, schema, tableName, column, value);
            list.add(alterDrop);
        } else {
            String ddlDrop = String.format(DROP_DEFAULT_SQL, schema, tableName, column);
            list.add(ddlDrop);
        }
        if (data.getIsOnly() != null) {
            if (data.getIsOnly()) {
                String unique = String.format(CONSTRAINT_UNIQUE_SQL, schema, tableName,
                        tableName + "_" + column + "_key", column);
                list.add(unique);
            } else {
                String uniqueKey = getConname(String.format(CONSTRAINT_TABLE_COLUMN_SQL, schema, tableName,
                                StringUtils.isEmpty(data.getNewColumnName()) ?
                                        DebugUtils.containsSqlInjection(data.getColumnName()) :
                                        DebugUtils.containsSqlInjection(data.getNewColumnName())),
                        uuid);
                String sql = String.format(CONSTRAINT_DROP_SQL, schema, tableName, uniqueKey);
                list.add(sql);
            }
        }
        if (StringUtils.isNotEmpty(data.getComment())) {
            String sql = String.format(COMMENT_ON_COLUMN_SQL, schema, tableName, column, data.getComment());
            log.info("Comment response is: " + sql);
            list.add(sql);
        }
        log.info("tableColumnUpdateSQL response is: " + list);
        return list;
    }


    private void addConstraint(DatabaseConstraintDTO request, ConstraintDTO obj, Statement statement)
            throws SQLException {
        if ("u".equals(obj.getConType())) {
            if (obj.getConDeferrable() == null || !obj.getConDeferrable()) {
                statement.addBatch(
                        String.format(SqlConstants.CONSTRAINT_UNIQUE_SQL, request.getSchema(), request.getTableName(),
                                obj.getConName(), obj.getAttName()));
            } else {
                statement.addBatch(String.format(SqlConstants.CONSTRAINT_UNIQUE_IMMEDIATE_SQL, request.getSchema(),
                        request.getTableName(), obj.getConName(), obj.getAttName()));
            }
        } else if ("p".equals(obj.getConType())) {
            if (obj.getConDeferrable() == null || !obj.getConDeferrable()) {
                statement.addBatch(
                        String.format(SqlConstants.CONSTRAINT_PRIMARY_SQL, request.getSchema(), request.getTableName(),
                                obj.getConName(), obj.getAttName()));
            } else {
                statement.addBatch(String.format(SqlConstants.CONSTRAINT_PRIMARY_IMMEDIATE_SQL, request.getSchema(),
                        request.getTableName(), obj.getConName(), obj.getAttName()));
            }
        } else if ("c".equals(obj.getConType())) {
            if (org.apache.commons.lang3.StringUtils.isAnyEmpty(obj.getConstraintDef())) {
                throw new CustomException("Expression is empty");
            }
            if (obj.getConstraintDef() != null && obj.getConstraintDef().toLowerCase().trim().startsWith("check")) {
                statement.addBatch(
                        String.format(SqlConstants.CONSTRAINT_NO_CHECK_SQL, request.getSchema(), request.getTableName(),
                                obj.getConName(), obj.getConstraintDef()));
            } else {
                statement.addBatch(
                        String.format(SqlConstants.CONSTRAINT_CHECK_SQL, request.getSchema(), request.getTableName(),
                                obj.getConName(), obj.getConstraintDef()));
            }
        } else if ("f".equals(obj.getConType())) {
            statement.addBatch(
                    String.format(SqlConstants.CONSTRAINT_FOREIGN_KEY_SQL, request.getSchema(), request.getTableName()
                            , obj.getConName(), obj.getAttName(), obj.getNspName(), obj.getTbName(), obj.getColName()));
        } else if ("s".equals(obj.getConType())) {
            statement.addBatch(String.format(SqlConstants.CONSTRAINT_PARTIAL_CLUSTER_KEY_SQL, request.getSchema(),
                    request.getTableName(), obj.getConName(), obj.getAttName()));
        }
        if (!StringUtils.isEmpty(obj.getDescription())) {
            statement.addBatch(String.format(SqlConstants.CONSTRAINT_COMMENT_SQL, obj.getConName(), request.getSchema(),
                    request.getTableName(), obj.getDescription()));
        }
    }

    private void updateConstraint(
            DatabaseConstraintDTO request, ConstraintDTO obj,
            Statement statement) throws SQLException {
        statement.addBatch(String.format(SqlConstants.CONSTRAINT_DROP_SQL, request.getSchema(), request.getTableName(),
                obj.getOldConName()));
        this.addConstraint(request, obj, statement);
    }

    private void addIndex(DatabaseIndexDTO request, IndexDTO obj, Statement statement) throws SQLException {
        statement.addBatch(this.addIndexSQL(DebugUtils.containsSqlInjection(request.getSchema()),
                DebugUtils.containsSqlInjection(request.getTableName()), obj));
        if (!StringUtils.isEmpty(obj.getDescription())) {
            statement.addBatch(this.addIndexCommentSQL(DebugUtils.containsSqlInjection(request.getSchema()), obj));
        }
    }

    private String addIndexSQL(String schema, String tableName, IndexDTO obj) {
        String unique = CommonConstants.SPACE;
        if (obj.getUnique() != null && obj.getUnique()) {
            unique = " UNIQUE ";
        }
        String amname = CommonConstants.SPACE;
        if (!StringUtils.isEmpty(obj.getAmName())) {
            amname = " using  " + DebugUtils.containsSqlInjection(obj.getAmName());
        }
        String att = DebugUtils.containsSqlInjection(obj.getAttName());
        if (StringUtils.isEmpty(att) && !StringUtils.isEmpty(obj.getExpression())) {
            att = DebugUtils.containsSqlInjection(obj.getExpression());
        }
        return String.format(SqlConstants.INDEX_CREATE_SQL, unique, obj.getIndexName(), schema,
                tableName, amname, att);
    }

    private String addIndexCommentSQL(String schema, IndexDTO obj) {
        if (!StringUtils.isEmpty(obj.getDescription())) {
            return String.format(SqlConstants.INDEX_COMMENT_SQL, schema, obj.getIndexName(), obj.getDescription());
        }
        return null;
    }

    private void updateIndex(DatabaseIndexDTO request, IndexDTO obj, Statement statement) throws SQLException {
        statement.addBatch(String.format(SqlConstants.INDEX_DROP_SQL, request.getSchema(), obj.getOldIndexName()));
        this.addIndex(request, obj, statement);
    }

    private String getConname(String sql, String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            String conName = null;
            while (resultSet.next()) {
                conName = resultSet.getString("conname");
            }
            return conName;
        }
    }

    public String createTable(DatabaseCreateTableDTO request) {
        log.info("createTable response is: {}", request);
        TableUnderlyingInfoQuery tableUnderlyingInfoQuery = request.getTableInfo();
        String ddl;
        if (tableUnderlyingInfoQuery.getIsExists() && tableUnderlyingInfoQuery.getTableType().equals("UNLOGGED")) {
            ddl = String.format(CREATE_UNLOGGED_TABLE_EXISTS_SQL, request.getSchema(),
                    tableUnderlyingInfoQuery.getTableName());
        } else if (tableUnderlyingInfoQuery.getIsExists()) {
            ddl = String.format(CREATE_TABLE_EXISTS_SQL, request.getSchema(), tableUnderlyingInfoQuery.getTableName());
        } else if (tableUnderlyingInfoQuery.getTableType().equals("UNLOGGED")) {
            ddl = String.format(CREATE_UNLOGGED_TABLE_SQL, request.getSchema(),
                    tableUnderlyingInfoQuery.getTableName());
        } else {
            ddl = String.format(CREATE_TABLE_SQL, request.getSchema(), tableUnderlyingInfoQuery.getTableName());
        }
        log.info("createTableFormat response is: " + ddl);
        StringBuilder cteate = new StringBuilder(ddl);
        StringBuilder olumnComment = new StringBuilder();
        for (int i = 0; i < request.getColumn().size(); i++) {
            DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO col = request.getColumn().get(i);
            String columnSql = columnAddSQL(col);
            cteate.append(col.getNewColumnName()).append(SPACE).append(columnSql);
            log.info("create response is: {}", cteate);
            if (i < request.getColumn().size() - 1) {
                cteate.append(QUOTES_LF_COMMA);
            } else {
                cteate.append(LF);
            }
            olumnComment.append(commentAddSQL(col, request.getSchema(), tableUnderlyingInfoQuery.getTableName()));
        }
        StringBuilder constraintsComment = new StringBuilder();
        for (int i = 0; i < request.getConstraints().size(); i++) {
            ConstraintDTO constraintDTO = request.getConstraints().get(i);
            String constraintsSql = getConstraintSQL(constraintDTO);
            cteate.append(constraintsSql);
            if (i < request.getConstraints().size() - 1) {
                cteate.append(QUOTES_LF_COMMA);
            } else {
                cteate.append(LF);
            }
            if (StringUtils.isNotEmpty(constraintDTO.getDescription())) {
                constraintsComment.append(
                        String.format(CONSTRAINT_COMMENT_SQL, constraintDTO.getConName(), request.getSchema(),
                                tableUnderlyingInfoQuery.getTableName(), constraintDTO.getDescription())).append(LF);
            }
        }
        cteate.append(RIGHT_BRACKET);
        if (tableUnderlyingInfoQuery.getStorage().equals("COLUMN")) {
            cteate.append(COLUMN_SQL);
        } else if (tableUnderlyingInfoQuery.getIsOids() && tableUnderlyingInfoQuery.getFillingFactor() != 100) {
            cteate.append(String.format(WITH_DOUBLE_SQL, FILLFACTOR_SQL, tableUnderlyingInfoQuery.getFillingFactor(),
                    OIDS_SQL));
        } else if (tableUnderlyingInfoQuery.getIsOids()) {
            cteate.append(String.format(WITH_SQL, OIDS_SQL));
        } else if (tableUnderlyingInfoQuery.getFillingFactor() != 100) {
            cteate.append(String.format(WITH_SQL, FILLFACTOR_SQL, tableUnderlyingInfoQuery.getFillingFactor()));
        }
        cteate.append(String.format(TABLESPACE_SQL, tableUnderlyingInfoQuery.getTableSpace())).append(SEMICOLON).append(
                getPartitionSQL(request.getPartitionInfo())).append(LF);
        StringBuilder indexComment = new StringBuilder();
        for (var index : request.getIndexs()) {
            String indexSql = addIndexSQL(request.getSchema(), tableUnderlyingInfoQuery.getTableName(), index);
            cteate.append(indexSql).append(LF);
            if (StringUtils.isNotEmpty(index.getDescription())) {
                indexComment.append(addIndexCommentSQL(request.getSchema(), index)).append(LF);
            }
        }

        if (StringUtils.isNotEmpty(tableUnderlyingInfoQuery.getComment()) && !tableUnderlyingInfoQuery.getComment()
                .equals("")) {
            cteate.append(String.format(COMMENT_TABLE_SQL, request.getSchema(), tableUnderlyingInfoQuery.getTableName(),
                    tableUnderlyingInfoQuery.getComment()));
        }
        cteate.append(indexComment).append(olumnComment).append(constraintsComment);
        log.info("createTable response is: {}", cteate);
        return cteate.toString();
    }

    private String getPartitionSQL(TablePartitionInfoQuery request) {
        log.info("getPartitionSQL response is: {}", request);
        if (request != null && StringUtils.isNotEmpty(
                request.getPartitionName()) && !request.getPartitionName().equals("")) {
            StringBuilder partition = new StringBuilder().append(PARTITION_BY_SQL);
            if (request.getType().equals("range")) {
                partition.append(String.format(RANGE_SQL, request.getPartitionColumn(), request.getPartitionName(),
                        request.getPartitionValue(), request.getTableSpace()));
            } else if (request.getType().equals("interval")) {
                partition.append(String.format(INTERVAL_SQL, request.getPartitionColumn(), request.getValueInterval(),
                        request.getPartitionName(), request.getPartitionValue(), request.getTableSpace()));
            } else if (request.getType().equals("list")) {
                partition.append(String.format(LIST_SQL, request.getPartitionColumn(), request.getPartitionName(),
                        request.getPartitionValue(), request.getTableSpace()));
            } else if (request.getType().equals("hash")) {
                partition.append(String.format(HASH_SQL, request.getPartitionColumn(), request.getPartitionName(),
                        request.getTableSpace()));
            }
            partition.append(SEMICOLON);
            return partition.toString();
        }
        return "";
    }

    private String getConstraintSQL(ConstraintDTO request) {
        StringBuilder partition = new StringBuilder();
        if (StringUtils.isNotEmpty(request.getConType())) {
            if (request.getConType().equals("u")) {
                if (request.getConDeferrable() != null && request.getConDeferrable()) {
                    partition.append(String.format(UNIQUE_IMMEDIATE_SQL, request.getConName(), request.getAttName()));
                } else {
                    partition.append(String.format(UNIQUE_SQL, request.getConName(), request.getAttName()));
                }
            } else if (request.getConType().equals("p")) {
                partition.append(String.format(PRIMARY_KEY_SQL, request.getConName(), request.getAttName()));
            } else if (request.getConType().equals("c")) {
                partition.append(String.format(CHECK_SQL, request.getConName(), request.getAttName()));
            } else if (request.getConType().equals("f")) {
                partition.append(
                        String.format(FOREIGN_KEY_SQL, request.getConName(), request.getAttName(), request.getNspName(),
                                request.getTbName(), request.getColName()));
            } else if (request.getConType().equals("s")) {
                partition.append(String.format(PARTIAL_CLUSTER_KEY_SQL, request.getConName(), request.getAttName()));
            }
            return partition.toString();
        }
        return "";

    }
}

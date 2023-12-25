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
 *  ForeignTableSqlServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/ForeignTableSqlServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.ForeignTableSqlService;
import com.nctigba.datastudio.compatible.TableColumnSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.ForeignTableQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.ExecuteUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DESCRIPTION;
import static com.nctigba.datastudio.constants.CommonConstants.FDW_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.FILE_FDW;
import static com.nctigba.datastudio.constants.CommonConstants.MYSQL;
import static com.nctigba.datastudio.constants.CommonConstants.MYSQL_FDW;
import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.NSP_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.CommonConstants.ORACLE;
import static com.nctigba.datastudio.constants.CommonConstants.ORACLE_FDW;
import static com.nctigba.datastudio.constants.CommonConstants.POSTGRES_FDW;
import static com.nctigba.datastudio.constants.CommonConstants.REL_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SRV_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMENT_ON_COLUMN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_EXTENSION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_FOREIGN_SERVER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_MAPPING_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DEFAULT_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DELETE_FOREIGN_MAPPING_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DELETE_FOREIGN_SERVER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DELETE_FOREIGN_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.FAR_OPTION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.FOREIGN_TABLE_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;
import static com.nctigba.datastudio.constants.SqlConstants.LEFT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.MAPPING_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NOT_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NULL_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_EXTENSION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_FOREIGN_SERVER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_LF_COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.RIGHT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DDL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DESCRIPTION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.UNIQUE_KEYWORD_SQL;
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;
import static com.nctigba.datastudio.utils.SecretUtils.desEncrypt;

/**
 * ForeignTableSQLServiceImpl
 *
 * @since 2023-10-16
 */
@Slf4j
@Service
public class ForeignTableSqlServiceImpl implements ForeignTableSqlService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Autowired
    private MetaDataByJdbcService metaDataByJdbcService;
    private Map<String, TableColumnSQLService> tableColumnSQLService;

    @Resource
    public void setTableColumnSQLService(List<TableColumnSQLService> sqlServiceList) {
        tableColumnSQLService = new HashMap<>();
        for (TableColumnSQLService service : sqlServiceList) {
            tableColumnSQLService.put(service.type(), service);
        }
    }

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public List<Map<String, String>> queryServer(ForeignTableQuery request) throws SQLException {
        log.info("ForeignTableSqlServiceImpl queryServer request: " + request);
        String sql = String.format(QUERY_FOREIGN_SERVER_SQL, DebugUtils.needQuoteName(request.getSchema()));
        log.info("ForeignTableSqlServiceImpl queryServer sql: " + sql);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            List<Map<String, String>> list = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(NAME, resultSet.getString(SRV_NAME));
                String fdwName = resultSet.getString(FDW_NAME);
                if (POSTGRES_FDW.equals(fdwName)) {
                    map.put(TYPE, OPENGAUSS);
                } else if (MYSQL_FDW.equals(fdwName)) {
                    map.put(TYPE, MYSQL);
                } else if (ORACLE_FDW.equals(fdwName)) {
                    map.put(TYPE, ORACLE);
                } else {
                    map.put(TYPE, "File");
                }
                list.add(map);
            }

            log.info("ForeignTableSqlServiceImpl queryServer list: " + list);
            return list;
        }
    }

    @Override
    public void create(ForeignTableQuery request) throws SQLException {
        log.info("ForeignTableSqlServiceImpl create request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            createExtension(statement, request.getDatasourceType());
            createForeignTable(statement, request);
        }
    }

    @Override
    public void deleteForeignTable(ForeignTableQuery request) throws SQLException {
        log.info("ForeignTableSqlServiceImpl deleteForeignTable request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid())
        ) {
            String sql = String.format(DELETE_FOREIGN_TABLE_SQL, DebugUtils.needQuoteName(request.getSchema()),
                    DebugUtils.needQuoteName(request.getForeignTable()));
            ExecuteUtils.execute(connection, sql);
        }
    }

    @Override
    public void deleteForeignServer(ForeignTableQuery request) throws SQLException {
        log.info("ForeignTableSqlServiceImpl deleteForeignServer request: " + request);
        String foreignServer = request.getForeignServer();
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(MAPPING_NAME_SQL, foreignServer));
        ) {
            List<String> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getString("usename"));
            }
            for (String s : list) {
                ExecuteUtils.execute(connection, String.format(DELETE_FOREIGN_MAPPING_SQL, s, foreignServer));
            }

            ExecuteUtils.execute(connection, String.format(DELETE_FOREIGN_SERVER_SQL, foreignServer));
        }
    }

    @Override
    public Map<String, String> ddl(ForeignTableQuery request) throws SQLException {
        log.info("ForeignTableSqlServiceImpl ddl request: " + request);
        Map<String, String> resultMap = new HashMap<>();
        String sql = String.format(TABLE_DDL_SQL, DebugUtils.needQuoteName(request.getSchema()),
                DebugUtils.needQuoteName(request.getForeignTable()));
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                String tableDef = resultSet.getString("pg_get_tabledef");
                if (StringUtils.isEmpty(tableDef)) {
                    throw new CustomException(LocaleStringUtils.transLanguage("2010"));
                }
                resultMap.put(RESULT, tableDef);
            }

            log.info("ForeignTableSqlServiceImpl ddl resultMap: " + resultMap);
            return resultMap;
        }
    }

    @Override
    public List<Map<String, String>> attribute(ForeignTableQuery request) throws SQLException {
        log.info("ForeignTableSqlServiceImpl attribute request: " + request);
        List<Map<String, String>> list = new ArrayList<>();
        String sql = String.format(FOREIGN_TABLE_ATTRIBUTE_SQL, request.getForeignTable(), request.getSchema());
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            List<String> resultList = new ArrayList<>();
            while (resultSet.next()) {
                String srvOptions = resultSet.getString("srvoptions");
                String ftOptions = resultSet.getString("ftoptions");
                Map<String, String> serverMap = parseString(srvOptions);
                Map<String, String> tableMap = parseString(ftOptions);

                resultList.add(resultSet.getString(REL_NAME));
                resultList.add(covertType(resultSet.getString(FDW_NAME)));
                resultList.add(resultSet.getString(SRV_NAME));
                resultList.add(serverMap.get("host"));
                resultList.add(serverMap.get("port"));
                resultList.add(resultSet.getString(NSP_NAME));
                resultList.add(tableMap.get("schema_name"));
                resultList.add(serverMap.get("dbname"));
                resultList.add(tableMap.get("table_name"));
                resultList.add(resultSet.getString(DESCRIPTION));
            }

            for (int i = 0; i < resultList.size(); i++) {
                Map<String, String> map = new HashMap<>();
                map.put(LocaleStringUtils.transLanguage(String.valueOf(i + 4001)), resultList.get(i));
                list.add(map);
            }
            log.info("ForeignTableSqlServiceImpl attribute list: " + list);
            return list;
        }
    }

    @Override
    public String test(ForeignTableQuery request) {
        return metaDataByJdbcService.versionSQL(
                GET_URL_JDBC + request.getRemoteHost() + ":" + request.getRemotePort() + "/"
                        + request.getRemoteDatabase() + CONFIGURE_TIME,
                request.getRemoteUsername(), request.getRemotePassword(), "select version();");
    }

    @Override
    public void createServer(ForeignTableQuery request) throws SQLException {
        log.info("ForeignTableSqlServiceImpl createServer request: " + request);
        String type;
        String datasourceType = request.getDatasourceType();
        switch (datasourceType) {
            case POSTGRES_FDW:
                type = POSTGRES_FDW;
                break;
            case MYSQL:
                type = MYSQL_FDW;
                break;
            case ORACLE:
                type = ORACLE_FDW;
                break;
            default:
                type = FILE_FDW;
                break;
        }
        String sql = String.format(CREATE_FOREIGN_SERVER_SQL, DebugUtils.needQuoteName(request.getForeignServer()),
                type, request.getRemoteHost(), request.getRemotePort(), request.getRemoteDatabase());
        log.info("ForeignTableSqlServiceImpl createServer sql: " + sql);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            statement.execute(sql);
            log.info("ForeignTableSqlServiceImpl createServer end: ");

            ResultSet resultSet = statement.executeQuery(String.format(MAPPING_NAME_SQL, request.getForeignServer()));
            List<String> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getString("usename"));
            }
            if (list.contains(request.getRole())) {
                return;
            }
            String mappingSql = String.format(CREATE_MAPPING_SQL, DebugUtils.needQuoteName(request.getRole()),
                    request.getForeignServer(), request.getRemoteUsername(), desEncrypt(request.getRemotePassword()));
            log.info("ForeignTableSqlServiceImpl createMapping sql: " + mappingSql);
            statement.execute(mappingSql);
        }
    }

    @Override
    public void edit(TableDataEditQuery request) throws SQLException {
        log.info("tableColumnEdit request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
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
        }
    }

    private void createExtension(Statement statement, String datasourceType) throws SQLException {
        log.info("ForeignTableSqlServiceImpl createExtension datasourceType: " + datasourceType);
        List<String> extensionList = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery(QUERY_EXTENSION_SQL);
        while (resultSet.next()) {
            extensionList.add(resultSet.getString(FDW_NAME));
        }
        log.info("ForeignTableSqlServiceImpl createExtension extensionList: " + extensionList);

        if (datasourceType.equals(OPENGAUSS) && !extensionList.contains(POSTGRES_FDW)) {
            statement.execute(CREATE_EXTENSION_SQL + POSTGRES_FDW + SEMICOLON);
        } else if (datasourceType.equals(MYSQL) && !extensionList.contains(MYSQL_FDW)) {
            statement.execute(CREATE_EXTENSION_SQL + MYSQL_FDW + SEMICOLON);
        } else if (datasourceType.equals(ORACLE) && !extensionList.contains(ORACLE_FDW)) {
            statement.execute(CREATE_EXTENSION_SQL + ORACLE_FDW + SEMICOLON);
        } else {
            statement.execute(CREATE_EXTENSION_SQL + FILE_FDW + SEMICOLON);
        }
        log.info("ForeignTableSqlServiceImpl createExtension end: ");
    }

    private void createForeignTable(Statement statement, ForeignTableQuery request) throws SQLException {
        log.info("ForeignTableSqlServiceImpl createForeignTable request: " + request);
        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("CREATE FOREIGN TABLE ");
        if (request.isExists()) {
            sqlSb.append("IF NOT EXISTS ");
        }
        String schema = request.getSchema();
        String foreignTable = request.getForeignTable();
        sqlSb.append(DebugUtils.needQuoteName(schema)).append(POINT)
                .append(DebugUtils.needQuoteName(foreignTable)).append(LEFT_BRACKET).append(LF);

        List<ForeignTableQuery.ColumnDTO> columnList = request.getColumnList();
        log.info("ForeignTableSqlServiceImpl createForeignTable columnList: " + columnList);
        StringBuilder commentSb = new StringBuilder();
        if (!CollectionUtils.isEmpty(columnList)) {
            for (int i = 0; i < columnList.size(); i++) {
                ForeignTableQuery.ColumnDTO col = columnList.get(i);
                sqlSb.append(addColumnSql(col));
                if (i < columnList.size() - 1) {
                    sqlSb.append(QUOTES_LF_COMMA);
                } else {
                    sqlSb.append(LF);
                }
                commentSb.append(addCommentSql(col, schema, foreignTable));
            }
        }
        log.info("ForeignTableSqlServiceImpl createForeignTable commentSb: " + commentSb);

        sqlSb.append(RIGHT_BRACKET).append(LF).append("SERVER ").append(request.getForeignServer()).append(LF);
        String remoteSchema = request.getRemoteSchema();
        if (StringUtils.isEmpty(remoteSchema)) {
            remoteSchema = DebugUtils.needQuoteName(schema);
        }
        String remoteTable = request.getRemoteTable();
        if (StringUtils.isEmpty(remoteTable)) {
            remoteTable = DebugUtils.needQuoteName(foreignTable);
        }
        sqlSb.append(String.format(FAR_OPTION_SQL, remoteSchema, remoteTable));
        sqlSb.append(LF).append(commentSb);

        String description = request.getDescription();
        if (StringUtils.isNotEmpty(description)) {
            sqlSb.append(LF).append(String.format(TABLE_DESCRIPTION_SQL, DebugUtils.needQuoteName(schema),
                    DebugUtils.needQuoteName(foreignTable), description));
        }
        log.info("ForeignTableSqlServiceImpl createForeignTable sqlSb: " + sqlSb);
        statement.execute(sqlSb.toString());
    }

    private String addColumnSql(ForeignTableQuery.ColumnDTO columnDto) {
        log.info("ForeignTableSqlServiceImpl addColumnSql columnDto: " + columnDto);
        StringBuilder precision = new StringBuilder();
        if (!StringUtils.isEmpty(columnDto.getPrecision())) {
            precision.append(LEFT_BRACKET).append(DebugUtils.containsSqlInjection(columnDto.getPrecision()));
            if (!StringUtils.isEmpty(columnDto.getScope())) {
                precision.append(COMMA).append(DebugUtils.containsSqlInjection(columnDto.getScope()));
            }
            precision.append(RIGHT_BRACKET);
        }
        log.info("ForeignTableSqlServiceImpl addColumnSql precision: " + precision);

        StringBuilder ddl = new StringBuilder();
        ddl.append(SPACE).append(SPACE).append(columnDto.getForeignColumn()).append(SPACE);
        if (columnDto.getType().endsWith("[]")) {
            ddl.append(columnDto.getType(), 0, columnDto.getType().length() - 2);
            ddl.append(precision).append("[]");
        } else {
            ddl.append(columnDto.getType());
            ddl.append(precision);
        }
        if (columnDto.getIsEmpty() != null && columnDto.getIsEmpty()) {
            ddl.append(NOT_KEYWORD_SQL).append(NULL_KEYWORD_SQL);
        }

        String[] arrayRefVar = {"bigint", "smallint", "tinyint", "integer", "int16", "real", "double precision"};
        String defaultValue = columnDto.getDefaultValue();
        if (!StringUtils.isEmpty(defaultValue)) {
            if (Arrays.asList(arrayRefVar).contains(columnDto.getType())) {
                ddl.append(DEFAULT_KEYWORD_SQL).append(DebugUtils.containsSqlInjection(defaultValue));
            } else {
                ddl.append(DEFAULT_KEYWORD_SQL).append(QUOTES).append(
                        DebugUtils.containsSqlInjection(defaultValue)).append(QUOTES);
            }
        }
        if (columnDto.getIsOnly() != null && columnDto.getIsOnly()) {
            ddl.append(UNIQUE_KEYWORD_SQL);
        }

        String farColumn = columnDto.getFarColumn();
        if (StringUtils.isNotEmpty(farColumn)) {
            ddl.append(" options (column_name '").append(farColumn).append("')");
        }
        log.info("ForeignTableSqlServiceImpl addColumnSql ddl: " + ddl);
        return ddl.toString();
    }

    private String addCommentSql(ForeignTableQuery.ColumnDTO columnDto, String schema, String tableName) {
        if (StringUtils.isEmpty(columnDto.getComment())) {
            return "";
        }

        String ddl = String.format(COMMENT_ON_COLUMN_SQL,
                DebugUtils.needQuoteName(schema), DebugUtils.needQuoteName(tableName),
                DebugUtils.needQuoteName(columnDto.getForeignColumn()), columnDto.getComment());
        log.info("ForeignTableSqlServiceImpl addCommentSql ddl: " + ddl);
        return ddl;
    }

    private Map<String, String> parseString(String options) {
        log.info("ForeignTableSqlServiceImpl parseString options: " + options);
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(options)) {
            return map;
        }

        String substring = options.substring(1, options.length() - 1);
        String[] split = substring.split(COMMA);
        for (String str : split) {
            String[] item = str.split("=");
            map.put(item[0], item[1]);
        }
        log.info("ForeignTableSqlServiceImpl parseString map: " + map);
        return map;
    }

    private String covertType(String name) {
        log.info("ForeignTableSqlServiceImpl covertType name: " + name);
        String type = Strings.EMPTY;
        switch (name) {
            case POSTGRES_FDW:
                type = OPENGAUSS;
                break;
            case MYSQL_FDW:
                type = MYSQL;
                break;
            case ORACLE_FDW:
                type = ORACLE;
                break;
            default:
                break;
        }
        log.info("ForeignTableSqlServiceImpl covertType type: " + type);
        return type;
    }
}

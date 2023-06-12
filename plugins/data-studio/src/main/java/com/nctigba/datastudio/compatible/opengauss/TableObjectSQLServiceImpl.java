/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.TableObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.CommonConstants.PG_GET_TABLEDEF;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.SqlConstants.COMMENT_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.REINDEX_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.RENAME_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SCHEMA_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLESPACE_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_ANALYSE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DATA_COUNT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DATA_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DDL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_PARTITIONG_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_SEQUENCE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_TRUNCATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.VACUUM_SQL;

@Slf4j
@Service
public class TableObjectSQLServiceImpl implements TableObjectSQLService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String tableDataSQL(String schema, String tableName) {
        StringBuilder ddl = new StringBuilder(String.format(TABLE_DATA_SQL, schema, tableName));
        log.info("tableDataSQL response is: " + ddl);
        return ddl.toString();
    }

    @Override
    public String tableDataCountSQL(String schema, String tableName) {
        String ddl = String.format(TABLE_DATA_COUNT_SQL, schema, tableName);
        log.info("tableDataCountSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableAnalyseSQL(String schema, String tableName) {
        String ddl = String.format(TABLE_ANALYSE_SQL, schema, tableName);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public Map<String, String> tableDdl(SelectDataQuery request) {
        log.info("tableDdl request is: " + request);
        Map<String, String> resultMap = new HashMap<>();
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sql = String.format(TABLE_DDL_SQL, request.getSchema(), request.getTableName());
            try (
                    ResultSet resultSet = statement.executeQuery(sql)
            ) {
                resultSet.next();
                if (StringUtils.isBlank(String.valueOf(resultSet.getString("pg_get_tabledef")))) {
                    throw new CustomException(LocaleString.transLanguage("2010"));
                }
                resultMap.put(RESULT, resultSet.getString(PG_GET_TABLEDEF));
            }
            log.info("tableDdl map is: " + resultMap);
            return resultMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String tableTruncateSQL(String schema, String tableName) {
        String ddl = String.format(TABLE_TRUNCATE_SQL, schema, tableName);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableVacuumSQL(String schema, String tableName) {
        String ddl = String.format(VACUUM_SQL, schema, tableName);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableReindexSQL(String schema, String tableName) {
        String ddl = String.format(REINDEX_TABLE_SQL, schema, tableName);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableRenameSQL(String schema, String tableName, String newName) {
        String ddl = String.format(RENAME_TABLE_SQL, schema, tableName, newName);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableCommentSQL(String schema, String tableName, String comment) {
        String ddl = String.format(COMMENT_TABLE_SQL, schema, tableName, comment);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableAlterSchemaSQL(String schema, String tableName, String newSchema) {
        String ddl = String.format(SCHEMA_TABLE_SQL, schema, tableName, newSchema);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableDropSQL(String schema, String tableName) {
        String ddl = String.format(DROP_TABLE_SQL, schema, tableName);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableAlterTablespaceSQL(String schema, String tableName, String tablespace) {
        String ddl = String.format(TABLESPACE_TABLE_SQL, schema, tableName, tablespace);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableSequenceSQL(String schema, String tableName) {
        String ddl = String.format(TABLE_SEQUENCE_SQL, schema, tableName);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public List<Map<String, Object>> tableAttributeSQL(String uuid, String oid, String tableType) {
        String ddl;
        if (tableType.equals("n")) {
            ddl = String.format(TABLE_ATTRIBUTE_SQL, oid);
        } else {
            ddl = String.format(TABLE_PARTITIONG_ATTRIBUTE_SQL, oid);
        }
        log.info("tableAttributeSQL response is: " + ddl);
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement()
        ) {
            try (ResultSet resultSet = statement.executeQuery(ddl)) {
                List list;
                if (tableType.equals("y")) {
                    list = tableAttributeResultSet(resultSet, 15);
                } else {
                    list = tableAttributeResultSet(resultSet, 14);
                }
                log.info("tableColumn list is: " + list);
                return list;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> tableAttributeResultSet(ResultSet resultSet, int num) throws SQLException {
        List list = new ArrayList<>();
        resultSet.next();
        for (int i = 1; i <= num; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put(LocaleString.transLanguage(String.valueOf(i)), resultSet.getObject(i));
            list.add(map);
        }
        log.info("tableAttributeResultSet List {}", list);
        return list;
    }
}

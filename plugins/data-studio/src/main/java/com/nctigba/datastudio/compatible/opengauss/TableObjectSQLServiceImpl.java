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
 *  TableObjectSQLServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/TableObjectSQLServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.TableObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.SelectDataFiltrationQuery;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.utils.LocaleStringUtils;
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

import static com.nctigba.datastudio.constants.CommonConstants.NUM_PARTITION_TABLE_ATTR_ROWS;
import static com.nctigba.datastudio.constants.CommonConstants.NUM_TABLE_ATTR_ROWS;
import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.CommonConstants.PG_GET_TABLEDEF;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.SqlConstants.ANALYZE_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMENT_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COUNT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LIMIT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ORDER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.REINDEX_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.RENAME_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SCHEMA_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.TABLESPACE_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_ANALYSE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_ATTRIBUTE_PARTITION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DATA_LIMIT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DATA_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DDL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_PARTITIONG_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_SEQUENCE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_TRUNCATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.VACUUM_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.WHERE_SQL;
import static com.nctigba.datastudio.utils.DebugUtils.needQuoteName;

/**
 * TableObjectSQLService achieve
 *
 * @since 2023-06-26
 */
@Slf4j
@Service
public class TableObjectSQLServiceImpl implements TableObjectSQLService {
    @Autowired
    private ConnectionConfig connectionConfig;

    public static List<Map<String, Object>> tableAttributeResultSet(ResultSet resultSet, int num) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        resultSet.next();
        for (int i = 1; i <= num; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put(LocaleStringUtils.transLanguage(String.valueOf(i)), resultSet.getObject(i));
            list.add(map);
        }
        log.info("tableAttributeResultSet List {}", list);
        return list;
    }

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String tableDataSQL(
            String schema, String tableName, Integer start, Integer pageSize,
            SelectDataFiltrationQuery query) {
        log.info("SelectDataFiltrationQuery request is: {}", query);
        StringBuilder ddl = new StringBuilder();
        if (query == null) {
            ddl.append(String.format(TABLE_DATA_LIMIT_SQL, schema, tableName, start, pageSize));
        } else {
            ddl.append(String.format(TABLE_DATA_SQL, schema, tableName));
            concatCondition(query, ddl);
            ddl.append(String.format(LIMIT_SQL, start, pageSize));
        }
        log.info("tableDataSQL response is: " + ddl);
        return ddl.toString();
    }

    @Override
    public String tableDataCountSQL(String schema, String tableName) {
        String ddl = String.format(COUNT_SQL, schema, tableName);
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
    public Map<String, String> tableDdl(SelectDataQuery request) throws SQLException {
        log.info("tableDdl request is: " + request);
        Map<String, String> resultMap = new HashMap<>();
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sql = String.format(TABLE_DDL_SQL, request.getSchema(), needQuoteName(request.getTableName()));
            try (
                    ResultSet resultSet = statement.executeQuery(sql)
            ) {
                resultSet.next();
                if (StringUtils.isBlank(String.valueOf(resultSet.getString("pg_get_tabledef")))) {
                    throw new CustomException(LocaleStringUtils.transLanguage("2010"));
                }
                resultMap.put(RESULT, resultSet.getString(PG_GET_TABLEDEF));
            }
            log.info("tableDdl map is: " + resultMap);
            return resultMap;
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
        String ddl = String.format(DROP_TABLE_SQL, schema, needQuoteName(tableName));
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
    public List<Map<String, Object>> tableAttributeSQL(String uuid, String oid, String tableType) throws SQLException {
        String ddl;
        if (tableType.equals("n")) {
            ddl = String.format(TABLE_ATTRIBUTE_SQL, oid);
        } else {
            ddl = String.format(TABLE_PARTITIONG_ATTRIBUTE_SQL, oid, oid);
        }
        log.info("tableAttributeSQL response is: " + ddl);
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement()
        ) {
            try (ResultSet resultSet = statement.executeQuery(ddl)) {
                List<Map<String, Object>> list;
                if (tableType.equals("y")) {
                    list = tableAttributeResultSet(resultSet, NUM_PARTITION_TABLE_ATTR_ROWS);
                } else {
                    list = tableAttributeResultSet(resultSet, NUM_TABLE_ATTR_ROWS);
                }
                log.info("tableColumn list is: " + list);
                return list;
            }
        }
    }

    @Override
    public String tableAnalyzeSQL(String schema, String tableName) {
        String ddl = String.format(ANALYZE_TABLE_SQL, schema, tableName);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableAttributePartitionSQL(String oid) {
        String ddl = String.format(TABLE_ATTRIBUTE_PARTITION_SQL, oid);
        log.info("tableAnalyseSQL response is: " + ddl);
        return ddl;
    }

    @Override
    public String exportTableData(
            String schema, String tableName, Integer pageNum, Integer pageSize, SelectDataFiltrationQuery query) {
        log.info("TableObjectSQLServiceImpl exportTableData query: " + query);
        StringBuilder ddl = new StringBuilder();
        ddl.append(String.format(TABLE_DATA_SQL,
                needQuoteName(schema), needQuoteName(tableName)));

        if (query != null) {
            concatCondition(query, ddl);
        }

        if (pageNum != null) {
            ddl.append(String.format(LIMIT_SQL, (pageNum - 1) * pageSize, pageSize));
        }
        log.info("TableObjectSQLServiceImpl exportTableData ddl: " + ddl);
        return ddl.toString();
    }

    private void concatCondition(SelectDataFiltrationQuery query, StringBuilder ddl) {
        int filtrationSize = query.getFiltration().size();
        StringBuilder filtration = new StringBuilder();
        for (int i = 0; i < filtrationSize; i++) {
            if (i < filtrationSize - 1) {
                filtration.append(SPACE).append(query.getFiltration().get(i));
            } else {
                String end = query.getFiltration().get(i).trim();
                if (end.endsWith("or")) {
                    filtration.append(SPACE).append(end, 0, end.length() - 2);
                } else if (end.endsWith("and")) {
                    filtration.append(SPACE).append(end, 0, end.length() - 3);
                } else {
                    filtration.append(SPACE).append(end);
                }
            }
        }
        if (filtrationSize > 0) {
            ddl.append(String.format(WHERE_SQL, filtration));
        }

        int orderSize = query.getOrder().size();
        StringBuilder order = new StringBuilder();
        for (int i = 0; i < orderSize; i++) {
            order.append(query.getOrder().get(i));
            if (i < orderSize - 1) {
                order.append(COMMA);
            }
        }
        if (orderSize > 0) {
            ddl.append(String.format(ORDER_SQL, order));
        }
    }
}
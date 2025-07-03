/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2025.
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
 *  SqlProvider.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/SqlProvider.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl;

import com.nctigba.observability.sql.model.dto.SlowSqlDTO;
import org.opengauss.admin.common.exception.CustomException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SqlProvider
 *
 * @author luomeng
 * @since 2024/4/10
 */
public class SqlProvider {
    private static final String CREATE_TABLE_TEMPLATE = "CREATE TABLE IF NOT EXISTS %s (\n"
            + "              db_name text,\n"
            + "              schema_name text,\n"
            + "              origin_node integer,\n"
            + "              user_name text,\n"
            + "              application_name text,\n"
            + "              client_addr text,\n"
            + "              client_port integer,\n"
            + "              unique_query_id bigint,\n"
            + "              debug_query_id bigint,\n"
            + "              query text,\n"
            + "              start_time timestamp with time zone,\n"
            + "              finish_time timestamp with time zone,\n"
            + "              slow_sql_threshold bigint,\n"
            + "              transaction_id bigint,\n"
            + "              thread_id bigint,\n"
            + "              session_id bigint,\n"
            + "              n_soft_parse bigint,\n"
            + "              n_hard_parse bigint,\n"
            + "              query_plan text,\n"
            + "              n_returned_rows bigint,\n"
            + "              n_tuples_fetched bigint,\n"
            + "              n_tuples_returned bigint,\n"
            + "              n_tuples_inserted bigint,\n"
            + "              n_tuples_updated bigint,\n"
            + "              n_tuples_deleted bigint,\n"
            + "              n_blocks_fetched bigint,\n"
            + "              n_blocks_hit bigint,\n"
            + "              db_time bigint,\n"
            + "              cpu_time bigint,\n"
            + "              execution_time bigint,\n"
            + "              parse_time bigint,\n"
            + "              plan_time bigint,\n"
            + "              rewrite_time bigint,\n"
            + "              pl_execution_time bigint,\n"
            + "              pl_compilation_time bigint,\n"
            + "              data_io_time bigint,\n"
            + "              net_send_info text,\n"
            + "              net_recv_info text,\n"
            + "              net_stream_send_info text,\n"
            + "              net_stream_recv_info text,\n"
            + "              lock_count bigint,\n"
            + "              lock_time bigint,\n"
            + "              lock_wait_count bigint,\n"
            + "              lock_wait_time bigint,\n"
            + "              lock_max_count bigint,\n"
            + "              lwlock_count bigint,\n"
            + "              lwlock_wait_count bigint,\n"
            + "              lwlock_time bigint,\n"
            + "              lwlock_wait_time bigint,\n"
            + "              details bytea,\n"
            + "              is_slow_sql boolean,\n"
            + "              trace_id text,\n"
            + "              advise text,\n"
            + "              net_send_time bigint\n"
            + "              UNIQUE(debug_query_id, start_time)"
            + ");";
    private static final String DELETE_EXIPRED_DATA = "delete from %s where finish_time < '%s'";
    private static final String INSERT_COLUMN_LIST = "INSERT OR IGNORE INTO %s ( %s ) VALUES ( %s )";

    /**
     * Dynamic construction of SQL for querying slow SQL aggregate data
     *
     * @param tableName String
     * @param slowSqlDTO SlowSqlDTO
     * @return String
     */
    public String getAggSlowSql(String tableName, SlowSqlDTO slowSqlDTO) {
        // Check the legality of input parameters
        validateInput(slowSqlDTO);
        // Using StringBuilder to build SQL and improve efficiency
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT unique_query_id, query as sql_template, "
                + "COUNT(unique_query_id) AS execute_num, "
                + "ROUND(SUM(db_time*1.0/1000/1000),3) AS total_execute_time, "
                + "ROUND(SUM(db_time*1.0/1000/1000)/ COUNT(unique_query_id),3) AS avg_execute_time, "
                + "SUM(n_tuples_fetched+n_tuples_returned) AS total_scan_rows, "
                + "ROUND(SUM(n_tuples_fetched+n_tuples_returned)/COUNT(unique_query_id)) AS avg_scan_rows, "
                + "SUM(n_tuples_fetched) AS total_random_scan_rows, "
                + "ROUND(SUM(n_tuples_fetched)/COUNT(unique_query_id)) AS avg_random_scan_rows, "
                + "SUM(n_tuples_returned) AS total_order_scan_rows, "
                + "ROUND(SUM(n_tuples_returned)/COUNT(unique_query_id)) AS avg_order_scan_rows, "
                + "ROUND(SUM(lock_time*1.0/1000/1000)/ COUNT(unique_query_id),3) AS avg_lock_time, "
                + "ROUND(SUM(n_returned_rows)/COUNT(unique_query_id)) AS avg_return_rows,"
                + "MIN(start_time) AS first_execute_time, "
                + "MAX(finish_time) AS final_execute_time"
                + " FROM %s WHERE is_slow_sql=true");
        // Dynamically adding conditions
        appendCondition(sqlBuilder, "db_name", "=", slowSqlDTO.getDbName());
        appendDateCondition(sqlBuilder, "start_time", ">=", slowSqlDTO.getStartTime().getTime());
        appendDateCondition(sqlBuilder, "finish_time", "<=", slowSqlDTO.getFinishTime().getTime());
        appendGroup(sqlBuilder, "unique_query_id,query");
        // sort
        appendOrder(sqlBuilder, slowSqlDTO.getOrderByColumn(), slowSqlDTO.getIsAsc());
        // paging
        appendPagination(sqlBuilder, slowSqlDTO.getLimit(), slowSqlDTO.getOffset());
        return String.format(sqlBuilder.toString(), tableName);
    }

    /**
     * Dynamic construction of SQL for querying all slow SQL
     *
     * @param tableName String
     * @param slowSqlDTO SlowSqlDTO
     * @return String
     */
    public String getAllSlowSql(String tableName, SlowSqlDTO slowSqlDTO) {
        // Check the legality of input parameters
        validateInput(slowSqlDTO);
        // Using StringBuilder to build SQL and improve efficiency
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT debug_query_id,start_time,finish_time"
                + ",query as sql_template,db_name,schema_name,client_addr,user_name"
                + ",ROUND((db_time*1.0)/1000,3) as db_time,ROUND((cpu_time*1.0)/1000,3) as cpu_time"
                + ",ROUND((data_io_time*1.0)/1000,3) as data_io_time"
                + ",ROUND((parse_time*1.0)/1000,3) as parse_time,"
                + "ROUND((pl_execution_time*1.0)/1000,3) as pl_execution_time"
                + ",ROUND((lock_wait_time*1.0)/1000,3) as lock_wait_time"
                + ",n_tuples_returned,n_returned_rows,unique_query_id"
                + " FROM %s WHERE is_slow_sql=true");
        // Dynamically adding conditions
        appendCondition(sqlBuilder, "db_name", "=", slowSqlDTO.getDbName());
        appendDateCondition(sqlBuilder, "start_time", ">=", slowSqlDTO.getStartTime().getTime());
        appendDateCondition(sqlBuilder, "finish_time", "<=", slowSqlDTO.getFinishTime().getTime());
        // sort
        appendOrder(sqlBuilder, slowSqlDTO.getOrderByColumn(), slowSqlDTO.getIsAsc());
        // paging
        appendPagination(sqlBuilder, slowSqlDTO.getLimit(), slowSqlDTO.getOffset());
        return String.format(sqlBuilder.toString(), tableName);
    }

    /**
     * build insert sql statement, insert info to hisSlowsqlInfo table
     *
     * @param params Map<String, Object> column and value map
     * @return String sql statement
     */
    public String buildInsertSql(Map<String, Object> params) {
        String tableName = "";
        if (params.get("tableName") instanceof String) {
            tableName = (String) params.get("tableName");
        }
        Object data = params.get("data");
        try {
            Class<?> clazz = data.getClass();
            Field[] fields = clazz.getDeclaredFields();
            List<String> columnNames = new ArrayList<>();
            List<String> valueExpressions = new ArrayList<>();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.get(data) != null) {
                    columnNames.add(toSnakeCase(field.getName()));
                    valueExpressions.add("#{data." + field.getName() + "}");
                }
            }
            return String.format(Locale.ROOT, INSERT_COLUMN_LIST, tableName, String.join(",", columnNames),
                    String.join(",", valueExpressions));
        } catch (IllegalAccessException e) {
            throw new CustomException("construct sql statement.", e);
        }
    }

    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * build create sql statement
     *
     * @param tableName String
     * @return String sql statement
     */
    public String buildCreateSql(String tableName) {
        return String.format(CREATE_TABLE_TEMPLATE, tableName);
    }

    /**
     * build clean sql statement
     *
     * @param tableName String
     * @param peroid int
     * @return String sql statement
     */
    public String getCleanupSql(String tableName, int peroid) {
        LocalDate peroidDaysAgo = LocalDate.now().minusDays(peroid);
        return String.format(DELETE_EXIPRED_DATA, tableName, peroidDaysAgo.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
    }

    // Verify the legitimacy of input parameters
    private void validateInput(SlowSqlDTO slowSqlDTO) {
        if (slowSqlDTO.getStartTime() != null && slowSqlDTO.getFinishTime() != null) {
            if (slowSqlDTO.getStartTime().after(slowSqlDTO.getFinishTime())) {
                throw new IllegalArgumentException("The start time must be earlier than or equal to the end time");
            }
        }
    }

    // Dynamically adding conditional statements
    private void appendCondition(StringBuilder sqlBuilder, String columnName, String operator, Object value) {
        if (value != null && value != "") {
            sqlBuilder.append(" AND ").append(columnName).append(operator).append("'").append(value).append("'");
        }
    }

    // Dynamically adding conditional statements
    private void appendDateCondition(StringBuilder sqlBuilder, String columnName, String operator, Long timestamp) {
        if (timestamp != null) {
            sqlBuilder.append(" AND ").append(columnName).append(operator).append(timestamp);
        }
    }

    // Dynamically adding group statements
    private void appendGroup(StringBuilder sqlBuilder, String columnName) {
        sqlBuilder.append(" GROUP BY ").append(columnName);
    }

    // Dynamically adding sorting statements
    private void appendOrder(StringBuilder sqlBuilder, String orderByColumn, String isAsc) {
        Map<String, String> columnMap = paramToColumn();
        if (orderByColumn != null && isAsc != null) {
            sqlBuilder.append(" ORDER BY ").append(columnMap.get(orderByColumn)).append(" ").append(isAsc);
        }
    }

    // Dynamically adding paging statements
    private void appendPagination(StringBuilder sqlBuilder, int limit, int offset) {
        if (limit > 0 && offset >= 0) {
            sqlBuilder.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
        }
    }

    private Map<String, String> paramToColumn() {
        Map<String, String> columnMap = new HashMap<>();
        columnMap.put("uniqueQueryId", "unique_query_id");
        columnMap.put("query", "sql_template");
        columnMap.put("executeNum", "execute_num");
        columnMap.put("totalExecuteTime", "total_execute_time");
        columnMap.put("avgExecuteTime", "avg_execute_time");
        columnMap.put("totalScanRows", "total_scan_rows");
        columnMap.put("avgScanRows", "avg_scan_rows");
        columnMap.put("totalRandomScanRows", "total_random_scan_rows");
        columnMap.put("avgRandomScanRows", "avg_random_scan_rows");
        columnMap.put("totalOrderScanRows", "total_order_scan_rows");
        columnMap.put("avgOrderScanRows", "avg_order_scan_rows");
        columnMap.put("avgReturnRows", "avg_return_rows");
        columnMap.put("avgLockTime", "avg_lock_time");
        columnMap.put("firstExecuteTime", "first_execute_time");
        columnMap.put("finalExecuteTime", "final_execute_time");
        columnMap.put("debugQueryId", "debug_query_id");
        columnMap.put("dbName", "db_name");
        columnMap.put("userName", "user_name");
        columnMap.put("schemaName", "schema_name");
        columnMap.put("clientAddr", "client_addr");
        columnMap.put("sqlTemplate", "sql_template");
        columnMap.put("startTime", "start_time");
        columnMap.put("finishTime", "finish_time");
        columnMap.put("nReturnedRows", "n_returned_rows");
        columnMap.put("nTuplesReturned", "n_tuples_returned");
        columnMap.put("dbTime", "db_time");
        columnMap.put("cpuTime", "cpu_time");
        columnMap.put("executionTime", "execution_time");
        columnMap.put("parseTime", "parse_time");
        columnMap.put("plExecutionTime", "pl_execution_time");
        columnMap.put("dataIoTime", "data_io_time");
        columnMap.put("lockWaitTime", "lock_wait_time");
        return columnMap;
    }
}

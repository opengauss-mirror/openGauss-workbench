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
 *  TableObjectSQLService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/TableObjectSQLService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.SelectDataFiltrationQuery;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * TableObjectSQLService
 *
 * @since 2023-06-26
 */
public interface TableObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * table data sql
     *
     * @param schema schema
     * @param tableName tableName
     * @param start start
     * @param pageSize pageSize
     * @param filtration filtration
     * @return String
     */
    default String tableDataSQL(
            String schema, String tableName, Integer start, Integer pageSize,
            SelectDataFiltrationQuery filtration) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table ddl
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    default Map<String, String> tableDdl(SelectDataQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table data count sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @return String
     */
    default String tableDataCountSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table analyse sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @return String
     */
    default String tableAnalyseSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table truncate sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @return String
     */
    default String tableTruncateSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table vacuum sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @return String
     */
    default String tableVacuumSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table reindex sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @return String
     */
    default String tableReindexSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table rename sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @param newName   newName
     * @return String
     */
    default String tableRenameSQL(String schema, String tableName, String newName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table comment sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @param comment   comment
     * @return String
     */
    default String tableCommentSQL(String schema, String tableName, String comment) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table alter schema sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @param newSchema newSchema
     * @return String
     */
    default String tableAlterSchemaSQL(String schema, String tableName, String newSchema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table drop sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @return String
     */
    default String tableDropSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table alter table space sql
     *
     * @param schema     schema
     * @param tableName  tableName
     * @param tablespace tablespace
     * @return String
     */
    default String tableAlterTablespaceSQL(String schema, String tableName, String tablespace) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table sequence sql
     *
     * @param schema    schema
     * @param tableName tableName
     * @return String
     */
    default String tableSequenceSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table attribute sql
     *
     * @param uuid      uuid
     * @param oid       oid
     * @param tableType tableType
     * @return List
     * @throws SQLException SQLException
     */
    default List<Map<String, Object>> tableAttributeSQL(String uuid, String oid, String tableType) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table analyze sql
     *
     * @param schema schema
     * @param tableName tableName
     * @return String
     */
    default String tableAnalyzeSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table sequence sql
     *
     * @param oid    oid
     * @return String
     */
    default String tableAttributePartitionSQL(String oid) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * export table data sql
     *
     * @param schema schema
     * @param tableName tableName
     * @param pageNum pageNum
     * @param pageSize pageSize
     * @param filtration filtration
     * @return String
     */
    default String exportTableData(
            String schema, String tableName, Integer pageNum, Integer pageSize, SelectDataFiltrationQuery filtration) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
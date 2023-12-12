/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.DatabaseMetaArrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArraySchemaQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * GainObjectSQLService
 *
 * @since 2023-6-26
 */
public interface GainObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * database list
     *
     * @return String
     */
    default String databaseList() {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * schema list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    default List<Map<String, String>> schemaList(DatabaseMetaArraySchemaQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * object list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    default List<String> objectList(DatabaseMetaArrayQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table column list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    default List<String> tableColumnList(DatabaseMetaArrayColumnQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * database version
     *
     * @return List
     */
    default String databaseVersion() {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table sql
     *
     * @param schema schema
     * @return List
     */
    default String tableSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * foreign table sql
     *
     * @param schema schema
     * @return List
     */
    default String foreignTableSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * trigger sql
     *
     * @param schema schema
     * @return List
     */
    default String triggerSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * view sql
     *
     * @param schema schema
     * @return String
     */
    default String viewSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * function sql
     *
     * @param schema schema
     * @return String
     */
    default String fun_prosSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * sequence sql
     *
     * @param schema schema
     * @return String
     */
    default String sequenceSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * synonym sql
     *
     * @param schema schema
     * @return String
     */
    default String synonymSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * base type list sql
     *
     * @return String
     */
    default String baseTypeListSQL() {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table space list sql
     *
     * @return String
     */
    default String tablespaceListSQL() {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table space list sql
     *
     * @return String
     */
    default String tablespaceOidListSQL() {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * user sql
     *
     * @return String
     */
    default String userSql() {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table space list sql
     *
     * @return String
     */
    default String resourceListSQL() {
        throw new CustomException(DebugUtils.getMessage());
    }
}

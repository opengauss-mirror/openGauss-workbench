/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintPkDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateTableDTO;
import com.nctigba.datastudio.model.dto.DatabaseIndexDTO;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.util.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;
import java.util.List;

/**
 * TableColumnSQLService
 *
 * @since 2023-06-25
 */
public interface TableColumnSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * table constraint sql
     *
     * @param request request
     * @return String
     */
    default String tableConstraintSQL(SelectDataQuery request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table pk constraint sql
     *
     * @param schema schema
     * @param tableName tableName
     * @return String
     */
    default String tablePKConstraintSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * edit constraint
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void editConstraint(DatabaseConstraintDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * edit pk constraint
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void editPkConstraint(DatabaseConstraintPkDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table index sql
     *
     * @param request request
     * @return String
     */
    default String tableIndexSQL(SelectDataQuery request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table column sql
     *
     * @param oid oid
     * @param schema schema
     * @param tableName tableName
     * @return String
     */
    default String tableColumnSQL(String oid, String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table column add sql
     *
     * @param data data
     * @param schema schema
     * @param tableName tableName
     * @return List
     */
    default List<String> tableColumnAddSQL(
            DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data, String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table column drop sql
     *
     * @param data data
     * @param schema schema
     * @param tableName tableName
     * @return String
     */
    default String tableColumnDropSQL(
            DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data, String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table column update sql
     *
     * @param data data
     * @param schema schema
     * @param tableName tableName
     * @param uuid uuid
     * @return List
     * @throws SQLException SQLException
     */
    default List<String> tableColumnUpdateSQL(
            DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data, String schema, String tableName,
            String uuid) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * edit index
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void editIndex(DatabaseIndexDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create table
     *
     * @param request request
     * @return String
     */
    default String createTable(DatabaseCreateTableDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }


    /**
     * table data add sql
     *
     * @param data data
     * @param schema schema
     * @param tableName tableName
     * @return List
     */
    default String tableDataAddSQL(
            TableDataEditQuery.TableDataDTO data, String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table data drop sql
     *
     * @param data      data
     * @param schema    schema
     * @param tableName tableName
     * @return List
     */
    default String tableDataDropSQL(
            TableDataEditQuery.TableDataDTO data, String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * table data update sql
     *
     * @param data data
     * @param schema schema
     * @param tableName tableName
     * @return List
     */
    default String tableDataUpdateSQL(
            TableDataEditQuery.TableDataDTO data, String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }
}

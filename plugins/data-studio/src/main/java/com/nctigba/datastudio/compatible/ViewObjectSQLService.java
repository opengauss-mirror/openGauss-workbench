/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;
import java.util.Map;

/**
 * ViewObjectSQLService
 *
 * @since 2023-6-26
 */
public interface ViewObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * splicing view ddl
     *
     * @param request request
     * @return String
     */
    default String splicingViewDDL(DatabaseCreateViewDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * return view ddl data
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    default Map<String, Object> returnViewDDLData(DatabaseViewDdlDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * return database view ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    default String returnDatabaseViewDDL(DatabaseViewDdlDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * return drop view sql
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    default String returnDropViewSQL(DatabaseViewDdlDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * return select view sql
     *
     * @param request request
     * @param star star
     * @param end end
     * @return String
     */
    default String returnSelectViewSQL(DatabaseSelectViewDTO request, Integer star, Integer end) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * return select count view sql
     *
     * @param request request
     * @return String
     */
    default String viewDataCountSQL(DatabaseSelectViewDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * rename view
     *
     * @param schema schema
     * @param viewName viewName
     * @param newName newName
     * @return String
     */
    default String renameView(String schema, String viewName, String newName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * set view schema
     *
     * @param schema        schema
     * @param viewName      viewName
     * @param newSchemaName newSchemaName
     * @return String
     */
    default String setViewSchema(String schema, String viewName, String newSchemaName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * get view column
     *
     * @param schema   schema
     * @param viewName viewName
     * @return String
     */
    default String getViewColumn(String schema, String viewName) {
        throw new CustomException(DebugUtils.getMessage());
    }
}

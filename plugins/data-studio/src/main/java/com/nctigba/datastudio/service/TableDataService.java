/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;


import com.nctigba.datastudio.model.dto.DatabaseConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintPkDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateTableDTO;
import com.nctigba.datastudio.model.dto.DatabaseIndexDTO;
import com.nctigba.datastudio.model.dto.TableAlterDTO;
import com.nctigba.datastudio.model.dto.TableAttributeDTO;
import com.nctigba.datastudio.model.dto.TableDataDTO;
import com.nctigba.datastudio.model.dto.TableNameDTO;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.model.query.TableDataQuery;
import com.nctigba.datastudio.model.query.TablePKDataQuery;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * TableDataService
 *
 * @since 2023-6-26
 */
public interface TableDataService {
    /**
     * table data
     *
     * @param request request
     * @return TableDataDTO
     * @throws SQLException SQLException
     */
    TableDataDTO tableData(TableDataQuery request) throws SQLException;

    /**
     * table column
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> tableColumn(SelectDataQuery request) throws SQLException;

    /**
     * table index
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> tableIndex(SelectDataQuery request) throws SQLException;

    /**
     * table constraint
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> tableConstraint(SelectDataQuery request) throws SQLException;

    /**
     * table ddl
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, String> tableDdl(SelectDataQuery request) throws SQLException;

    /**
     * edit table column
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> tableColumnEdit(DatabaseCreUpdColumnDTO request) throws SQLException;


    /**
     * table data close
     *
     * @param winId winId
     * @throws SQLException SQLException
     */
    void tableDataClose(String winId) throws SQLException;

    /**
     * edit table data
     *
     * @param request request
     * @return TablePKDataQuery
     * @throws SQLException SQLException
     */
    TablePKDataQuery tableDataEdit(TableDataEditQuery request) throws SQLException;

    /**
     * edit table constraint
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableEditConstraint(DatabaseConstraintDTO request) throws SQLException;

    /**
     * edit table pk constraint
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableEditPkConstraint(DatabaseConstraintPkDTO request) throws SQLException;

    /**
     * edit table index
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableEditIndex(DatabaseIndexDTO request) throws SQLException;

    /**
     * create table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableCreate(DatabaseCreateTableDTO request) throws SQLException;

    /**
     * return table create
     *
     * @param request request
     * @return String
     */
    String returnTableCreate(DatabaseCreateTableDTO request);

    /**
     * table truncate
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableTruncate(TableNameDTO request) throws SQLException;

    /**
     * table vacuum
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableVacuum(TableNameDTO request) throws SQLException;

    /**
     * table reindex
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableReindex(TableNameDTO request) throws SQLException;

    /**
     * table rename
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableRename(TableAlterDTO request) throws SQLException;

    /**
     * table comment
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableComment(TableAlterDTO request) throws SQLException;

    /**
     * table alter schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableAlterSchema(TableAlterDTO request) throws SQLException;

    /**
     * drop table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableDrop(TableNameDTO request) throws SQLException;

    /**
     * table alter space
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableAlterTablespace(TableAlterDTO request) throws SQLException;

    /**
     * table sequence
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> tableSequence(TableNameDTO request) throws SQLException;

    /**
     * table attribute
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    List<Map<String, Object>> tableAttribute(TableAttributeDTO request) throws SQLException;

    /**
     * table analyze
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void tableAnalyze(TableNameDTO request) throws SQLException;

    /**
     * table attribute partition
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> tableAttributePartition(TableNameDTO request) throws SQLException;

    /**
     * export data
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportData(TableDataQuery request, HttpServletResponse response) throws SQLException, IOException;
}

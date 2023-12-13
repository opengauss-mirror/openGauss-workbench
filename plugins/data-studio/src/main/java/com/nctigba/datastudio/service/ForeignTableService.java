/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.ForeignTableQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * ForeignTableService
 *
 * @since 2023-10-10
 */
public interface ForeignTableService {
    /**
     * query server
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    List<Map<String, String>> queryServer(ForeignTableQuery request) throws SQLException;

    /**
     * create foreign table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void create(ForeignTableQuery request) throws SQLException;

    /**
     * delete foreign table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void deleteForeignTable(ForeignTableQuery request) throws SQLException;


    /**
     * delete foreign server
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void deleteForeignServer(ForeignTableQuery request) throws SQLException;

    /**
     * show foreign table ddl
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, String> ddl(ForeignTableQuery request) throws SQLException;

    /**
     * show foreign table attribute
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    List<Map<String, String>> attribute(ForeignTableQuery request) throws SQLException;

    /**
     * test connection
     *
     * @param request request
     * @return String
     */
    String test(ForeignTableQuery request);

    /**
     * create foreign server
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void createServer(ForeignTableQuery request) throws SQLException;

    /**
     * edit foreign table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void edit(TableDataEditQuery request) throws SQLException;
}

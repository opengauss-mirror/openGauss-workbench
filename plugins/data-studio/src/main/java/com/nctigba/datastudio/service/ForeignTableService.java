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
 *  ForeignTableService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/ForeignTableService.java
 *
 *  -------------------------------------------------------------------------
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

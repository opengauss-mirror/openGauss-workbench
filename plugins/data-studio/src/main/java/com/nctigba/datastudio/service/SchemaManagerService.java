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
 *  SchemaManagerService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/SchemaManagerService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.SchemaManagerQuery;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * SchemaManagerService
 *
 * @since 2023-6-26
 */
public interface SchemaManagerService {
    /**
     * query all users
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    List<String> queryAllUsers(SchemaManagerQuery request) throws SQLException;

    /**
     * query schema
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, String> querySchema(SchemaManagerQuery request) throws SQLException;

    /**
     * create schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void createSchema(SchemaManagerQuery request) throws SQLException;

    /**
     * update schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void updateSchema(SchemaManagerQuery request) throws SQLException;

    /**
     * delete schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void deleteSchema(SchemaManagerQuery request) throws SQLException;
}

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
 *  DbConnectionService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/DbConnectionService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.ConnectionTimeLengthDTO;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.dto.GetConnectionAttributeDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayIdSchemaQuery;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * DataListByJdbcService
 *
 * @since 2023-6-26
 */
public interface DbConnectionService {
    /**
     * test connection
     *
     * @param request request
     * @return String
     */
    String test(DbConnectionCreateDTO request);

    /**
     * delete database connection
     *
     * @param id id
     */
    void deleteDatabaseConnectionList(String id);

    /**
     * database connection attribute
     *
     * @param request request
     * @return DatabaseConnectionDO
     */
    DatabaseConnectionDO databaseAttributeConnection(GetConnectionAttributeDTO request);

    /**
     * database connection list
     *
     * @param webUser webUser
     * @return List
     */
    List<DatabaseConnectionDO> databaseConnectionList(String webUser);

    /**
     * update database connection
     *
     * @param request request
     * @return DatabaseConnectionDO
     * @throws SQLException SQLException
     */
    DatabaseConnectionDO updateDatabaseConnection(DbConnectionCreateDTO request) throws SQLException;

    /**
     * schema object list
     *
     * @param schema schema
     * @return List
     */
    List<DataListDTO> schemaObjectList(DatabaseMetaArrayIdSchemaQuery schema);

    /**
     * get schema object list
     *
     * @param query query
     * @return List
     * @throws SQLException SQLException
     */
    List<Map<String, Object>> schemaObjects(DatabaseMetaArrayIdSchemaQuery query) throws SQLException;

    /**
     * add database connection
     *
     * @param request request
     * @return DatabaseConnectionDO
     */
    DatabaseConnectionDO addDatabaseConnection(DbConnectionCreateDTO request);

    /**
     * login database connection
     *
     * @param request request
     * @return DatabaseConnectionDO
     */
    DatabaseConnectionDO loginDatabaseConnection(DbConnectionCreateDTO request);

    /**
     * login database connection
     *
     * @param request request
     * @return DatabaseConnectionDO
     */
    DatabaseConnectionDO loginConnection(DbConnectionCreateDTO request);

    /**
     * update time length
     *
     * @param request request
     */
    void timeLength(ConnectionTimeLengthDTO request);

    /**
     * get connection time length
     *
     * @param uuid uuid
     * @return Integer
     */
    Integer getTimeLength(String uuid);
}

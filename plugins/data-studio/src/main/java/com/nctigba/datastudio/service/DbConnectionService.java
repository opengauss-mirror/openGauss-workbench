/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayIdSchemaQuery;

import java.sql.SQLException;
import java.util.List;

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
     * @param id id
     * @return DatabaseConnectionDO
     */
    DatabaseConnectionDO databaseAttributeConnection(String id);

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
    List<DataListDTO> schemaObjectList(DatabaseMetaarrayIdSchemaQuery schema);

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

}

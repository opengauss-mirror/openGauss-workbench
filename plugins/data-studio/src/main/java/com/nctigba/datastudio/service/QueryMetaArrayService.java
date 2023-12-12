/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.DatabaseMetaArrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArraySchemaQuery;
import com.nctigba.datastudio.model.query.UserQuery;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * QueryMetaArrayService
 *
 * @since 2023-6-26
 */
public interface QueryMetaArrayService {
    /**
     * database list
     *
     * @param uuid uuid
     * @return List
     */
    List<String> databaseList(String uuid);

    /**
     * object list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    List<String> objectList(DatabaseMetaArrayQuery request) throws SQLException;

    /**
     * table column list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    List<String> tableColumnList(DatabaseMetaArrayColumnQuery request) throws SQLException;

    /**
     * schema list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    List<Map<String, String>> schemaList(DatabaseMetaArraySchemaQuery request) throws SQLException;

    /**
     * base type list
     *
     * @param uuid uuid
     * @return List
     * @throws SQLException SQLException
     */
    List<String> baseTypeList(String uuid) throws SQLException;

    /**
     * table space list
     *
     * @param uuid uuid
     * @return List
     * @throws SQLException SQLException
     */
    List<String> tablespaceList(String uuid) throws SQLException;


    /**
     * table space list
     *
     * @param uuid uuid
     * @return List
     * @throws SQLException SQLException
     */
    List<Map<String, String>> tablespaceOidList(String uuid) throws SQLException;

    /**
     * user list
     *
     * @param uuid uuid
     * @return UserQuery
     * @throws SQLException SQLException
     */
    UserQuery userList(String uuid) throws SQLException;

    /**
     * resource list
     *
     * @param uuid uuid
     * @return List<String>
     * @throws SQLException SQLException
     */
    List<String> resourceList(String uuid) throws SQLException;

}

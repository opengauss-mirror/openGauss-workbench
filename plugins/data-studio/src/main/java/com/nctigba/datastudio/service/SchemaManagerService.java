/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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

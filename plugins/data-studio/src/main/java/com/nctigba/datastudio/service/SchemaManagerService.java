/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.SchemaManagerRequest;

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
    List<String> queryAllUsers(SchemaManagerRequest request) throws SQLException;

    /**
     * query schema
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, String> querySchema(SchemaManagerRequest request) throws SQLException;

    /**
     * create schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void createSchema(SchemaManagerRequest request) throws SQLException;

    /**
     * update schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void updateSchema(SchemaManagerRequest request) throws SQLException;

    /**
     * delete schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void deleteSchema(SchemaManagerRequest request) throws SQLException;
}

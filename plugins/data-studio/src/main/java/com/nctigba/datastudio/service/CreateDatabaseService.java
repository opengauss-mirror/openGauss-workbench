/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;

import java.sql.SQLException;
import java.util.Map;

/**
 * CreateDatabaseService
 *
 * @since 2023-6-26
 */
public interface CreateDatabaseService {
    /**
     * create database
     *
     * @param database database
     * @throws SQLException SQLException
     */
    void createDatabase(CreateDatabaseDTO database) throws SQLException;

    /**
     * connection database
     *
     * @param database database
     * @return DatabaseConnectionDO
     */
    DatabaseConnectionDO connectionDatabase(DatabaseConnectionDO database);

    /**
     * delete database
     *
     * @param database database
     * @throws SQLException SQLException
     */
    void deleteDatabase(DatabaseNameDTO database) throws SQLException;

    /**
     * rename database
     *
     * @param database database
     * @throws SQLException SQLException
     */
    void renameDatabase(RenameDatabaseDTO database) throws SQLException;

    /**
     * database attribute
     *
     * @param database database
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> databaseAttribute(DatabaseNameDTO database) throws SQLException;

    /**
     * update database attribute
     *
     * @param database database
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> databaseAttributeUpdate(DatabaseNameDTO database) throws SQLException;

}

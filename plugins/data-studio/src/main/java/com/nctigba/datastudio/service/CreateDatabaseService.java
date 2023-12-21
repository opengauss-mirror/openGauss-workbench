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
 *  CreateDatabaseService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/CreateDatabaseService.java
 *
 *  -------------------------------------------------------------------------
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

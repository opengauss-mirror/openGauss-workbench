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
 *  CreateDatabaseController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/CreateDatabaseController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.service.CreateDatabaseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Map;

/**
 * CreateDatabaseController
 *
 * @since 2023-6-26
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class CreateDatabaseController {

    @Resource
    private CreateDatabaseService createDatabaseService;


    /**
     * create database
     *
     * @param database database
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/database/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createDatabase(@RequestBody CreateDatabaseDTO database) throws SQLException {
        createDatabaseService.createDatabase(database);
    }

    /**
     * connection database
     *
     * @param database database
     * @return DatabaseConnectionDO
     */
    @PostMapping(value = "/database/connection", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseConnectionDO connectionDatabase(@RequestBody DatabaseConnectionDO database) {
        return createDatabaseService.connectionDatabase(database);
    }

    /**
     * delete database
     *
     * @param database database
     * @throws SQLException SQLException
     */
    @DeleteMapping(value = "/database/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteDatabase(@RequestBody DatabaseNameDTO database) throws SQLException {
        createDatabaseService.deleteDatabase(database);
    }

    /**
     * rename database
     *
     * @param database database
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/database/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public void renameDatabase(@RequestBody RenameDatabaseDTO database) throws SQLException {
        createDatabaseService.renameDatabase(database);
    }

    /**
     * database attribute
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/database/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> databaseAttribute(DatabaseNameDTO request) throws SQLException {
        return createDatabaseService.databaseAttribute(request);
    }

    /**
     * update database attribute
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/database/attribute/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> databaseAttributeUpdate(DatabaseNameDTO request) throws SQLException {
        return createDatabaseService.databaseAttributeUpdate(request);
    }

}

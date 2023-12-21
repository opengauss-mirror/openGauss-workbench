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
 *  SchemaManagerController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/SchemaManagerController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.query.SchemaManagerQuery;
import com.nctigba.datastudio.service.SchemaManagerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * SchemaManagerController
 *
 * @since 2023-6-26
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class SchemaManagerController {
    @Resource
    private SchemaManagerService schemaManagerService;

    /**
     * query all users
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/schema/query/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> queryAllUsers(@RequestBody SchemaManagerQuery request) throws SQLException {
        return schemaManagerService.queryAllUsers(request);
    }

    /**
     * query schema
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/schema/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> querySchema(@RequestBody SchemaManagerQuery request) throws SQLException {
        return schemaManagerService.querySchema(request);
    }

    /**
     * create schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/schema/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createSchema(@RequestBody SchemaManagerQuery request) throws SQLException {
        schemaManagerService.createSchema(request);
    }

    /**
     * update schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/schema/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateSchema(@RequestBody SchemaManagerQuery request) throws SQLException {
        schemaManagerService.updateSchema(request);
    }

    /**
     * delete schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/schema/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteSchema(@RequestBody SchemaManagerQuery request) throws SQLException {
        schemaManagerService.deleteSchema(request);
    }
}

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
 *  ForeignTableController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/ForeignTableController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.query.ForeignTableQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.service.ForeignTableService;
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
 * ForeignTableController
 *
 * @since 2023-10-10
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class ForeignTableController {
    @Resource
    private ForeignTableService foreignTableService;

    /**
     * query server
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/foreignTable/query/server", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, String>> queryServer(@RequestBody ForeignTableQuery request) throws SQLException {
        return foreignTableService.queryServer(request);
    }

    /**
     * create foreign table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/foreignTable/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void create(@RequestBody ForeignTableQuery request) throws SQLException {
        foreignTableService.create(request);
    }

    /**
     * delete foreign table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/foreignTable/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteForeignTable(@RequestBody ForeignTableQuery request) throws SQLException {
        foreignTableService.deleteForeignTable(request);
    }

    /**
     * delete foreign server
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/foreignServer/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteForeignServer(@RequestBody ForeignTableQuery request) throws SQLException {
        foreignTableService.deleteForeignServer(request);
    }

    /**
     * show foreign table ddl
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/foreignTable/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> showDdl(@RequestBody ForeignTableQuery request) throws SQLException {
        return foreignTableService.ddl(request);
    }

    /**
     * show foreign table attribute
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/foreignTable/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, String>> attribute(@RequestBody ForeignTableQuery request) throws SQLException {
        return foreignTableService.attribute(request);
    }

    /**
     * test connection
     *
     * @param request request
     * @return String
     */
    @PostMapping(value = "/foreignTable/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public String test(@RequestBody ForeignTableQuery request) {
        return foreignTableService.test(request);
    }

    /**
     * create foreign server
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/foreignTable/createServer", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createServer(@RequestBody ForeignTableQuery request) throws SQLException {
        foreignTableService.createServer(request);
    }

    /**
     * edit foreign table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/foreignTable/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public void edit(@RequestBody TableDataEditQuery request) throws SQLException {
        foreignTableService.edit(request);
    }
}

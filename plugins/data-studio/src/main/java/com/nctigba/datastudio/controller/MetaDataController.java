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
 *  MetaDataController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/MetaDataController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayIdSchemaQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArraySchemaQuery;
import com.nctigba.datastudio.model.query.UserQuery;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.QueryMetaArrayService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * MetaDataController
 *
 * @since 2023-6-26
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1/metaData")
public class MetaDataController {
    @Resource
    private DbConnectionService dbConnectionService;
    @Resource
    private QueryMetaArrayService queryMetaArrayService;

    /**
     * database list
     *
     * @param uuid uuid
     * @return List
     */
    @GetMapping(value = "/databaseList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> databaseList(@RequestParam("uuid") String uuid) {
        return queryMetaArrayService.databaseList(uuid);
    }

    /**
     * schema object list
     *
     * @param schema schema
     * @return List
     */
    @GetMapping(value = "/schemaObjectList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DataListDTO> schemaObjectList(DatabaseMetaArrayIdSchemaQuery schema) {
        return dbConnectionService.schemaObjectList(schema);
    }

    /**
     * get schema object list
     *
     * @param query query
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/schemaObjects", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> schemaObjects(DatabaseMetaArrayIdSchemaQuery query) throws SQLException {
        return dbConnectionService.schemaObjects(query);
    }

    /**
     * schema list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/schemaList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, String>> schemaList(DatabaseMetaArraySchemaQuery request) throws SQLException {
        return queryMetaArrayService.schemaList(request);
    }

    /**
     * object list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/objectList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> objectList(DatabaseMetaArrayQuery request) throws SQLException {
        return queryMetaArrayService.objectList(request);
    }

    /**
     * column list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/columnList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> tableColumnList(DatabaseMetaArrayColumnQuery request) throws SQLException {
        return queryMetaArrayService.tableColumnList(request);
    }

    /**
     * type list
     *
     * @param uuid uuid
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/typeList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> typeList(@RequestParam("uuid") String uuid) throws SQLException {
        return queryMetaArrayService.baseTypeList(uuid);
    }

    /**
     * table space list
     *
     * @param uuid uuid
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/tablespace", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> tablespaceList(@RequestParam("uuid") String uuid) throws SQLException {
        return queryMetaArrayService.tablespaceList(uuid);
    }

    /**
     * table space list
     *
     * @param uuid uuid
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/tablespace/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, String>> tablespaceOidList(@RequestParam("uuid") String uuid) throws SQLException {
        return queryMetaArrayService.tablespaceOidList(uuid);
    }

    /**
     * user list
     *
     * @param uuid uuid..
     * @return UserQuery
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserQuery userList(@RequestParam("uuid") String uuid) throws SQLException {
        return queryMetaArrayService.userList(uuid);
    }

    /**
     * user list
     *
     * @param uuid uuid..
     * @return List<String>
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/resource", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> resourceList(@RequestParam("uuid") String uuid) throws SQLException {
        return queryMetaArrayService.resourceList(uuid);
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayIdSchemaQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarraySchemaQuery;
import com.nctigba.datastudio.model.query.UserQuery;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.QueryMetaArrayService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @GetMapping(value = "/databaseList/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> databaseList(@PathVariable("uuid") String uuid) {
        return queryMetaArrayService.databaseList(uuid);
    }

    /**
     * schema object list
     *
     * @param schema schema
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/schemaObjectList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DataListDTO> schemaObjectList(DatabaseMetaarrayIdSchemaQuery schema) throws SQLException {
        return dbConnectionService.schemaObjectList(schema);
    }

    /**
     * schema list
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/schemaList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, String>> schemaList(DatabaseMetaarraySchemaQuery request) throws SQLException {
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
    public List<String> objectList(DatabaseMetaarrayQuery request) throws SQLException {
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
    public List<String> tableColumnList(DatabaseMetaarrayColumnQuery request) throws SQLException {
        return queryMetaArrayService.tableColumnList(request);
    }

    /**
     * type list
     *
     * @param uuid uuid
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/typeList/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> typeList(@PathVariable("uuid") String uuid) throws SQLException {
        return queryMetaArrayService.baseTypeList(uuid);
    }

    /**
     * table space list
     *
     * @param uuid uuid
     * @return List
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/tablespace/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> tablespaceList(@PathVariable("uuid") String uuid) throws SQLException {
        return queryMetaArrayService.tablespaceList(uuid);
    }

    /**
     * user list
     *
     * @param uuid uuid..
     * @return UserQuery
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/user/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserQuery userList(@PathVariable("uuid") String uuid) throws SQLException {
        return queryMetaArrayService.userList(uuid);
    }

    /**
     * user list
     *
     * @param uuid uuid..
     * @return List<String>
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/resource/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> resourceList(@PathVariable("uuid") String uuid) throws SQLException {
        return queryMetaArrayService.resourceList(uuid);
    }
}

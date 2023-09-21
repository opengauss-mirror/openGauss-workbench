/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.query.SchemaManagerRequest;
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
    public List<String> queryAllUsers(@RequestBody SchemaManagerRequest request) throws SQLException {
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
    public Map<String, String> querySchema(@RequestBody SchemaManagerRequest request) throws SQLException {
        return schemaManagerService.querySchema(request);
    }

    /**
     * create schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/schema/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createSchema(@RequestBody SchemaManagerRequest request) throws SQLException {
        schemaManagerService.createSchema(request);
    }

    /**
     * update schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/schema/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateSchema(@RequestBody SchemaManagerRequest request) throws SQLException {
        schemaManagerService.updateSchema(request);
    }

    /**
     * delete schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/schema/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteSchema(@RequestBody SchemaManagerRequest request) throws SQLException {
        schemaManagerService.deleteSchema(request);
    }
}

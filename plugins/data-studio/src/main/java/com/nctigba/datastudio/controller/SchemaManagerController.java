/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.query.SchemaManagerRequest;
import com.nctigba.datastudio.service.SchemaManagerService;
import io.swagger.annotations.Api;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = {"Schema manager interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class SchemaManagerController {
    @Resource
    private SchemaManagerService schemaManagerService;

    @PostMapping(value = "/schema/query/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> queryAllUsers(@RequestBody SchemaManagerRequest request) throws Exception {
        return schemaManagerService.queryAllUsers(request);
    }

    @PostMapping(value = "/schema/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> querySchema(@RequestBody SchemaManagerRequest request) throws Exception {
        return schemaManagerService.querySchema(request);
    }

    @PostMapping(value = "/schema/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createSchema(@RequestBody SchemaManagerRequest request) throws Exception {
        schemaManagerService.createSchema(request);
    }

    @PostMapping(value = "/schema/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateSchema(@RequestBody SchemaManagerRequest request) throws Exception {
        schemaManagerService.updateSchema(request);
    }

    @PostMapping(value = "/schema/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteSchema(@RequestBody SchemaManagerRequest request) throws Exception {
        schemaManagerService.deleteSchema(request);
    }
}

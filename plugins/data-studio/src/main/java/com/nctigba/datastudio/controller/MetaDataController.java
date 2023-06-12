/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayIdSchemaQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarraySchemaQuery;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.QueryMetaArrayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1/metaData")
public class MetaDataController {

    @Resource
    private DbConnectionService dbConnectionService;
    @Resource
    private QueryMetaArrayService queryMetaArrayService;

    @ApiOperation(value = "Database List")
    @GetMapping(value = "/databaseList/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> databaseList(@PathVariable("uuid") String uuid) throws Exception {
        return queryMetaArrayService.databaseList(uuid);
    }

    @ApiOperation(value = "Database List")
    @GetMapping(value = "/schemaObjectList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DataListDTO> schemaObjectList(DatabaseMetaarrayIdSchemaQuery schema) throws Exception {
        return dbConnectionService.schemaObjectList(schema);
    }

    @ApiOperation(value = "Schema List")
    @GetMapping(value = "/schemaList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, String>> schemaList(DatabaseMetaarraySchemaQuery request) throws Exception {
        return queryMetaArrayService.schemaList(request);
    }


    @ApiOperation(value = "Object List")
    @GetMapping(value = "/objectList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> objectList(DatabaseMetaarrayQuery request) throws Exception {
        return queryMetaArrayService.objectList(request);
    }


    @ApiOperation(value = "Column List")
    @GetMapping(value = "/columnList", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> tableColumnList(DatabaseMetaarrayColumnQuery request) throws Exception {
        return queryMetaArrayService.tableColumnList(request);
    }


    @ApiOperation(value = "Column List")
    @GetMapping(value = "/typeList/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> typeList(@PathVariable("uuid") String uuid) throws Exception {
        return queryMetaArrayService.baseTypeList(uuid);
    }

    @ApiOperation(value = "Column List")
    @GetMapping(value = "/tablespace/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> tablespaceList(@PathVariable("uuid") String uuid) throws Exception {
        return queryMetaArrayService.tablespaceList(uuid);
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.service.DatabaseSynonymService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
 * DatabaseSynonymController
 *
 * @since 2023-6-26
 */
@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseSynonymController {
    @Resource
    private DatabaseSynonymService databaseSynonymService;

    /**
     * create
     *
     * @param request request
     * @return String
     */
    @ApiOperation(value = "CREATE SYNONYM DDL")
    @PostMapping(value = "/synonyms/action", params = "action=createSynonymDdl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createSynonymDDL(@RequestBody DatabaseCreateSynonymDTO request) {
        return databaseSynonymService.createSynonymDDL(request);
    }

    /**
     * create synonym
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "CREATE SYNONYM")
    @PostMapping(value = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createSynonym(@RequestBody DatabaseCreateSynonymDTO request) throws SQLException {
        databaseSynonymService.createSynonym(request);
    }

    /**
     * synonym attribute
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "CREATE SYNONYM")
    @GetMapping(value = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> synonymAttribute(DatabaseSynonymAttributeDTO request) throws SQLException {
        return databaseSynonymService.synonymAttribute(request);
    }

    /**
     * drop synonym
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "DROP SYNONYM")
    @DeleteMapping(value = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropSynonym(@RequestBody DatabaseDropSynonymDTO request) throws SQLException {
        databaseSynonymService.dropSynonym(request);
    }
}

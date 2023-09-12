/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.service.DatabaseViewService;
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
import java.util.List;
import java.util.Map;

/**
 * DatabaseViewController
 *
 * @since 2023-6-26
 */
@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseViewController {
    @Resource
    private DatabaseViewService databaseViewService;

    /**
     * create view ddl
     *
     * @param request request
     * @return String
     */
    @ApiOperation(value = "CREATE VIEW DDL")
    @PostMapping(value = "/views/action", params = "action=createViewDdl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createViewDDL(@RequestBody DatabaseCreateViewDTO request) {
        return databaseViewService.createViewDDL(request);
    }

    /**
     * create view
     *
     * @param request request
     */
    @ApiOperation(value = "CREATE VIEW")
    @PostMapping(value = "/views", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createView(@RequestBody DatabaseCreateViewDTO request) {
        databaseViewService.createView(request);
    }

    /**
     * drop view
     *
     * @param request request
     */
    @ApiOperation(value = "DROP VIEW")
    @DeleteMapping(value = "/views", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropView(@RequestBody DatabaseViewDdlDTO request) {
        databaseViewService.dropView(request);
    }

    /**
     * select view
     *
     * @param request request
     * @return Map
     */
    @ApiOperation(value = "SELECT VIEW")
    @GetMapping(value = "/viewDatas", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> selectView(DatabaseSelectViewDTO request) {
        return databaseViewService.selectView(request);
    }

    /**
     * return view ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "RETURN VIEW DDL")
    @PostMapping(value = "/viewDdls", produces = MediaType.APPLICATION_JSON_VALUE)
    public String returnViewDDL(@RequestBody DatabaseViewDdlDTO request) throws SQLException {
        return databaseViewService.returnViewDDL(request);
    }

    /**
     * edit view
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/view/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public void editView(@RequestBody DatabaseViewDTO request) throws SQLException {
        databaseViewService.editView(request);
    }

    /**
     * show view attribute
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/view/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, String>> viewAttribute(@RequestBody DatabaseViewDdlDTO request) throws SQLException {
        return databaseViewService.viewAttribute(request);
    }

    /**
     * show view column
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/view/column", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> viewColumn(@RequestBody DatabaseViewDTO request) throws SQLException {
        return databaseViewService.viewColumn(request);
    }

    /**
     * query view
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/view/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> queryView(@RequestBody DatabaseViewDdlDTO request) throws SQLException {
        return databaseViewService.queryView(request);
    }
}

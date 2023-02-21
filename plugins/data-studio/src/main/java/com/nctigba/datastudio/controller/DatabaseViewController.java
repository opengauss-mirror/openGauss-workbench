package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
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
import java.util.Map;

@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseViewController {

    @Resource
    private DatabaseViewService databaseViewService;


    @ApiOperation(value = "CREATE VIEW DDL")
    @PostMapping(value = "/views/action", params = "action=createViewDdl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createViewDDL(@RequestBody DatabaseCreateViewDTO request) throws Exception {
        return databaseViewService.createViewDDL(request);
    }


    @ApiOperation(value = "CREATE VIEW")
    @PostMapping(value = "/views", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createView(@RequestBody DatabaseCreateViewDTO request) throws Exception {
        databaseViewService.createView(request);
    }


    @ApiOperation(value = "DROP VIEW")
    @DeleteMapping(value = "/views", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropView(@RequestBody DatabaseViewDdlDTO request) throws Exception {
        databaseViewService.dropView(request);
    }


    @ApiOperation(value = "SELECT VIEW")
    @GetMapping(value = "/viewDatas", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> selectView(DatabaseSelectViewDTO request) throws Exception {
        return databaseViewService.selectView(request);
    }


    @ApiOperation(value = "RETURN VIEW DDL")
    @PostMapping(value = "/viewDdls", produces = MediaType.APPLICATION_JSON_VALUE)
    public String returnViewDDL(@RequestBody DatabaseViewDdlDTO request) throws Exception {
        return databaseViewService.returnViewDDL(request);
    }
}

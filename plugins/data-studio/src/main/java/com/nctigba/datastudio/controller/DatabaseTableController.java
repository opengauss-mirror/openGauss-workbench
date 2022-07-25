package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.service.TableDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseTableController {

    @Resource
    private TableDataService tableDataService;


    @ApiOperation(value = "Table Data List")
    @GetMapping(value = "/tableDatas", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableData(SelectDataQuery request) throws Exception {
        return tableDataService.tableData(request);
    }


    @ApiOperation(value = "Table Column List")
    @GetMapping(value = "/tableColumns", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableColumn(SelectDataQuery request) throws Exception {
        return tableDataService.tableColumn(request);
    }


    @ApiOperation(value = "Table Index List")
    @GetMapping(value = "/tableIndexs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableIndex(SelectDataQuery request) throws Exception {
        return tableDataService.tableIndex(request);
    }


    @ApiOperation(value = "Table Constraint List")
    @GetMapping(value = "/tableConstraints", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableConstraint(SelectDataQuery request) throws Exception {
        return tableDataService.tableConstraint(request);
    }


    @ApiOperation(value = "Table DDL SQL")
    @GetMapping(value = "/tableDdls", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> tableDdl(SelectDataQuery request) throws Exception {
        return tableDataService.tableDdl(request);
    }
}

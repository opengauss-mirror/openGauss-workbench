/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DatabaseConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintPkDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateTableDTO;
import com.nctigba.datastudio.model.dto.DatabaseIndexDTO;
import com.nctigba.datastudio.model.dto.TableAlterDTO;
import com.nctigba.datastudio.model.dto.TableAttributeDTO;
import com.nctigba.datastudio.model.dto.TableDataDTO;
import com.nctigba.datastudio.model.dto.TableNameDTO;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.model.query.TableDataQuery;
import com.nctigba.datastudio.model.query.TablePKDataQuery;
import com.nctigba.datastudio.service.TableDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ResultSetMapDAO.winMap;

@Slf4j
@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseTableController {

    @Resource
    private TableDataService tableDataService;


    @ApiOperation(value = "Table Data List")
    @GetMapping(value = "/tableDatas", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableDataDTO tableData(TableDataQuery request) throws Exception {
        log.info("tableDataController request is: {}", request);
        return tableDataService.tableData(request);
    }

    @ApiOperation(value = "Table Data List")
    @PostMapping(value = "/tableDatas/edit")
    public TablePKDataQuery tableDataEdit(@RequestBody TableDataEditQuery request) throws Exception {
        log.info("tableDataEditController request is: {}", request);
        return tableDataService.tableDataEdit(request);
    }

    @ApiOperation(value = "Table Data List")
    @GetMapping(value = "/tableDatas/close/{winId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableDataClose(@PathVariable("winId") String winId) throws Exception {
        log.info("map log winID :{}",winMap);
        tableDataService.tableDataClose(winId);
    }

    @ApiOperation(value = "Table Column List")
    @GetMapping(value = "/tableColumns", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableColumn(SelectDataQuery request) throws Exception {
        return tableDataService.tableColumn(request);
    }

    @ApiOperation(value = "Edit Table Column")
    @PostMapping(value = "/tableColumns/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> editTableColumn(@RequestBody DatabaseCreUpdColumnDTO request) throws Exception {
        return tableDataService.tableColumnEdit(request);
    }

    @ApiOperation(value = "Table Index List")
    @GetMapping(value = "/tableIndexs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableIndex(SelectDataQuery request) throws Exception {
        return tableDataService.tableIndex(request);
    }

    @ApiOperation(value = "edit index")
    @PostMapping(value = "/index", produces = MediaType.APPLICATION_JSON_VALUE)
    public void editIndex(@RequestBody DatabaseIndexDTO request) throws Exception {
        tableDataService.tableEditIndex(request);
    }


    @ApiOperation(value = "Table Constraint List")
    @GetMapping(value = "/tableConstraints", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableConstraint(SelectDataQuery request) throws Exception {
        return tableDataService.tableConstraint(request);
    }

    @ApiOperation(value = "edit constraint")
    @PostMapping(value = "/constraint", produces = MediaType.APPLICATION_JSON_VALUE)
    public void editConstraint(@RequestBody DatabaseConstraintDTO request) throws Exception {
        tableDataService.tableEditConstraint(request);
    }

    @ApiOperation(value = "add constraint PK")
    @PostMapping(value = "/constraint/pk", produces = MediaType.APPLICATION_JSON_VALUE)
    public void editPkConstraint(@RequestBody DatabaseConstraintPkDTO request) throws Exception {
        tableDataService.tableEditPkConstraint(request);
    }

    @ApiOperation(value = "Table DDL SQL")
    @GetMapping(value = "/tableDdls", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> tableDdl(SelectDataQuery request) throws Exception {
        return tableDataService.tableDdl(request);
    }

    @ApiOperation(value = "Table CREATE")
    @PostMapping(value = "/table", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableCreate(@RequestBody DatabaseCreateTableDTO request) throws Exception {
        tableDataService.tableCreate(request);
    }

    @ApiOperation(value = "Table CREATE DDL")
    @PostMapping(value = "/table/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String tableCreateDDL(@RequestBody DatabaseCreateTableDTO request) throws Exception {
        return tableDataService.returnTableCreate(request);
    }

    @ApiOperation(value = "Table TRUNCATE")
    @PostMapping(value = "/table/truncate", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableTruncate(@RequestBody TableNameDTO request) throws Exception {
        tableDataService.tableTruncate(request);
    }

    @ApiOperation(value = "Table vacuum")
    @PostMapping(value = "/table/vacuum", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableVacuum(@RequestBody TableNameDTO request) throws Exception {
        tableDataService.tableVacuum(request);
    }

    @ApiOperation(value = "Table reindex")
    @PostMapping(value = "/table/reindex", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableReindex(@RequestBody TableNameDTO request) throws Exception {
        tableDataService.tableReindex(request);
    }

    @ApiOperation(value = "Table rename")
    @PostMapping(value = "/table/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableRename(@RequestBody TableAlterDTO request) throws Exception {
        tableDataService.tableRename(request);
    }

    @ApiOperation(value = "Table comment")
    @PostMapping(value = "/table/comment", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableComment(@RequestBody TableAlterDTO request) throws Exception {
        tableDataService.tableComment(request);
    }

    @ApiOperation(value = "Table reschema")
    @PostMapping(value = "/table/reschema", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableAlterSchema(@RequestBody TableAlterDTO request) throws Exception {
        tableDataService.tableAlterSchema(request);
    }

    @ApiOperation(value = "Table drop")
    @DeleteMapping(value = "/table/drop", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableDrop(@RequestBody TableNameDTO request) throws Exception {
        tableDataService.tableDrop(request);
    }

    @ApiOperation(value = "Table reschema")
    @PostMapping(value = "/table/retablespace", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableAlterTablespace(@RequestBody TableAlterDTO request) throws Exception {
        tableDataService.tableAlterTablespace(request);
    }

    @ApiOperation(value = "Table drop")
    @PostMapping(value = "/table/sequence", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableSequence(@RequestBody TableNameDTO request) throws Exception {
        return tableDataService.tableSequence(request);
    }

    @ApiOperation(value = "Table drop")
    @PostMapping(value = "/table/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> tableAttribute(@RequestBody TableAttributeDTO request) throws Exception {
        return tableDataService.tableAttribute(request);
    }

}

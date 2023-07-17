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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ResultSetMapDAO.winMap;

/**
 * DatabaseTableController
 *
 * @since 2023-6-26
 */
@Slf4j
@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseTableController {
    @Resource
    private TableDataService tableDataService;

    /**
     * table data
     *
     * @param request request
     * @return TableDataDTO
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table Data List")
    @GetMapping(value = "/tableDatas", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableDataDTO tableData(TableDataQuery request) throws SQLException {
        log.info("tableDataController request is: {}", request);
        return tableDataService.tableData(request);
    }

    /**
     * edit table data
     *
     * @param request request
     * @return TablePKDataQuery
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table Data List")
    @PostMapping(value = "/tableDatas/edit")
    public TablePKDataQuery tableDataEdit(@RequestBody TableDataEditQuery request) throws SQLException {
        log.info("tableDataEditController request is: {}", request);
        return tableDataService.tableDataEdit(request);
    }

    /**
     * close table data
     *
     * @param winId winId
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table Data List")
    @GetMapping(value = "/tableDatas/close/{winId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableDataClose(@PathVariable("winId") String winId) throws SQLException {
        log.info("map log winID :{}", winMap);
        tableDataService.tableDataClose(winId);
    }

    /**
     * table column list
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table Column List")
    @GetMapping(value = "/tableColumns", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableColumn(SelectDataQuery request) throws SQLException {
        return tableDataService.tableColumn(request);
    }

    /**
     * edit table column
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Edit Table Column")
    @PostMapping(value = "/tableColumns/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> editTableColumn(@RequestBody DatabaseCreUpdColumnDTO request) throws SQLException {
        return tableDataService.tableColumnEdit(request);
    }

    /**
     * table index
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table Index List")
    @GetMapping(value = "/tableIndexs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableIndex(SelectDataQuery request) throws SQLException {
        return tableDataService.tableIndex(request);
    }

    /**
     * edit table index
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "edit index")
    @PostMapping(value = "/index", produces = MediaType.APPLICATION_JSON_VALUE)
    public void editIndex(@RequestBody DatabaseIndexDTO request) throws SQLException {
        tableDataService.tableEditIndex(request);
    }

    /**
     * table constrain
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table Constraint List")
    @GetMapping(value = "/tableConstraints", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableConstraint(SelectDataQuery request) throws SQLException {
        return tableDataService.tableConstraint(request);
    }

    /**
     * edit constraint
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "edit constraint")
    @PostMapping(value = "/constraint", produces = MediaType.APPLICATION_JSON_VALUE)
    public void editConstraint(@RequestBody DatabaseConstraintDTO request) throws SQLException {
        tableDataService.tableEditConstraint(request);
    }

    /**
     * edit PK constraint
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "add constraint PK")
    @PostMapping(value = "/constraint/pk", produces = MediaType.APPLICATION_JSON_VALUE)
    public void editPkConstraint(@RequestBody DatabaseConstraintPkDTO request) throws SQLException {
        tableDataService.tableEditPkConstraint(request);
    }

    /**
     * table ddl
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table DDL SQL")
    @GetMapping(value = "/tableDdls", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> tableDdl(SelectDataQuery request) throws SQLException {
        return tableDataService.tableDdl(request);
    }

    /**
     * create table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table CREATE")
    @PostMapping(value = "/table", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableCreate(@RequestBody DatabaseCreateTableDTO request) throws SQLException {
        tableDataService.tableCreate(request);
    }

    /**
     * table create ddl
     *
     * @param request request
     * @return String
     */
    @ApiOperation(value = "Table CREATE DDL")
    @PostMapping(value = "/table/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String tableCreateDDL(@RequestBody DatabaseCreateTableDTO request) {
        return tableDataService.returnTableCreate(request);
    }

    /**
     * table truncate
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table TRUNCATE")
    @PostMapping(value = "/table/truncate", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableTruncate(@RequestBody TableNameDTO request) throws SQLException {
        tableDataService.tableTruncate(request);
    }

    /**
     * table vacuum
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table vacuum")
    @PostMapping(value = "/table/vacuum", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableVacuum(@RequestBody TableNameDTO request) throws SQLException {
        tableDataService.tableVacuum(request);
    }

    /**
     * table reindex
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table reindex")
    @PostMapping(value = "/table/reindex", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableReindex(@RequestBody TableNameDTO request) throws SQLException {
        tableDataService.tableReindex(request);
    }

    /**
     * table rename
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table rename")
    @PostMapping(value = "/table/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableRename(@RequestBody TableAlterDTO request) throws SQLException {
        tableDataService.tableRename(request);
    }

    /**
     * table comment
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table comment")
    @PostMapping(value = "/table/comment", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableComment(@RequestBody TableAlterDTO request) throws SQLException {
        tableDataService.tableComment(request);
    }

    /**
     * table alter schema
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table reschema")
    @PostMapping(value = "/table/reschema", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableAlterSchema(@RequestBody TableAlterDTO request) throws SQLException {
        tableDataService.tableAlterSchema(request);
    }

    /**
     * drop table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table drop")
    @DeleteMapping(value = "/table/drop", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableDrop(@RequestBody TableNameDTO request) throws SQLException {
        tableDataService.tableDrop(request);
    }

    /**
     * alter table space
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table reschema")
    @PostMapping(value = "/table/retablespace", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableAlterTablespace(@RequestBody TableAlterDTO request) throws SQLException {
        tableDataService.tableAlterTablespace(request);
    }

    /**
     * get table sequence
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table drop")
    @PostMapping(value = "/table/sequence", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableSequence(@RequestBody TableNameDTO request) throws SQLException {
        return tableDataService.tableSequence(request);
    }

    /**
     * get table attribute
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @ApiOperation(value = "Table drop")
    @PostMapping(value = "/table/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> tableAttribute(@RequestBody TableAttributeDTO request) throws SQLException {
        return tableDataService.tableAttribute(request);
    }

}

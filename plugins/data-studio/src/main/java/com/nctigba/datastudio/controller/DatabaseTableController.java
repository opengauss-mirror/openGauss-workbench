/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  DatabaseTableController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/DatabaseTableController.java
 *
 *  -------------------------------------------------------------------------
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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    @PostMapping(value = "/tableDatas", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableDataDTO tableData(@RequestBody TableDataQuery request) throws SQLException {
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
    @PostMapping(value = "/tableDatas/edit", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping(value = "/table/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> tableAttribute(@RequestBody TableAttributeDTO request) throws SQLException {
        return tableDataService.tableAttribute(request);
    }

    /**
     * get table analyze
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/table/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tableAnalyze(@RequestBody TableNameDTO request) throws SQLException {
        tableDataService.tableAnalyze(request);
    }

    /**
     * get table attribute partition
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/table/attribute/partition", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> tableAttributePartition(TableNameDTO request) throws SQLException {
        return tableDataService.tableAttributePartition(request);
    }

    /**
     * export table data by page
     *
     * @param request  request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    @PostMapping(value = "/table/exportData", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportData(
            @RequestBody TableDataQuery request, HttpServletResponse response) throws SQLException, IOException {
        tableDataService.exportData(request, response);
    }
}

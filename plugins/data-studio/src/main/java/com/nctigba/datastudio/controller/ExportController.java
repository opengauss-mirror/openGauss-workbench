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
 *  ExportController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/ExportController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.query.ExportQuery;
import com.nctigba.datastudio.service.ExportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * ExportController
 *
 * @since 2023-06-26
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class ExportController {
    @Resource
    private ExportService exportService;

    /**
     * export table ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    @PostMapping(value = "/export/table/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportTableDdl(@RequestBody ExportQuery request, HttpServletResponse response)
            throws SQLException, IOException {
        exportService.exportTableDdl(request, response);
    }

    /**
     * export table data
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    @PostMapping(value = "/export/table/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportTableData(@RequestBody ExportQuery request, HttpServletResponse response)
            throws SQLException, IOException {
        exportService.exportTableData(request, response);
    }

    /**
     * import table data
     *
     * @param request request
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    @PostMapping(value = "/import/table/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public void importTableData(ExportQuery request) throws SQLException, IOException {
        exportService.importTableData(request);
    }

    /**
     * export function ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    @PostMapping(value = "/export/function/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportFunctionDdl(@RequestBody ExportQuery request, HttpServletResponse response)
            throws SQLException, IOException {
        exportService.exportFunctionDdl(request, response);
    }

    /**
     * export view ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    @PostMapping(value = "/export/view/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportViewDdl(@RequestBody ExportQuery request, HttpServletResponse response)
            throws SQLException, IOException {
        exportService.exportViewDdl(request, response);
    }

    /**
     * export sequence ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    @PostMapping(value = "/export/sequence/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportSequenceDdl(@RequestBody ExportQuery request, HttpServletResponse response)
            throws SQLException, IOException {
        exportService.exportSequenceDdl(request, response);
    }

    /**
     * export schema ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    @PostMapping(value = "/export/schema/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportSchemaDdl(@RequestBody ExportQuery request, HttpServletResponse response)
            throws SQLException, IOException {
        exportService.exportSchemaDdl(request, response);
    }
}

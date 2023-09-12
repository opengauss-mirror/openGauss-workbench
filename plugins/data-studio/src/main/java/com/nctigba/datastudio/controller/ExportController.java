/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.query.ExportRequest;
import com.nctigba.datastudio.service.ExportService;
import io.swagger.annotations.Api;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * ExportController
 *
 * @since 2023-06-26
 */
@Api(tags = {"Schema manager interface"})
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
    public void exportTableDdl(@RequestBody ExportRequest request, HttpServletResponse response)
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
    public void exportTableData(@RequestBody ExportRequest request, HttpServletResponse response)
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
    public void importTableData(ExportRequest request) throws SQLException, IOException {
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
    public void exportFunctionDdl(@RequestBody ExportRequest request, HttpServletResponse response)
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
    public void exportViewDdl(@RequestBody ExportRequest request, HttpServletResponse response)
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
    public void exportSequenceDdl(@RequestBody ExportRequest request, HttpServletResponse response)
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
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    @PostMapping(value = "/export/schema/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportSchemaDdl(@RequestBody ExportRequest request, HttpServletResponse response)
            throws SQLException, IOException, ExecutionException, InterruptedException {
        exportService.exportSchemaDdl(request, response);
    }
}

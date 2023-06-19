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

@Api(tags = {"Schema manager interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class ExportController {
    @Resource
    private ExportService exportService;

    @PostMapping(value = "/export/table/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportTableDdl(@RequestBody ExportRequest request, HttpServletResponse response) throws Exception {
        exportService.exportTableDdl(request, response);
    }

    @PostMapping(value = "/export/table/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportTableData(@RequestBody ExportRequest request, HttpServletResponse response) throws Exception {
        exportService.exportTableData(request, response);
    }

    @PostMapping(value = "/export/function/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportFunctionDdl(@RequestBody ExportRequest request, HttpServletResponse response) throws Exception {
        exportService.exportFunctionDdl(request, response);
    }

    @PostMapping(value = "/export/view/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportViewDdl(@RequestBody ExportRequest request, HttpServletResponse response) throws Exception {
        exportService.exportViewDdl(request, response);
    }

    @PostMapping(value = "/export/sequence/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportSequenceDdl(@RequestBody ExportRequest request, HttpServletResponse response) throws Exception {
        exportService.exportSequenceDdl(request, response);
    }

    @PostMapping(value = "/export/schema/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportSchemaDdl(@RequestBody ExportRequest request, HttpServletResponse response) throws Exception {
        exportService.exportSchemaDdl(request, response);
    }
}

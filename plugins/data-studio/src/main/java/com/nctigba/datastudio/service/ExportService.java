/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.ExportRequest;

import javax.servlet.http.HttpServletResponse;

public interface ExportService {
    void exportTableDdl(ExportRequest request, HttpServletResponse response) throws Exception;

    void exportTableData(ExportRequest request, HttpServletResponse response) throws Exception;

    void exportFunctionDdl(ExportRequest request, HttpServletResponse response) throws Exception;

    void exportViewDdl(ExportRequest request, HttpServletResponse response) throws Exception;

    void exportSequenceDdl(ExportRequest request, HttpServletResponse response) throws Exception;

    void exportSchemaDdl(ExportRequest request, HttpServletResponse response) throws Exception;
}

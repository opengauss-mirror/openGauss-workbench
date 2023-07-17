/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.ExportRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * ExportService
 *
 * @since 2023-6-26
 */
public interface ExportService {
    /**
     * export table ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportTableDdl(ExportRequest request, HttpServletResponse response) throws SQLException, IOException;

    /**
     * export table data
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportTableData(ExportRequest request, HttpServletResponse response) throws SQLException, IOException;

    /**
     * export function ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportFunctionDdl(ExportRequest request, HttpServletResponse response) throws SQLException, IOException;

    /**
     * export view ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportViewDdl(ExportRequest request, HttpServletResponse response) throws SQLException, IOException;

    /**
     * export sequence ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportSequenceDdl(ExportRequest request, HttpServletResponse response) throws SQLException, IOException;

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
    void exportSchemaDdl(
            ExportRequest request,
            HttpServletResponse response) throws SQLException, IOException, ExecutionException, InterruptedException;
}

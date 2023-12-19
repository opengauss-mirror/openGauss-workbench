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
 *  ExportService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/ExportService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.ExportQuery;

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
    void exportTableDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException;

    /**
     * export table data
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportTableData(ExportQuery request, HttpServletResponse response) throws SQLException, IOException;

    /**
     * import table data
     *
     * @param request request
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void importTableData(ExportQuery request) throws SQLException, IOException;


    /**
     * export function ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportFunctionDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException;

    /**
     * export view ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportViewDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException;

    /**
     * export sequence ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void exportSequenceDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException;

    /**
     * export schema ddl
     *
     * @param request request
     * @param response response
     * @throws IOException IOException
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    void exportSchemaDdl(ExportQuery request, HttpServletResponse response)
            throws IOException, ExecutionException, InterruptedException;
}

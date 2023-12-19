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
 *  ExportServiceSqlService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/ExportServiceSqlService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.ExportQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * ExportServiceSqlService
 *
 * @since 2023-12-12
 */
public interface ExportServiceSqlService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * export table ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    default void exportTableDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * export table data
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    default void exportTableData(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * import table data
     *
     * @param request request
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    default void importTableData(ExportQuery request) throws SQLException, IOException {
        throw new CustomException(DebugUtils.getMessage());
    }


    /**
     * export function ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    default void exportFunctionDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * export view ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    default void exportViewDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * export sequence ddl
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    default void exportSequenceDdl(ExportQuery request, HttpServletResponse response) throws SQLException, IOException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * export schema ddl
     *
     * @param request request
     * @param response response
     * @throws IOException IOException
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    default void exportSchemaDdl(ExportQuery request, HttpServletResponse response)
            throws IOException, ExecutionException, InterruptedException {
        throw new CustomException(DebugUtils.getMessage());
    }
}

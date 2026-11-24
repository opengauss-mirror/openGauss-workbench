/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.ExportQuery;
import com.nctigba.datastudio.model.query.ExportResultQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

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
     * @throws SQLException SQLException
     */
    default void exportSchemaDdl(ExportQuery request, HttpServletResponse response) throws IOException, SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * export result
     *
     * @param request ExportResultQuery
     * @param response HttpServletResponse
     * @throws IOException IOException
     * @throws SQLException SQLException
     */
    default void exportResult(ExportResultQuery request, HttpServletResponse response)
            throws IOException, SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }
}

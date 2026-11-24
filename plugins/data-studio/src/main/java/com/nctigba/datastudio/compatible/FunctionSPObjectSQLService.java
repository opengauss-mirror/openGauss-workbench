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

import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.model.query.PackageQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;

/**
 * FunctionSPObjectSQLService
 *
 * @since 2023-6-26
 */
public interface FunctionSPObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * get function ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    default String functionDdl(DatabaseFunctionSPDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * drop function
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    default String dropFunctionSP(DatabaseFunctionSPDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * drop package
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    default String dropPackage(PackageQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }
}

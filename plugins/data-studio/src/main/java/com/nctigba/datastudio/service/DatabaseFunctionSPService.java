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

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.model.query.PackageQuery;

import java.sql.SQLException;

/**
 * DatabaseFunctionSPService
 *
 * @since 2023-6-26
 */
public interface DatabaseFunctionSPService {
    /**
     * get function ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    String functionDdl(DatabaseFunctionSPDTO request) throws SQLException;

    /**
     * drop function
     *
     * @param request request
     */
    void dropFunctionSP(DatabaseFunctionSPDTO request);

    /**
     * drop package
     *
     * @param request request
     */
    void dropPackage(PackageQuery request);
}

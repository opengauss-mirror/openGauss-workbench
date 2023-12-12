/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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

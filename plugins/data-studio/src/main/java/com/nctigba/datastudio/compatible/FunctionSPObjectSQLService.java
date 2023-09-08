/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.model.query.PackageRequest;
import com.nctigba.datastudio.util.DebugUtils;
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
    default String dropPackage(PackageRequest request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }
}

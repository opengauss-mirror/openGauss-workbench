/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.util.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;

/**
 * ViewObjectSQLService
 *
 * @since 2023-6-26
 */
public interface ViewObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * splicing view ddl
     *
     * @param request request
     * @return String
     */
    default String splicingViewDDL(DatabaseCreateViewDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * return database view ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    default String returnDatabaseViewDDL(DatabaseViewDdlDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * return drop view sql
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    default String returnDropViewSQL(DatabaseViewDdlDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * return select view sql
     *
     * @param request request
     * @return String
     */
    default String returnSelectViewSQL(DatabaseSelectViewDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }
}

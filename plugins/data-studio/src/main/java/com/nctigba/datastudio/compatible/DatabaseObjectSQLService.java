/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

/**
 * DatabaseObjectSQLService
 *
 * @since 2023-6-26
 */
public interface DatabaseObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * create database
     *
     * @param database database
     * @return String
     */
    default String createDatabase(CreateDatabaseDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * connection database test
     *
     * @return String
     */
    default String connectionDatabaseTest() {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * delete database SQL
     *
     * @param database database
     * @return String
     */
    default String deleteDatabaseSQL(DatabaseNameDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * rename database SQL
     *
     * @param database database
     * @return String
     */
    default String renameDatabaseSQL(RenameDatabaseDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * con restrictions SQL
     *
     * @param database database
     * @return String
     */
    default String conRestrictionsSQL(RenameDatabaseDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * database attribute SQL
     *
     * @param database database
     * @return String
     */
    default String databaseAttributeSQL(DatabaseNameDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * database attribute update SQL
     *
     * @param database database
     * @return String
     */
    default String databaseAttributeUpdateSQL(DatabaseNameDTO database) {
        throw new CustomException(DebugUtils.getMessage());
    }
}

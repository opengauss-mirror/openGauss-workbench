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
 *  DatabaseObjectSQLService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/DatabaseObjectSQLService.java
 *
 *  -------------------------------------------------------------------------
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

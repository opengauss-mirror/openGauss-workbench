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
 *  ForeignTableSqlService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/ForeignTableSqlService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.ForeignTableQuery;
import com.nctigba.datastudio.model.query.TableDataEditQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * ForeignTableSQLService
 *
 * @since 2023-10-16
 */
public interface ForeignTableSqlService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * foreign table ddl
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    default List<Map<String, String>> queryServer(ForeignTableQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create foreign table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void create(ForeignTableQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }


    /**
     * delete foreign table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void deleteForeignTable(ForeignTableQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }


    /**
     * delete foreign server
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void deleteForeignServer(ForeignTableQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * show foreign table ddl
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    default Map<String, String> ddl(ForeignTableQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * show foreign table attribute
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    default List<Map<String, String>> attribute(ForeignTableQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * test connection
     *
     * @param request request
     * @return String
     */
    default String test(ForeignTableQuery request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create foreign server
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void createServer(ForeignTableQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * edit foreign table
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void edit(TableDataEditQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }
}

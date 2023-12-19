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
 *  CronJobSqlService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/CronJobSqlService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.CronJobQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;
import java.util.Map;

/**
 * CronJobSqlService
 *
 * @since 2023-11-08
 */
public interface CronJobSqlService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * query cron job list
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    default Map<String, Object> queryList(CronJobQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * query cron job
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    default Map<String, Object> query(CronJobQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void create(CronJobQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * edit cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void edit(CronJobQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * delete cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void delete(CronJobQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * enable cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void enable(CronJobQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * disable cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    default void disable(CronJobQuery request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }
}

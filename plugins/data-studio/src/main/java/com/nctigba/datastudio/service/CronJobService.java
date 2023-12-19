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
 *  CronJobService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/CronJobService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.CronJobQuery;

import java.sql.SQLException;
import java.util.Map;

/**
 * CronJobService
 *
 * @since 2023-11-08
 */
public interface CronJobService {
    /**
     * query cron job list
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> queryList(CronJobQuery request) throws SQLException;

    /**
     * query cron job
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    Map<String, Object> query(CronJobQuery request) throws SQLException;

    /**
     * create cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void create(CronJobQuery request) throws SQLException;

    /**
     * edit cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void edit(CronJobQuery request) throws SQLException;

    /**
     * delete cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void delete(CronJobQuery request) throws SQLException;

    /**
     * enable cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void enable(CronJobQuery request) throws SQLException;

    /**
     * disable cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void disable(CronJobQuery request) throws SQLException;
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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

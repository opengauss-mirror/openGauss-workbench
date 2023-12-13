/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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

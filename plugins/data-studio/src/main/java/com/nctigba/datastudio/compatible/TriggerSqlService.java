/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.TriggerQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * TriggerSqlService
 *
 * @since 2023-10-19
 */
public interface TriggerSqlService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * get trigger function list
     *
     * @param query query
     * @return List
     * @throws SQLException SQLException
     */
    default List<String> queryFunction(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create trigger function
     *
     * @param query query
     * @throws SQLException SQLException
     */
    default void createFunction(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }


    /**
     * create trigger
     *
     * @param query query
     * @return String
     */
    default String ddlPreview(TriggerQuery query) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    default void create(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * query trigger
     *
     * @param query query
     * @return Map
     * @throws SQLException SQLException
     */
    default Map<String, Map<String, Object>> query(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * rename trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    default void rename(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * edit trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    default void edit(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * delete trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    default void delete(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * enable trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    default void enable(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * disable trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    default void disable(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * show trigger ddl
     *
     * @param query query
     * @return Map
     * @throws SQLException SQLException
     */
    default Map<String, String> showDdl(TriggerQuery query) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }
}

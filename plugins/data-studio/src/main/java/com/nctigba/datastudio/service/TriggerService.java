/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.TriggerQuery;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * TriggerService
 *
 * @since 2023-10-19
 */
public interface TriggerService {
    /**
     * get trigger function list
     *
     * @param query query
     * @return List
     * @throws SQLException SQLException
     */
    List<String> queryFunction(TriggerQuery query) throws SQLException;

    /**
     * create trigger function
     *
     * @param query query
     * @throws SQLException SQLException
     */
    void createFunction(TriggerQuery query) throws SQLException;

    /**
     * create trigger
     *
     * @param query query
     * @return String
     */
    String ddlPreview(TriggerQuery query);

    /**
     * create trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    void create(TriggerQuery query) throws SQLException;

    /**
     * query trigger
     *
     * @param query query
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Map<String, Object>> query(TriggerQuery query) throws SQLException;

    /**
     * rename trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    void rename(TriggerQuery query) throws SQLException;

    /**
     * edit trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    void edit(TriggerQuery query) throws SQLException;

    /**
     * delete trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    void delete(TriggerQuery query) throws SQLException;

    /**
     * enable trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    void enable(TriggerQuery query) throws SQLException;

    /**
     * disable trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    void disable(TriggerQuery query) throws SQLException;

    /**
     * show trigger ddl
     *
     * @param query query
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, String> showDdl(TriggerQuery query) throws SQLException;
}

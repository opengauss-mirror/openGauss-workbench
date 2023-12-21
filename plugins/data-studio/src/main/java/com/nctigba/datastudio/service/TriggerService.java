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
 *  TriggerService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/TriggerService.java
 *
 *  -------------------------------------------------------------------------
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

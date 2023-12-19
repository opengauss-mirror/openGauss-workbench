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
 *  DatabaseViewService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/DatabaseViewService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.model.dto.ViewDataDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * DatabaseViewService
 *
 * @since 2023-6-26
 */
public interface DatabaseViewService {
    /**
     * create view dll
     *
     * @param request request
     * @return String
     */
    String createViewDDL(DatabaseCreateViewDTO request);

    /**
     * return view dll
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    String returnViewDDL(DatabaseViewDdlDTO request) throws SQLException;

    /**
     * create view
     *
     * @param request request
     */
    void createView(DatabaseCreateViewDTO request);

    /**
     * drop view
     *
     * @param request request
     */
    void dropView(DatabaseViewDdlDTO request);

    /**
     * select view
     *
     * @param request request
     * @return Map
     */
    ViewDataDTO selectView(DatabaseSelectViewDTO request);

    /**
     * edit view
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void editView(DatabaseViewDTO request) throws SQLException;

    /**
     * show view attribute
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    List<Map<String, String>> viewAttribute(DatabaseViewDdlDTO request) throws SQLException;

    /**
     * show view column
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> viewColumn(DatabaseViewDTO request) throws SQLException;

    /**
     * query view
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> queryView(DatabaseViewDdlDTO request) throws SQLException;
}

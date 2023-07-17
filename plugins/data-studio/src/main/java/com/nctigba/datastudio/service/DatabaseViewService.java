/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;

import java.sql.SQLException;
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
    Map<String, Object> selectView(DatabaseSelectViewDTO request);

}

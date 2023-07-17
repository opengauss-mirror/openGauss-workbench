/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.entity.CoverageRateDO;
import com.nctigba.datastudio.model.query.CoverageRateRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * CoverageRateService
 *
 * @since 2023-6-26
 */
public interface CoverageRateService {
    /**
     * query coverage rate
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    List<CoverageRateDO> queryCoverageRate(CoverageRateRequest request) throws SQLException;

    /**
     * delete coverage rate
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void delete(CoverageRateRequest request) throws SQLException;

    /**
     * export coverage rate
     *
     * @param request request
     * @param response response
     * @throws SQLException SQLException
     * @throws IOException IOException
     * @throws NoSuchFieldException NoSuchFieldException
     * @throws IllegalAccessException IllegalAccessException
     */
    void export(CoverageRateRequest request, HttpServletResponse response)
            throws SQLException, IOException, NoSuchFieldException, IllegalAccessException;
}

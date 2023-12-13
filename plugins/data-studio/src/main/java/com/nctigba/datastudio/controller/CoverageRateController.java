/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.entity.CoverageRateDO;
import com.nctigba.datastudio.model.query.CoverageRateQuery;
import com.nctigba.datastudio.service.CoverageRateService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * CoverageRateController
 *
 * @since 2023-06-25
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class CoverageRateController {
    @Resource
    private CoverageRateService coverageRateService;

    /**
     * query coverage rate
     *
     * @param request request
     * @return List
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/coverageRate/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CoverageRateDO> queryByOid(@RequestBody CoverageRateQuery request) throws SQLException {
        return coverageRateService.queryCoverageRate(request);
    }

    /**
     * delete coverage rate
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/coverageRate/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody CoverageRateQuery request) throws SQLException {
        coverageRateService.delete(request);
    }

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
    @PostMapping(value = "/coverageRate/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public void export(@RequestBody CoverageRateQuery request, HttpServletResponse response)
            throws SQLException, IOException, NoSuchFieldException, IllegalAccessException {
        coverageRateService.export(request, response);
    }
}

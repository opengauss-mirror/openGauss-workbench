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
 *  CoverageRateController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/CoverageRateController.java
 *
 *  -------------------------------------------------------------------------
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

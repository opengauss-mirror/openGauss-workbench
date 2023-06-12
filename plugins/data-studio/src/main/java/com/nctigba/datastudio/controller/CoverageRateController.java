/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.entity.CoverageRateDO;
import com.nctigba.datastudio.model.query.CoverageRateRequest;
import com.nctigba.datastudio.service.CoverageRateService;
import io.swagger.annotations.Api;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = {"Schema manager interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class CoverageRateController {
    @Resource
    private CoverageRateService coverageRateService;

    @PostMapping(value = "/coverageRate/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CoverageRateDO> queryByOid(@RequestBody CoverageRateRequest request) throws Exception {
        return coverageRateService.queryCoverageRate(request);
    }

    @PostMapping(value = "/coverageRate/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody CoverageRateRequest request) throws Exception {
        coverageRateService.delete(request);
    }

    @PostMapping(value = "/coverageRate/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public void export(@RequestBody CoverageRateRequest request, HttpServletResponse response) throws Exception {
        coverageRateService.export(request, response);
    }
}

/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.entity.CoverageRateDO;
import com.nctigba.datastudio.model.query.CoverageRateRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface CoverageRateService {
    List<CoverageRateDO> queryCoverageRate(CoverageRateRequest request) throws Exception;

    void delete(CoverageRateRequest request) throws Exception;

    void export(CoverageRateRequest request, HttpServletResponse response) throws Exception;
}

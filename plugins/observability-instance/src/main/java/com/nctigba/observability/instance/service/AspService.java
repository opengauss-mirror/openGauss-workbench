/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service;

import com.nctigba.observability.instance.dto.asp.AnalysisDto;
import com.nctigba.observability.instance.dto.asp.AspCountReq;

import java.util.List;
import java.util.Map;

/**
 * AspService
 *
 * @author liupengfei
 * @since 2023/8/11
 */
public interface AspService {
    /**
     * count
     *
     * @param req AspCountReq
     * @return Map<String, List<Object>>
     */
    Map<String, List<Object>> count(AspCountReq req);

    /**
     * analysis
     *
     * @param req AspCountReq
     * @return List<AnalysisDto>
     */
    List<AnalysisDto> analysis(AspCountReq req);
}

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
 *  AspService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/AspService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import com.nctigba.observability.instance.model.dto.asp.AnalysisDTO;
import com.nctigba.observability.instance.model.query.AspCountQuery;

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
    Map<String, List<Object>> count(AspCountQuery req);

    /**
     * analysis
     *
     * @param req AspCountReq
     * @return List<AnalysisDto>
     */
    List<AnalysisDTO> analysis(AspCountQuery req);
}

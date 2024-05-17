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
 *  CollectService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/CollectService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service;

import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;

import java.io.IOException;
import java.util.Optional;

/**
 * Service to collect metric data
 *
 * @since 2023/12/1
 */
public interface CollectService {

    /**
     * Call prometheus java client to get metric data
     *
     * @param url  Real prometheus java client url
     * @param name Metric names to get
     * @return Prometheus client http server response body
     * @throws IOException Read og_exporter.yml error
     * @since 2023/12/1
     */
    String getMetricsData(String url, Optional<String[]> name, String nodeId) throws IOException;
}

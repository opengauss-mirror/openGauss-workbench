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
 *  MetricsController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/controller/MetricsController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.controller;

import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.service.CollectService;
import com.nctigba.observability.instance.agent.service.TargetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * API for scraping metrics values
 *
 * @since 2023/12/1
 */
@Controller
@Slf4j
public class MetricsController {
    @Autowired
    TargetService targetService;
    @Autowired
    CollectService collectService;

    /**
     * API for scraping metrics values
     *
     * @param name   Array, which metrics to collect
     * @param nodeId Which database to collect metrics values
     * @return Prometheus exporter text
     * @throws IOException Yml read error
     * @since 2023/12/1
     */
    @RequestMapping(value = "/metrics", produces = "text/plain")
    @ResponseBody
    public String scrapeMetrics(
            @RequestParam(name = "name[]", required = false) Optional<String[]> name,
            @RequestParam(required = true) String nodeId) throws IOException {
        log.info("Scrape metrics for node {}:{}", nodeId, name.orElse(new String[0]));
        long startTime = System.currentTimeMillis();

        // get target
        Optional<TargetConfig> targetConfig =
                targetService.getTargetConfigs().stream().filter(z -> z.getNodeId().equals(nodeId)).findFirst();
        if (!targetConfig.isPresent()) {
            String errorMsg = String.format("No match node id targets!!!!!Node id = %s", nodeId);
            log.error(errorMsg);
            return errorMsg;
        }

        // build collect params
        CollectTargetDTO target = new CollectTargetDTO();
        target.setTargetConfig(targetConfig.get());
        CollectParamDTO param = new CollectParamDTO();
        if (name.isPresent()) {
            log.debug("name[]:{}", new ArrayList<>(Arrays.asList(name.get())));
            param.setGroupNames(Arrays.asList(name.get()));
        }

        // collect metrics data
        collectService.collectMetricsData(target, param);

        // get metrics data
        String url = "http://localhost:" + targetConfig.get().getExporterPort() + "/metrics";
        String responseBody = collectService.getMetricsData(url, name);

        // print run time
        long endTime = System.currentTimeMillis();
        log.info("Scrape metrics for node {}:takes {} ms", nodeId, endTime - startTime);

        return responseBody;
    }
}

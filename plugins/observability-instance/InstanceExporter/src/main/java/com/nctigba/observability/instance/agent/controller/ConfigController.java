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
 *  ConfigController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/controller/ConfigController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.controller;

import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.model.dto.TargetConfigDTO;
import com.nctigba.observability.instance.agent.service.ClientService;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import com.nctigba.observability.instance.agent.service.PrometheusRegistryManagerService;
import com.nctigba.observability.instance.agent.service.TargetService;
import com.nctigba.observability.instance.agent.util.CmdUtils;
import com.nctigba.observability.instance.agent.util.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * Controller to set and get agent config
 *
 * @since 2023/12/1
 */
@RestController
@RequestMapping("/config")
@Slf4j
public class ConfigController {
    @Autowired
    TargetService targetService;
    @Autowired
    ClientService clientService;
    @Autowired
    CmdUtils cmdUtil;
    @Autowired
    DbUtils dbUtil;
    @Autowired
    MetricCollectManagerService metricCollectManagerService;
    @Autowired
    PrometheusRegistryManagerService prometheusRegistryManagerService;

    /**
     * To add, modify targets which this agent manager
     *
     * @param newConfigDTOs New Config DTO
     * @throws IOException Yml file error
     * @since 2023/12/1
     */
    @PostMapping("/set")
    public void set(@RequestBody List<TargetConfigDTO> newConfigDTOs) throws IOException {
        // clear http server
        clientService.clear();

        // clear all cache
        prometheusRegistryManagerService.clear();
        metricCollectManagerService.clear();

        // clear util
        cmdUtil.clear();
        dbUtil.clear();

        // clear all config file
        targetService.clearTargets();

        for (int i = 0; i < newConfigDTOs.size(); i++) {
            TargetConfigDTO newConfigDTO = newConfigDTOs.get(i);
            TargetConfig newConfig = newConfigDTO.toTargetConfig();
            newConfig.setExporterPort(clientService.getTargetStartPort(newConfig));
            // should update before init, because init client will use this data
            clientService.updateYmlTargetConfig(newConfig);
            // init client
            clientService.initClient(newConfig);
        }
    }

    /**
     * Get all targets which this agent managed
     *
     * @return All managed targets
     * @since 2023/12/1
     */
    @GetMapping("/list")
    public List<TargetConfig> list() {
        return targetService.getTargetConfigs();
    }
}
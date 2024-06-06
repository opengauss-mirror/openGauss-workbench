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
 *  ClientService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/ClientService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service;

import com.nctigba.observability.instance.agent.config.model.TargetConfig;

import java.io.IOException;
import java.util.List;

/**
 * Service to manage client
 *
 * @since 2023/12/1
 */
public interface ClientService {
    /**
     * Start a prometheus client by TargetConfig
     *
     * @return Result
     * @since 2023/12/1
     */
    void initClient();

    /**
     * Update Yml file whit target new config
     *
     * @param newConfig New target config
     * @return TargetConfig, exporter port will be update
     * @throws IOException Read yml file error
     * @since 2023/12/1
     */
    TargetConfig updateYmlTargetConfig(TargetConfig newConfig) throws IOException;

    /**
     * Update Yml file whit target new config list
     *
     * @param newConfigs New target config list
     * @return TargetConfigList, exporter port will be update
     * @throws IOException Read yml file error
     */
    List<TargetConfig> updateYmlTargetConfigs(List<TargetConfig> newConfigs) throws IOException;

    /**
     * close
     */
    void close();
}

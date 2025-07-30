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
 *  ClientInitRunner.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/config/runner/ClientInitRunner.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.config.runner;

import com.nctigba.observability.instance.agent.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Init Prometheus java client with yml file when startup
 *
 * @since 2023/12/1
 */
@Component
public class ClientInitRunner implements ApplicationRunner {
    @Autowired
    ClientService clientService;

    /**
     * @inheritDoc
     */
    @Override
    public void run(ApplicationArguments args) {
        clientService.initClient();
    }
}
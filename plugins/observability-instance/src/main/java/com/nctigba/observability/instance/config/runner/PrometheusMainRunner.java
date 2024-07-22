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
 *  PrometheusMainRunner.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/config/runner/PrometheusMainRunner.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.config.runner;

import com.nctigba.observability.instance.service.PrometheusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Check if the main Prometheus is installed. if not, install
 *
 * @since 2024/2/2 17:21
 */
@Component
@Slf4j
@Profile("!dev")
public class PrometheusMainRunner implements ApplicationRunner {
    @Autowired
    private PrometheusService prometheusService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            prometheusService.installMainProm();
        } catch (Exception e) {
            log.error("The main install or update fail:{}, exception is {}", e.getMessage(), e);
        }
    }
}

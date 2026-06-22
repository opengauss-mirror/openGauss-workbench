/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.nctigba.observability.instance.config.runner;

import com.nctigba.observability.instance.service.ExporterInstallService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Refresh instance-exporter third party tool install info runner
 *
 * @since 2026/6/17
 */
@Component
@Order(3)
@Slf4j
@Profile("!dev")
public class ExporterThirdPartyToolInstallInfoRefreshRunner implements ApplicationRunner {
    @Autowired
    private ExporterInstallService exporterInstallService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            exporterInstallService.refreshExporterInstallInfo();
        } catch (Exception e) {
            log.error("The third party tool install info of instance-exporter refresh fail", e);
        }
    }
}

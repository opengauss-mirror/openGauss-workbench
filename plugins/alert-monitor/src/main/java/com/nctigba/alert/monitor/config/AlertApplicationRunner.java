/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config;

import lombok.extern.slf4j.Slf4j;
import com.nctigba.alert.monitor.service.PrometheusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author wuyuebin
 * @date 2023/4/27 16:17
 * @description
 */
@Component
@Slf4j
public class AlertApplicationRunner implements ApplicationRunner {
    @Autowired
    private PrometheusService prometheusService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        prometheusService.initPrometheusConfig();
    }
}

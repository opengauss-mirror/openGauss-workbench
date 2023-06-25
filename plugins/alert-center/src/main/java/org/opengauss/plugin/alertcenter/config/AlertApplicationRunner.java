/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.config;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.plugin.alertcenter.service.PrometheusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author wuyuebin
 * @date 2023/4/27 16:17
 * @description 初始化操作
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

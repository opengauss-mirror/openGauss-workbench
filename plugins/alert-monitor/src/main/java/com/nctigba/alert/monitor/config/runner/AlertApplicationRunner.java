/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.alert.monitor.config.runner;

import lombok.extern.slf4j.Slf4j;
import com.nctigba.alert.monitor.service.impl.PrometheusServiceImpl;
import org.opengauss.admin.common.exception.ServiceException;
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
    private PrometheusServiceImpl prometheusService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            prometheusService.initPrometheusConfig();
        } catch (ServiceException | IllegalStateException e) {
            log.warn("init prometheus configuration fail: ", e.getMessage());
        }
    }
}

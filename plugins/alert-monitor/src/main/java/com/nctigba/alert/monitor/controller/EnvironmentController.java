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
 *  EnvironmentController.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/controller/EnvironmentController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import com.nctigba.alert.monitor.service.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/29 17:36
 * @description
 */
@RestController
@RequestMapping("/api/v1/environment")
public class EnvironmentController {
    @Autowired
    private EnvironmentService environmentService;

    @GetMapping(value = "/cluster")
    public AjaxResult cluster() {
        List cluster = environmentService.cluster();
        return AjaxResult.success(cluster);
    }

    @GetMapping("/checkPrometheus")
    public AjaxResult checkPrometheus() {
        environmentService.checkPrometheus();
        return AjaxResult.success();
    }
}

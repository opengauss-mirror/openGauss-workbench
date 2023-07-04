/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import com.nctigba.alert.monitor.service.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    /**
     * get the constant parameters
     *
     * @param type: alert and notify,alert: the constant parameters for querying alert content,  notify: the constant
     *            parameters for notify content
     * @return return the constant parameters
     */
    @GetMapping("/alertContentParam")
    public AjaxResult getAlertContentParam(@RequestParam String type) {
        Map<String, Map<String, String>> map = environmentService.getAlertContentParam(type);
        return AjaxResult.success(map);
    }
}

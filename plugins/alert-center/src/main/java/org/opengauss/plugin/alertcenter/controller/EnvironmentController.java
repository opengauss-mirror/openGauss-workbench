/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.plugin.alertcenter.service.EnvironmentService;
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
@RequestMapping("/alertCenter/api/v1/environment")
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
     * @param type 分 alert和 notify,alert表示查询告警内容常驻参数，notify表示通知内容的常驻参数
     * @return 返回常驻参数
     */
    @GetMapping("/alertContentParam")
    public AjaxResult getAlertContentParam(@RequestParam String type) {
        Map<String, Map<String, String>> map = environmentService.getAlertContentParam(type);
        return AjaxResult.success(map);
    }
}

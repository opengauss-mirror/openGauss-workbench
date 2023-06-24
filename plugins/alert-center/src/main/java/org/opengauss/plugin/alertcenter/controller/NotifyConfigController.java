/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.plugin.alertcenter.entity.NotifyConfig;
import org.opengauss.plugin.alertcenter.model.NotifyConfigReq;
import org.opengauss.plugin.alertcenter.service.NotifyConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/6/1 01:48
 * @description
 */
@RestController
@RequestMapping("/alertCenter/api/v1/notifyConfig")
public class NotifyConfigController {
    @Autowired
    private NotifyConfigService notifyConfigService;

    @GetMapping("/list")
    public AjaxResult getAllList() {
        List<NotifyConfig> list = notifyConfigService.getAllList();
        return AjaxResult.success(list);
    }

    @PostMapping("/list")
    public AjaxResult saveList(@RequestBody List<NotifyConfig> list) {
        notifyConfigService.saveList(list);
        return AjaxResult.success();
    }

    @PostMapping("/testConfig")
    public AjaxResult testConfig(@RequestBody NotifyConfigReq notifyConfigReq) {
        notifyConfigService.testConfig(notifyConfigReq);
        return AjaxResult.success();
    }
}

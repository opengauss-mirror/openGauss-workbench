/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.controller;

import cn.hutool.core.collection.CollectionUtil;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.plugin.alertcenter.entity.AlertConfig;
import org.opengauss.plugin.alertcenter.service.AlertConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/6/9 10:47
 * @description
 */
@RestController
@RequestMapping("/alertCenter/api/v1/alertConf")
public class AlertConfigController {
    @Autowired
    private AlertConfigService alertConfigService;

    @GetMapping
    public AjaxResult getAlertConf() {
        List<AlertConfig> list = alertConfigService.list();
        if (CollectionUtil.isEmpty(list)) {
            return AjaxResult.success(new AlertConfig());
        }
        return AjaxResult.success(list.get(0));
    }

    @PostMapping
    public AjaxResult saveAlertConf(@Valid @RequestBody AlertConfig alertConfig) {
        alertConfigService.saveAlertConf(alertConfig);
        return AjaxResult.success();
    }
}

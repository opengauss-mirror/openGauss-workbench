/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.plugin.alertcenter.dto.AlertClusterNodeConfDto;
import org.opengauss.plugin.alertcenter.entity.AlertClusterNodeConf;
import org.opengauss.plugin.alertcenter.model.AlertClusterNodeAndTemplateReq;
import org.opengauss.plugin.alertcenter.model.AlertClusterNodeConfReq;
import org.opengauss.plugin.alertcenter.service.AlertClusterNodeConfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/26 15:57
 * @description
 */
@RestController
@RequestMapping("/alertCenter/api/v1/alertClusterNodeConf")
public class AlertClusterNodeConfController extends BaseController {
    @Autowired
    private AlertClusterNodeConfService alertClusterNodeConfService;

    @GetMapping
    public AjaxResult getList() {
        List<AlertClusterNodeConfDto> list = alertClusterNodeConfService.getList();
        return AjaxResult.success(list);
    }

    @GetMapping("/clusterNode/{clusterNodeId}")
    public AjaxResult getByClusterNodeId(@PathVariable String clusterNodeId) {
        AlertClusterNodeConf alertClusterNodeConf = alertClusterNodeConfService.getByClusterNodeId(clusterNodeId);
        return AjaxResult.success(alertClusterNodeConf);
    }

    @PostMapping("")
    public AjaxResult saveClusterNodeConf(@Valid @RequestBody AlertClusterNodeConfReq alertClusterNodeConfReq) {
        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfReq);
        return AjaxResult.success();
    }

    @PostMapping("/alertTemplate")
    public AjaxResult saveAlertTemplateAndConfig(
            @Valid @RequestBody AlertClusterNodeAndTemplateReq clusterNodeAndTemplateReq) {
        alertClusterNodeConfService.saveAlertTemplateAndConfig(clusterNodeAndTemplateReq);
        return AjaxResult.success();
    }
}

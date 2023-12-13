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
 *  AlertClusterNodeConfController.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/controller/AlertClusterNodeConfController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import com.nctigba.alert.monitor.model.dto.AlertClusterNodeConfDTO;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeAndTemplateQuery;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeConfQuery;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/26 15:57
 * @description
 */
@RestController
@RequestMapping("/api/v1/alertClusterNodeConf")
public class AlertClusterNodeConfController extends BaseController {
    @Autowired
    private AlertClusterNodeConfService alertClusterNodeConfService;

    /**
     * getList
     *
     * @return AjaxResult
     */
    @GetMapping
    public AjaxResult getList() {
        List<AlertClusterNodeConfDTO> list = alertClusterNodeConfService.getList();
        return AjaxResult.success(list);
    }

    /**
     * getByClusterNodeId
     *
     * @param clusterNodeId clusterNodeId
     * @return AjaxResult
     */
    @GetMapping("/clusterNode/{clusterNodeId}")
    public AjaxResult getByClusterNodeId(@PathVariable String clusterNodeId) {
        AlertClusterNodeConfDO alertClusterNodeConfDO = alertClusterNodeConfService.getByClusterNodeId(clusterNodeId);
        return AjaxResult.success(alertClusterNodeConfDO);
    }

    /**
     * save or update
     *
     * @param alertClusterNodeConfQuery params
     * @return AjaxResult
     */
    @PostMapping("")
    public AjaxResult saveClusterNodeConf(@Valid @RequestBody AlertClusterNodeConfQuery alertClusterNodeConfQuery) {
        alertClusterNodeConfService.saveClusterNodeConf(alertClusterNodeConfQuery);
        return AjaxResult.success();
    }

    /**
     * save or update
     *
     * @param clusterNodeAndTemplateReq params
     * @return AjaxResult
     */
    @PostMapping("/alertTemplate")
    public AjaxResult saveAlertTemplateAndConfig(
        @Valid @RequestBody AlertClusterNodeAndTemplateQuery clusterNodeAndTemplateReq) {
        alertClusterNodeConfService.saveAlertTemplateAndConfig(clusterNodeAndTemplateReq);
        return AjaxResult.success();
    }

    /**
     * unbind the clusterNodes and rule template
     *
     * @param clusterNodeIds clusterNodeIds
     * @return AjaxResult
     */
    @DeleteMapping("/unbind")
    public AjaxResult unbind(@RequestParam String clusterNodeIds) {
        alertClusterNodeConfService.unbindByIds(clusterNodeIds);
        return AjaxResult.success();
    }
}
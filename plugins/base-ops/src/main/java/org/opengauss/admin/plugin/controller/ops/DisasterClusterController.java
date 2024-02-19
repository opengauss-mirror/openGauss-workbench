/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.controller.ops;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.model.ops.DisasterBody;
import org.opengauss.admin.plugin.service.ops.IOpsDisasterClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Operation and maintenance disaster cluster operations
 *
 * @author wbd
 * @since 2024/1/27 17:38
 **/
@RestController
@RequestMapping("/disasterCluster")
public class DisasterClusterController extends BaseController {
    @Autowired
    private IOpsDisasterClusterService opsDisasterClusterService;

    /**
     * list all disaster cluster
     *
     * @return AjaxResult
     */
    @GetMapping("/listCluster")
    public AjaxResult listCluster() {
        return AjaxResult.success(opsDisasterClusterService.listCluster());
    }

    /**
     * monitor disaster cluster and sub cluster status
     *
     * @param clusterId clusterId
     * @param businessId businessId
     * @return AjaxResult
     */
    @GetMapping("/monitor")
    public AjaxResult monitor(@RequestParam String clusterId, @RequestParam String businessId) {
        opsDisasterClusterService.monitor(clusterId, businessId);
        return AjaxResult.success();
    }

    /**
     * list all cluster and device manager
     *
     * @return AjaxResult
     */
    @GetMapping("/listClusterAndDeviceManager")
    public AjaxResult listClusterAndDeviceManager() {
        return AjaxResult.success(opsDisasterClusterService.listClusterAndDeviceManager());
    }

    /**
     * install disaster cluster
     *
     * @param disasterBody disasterBody
     * @return AjaxResult
     */
    @PostMapping("/install")
    public AjaxResult install(@RequestBody DisasterBody disasterBody) {
        return opsDisasterClusterService.install(disasterBody);
    }

    /**
     * switchover disaster cluster
     *
     * @param disasterBody disasterBody
     * @return AjaxResult
     */
    @PostMapping("/switchover")
    public AjaxResult switchover(@RequestBody DisasterBody disasterBody) {
        return opsDisasterClusterService.switchover(disasterBody);
    }

    /**
     * delete disaster cluster
     *
     * @param disasterBody disasterBody
     * @return AjaxResult
     */
    @PostMapping("/delete")
    public AjaxResult delete(@RequestBody DisasterBody disasterBody) {
        opsDisasterClusterService.removeDisasterCluster(disasterBody);
        return AjaxResult.success();
    }

    /**
     * when install failed,get all host nodes of disaster cluster
     *
     * @param primaryClusterName primaryClusterName
     * @param standbyClusterName standbyClusterName
     * @return AjaxResult
     */
    @GetMapping("/getHosts")
    public AjaxResult getHosts(@RequestParam String primaryClusterName, @RequestParam String standbyClusterName) {
        return AjaxResult.success(opsDisasterClusterService.getHosts(primaryClusterName, standbyClusterName));
    }
}

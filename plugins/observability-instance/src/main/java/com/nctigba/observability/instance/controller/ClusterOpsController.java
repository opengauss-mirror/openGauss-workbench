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
 *  ClusterOpsController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/controller/ClusterOpsController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.instance.service.ClusterOpsService;

import lombok.RequiredArgsConstructor;

/**
 * ClusterOpsController.java
 *
 * @since 2023-08-25
 */
@RestController
@RequestMapping("/instanceMonitoring/api/v1/clusters")
@RequiredArgsConstructor
public class ClusterOpsController {
    private final ClusterOpsService clusterOpsService;

    /**
     * list
     *
     * @return AjaxResult
     */
    @GetMapping(value = "/list")
    public AjaxResult list() {
        return AjaxResult.success(clusterOpsService.listClusters());
    }

    /**
     * nodes
     *
     * @param clusterId clusterId
     * @return AjaxResult
     */
    @GetMapping(value = "/{clusterId}/nodes")
    public AjaxResult nodes(@PathVariable("clusterId") String clusterId) {
        return AjaxResult.success(clusterOpsService.nodes(clusterId));
    }

    /**
     * allClusterState
     *
     * @return AjaxResult
     */
    @GetMapping(value = "/allClusterState")
    public AjaxResult allClusterState() {
        return AjaxResult.success(clusterOpsService.allClusterState());
    }

    /**
     * allStandbyNodes
     *
     * @return AjaxResult
     */
    @GetMapping(value = "/nodes")
    public AjaxResult allStandbyNodes() {
        return AjaxResult.success(clusterOpsService.allStandbyNodes());
    }

    /**
     * clusterMetrics
     *
     * @param clusterId clusterId
     * @param start     start
     * @param end       end
     * @param step      step
     * @return AjaxResult
     */
    @GetMapping(value = "/{clusterId}/metrics")
    public AjaxResult clusterMetrics(@PathVariable("clusterId") String clusterId, Long start, Long end, Integer step) {
        return AjaxResult.success(clusterOpsService.clusterMetrics(clusterId, start, end, step));
    }

    /**
     * statistics
     *
     * @return AjaxResult
     */
    @GetMapping(value = "/statistics")
    public AjaxResult statistics() {
        return AjaxResult.success(clusterOpsService.statistics());
    }

    /**
     * switchRecord
     *
     * @param clusterId clusterId
     * @param start start
     * @param end end
     * @return AjaxResult
     */
    @GetMapping(value = "/{clusterId}/switchRecord")
    public AjaxResult switchRecord(@PathVariable("clusterId") String clusterId, Long start, Long end,
            Integer pageSize, Integer pageNum) {
        return AjaxResult.success(clusterOpsService.switchRecord(clusterId, start, end, pageSize, pageNum));
    }
}

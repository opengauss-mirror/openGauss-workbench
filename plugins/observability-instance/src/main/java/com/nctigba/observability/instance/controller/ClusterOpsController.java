/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
     * relation
     *
     * @param clusterId clusterId
     * @return AjaxResult
     */
    @GetMapping(value = "/{clusterId}/relation")
    public AjaxResult relation(@PathVariable("clusterId") String clusterId) {
        return AjaxResult.success(clusterOpsService.relation(clusterId));
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
}

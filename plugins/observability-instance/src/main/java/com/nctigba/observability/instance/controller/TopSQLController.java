/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.service.TopSQLService;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * TopSQL Controller
 * </p>
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/15 17:05
 */
@RestController
@RequestMapping("/observability/v1/topsql")
@RequiredArgsConstructor
public class TopSQLController {
    private final TopSQLService topSQLService;
    private final ClusterManager clusterManager;
    private final MetricsService metricsService;

    @GetMapping(value = "/list")
    public AjaxResult top10(TopSQLListReq topSQLListReq) {
        var list = topSQLService.topSQLList(topSQLListReq);
        if (list == null) {
            return AjaxResult.error("602", "top sql pre check fail");
        }
        return AjaxResult.success(list);
    }

    @GetMapping(value = "/detail")
    public AjaxResult detail(String id, String sqlId) {
        return AjaxResult.success(topSQLService.detail(id, sqlId));
    }

    @GetMapping(value = "/plan")
    public AjaxResult plan(String id, String sqlId) {
        var plan = topSQLService.executionPlan(id, sqlId);
        Map<String, Object> parsedResult = new HashMap<>();
        parsedResult.put("data", List.of(plan));
        parsedResult.put("total",
                Map.of("totalPlanRows", plan.totalPlanRows(), "totalPlanWidth", plan.totalPlanWidth()));
        return AjaxResult.success(parsedResult);
    }

    @GetMapping("/sysResource")
    public AjaxResult sysResource(String id, Long start, Long end, Integer step) {
        MetricsLine[] metricKey = {
                MetricsLine.CPU_TOTAL,
                MetricsLine.CPU_USER,
                MetricsLine.CPU_SYSTEM,
                MetricsLine.CPU_IOWAIT,
                MetricsLine.CPU_IRQ,
                MetricsLine.CPU_SOFTIRQ,
                MetricsLine.CPU_NICE,
                MetricsLine.CPU_STEAL,
                MetricsLine.CPU_IDLE,
                MetricsLine.CPU_DB,
                MetricsLine.MEMORY_USED,
                MetricsLine.MEMORY_DB_USED,
                MetricsLine.IO_UTIL,
                MetricsLine.NETWORK_IN_TOTAL,
                MetricsLine.NETWORK_OUT_TOTAL
        };
        return AjaxResult.success(metricsService.listBatch(metricKey, id, start, end, step));
    }

    @GetMapping(value = "/index")
    public AjaxResult index(String id, String sqlId) {
        return AjaxResult.success(topSQLService.indexAdvice(id, sqlId));
    }

    @GetMapping(value = "/object")
    public AjaxResult object(String id, String sqlId) {
        return AjaxResult.success(topSQLService.objectInfo(id, sqlId));
    }

    @GetMapping(value = "/cluster")
    public AjaxResult cluster() {
        return AjaxResult.success(clusterManager.getAllOpsCluster());
    }

    @GetMapping(value = "/cluster/{id}")
    public AjaxResult clusterNode(@PathVariable("id") String id) {
        return AjaxResult.success(clusterManager.getOpsNodeById(id));
    }

    @GetMapping(value = "/waitevent")
    public AjaxResult waitevent(String id, String sqlId) {
        return AjaxResult.success(topSQLService.waitEvent(id, sqlId));
    }
}

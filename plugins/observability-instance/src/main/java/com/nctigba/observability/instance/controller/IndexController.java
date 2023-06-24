/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.controller;

import java.util.List;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.dto.topsql.TopSQLNowReq;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.service.TopSQLService;

@RestController
@RequestMapping("/instanceMonitoring/api/v1/")
public class IndexController extends ControllerConfig {
    @Autowired
    MetricsService metricsService;
    @Autowired
    TopSQLService topSQLService;

    private static final MetricsLine[] MAIN = {
            MetricsLine.CPU,
            MetricsLine.MEMORY,
            MetricsLine.IO,
            MetricsLine.NETWORK_IN_TOTAL,
            MetricsLine.NETWORK_OUT_TOTAL,
            MetricsLine.SWAP,
            MetricsLine.DB_THREAD_POOL,
            MetricsLine.DB_ACTIVE_SESSION };

    @GetMapping("mainMetrics")
    public AjaxResult mainMetrics(String id, Long start, Long end, Integer step) {
        return AjaxResult.success(metricsService.listBatch(MAIN, id, start, end, step));
    }

    @GetMapping(value = "/topSQLNow")
    public AjaxResult topSQLNow(TopSQLNowReq topSQLNowReq) {
        List<JSONObject> list = topSQLService.getTopSQLNow(topSQLNowReq);
        return AjaxResult.success(list);
    }
}
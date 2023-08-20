/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.dto.topsql.TopSQLNowReq;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.service.SessionService;
import com.nctigba.observability.instance.service.TopSQLService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/instanceMonitoring/api/v1/")
@RequiredArgsConstructor
public class IndexController extends ControllerConfig {
    private final MetricsService metricsService;
    private final TopSQLService topSQLService;
    private final SessionService sessionService;

    private static final MetricsLine[] MAIN = {
            MetricsLine.CPU,
            MetricsLine.MEMORY,
            MetricsLine.IO,
            MetricsLine.NETWORK_IN_TOTAL,
            MetricsLine.NETWORK_OUT_TOTAL,
            MetricsLine.SWAP,
            MetricsLine.DB_THREAD_POOL,
            MetricsLine.DB_ACTIVE_SESSION
    };

    @GetMapping("mainMetrics")
    public AjaxResult mainMetrics(String id, Long start, Long end, Integer step) {
        HashMap<String, Object> metrics = metricsService.listBatch(MAIN, id, start, end, step);
        JSONObject simpleStatistic = sessionService.simpleStatistic(id);
        metrics.putAll(simpleStatistic);
        return AjaxResult.success(metrics);
    }

    @GetMapping(value = "/topSQLNow")
    public AjaxResult topSQLNow(TopSQLNowReq topSQLNowReq) {
        Map<String, List<JSONObject>> blockAndLongTxc = sessionService.blockAndLongTxc(topSQLNowReq.getId());
        List<JSONObject> topSQLNow = topSQLService.getTopSQLNow(topSQLNowReq);
        blockAndLongTxc.put("topSQLNow", topSQLNow);
        return AjaxResult.success(blockAndLongTxc);
    }
}
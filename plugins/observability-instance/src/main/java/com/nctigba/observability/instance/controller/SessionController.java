/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.model.monitoring.MonitoringParam;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.service.MonitoringService;
import com.nctigba.observability.instance.service.SessionService;

import cn.hutool.core.thread.ThreadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/instanceMonitoring/api/v1/session/")
@RequiredArgsConstructor
@Slf4j
public class SessionController {
    private final SessionService sessionService;
    private final MetricsService metricsService;
    private final MonitoringService monitoringService;

    private static final MetricsLine[] SESSION_STATISTIC = {
            MetricsLine.SESSION_MAX_CONNECTION,
            MetricsLine.SESSION_IDLE_CONNECTION,
            MetricsLine.SESSION_ACTIVE_CONNECTION,
            MetricsLine.SESSION_WAITING_CONNECTION,
            MetricsLine.CPU_TIME,
            MetricsLine.NET_SEND_TIME,
            MetricsLine.DATA_IO_TIME
    };

    @GetMapping("sessionStatistic")
    public AjaxResult sessionStatistic(String id, Long start, Long end, Integer step) {
        MonitoringParam monitoringParam = new MonitoringParam();
        monitoringParam.setId(id);
        monitoringParam.setStart(String.valueOf(start));
        monitoringParam.setEnd(String.valueOf(end));
        monitoringParam.setStep(String.valueOf(step));
        monitoringParam.setQuery(MetricsLine.WAIT_EVENT_COUNT.promQl(null, id));
        monitoringParam.setType("LINE");
        monitoringParam.setLegendName("event");
        Future<Object> waitingEventFuture = ThreadUtil
                .execAsync(() -> monitoringService.getRangeMonitoringData(monitoringParam).get(0));
        Future<JSONObject> simpleFuture = ThreadUtil.execAsync(() -> sessionService.simpleStatistic(id));
        Future<HashMap<String, Object>> metricsFuture = ThreadUtil
                .execAsync(() -> metricsService.listBatch(SESSION_STATISTIC, id, start, end, step));
        JSONObject simple;
        Object waitEvent;
        Map<String, Object> metrics;
        try {
            waitEvent = waitingEventFuture.get();
            simple = simpleFuture.get();
            metrics = metricsFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("", e);
            throw new CustomException("", e);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("gauss_wait_events_value", waitEvent);
        List time = (List) metrics.get("time");
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            if (entry.getValue() == null) {
                entry.setValue(new int[time.size()]);
            }
        }
        map.putAll(metrics);
        map.putAll(simple);
        return AjaxResult.success(map);
    }

    @GetMapping(value = "blockAndLongTxc")
    public AjaxResult blockAndLongTxc(String id) {
        return AjaxResult.success(sessionService.blockAndLongTxc(id));
    }

    @GetMapping(value = "detail/general")
    public AjaxResult detailGeneral(String id, String sessionid) {
        return AjaxResult.success(sessionService.detailGeneral(id, sessionid));
    }

    @GetMapping(value = "detail/statistic")
    public AjaxResult detailStatistic(String id, String sessionid) {
        return AjaxResult.success(sessionService.detailStatistic(id, sessionid));
    }

    @GetMapping(value = "detail/blockTree")
    public AjaxResult detailBlockTree(String id, String sessionid) {
        return AjaxResult.success(sessionService.detailBlockTree(id, sessionid));
    }

    @GetMapping(value = "detail/waiting")
    public AjaxResult detailWaiting(String id, String sessionid) {
        return AjaxResult.success(sessionService.detailWaiting(id, sessionid));
    }

    @GetMapping(value = "detail")
    public AjaxResult detail(String id, String sessionid) {
        return AjaxResult.success(sessionService.detail(id, sessionid));
    }
}

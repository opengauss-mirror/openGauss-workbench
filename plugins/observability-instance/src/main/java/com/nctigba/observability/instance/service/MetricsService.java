/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.constants.MetricsValue;
import com.nctigba.observability.instance.constants.MonitoringConstants;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.MetricsService.PrometheusResult.PromData.MonitoringMetric;
import com.nctigba.observability.instance.util.ListUtil;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class MetricsService {
    private static final String TIME = "time";
    private static final Map<String, String> PROM = new HashMap<>();
    private static final String DEFAULT = "DEFAULT";
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    private ClusterManager clusterManager;

    private String getPrometheusUrl() {
        if (PROM.containsKey(DEFAULT)) {
            return PROM.get(DEFAULT);
        }
        var env = envMapper
                .selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, NctigbaEnv.envType.PROMETHEUS));
        if (env == null) {
            throw new CustomException("Prometheus not found");
        }
        var host = hostFacade.getById(env.getHostid());
        String url = "http://" + host.getPublicIp() + ":" + env.getPort();
        PROM.put(DEFAULT, url);
        return url;
    }

    private PrometheusResult query(String type, String query, Map<String, Object> param) {
        String baseUrl = getPrometheusUrl() + type;
        var result = HttpUtil.get(baseUrl, param);
        var prometheusResult = JSONUtil.toBean(result, PrometheusResult.class);
        prometheusResult.isSuccess();
        return prometheusResult;
    }

    private List<MonitoringMetric> value(String query, Long time) {
        if (StrUtil.isBlank(query)) {
            throw new NullPointerException("query null");
        }
        log.info("promQL:{}, time:{}", query, time);
        var prometheusResult = query(MonitoringConstants.PROMETHEUS_QUERY_POINT, query,
                Map.of("query", query, "time", time));
        return prometheusResult.getData().getResult();
    }

    private List<MonitoringMetric> list(String query, Number start, Number end, Integer step) {
        if (StrUtil.isBlank(query)) {
            return Collections.emptyList();
        }
        log.info("promQL:{}, start:{}, end:{}, step:{}", query, start, end, step);
        var prometheusResult = query(MonitoringConstants.PROMETHEUS_QUERY_RANGE, query,
                Map.of("query", query, "start", start, "end", end, "step", step));
        return prometheusResult.getData().getResult();
    }

    public HashMap<String, Object> listBatch(Enum<?>[] metricsArr, String nodeId, Long start, Long end, Integer step) {
        var result = new HashMap<String, Object>();
        var node = clusterManager.getOpsNodeById(nodeId);
        var hostId = node.getHostId();
        List<Long> timeline = new ArrayList<>();
        for (long l = start; l < end; l += step) {
            timeline.add(l);
        }
        result.put(TIME, timeline.stream().map(t -> new Date(t.longValue() * 1000)).collect(Collectors.toList()));
        var countDown = ThreadUtil.newCountDownLatch(metricsArr.length);
        for (Object metric : metricsArr) {
            ThreadUtil.execute(() -> {
                try {
                    if (metric instanceof MetricsLine) {
                        MetricsLine metricLine = (MetricsLine) metric;
                        String promQl = metricLine.promQl(hostId, nodeId);
                        var metrics = list(metricLine.promQl(hostId, nodeId), start, end, step);
                        result.put(metricLine.name(), parseLine(promQl, metrics, timeline, metricLine.getTemplate()));
                        return;
                    }
                    if (metric instanceof MetricsValue) {
                        MetricsValue m = (MetricsValue) metric;
                        String promQl = m.promQl(hostId, nodeId);
                        var metrics = value(m.promQl(hostId, nodeId), System.currentTimeMillis() / 1000);
                        result.put(m.name(), parseValue(promQl, metrics, m.getTemplate()));
                    }
                } finally {
                    countDown.countDown();
                }
            });
        }
        try {
            countDown.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new CustomException(e.getMessage());
        }
        return result;
    }

    private Object parseValue(String promQl, List<MonitoringMetric> metric, String template) {
        if (metric.size() == 0) {
            return null;
        } else if (metric.size() == 1) {
            return metric.get(0).getValue().get(1);
        } else {
            var map = new HashMap<String, Object>();
            for (var monitoringMetric : metric) {
                if (template == null)
                    throw new NullPointerException(promQl);
                String key = StrUtil.format(template, monitoringMetric.getMetric());
                map.put(key, monitoringMetric.getValue().get(1));
            }
            return map;
        }
    }

    private Object parseLine(String promQl, List<MonitoringMetric> metric, List<Long> timeline, String template) {
        if (metric.size() == 0) {
            return null;
        } else if (metric.size() == 1) {
            return ListUtil.collect(metric.get(0).getValues(), timeline);
        } else {
            var map = new HashMap<String, Object>();
            for (var monitoringMetric : metric) {
                if (template == null) {
                    throw new NullPointerException(promQl);
                }
                String key = StrUtil.format(template, monitoringMetric.getMetric());
                var lineNumber = ListUtil.collect(monitoringMetric.getValues(), timeline);
                map.put(key, lineNumber);
            }
            return map;
        }
    }

    @Data
    public static class PrometheusResult {
        Stat status;
        PromData data;

        public void isSuccess() {
            if (status == Stat.error) {
                throw new CustomException(error);
            }
        }

        @Data
        public static class PromData {
            ResultType resultType;
            List<MonitoringMetric> result;

            @Data
            public static class MonitoringMetric {
                Map<String, String> metric;
                List<Number> value;
                List<List<Number>> values;
            }

            public enum ResultType {
                matrix,
                vector,
                scalar,
                string;
            }
        }

        String errorType;
        String error;

        public enum Stat {
            success,
            error
        }
    }
}
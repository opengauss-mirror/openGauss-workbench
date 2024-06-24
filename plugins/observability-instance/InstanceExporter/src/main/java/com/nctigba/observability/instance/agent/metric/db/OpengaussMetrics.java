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
 *  OpengaussMetrics.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/db/OpengaussMetrics.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.db;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.nctigba.observability.instance.agent.constant.CollectConstants;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.metric.MetricTypeAndLabels;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import com.nctigba.observability.instance.agent.util.DbUtils;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * table pg_stat_activity
 *
 * @since 2023/12/1
 */
@Service
@Log4j2
public class OpengaussMetrics {
    // validity period of cached data
    static final long CACHE_TIME_OUT = 3000L;
    static final long MIN_COLLECT_INTERVAL = 5000L;
    static final String PRIMARY_ROLE = "primary";
    static final String SEPARATOR = CollectConstants.SEPARATOR;

    @Autowired
    MetricCollectManagerService metricCollectManager;
    @Autowired
    DbUtils dbUtil;

    private TimedCache<String, List<Map<String, Object>>> timedCache = CacheUtil.newTimedCache(CACHE_TIME_OUT);

    @PostConstruct
    private void init() throws IOException {
        timedCache.schedulePrune(CACHE_TIME_OUT);
    }

    private String getMetricKey(
            String nodeId, String exporterKey, String exporterName, String dbRole, String metricName) {
        return nodeId + SEPARATOR + exporterKey + SEPARATOR + exporterName + SEPARATOR + dbRole + SEPARATOR
                + metricName;
    }

    private String getGroupCollectKey(String nodeId, String exporterKey, String exporterName, String dbRole) {
        return nodeId + SEPARATOR + exporterKey + SEPARATOR + exporterName + SEPARATOR + dbRole;
    }

    /**
     * Collect database metric data
     *
     * @param target Target DTO
     * @param param  Param DTO
     * @throws IOException Read og_exporter.yml error
     */
    public List<MetricSnapshot> collectData(CollectTargetDTO target, CollectParamDTO param) throws IOException {
        long startTime = System.currentTimeMillis();
        List<MetricSnapshot> list = Collections.synchronizedList(new ArrayList<>());
        var in = new ClassPathResource("og_exporter.yml").getInputStream();
        Map<String, Object> map = new Yaml().loadAs(in, Map.class);
        CountDownLatch countDownLatch = new CountDownLatch(map.entrySet().size());
        ThreadPoolExecutor executors = ThreadUtil.newExecutor(0, map.entrySet().size());
        map.entrySet().forEach((entry) -> {
            executors.submit(() -> {
                try {
                    if (entry.getValue() instanceof Map) {
                        StopWatch stopWatch = new StopWatch();
                        stopWatch.start();
                        QueryInstance b = BeanUtil.mapToBean((Map<?, ?>) entry.getValue(), QueryInstance.class, false);
                        for (int i = 0; i < b.getQuery().size(); i++) {
                            QueryInstance.Query query = b.getQuery().get(i);
                            if (StrUtil.isBlank(query.getDbRole())) {
                                query.setDbRole(PRIMARY_ROLE);
                            }
                            // dbRole not match then ignore
                            // tod: resolve db role, now when same metrics,differ sql for differ role,will error with same name
                            String groupKey =
                                getGroupCollectKey(target.getTargetConfig().getNodeId(), entry.getKey(),
                                    query.getDbRole(),
                                    b.getName());

                            // filter collect data
                            if (param.getGroupNames() != null && !param.getGroupNames().isEmpty()
                                && param.getGroupNames().indexOf(b.name) < 0) {
                                continue;
                            }

                            // record group this collect time
                            metricCollectManager.storeGroupCollectTime(groupKey);
                            String sql = b.getQuery().get(0).getSql();

                            // if has time range after last scrape, build once timed task after (time range - 2s)
                            // just support smallest range = MIN_COLLECT_INTERVAL
                            // so, just create job when time range > (MIN_COLLECT_INTERVAL-1s)
                            // 15s gap time，and cache valid time is CACHE_TIME_OUT
                            // my once timed task should start after gap time - CACHE_TIME_OUT +1
                            // TOD: set a max value, to void task which is after a very long time
                            long gapTime = metricCollectManager.getGroupCollectGapTime(groupKey);
                            log.debug("Gap time:{}", gapTime);
                            if (gapTime >= (MIN_COLLECT_INTERVAL - 1000)) {
                                ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                                    .setNamePrefix("Metric-DB-" + gapTime + "-Collector-" + groupKey).build();
                                ScheduledExecutorService executor =
                                    Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
                                executor.schedule(() -> {
                                    try {
                                        StopWatch stopWatchTmp = new StopWatch();
                                        stopWatchTmp.start();
                                        log.debug("After 5s,run sql and cache");
                                        // real query job
                                        List<Map<String, Object>> resultNext =
                                            dbUtil.query(target.getTargetConfig().getNodeId(), sql);
                                        timedCache.put(groupKey, resultNext);
                                        stopWatchTmp.stop();
                                        if (stopWatchTmp.getTotalTimeSeconds() > CollectConstants.COLLECT_TIMEOUT + 1) {
                                            log.warn("The scheduled collection task for the metric [{}] of node {}:takes {} seconds, "
                                                    + "collection  has been timeout",
                                                entry.getKey(), target.getTargetConfig().getNodeId(),
                                                stopWatchTmp.getTotalTimeSeconds());
                                        } else {
                                            log.info("The scheduled collection task for the metric [{}] of node {}:takes {} seconds",
                                                entry.getKey(), target.getTargetConfig().getNodeId(),
                                                stopWatchTmp.getTotalTimeSeconds());
                                        }
                                    } catch (IllegalStateException e) {
                                        log.error("The scheduled collection task for the metric [{}] of node {}"
                                                + " is fail", entry.getKey(), target.getTargetConfig().getNodeId());
                                        log.error(e.getMessage());
                                    } finally {
                                        executor.shutdown();
                                    }
                                }, gapTime - CACHE_TIME_OUT + 1000, TimeUnit.MILLISECONDS);
                            }

                            // TOD: to remove same timed task with same key
                            // get cached data, if not > CACHE_TIME_OUT，return directly
                            List<Map<String, Object>> result = timedCache.get(groupKey, false);

                            // no cache, do query
                            if (CollectionUtil.isEmpty(result)) {
                                result = dbUtil.query(target.getTargetConfig().getNodeId(), sql);
                            }
                            if (CollectionUtil.isEmpty(result)) {
                                log.warn("Scrape metrics [{}] for node {}: value is empty",
                                    b.getName(), target.getTargetConfig().getNodeId());
                                return;
                            }

                            // trans sql result into
                            List<Map<String, Object>> finalResult = result;
                            // generate label names
                            List<String> labelNames = b.generateLabelNames();
                            // generate metric family
                            b.generateMetricFamily(entry.getKey(), labelNames);

                            // loop all item in [metrics] key in og_exporter.yml
                            b.getMetrics().forEach(metric -> {
                                // only do COUNTER GAUGE
                                if (!metric.getUsage().equals(QueryInstance.MetricInfo.Usage.COUNTER)
                                    && !metric.getUsage().equals(QueryInstance.MetricInfo.Usage.GAUGE)) {
                                    return;
                                }
                                if (metric.getUsage().equals(QueryInstance.MetricInfo.Usage.COUNTER)) {
                                    Counter counter = Counter.builder().name(b.name + '_' + metric.getName())
                                        .help(metric.getDescription())
                                        .labelNames(labelNames.toArray(new String[0])).build();
                                    finalResult.forEach(row -> {
                                        List<String> labelValues = new ArrayList<>();
                                        for (String name : labelNames) {
                                            if (CollectConstants.DB_DEFAULT_METRIC_LABELS.contains(name)) {
                                                continue;
                                            }
                                            labelValues.add(row.get(name).toString());
                                        }
                                        labelValues.add(target.getTargetConfig().getHostId());
                                        labelValues.add(target.getTargetConfig().getNodeId());
                                        Object value = row.get(metric.getName());
                                        if (value == null || StrUtil.isBlank(value.toString())) {
                                            log.error("No metric value:{}_{}{}", entry.getKey(),
                                                metric.getName(), labelValues);
                                        } else {
                                            counter.labelValues(labelValues.toArray(new String[0]))
                                                .inc(Double.parseDouble(value.toString()));
                                        }
                                    });
                                    list.add(counter.collect());
                                }
                                if (metric.getUsage().equals(QueryInstance.MetricInfo.Usage.GAUGE)) {
                                    Gauge gauge = Gauge.builder().name(b.name + '_' + metric.getName())
                                        .help(metric.getDescription())
                                        .labelNames(labelNames.toArray(new String[0])).build();
                                    finalResult.forEach(row -> {
                                        List<String> labelValues = new ArrayList<>();
                                        for (String name : labelNames) {
                                            if (CollectConstants.DB_DEFAULT_METRIC_LABELS.contains(name)) {
                                                continue;
                                            }
                                            labelValues.add(row.get(name).toString());
                                        }
                                        labelValues.add(target.getTargetConfig().getHostId());
                                        labelValues.add(target.getTargetConfig().getNodeId());
                                        Object value = row.get(metric.getName());
                                        if (value == null || StrUtil.isBlank(value.toString())) {
                                            log.error("No metric value:{}_{}{}", entry.getKey(), metric.getName(),
                                                labelValues);
                                        } else {
                                            gauge.labelValues(labelValues.toArray(new String[0]))
                                                .set(Double.parseDouble(value.toString()));
                                        }
                                    });
                                    list.add(gauge.collect());
                                }
                            });
                        }
                        stopWatch.stop();
                        if (stopWatch.getTotalTimeSeconds() > CollectConstants.COLLECT_TIMEOUT) {
                            log.warn("The real-time collection task for the metric [{}] of node {}:takes {} seconds, "
                                    + "collection  has been timeout",
                                entry.getKey(), target.getTargetConfig().getNodeId(), stopWatch.getTotalTimeSeconds());
                        } else {
                            log.info("The real-time collection task for the metric [{}] of node {}:takes {} seconds",
                                entry.getKey(), target.getTargetConfig().getNodeId(), stopWatch.getTotalTimeSeconds());
                        }
                    }
                }  catch (Exception e) {
                    log.error("The real-time collection task for the metric [{}] of node {} is fail!",
                        entry.getKey(), target.getTargetConfig().getNodeId());
                } finally {
                    countDownLatch.countDown();
                }
            });
        });

        try {
            countDownLatch.await(CollectConstants.COLLECT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        executors.shutdown();
        // print run time
        long endTime = System.currentTimeMillis();
        log.info("Scrape db metrics for node {}:takes {} ms", target.getTargetConfig().getNodeId(),
            endTime - startTime);
        return list;
    }

    /**
     * Convert groupNames to all metric names
     *
     * @param groupNames group names
     * @return Metrics names
     * @throws IOException Read og_exporter.yml error
     */
    public List<String> getRealMetricNameByGroupName(String[] groupNames) throws IOException {
        List<String> matchMetricsName = new ArrayList<>();
        var in = new ClassPathResource("og_exporter.yml").getInputStream();
        Map<String, Object> map = new Yaml().loadAs(in, Map.class);
        map.entrySet().forEach((entry) -> {
            if (!(entry.getValue() instanceof Map)) {
                return;
            }

            QueryInstance b = BeanUtil.mapToBean((Map<?, ?>) entry.getValue(), QueryInstance.class, false);

            if (!Arrays.stream(groupNames).anyMatch(z -> b.name.equalsIgnoreCase(z))) {
                return;
            }

            for (int i = 0; i < b.getQuery().size(); i++) {
                b.getMetrics().forEach(metric -> {
                    if (metric.getUsage() == QueryInstance.MetricInfo.Usage.COUNTER
                            || metric.getUsage() == QueryInstance.MetricInfo.Usage.GAUGE) {
                        matchMetricsName.add(
                                b.name + CollectConstants.SEPARATOR + metric.getName() + CollectConstants.SEPARATOR
                                        + "_total");
                    }
                });
            }
        });
        return matchMetricsName;
    }

    /**
     * QueryInstance
     *
     * @since 2023/12/1
     */
    @Data
    public static class QueryInstance {
        private String name;
        private String desc;
        private List<Query> query;
        private List<MetricInfo> metrics;
        private String status;

        /**
         * Generate label names
         *
         * @return java.util.List<java.lang.String>
         * @since 2023/12/1
         */
        public List<String> generateLabelNames() {
            List<String> labelNames = new ArrayList<>();
            getMetrics().forEach(metric -> {
                if (metric.getUsage() == MetricInfo.Usage.LABEL) {
                    labelNames.add(metric.getName());
                    return;
                }
            });
            labelNames.addAll(CollectConstants.DB_DEFAULT_METRIC_LABELS);
            return labelNames;
        }

        /**
         * generateMetricFamily
         *
         * @param base       base
         * @param labelNames labelNames
         * @return java.util.Map<java.lang.String, com.nctigba.observability.instance.agent.metric.MetricTypeAndLabels>
         * @since 2023/12/1
         */
        public Map<String, MetricTypeAndLabels> generateMetricFamily(String base, List<String> labelNames) {
            Map<String, MetricTypeAndLabels> result = new HashMap<>();
            getMetrics().forEach(metric -> {
                if (metric.getUsage().getType() != null) {
                    result.put(base + "_" + metric.getName(),
                            new MetricTypeAndLabels(metric.getUsage().type, labelNames));
                }
            });
            return result;
        }

        /**
         * Query entity
         *
         * @since 2023/12/1
         */
        @Data
        public static class Query {
            private String name;
            private String desc;
            private String sql;
            private String version;
            private String tags;
            private Integer timeout;
            private Integer ttl;
            private State status;
            private String dbRole;

            /**
             * State Enum
             *
             * @since 2023/12/1
             */
            public enum State {
                enable, disable
            }
        }

        /**
         * MetricInfo
         *
         * @since 2023/12/1
         */
        @Data
        public static class MetricInfo {
            private String name;
            private String description;
            private Usage usage;

            /**
             * Usage
             *
             * @since 2023/12/1
             */
            @Getter
            public enum Usage {
                COUNTER(MetricType.COUNTER), GAUGE(MetricType.GAUGE), LABEL, DISCARD;

                private MetricType type;

                private Usage() {
                }

                private Usage(MetricType type) {
                    this.type = type;
                }
            }
        }
    }
}
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
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.StrUtil;
import com.nctigba.observability.instance.agent.collector.AgentCollector;
import com.nctigba.observability.instance.agent.collector.AgentCounter;
import com.nctigba.observability.instance.agent.collector.AgentGauge;
import com.nctigba.observability.instance.agent.constant.CollectConstants;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.metric.MetricTypeAndLabels;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import com.nctigba.observability.instance.agent.util.DbUtils;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
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
    public void collectData(CollectTargetDTO target, CollectParamDTO param) throws IOException {
        long startTime = System.currentTimeMillis();
        var in = new ClassPathResource("og_exporter.yml").getInputStream();
        Map<String, Object> map = new Yaml().loadAs(in, Map.class);
        map.entrySet().forEach((entry) -> {
            if (entry.getValue() instanceof Map) {
                QueryInstance b = BeanUtil.mapToBean((Map<?, ?>) entry.getValue(), QueryInstance.class, false);
                for (int i = 0; i < b.getQuery().size(); i++) {
                    QueryInstance.Query query = b.getQuery().get(i);
                    if (StrUtil.isBlank(query.getDbRole())) {
                        query.setDbRole(PRIMARY_ROLE);
                    }

                    // dbRole not match then ignore
                    // tod: resolve db role, now when same metrics,differ sql for differ role,will error with same name

                    String groupKey =
                            getGroupCollectKey(target.getTargetConfig().getNodeId(), entry.getKey(), query.getDbRole(),
                                    b.getName());

                    // filter collect data
                    if (param.getGroupNames() != null && !param.getGroupNames().isEmpty()
                            && param.getGroupNames().indexOf(b.name) < 0) {
                        continue;
                    }

                    // record group this collect time
                    metricCollectManager.storeGroupCollectTime(groupKey);
                    String sql = b.getQuery().get(0).getSql();

                    // TOD: to remove same timed task with same key
                    // get cached data, if not > CACHE_TIME_OUT，return directly
                    List<Map<String, Object>> result = timedCache.get(groupKey, false);

                    // no cache, do query
                    if (result == null) {
                        result = dbUtil.query(target.getTargetConfig().getNodeId(), sql);
                        timedCache.put(groupKey, result);
                    }

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
                            log.debug("After 5s,run sql and cache");
                            // real query job
                            List<Map<String, Object>> resultNext =
                                    dbUtil.query(target.getTargetConfig().getNodeId(), sql);
                            timedCache.put(groupKey, resultNext);
                            executor.shutdown();
                        }, gapTime - CACHE_TIME_OUT + 1000, TimeUnit.MILLISECONDS);
                    }

                    // trans sql result into
                    List<Map<String, Object>> finalResult = result;
                    // generate label names
                    List<String> labelNames = b.generateLabelNames();
                    // generate metric family
                    Map<String, MetricTypeAndLabels> metricFamily = b.generateMetricFamily(entry.getKey(), labelNames);

                    // loop all item in [metrics] key in og_exporter.yml
                    b.getMetrics().forEach(metric -> {
                        // only do COUNTER GAUGE
                        if (metric.getUsage() != QueryInstance.MetricInfo.Usage.COUNTER
                                && metric.getUsage() != QueryInstance.MetricInfo.Usage.GAUGE) {
                            return;
                        }

                        String metricKey = getMetricKey(target.getTargetConfig().getNodeId(), entry.getKey(), b.name,
                                query.getDbRole(),
                                metric.getName());

                        // clear group data,
                        // void one data has data at A time and has no data at B time, then B time still collect
                        // value of Atime
                        AgentCollector agentCollector = metricCollectManager.getCollector(metricKey);
                        agentCollector.cleanAllLabelValuesData();

                        // loop all query result row
                        for (Map<String, Object> row : finalResult) {
                            // label values of row
                            List<String> labelValues = new ArrayList<>();
                            for (String name : labelNames) {
                                if (CollectConstants.DB_DEFAULT_METRIC_LABELS.contains(name)) {
                                    continue;
                                }
                                labelValues.add(row.get(name).toString());
                            }
                            labelValues.add(target.getTargetConfig().getHostId());
                            labelValues.add(target.getTargetConfig().getNodeId());

                            if (metric.getUsage().equals(QueryInstance.MetricInfo.Usage.COUNTER)) {
                                // get metric value
                                Object value = row.get(metric.getName());
                                if (value == null || StrUtil.isBlank(value.toString())) {
                                    log.debug("No metric value:{}{}", metric.getName(), labelValues);
                                } else if (agentCollector instanceof AgentCounter) {
                                    ((AgentCounter) agentCollector).labelValues(labelValues.toArray(new String[0]))
                                            .inc(Double.parseDouble(value.toString()));
                                } else {
                                    log.error("AgentCollector not instanceof AgentCounter");
                                }
                            } else if (metric.getUsage().equals(QueryInstance.MetricInfo.Usage.GAUGE)) {
                                // get metric value
                                Object value = row.get(metric.getName());
                                if (value == null || StrUtil.isBlank(value.toString())) {
                                    log.debug("No metric value:{}{}", metric.getName(), labelValues);
                                } else if (agentCollector instanceof AgentGauge) {
                                    ((AgentGauge) agentCollector).labelValues(labelValues.toArray(new String[0]))
                                            .set(Double.parseDouble(value.toString()));
                                } else {
                                    log.error("AgentCollector not instanceof AgentGauge");
                                }
                            } else {
                                log.error("Metric type not support:{}", metric.getUsage());
                            }
                        }
                    });
                }
            }
        });

        // print run time
        long endTime = System.currentTimeMillis();
        log.debug("Scrape db metrics for node {}:takes {} ms", target.getTargetConfig().getNodeId(),
                endTime - startTime);
    }

    /**
     * use og_exporter.yml register metrics
     * TOD: how to match other database
     *
     * @param targetConfig Target Config
     * @param registry     Prometheus Registry
     * @throws IOException Read or write yml file error
     */
    public void register(TargetConfig targetConfig, PrometheusRegistry registry) throws IOException {
        var in = new ClassPathResource("og_exporter.yml").getInputStream();
        Map<String, Object> map = new Yaml().loadAs(in, Map.class);
        map.entrySet().forEach((entry) -> {
            if (!(entry.getValue() instanceof Map)) {
                return;
            }

            QueryInstance b = BeanUtil.mapToBean((Map<?, ?>) entry.getValue(), QueryInstance.class, false);

            // loop every og_exporter.yml item
            for (int i = 0; i < b.getQuery().size(); i++) {
                QueryInstance.Query query = b.getQuery().get(i);

                // tod: abstract methods to do collect and register,to void edit 2 places
                // set default dbrole as "PRIMARY"
                if (StrUtil.isBlank(query.getDbRole())) {
                    query.setDbRole(PRIMARY_ROLE);
                }

                // dbRole not match then ignore
                // tod: resolve db role, now when same metrics,differ sql for differ role,will error with same name

                // generate label names
                List<String> labelNames = b.generateLabelNames();

                // every metric register a collector into PrometheusRegistry,
                // and store collector into collectorManager
                b.getMetrics().forEach(metric -> {
                    String metricKey = getMetricKey(targetConfig.getNodeId(), entry.getKey(), b.name, query.getDbRole(),
                            metric.getName());
                    log.debug("metricKey:{}", metricKey);
                    if (metric.getUsage() == QueryInstance.MetricInfo.Usage.LABEL) {
                        return;
                    }
                    if (metric.getUsage() == QueryInstance.MetricInfo.Usage.COUNTER) {
                        log.debug("metric:{}", metric.getName());
                        Counter collector =
                                Counter.builder().name(b.name + '_' + metric.getName()).help(metric.getDescription())
                                        .labelNames(labelNames.toArray(new String[0])).register(registry);
                        metricCollectManager.storeCollector(metricKey, new AgentCounter(collector));
                    } else if (metric.getUsage() == QueryInstance.MetricInfo.Usage.GAUGE) {
                        log.debug("metric:{}", metric.getName());
                        Gauge collector =
                                Gauge.builder().name(b.name + '_' + metric.getName()).help(metric.getDescription())
                                        .labelNames(labelNames.toArray(new String[0])).register(registry);
                        metricCollectManager.storeCollector(metricKey, new AgentGauge(collector));
                    } else {
                        log.error("Metric type not support:{}", metric.getUsage());
                    }
                });
            }
        });
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
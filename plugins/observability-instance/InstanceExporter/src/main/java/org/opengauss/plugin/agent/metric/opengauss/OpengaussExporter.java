/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.metric.opengauss;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.opengauss.plugin.agent.metric.DBmetric;
import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.metric.opengauss.OpengaussExporter.queryInstance.metricInfo.usage;
import org.opengauss.plugin.agent.metric.opengauss.OpengaussExporter.queryInstance.query;
import org.opengauss.plugin.agent.metric.opengauss.OpengaussExporter.queryInstance.query.state;
import org.opengauss.plugin.agent.util.DbUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import io.prometheus.client.Collector.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * table pg_stat_activity
 */
@Service
@Log4j2
@AllArgsConstructor
public class OpengaussExporter implements DBmetric, InitializingBean {
    private static final Map<String, queryInstance> CONFIG = new HashMap<>();
    private static Map<String, Metric> metricData = new HashMap<>();

    private final DbUtil dbUtil;

    /**
     * load open Gauss exporter config file
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        var in = new ClassPathResource("og_exporter.yml").getInputStream();
        Map<String, Object> map = new Yaml().loadAs(in, Map.class);
        map.entrySet().forEach((entry) -> {
            if (entry.getValue() instanceof Map) {
                var b = BeanUtil.mapToBean((Map<?, ?>) entry.getValue(), queryInstance.class, false);
                CONFIG.put(entry.getKey(), b);

            }
        });
    }

    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        return metricData;
    }

    @Scheduled(fixedDelay = 15000, initialDelay = 1000)
    public void catchMetric() {
        if (CONFIG.size() == 0) {
            return;
        }
        Map<String, Metric> all = new HashMap<>();
        var countDown = ThreadUtil.newCountDownLatch(CONFIG.size());
        CONFIG.entrySet().forEach(conf -> {
            ThreadUtil.execute(() -> {
                try {
                    all.putAll(metric(conf));
                } finally {
                    countDown.countDown();
                }
            });
        });
        try {
            countDown.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        metricData = all;
    }

    private Map<String, Metric> metric(Entry<String, queryInstance> conf) {
        String sql = null;
        for (query q : conf.getValue().getQuery()) {
            if (!isRoleMatched(q)) {
                continue;
            }
            if (q.getStatus() == state.enable) {
                sql = q.getSql();
                break;
            }
        }
        if (sql == null) {
            return Collections.emptyMap();
        }
        // generate label names
        List<String> labelNames = conf.getValue().generateLabelNames();
        // generate metric family
        Map<String, Metric> result = conf.getValue().generateMetricFamily(conf.getKey(), labelNames);
        // add data
        List<Map<String, Object>> r = dbUtil.query(sql);
        for (Map<String, Object> row : r) {
            // label values
            List<String> labelValues = new ArrayList<>();
            for (String name : labelNames) {
                try {
                    labelValues.add(row.get(name).toString());
                } catch (Exception e) {
                    log.debug("【labelNames】:【name】{}", name);
                    log.error("labelName not found from sql:{}", name);
                }
            }
            // metric values
            conf.getValue().getMetrics().forEach(metric -> {
                if (metric.getUsage().getType() != null) {
                    double val = 0;
                    Object value = row.get(metric.getName());
                    if (value == null) {
                        log.error("metric error:{}", metric.getName());
                        val = 0;
                    } else if (NumberUtil.isNumber(value.toString())) {
                        val = Double.parseDouble(value.toString());
                    } else {
                        if (value instanceof Timestamp)
                            val = ((Timestamp) value).getTime();
                    }
                    result.get(conf.getKey() + "_" + metric.getName()).addValue(labelValues, val);
                }
            });
        }
        return result;
    }

    private boolean isRoleMatched(query q) {
        String dbRole = q.getDbRole();
        if (!StringUtils.hasLength(dbRole)) {
            return true;
        }
        List<Map<String, Object>> query = dbUtil.query("SELECT pg_is_in_recovery();");
        return ((boolean) query.get(0).get("pg_is_in_recovery")) != "primary".equalsIgnoreCase(dbRole);
    }

    @Data
    public static class queryInstance {
        private String name;
        private String desc;
        private List<query> query;
        private List<metricInfo> metrics;
        private String status;

        public List<String> generateLabelNames() {
            List<String> labelNames = new ArrayList<>();
            getMetrics().forEach(metric -> {
                if (metric.getUsage() == usage.LABEL) {
                    labelNames.add(metric.getName());
                    return;
                }
            });
            return labelNames;
        }

        public Map<String, Metric> generateMetricFamily(String base, List<String> labelNames) {
            Map<String, Metric> result = new HashMap<>();
            getMetrics().forEach(metric -> {
                if (metric.getUsage().getType() != null) {
                    result.put(base + "_" + metric.getName(), new Metric(metric.getUsage().type, labelNames));
                }
            });
            return result;
        }

        @Data
        public static class query {
            private String name;
            private String desc;
            private String sql;
            private String version;
            private String tags;
            private Integer timeout;
            private Integer ttl;
            private state status;
            private String dbRole;

            public enum state {
                enable,
                disable
            }
        }

        @Data
        public static class metricInfo {
            private String name;
            private String description;
            private usage usage;

            @Getter
            public enum usage {
                COUNTER(Type.COUNTER),
                GAUGE(Type.GAUGE),
                LABEL,
                DISCARD;

                private Type type;

                private usage() {
                }

                private usage(Type type) {
                    this.type = type;
                }
            }
        }
    }
}
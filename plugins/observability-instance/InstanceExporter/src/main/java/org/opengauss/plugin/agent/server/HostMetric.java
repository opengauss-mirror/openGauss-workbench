/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengauss.plugin.agent.config.DbConfig;
import org.opengauss.plugin.agent.metric.Ametric;
import org.opengauss.plugin.agent.metric.DBmetric;
import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.StrUtil;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Component
@Order(Integer.MAX_VALUE)
@RequiredArgsConstructor
@Log4j2
@Data
@Accessors(chain = true)
public class HostMetric implements ApplicationRunner {
    private final Map<String, Ametric> collectors;
    private static final List<String> OS_LABEL_NAMES = Arrays.asList("host");
    private static final List<String> DB_LABEL_NAMES = Arrays.asList("instanceId");
    private static final Map<String, List<MetricFamilySamples>> CACHE = new HashMap<>();

    private final DbConfig config;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (config.getExporterPort() == null)
            return;
        collectors.forEach((collectorName, collector) -> {
            Map<String, Metric> metric;
            try {
                metric = collector.getMetric(null);
                if (metric == null) {
                    log.error("metric null:{}", collectorName);
                    return;
                }
            } catch (FileNotFoundException e) {
                log.error("collector:{}", collectorName, e);
                return;
            } catch (IOException e) {
                log.error("collector:{}", collectorName, e);
                return;
            }
            CollectorRegistry.defaultRegistry.register(new Collector() {
                @Override
                public List<MetricFamilySamples> collect() {
                    return CACHE.getOrDefault(collectorName, Collections.emptyList());
                }
            });
        });
        // c.unregister(cpuCollector);
        new HTTPServer.Builder().withPort(config.getExporterPort()).build();
    }

    private static String formatKey(String collectorName, Ametric collector, String k) {
        return StringUtil.replaceParenthesis((collector instanceof DBmetric) ? k : "agent_" + collectorName + k);
    }

    @Scheduled(fixedRate = 1000)
    public void cache() {
        try {
            collectors.forEach((collectorName, collector) -> {
                if (StrUtil.isBlank(config.getHostId()) || (collector instanceof DBmetric
                        && (StrUtil.isBlank(config.getNodeId()) || config.getDbport() == null)))
                    return;
                var list = new ArrayList<MetricFamilySamples>();
                try {
                    collector.getMetric(config.getDbport()).forEach((k, v) -> {
                        if (k == null)
                            return;
                        final String key = formatKey(collectorName, collector, k);
                        var samples = new ArrayList<Sample>();
                        List<String> labelNames = new ArrayList<>();
                        labelNames.addAll(v.getLabelNames());
                        // global label
                        labelNames.addAll(OS_LABEL_NAMES);
                        if (collector instanceof DBmetric)
                            labelNames.addAll(DB_LABEL_NAMES);
                        v.getValues().forEach(value -> {
                            List<String> labelValues = new ArrayList<>();
                            labelValues.addAll(value.getLabelValues());
                            // global value
                            labelValues.addAll(Arrays.asList(config.getHostId()));
                            if (collector instanceof DBmetric)
                                labelValues.addAll(Arrays.asList(config.getNodeId()));
                            if (labelNames.size() != labelValues.size()) {
                                log.error("collector {} metric {} error:{}{}", collectorName, key, labelNames,
                                        labelValues);
                                return;
                            }
                            samples.add(new Sample(key, labelNames, labelValues, value.getValue()));
                        });
                        String help = v.getHelp();
                        if (StrUtil.isBlank(help)) {
                            help = StrUtil.upperFirst(collectorName) + " information field " + k;
                        }
                        list.add(new MetricFamilySamples(key, v.getType(), help, samples));
                    });
                } catch (IOException e) {
                    log.error("collect fail:{}", collectorName);
                }
                CACHE.put(collectorName, list);
            });
        } catch (Exception e) {
            log.error("metric cache err", e);
        }
    }
}
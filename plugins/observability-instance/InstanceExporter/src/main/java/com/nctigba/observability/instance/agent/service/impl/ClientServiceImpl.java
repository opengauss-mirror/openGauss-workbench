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
 *  ClientServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/impl/ClientServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service.impl;

import com.nctigba.observability.instance.agent.collector.AgentCounter;
import com.nctigba.observability.instance.agent.collector.AgentGauge;
import com.nctigba.observability.instance.agent.config.model.AgentExporterConfig;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.exception.InitClientException;
import com.nctigba.observability.instance.agent.metric.Metric;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.metric.db.OpengaussMetrics;
import com.nctigba.observability.instance.agent.service.ClientService;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import com.nctigba.observability.instance.agent.service.PrometheusRegistryManagerService;
import com.nctigba.observability.instance.agent.service.TargetService;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Client Service implement
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
public class ClientServiceImpl implements ClientService {
    private static List<HTTPServer> servers = new ArrayList<>();

    @Autowired
    AgentExporterConfig agentExporterConfig;
    @Autowired
    TargetService targetService;
    @Autowired
    List<Metric> newAmetricList;
    @Autowired
    PrometheusRegistryManagerService prometheusRegistryManager;
    @Autowired
    OpengaussMetrics opengaussMetrics;
    @Autowired
    MetricCollectManagerService metricCollectManager;

    /**
     * @inheritDoc
     */
    @Override
    public String initClient(TargetConfig targetConfig) {
        // init prometheus HttpServer
        OptionalInt initServerResult = initPrometheusHttpServer(targetConfig);
        Integer port = null;
        if (initServerResult.isPresent()) {
            port = initServerResult.getAsInt();
        }

        targetConfig.setExporterPort(String.valueOf(port));

        // tod: Concurrency issue,one person reads the configuration file and another person modifies and overwrites it
        try {
            updateYmlTargetConfig(targetConfig);
        } catch (IOException e) {
            throw new InitClientException(e);
        }

        return String.valueOf(port);
    }

    /**
     * @inheritDoc
     */
    @Override
    public TargetConfig updateYmlTargetConfig(TargetConfig newConfig) throws IOException {
        File application = targetService.getOrCreateYmlFile();
        List<TargetConfig> targetConfigs = targetService.getTargetConfigs();

        try (InputStream inputStream = new FileInputStream(application);) {
            // when new config no exporter port, get old one
            Optional<TargetConfig> oldConfig = targetConfigs.stream()
                    .filter(z -> z.getNodeId().equals(newConfig.getNodeId())).findFirst();
            if (oldConfig.isPresent() && newConfig.getExporterPort() == null) {
                newConfig.setExporterPort(oldConfig.get().getExporterPort());
            }

            // remove old config and add new config
            if (!targetConfigs.isEmpty()) {
                targetConfigs.removeIf(z -> z.getNodeId().equals(newConfig.getNodeId()));
            }
            targetConfigs.add(newConfig);

            // get yml file to map
            Yaml yaml = new Yaml();
            Map mapConfig = yaml.loadAs(inputStream, Map.class);
            if (mapConfig == null) {
                mapConfig = new LinkedHashMap();
            }
            log.debug("yml now:{}", mapConfig);

            // remove targets node
            mapConfig.remove("targets");
            log.debug("yml after remove targets:{}", mapConfig);

            // set new 'targets' node
            List<Map> mapTargets = new ArrayList<>();
            targetConfigs.stream().forEach(z -> {
                Map mapTarget = new LinkedHashMap<String, Object>();
                Field[] declaredFields = z.getClass().getDeclaredFields();
                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    try {
                        mapTarget.put(field.getName(), field.get(z));
                    } catch (IllegalAccessException e) {
                        throw new InitClientException(e);
                    }
                }
                mapTargets.add(mapTarget);
            });
            mapConfig.put("targets", mapTargets);
            log.debug("mapConfig:{}", mapConfig);

            // overwrite application.yml
            try (FileWriter writer = new FileWriter(application)) {
                writer.write(yaml.dumpAsMap(mapConfig));
            }
        } catch (IOException e) {
            throw e;
        }
        return newConfig;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getTargetStartPort(TargetConfig newConfig) {
        List<TargetConfig> targetConfigs = targetService.getTargetConfigs();

        // when new config no exporter port, get old one
        if (targetConfigs != null) {
            Optional<TargetConfig> oldConfig = targetConfigs.stream()
                    .filter(z -> z.getNodeId().equals(newConfig.getNodeId())).findFirst();
            if (oldConfig.isPresent()) {
                return oldConfig.get().getExporterPort();
            }
        }
        return String.valueOf(agentExporterConfig.getStartPort());
    }


    OptionalInt initPrometheusHttpServer(TargetConfig targetConfig) {
        // if PrometheusRegistry for target existed
        log.debug("initPrometheusHttpServer:{}", targetConfig);
        if (prometheusRegistryManager.isTargetRegistryStored(targetConfig)) {
            log.debug("targetConfig already stored in prometheus registry:{}", targetConfig);
            List<TargetConfig> targetConfigs = targetService.getTargetConfigs();
            // when new config no exporter port, get old one
            if (targetConfigs != null) {
                Optional<TargetConfig> oldConfig = targetConfigs.stream()
                        .filter(z -> z.getNodeId().equals(targetConfig.getNodeId())).findFirst();
                if (oldConfig.isPresent()) {
                    return OptionalInt.of(Integer.valueOf(oldConfig.get().getExporterPort()));
                }
            }
            return OptionalInt.empty();
        }

        // new and store PrometheusRegistry
        PrometheusRegistry registry = new PrometheusRegistry();
        prometheusRegistryManager.storeRegistry(targetConfig, registry);
        // TOD: now only Counter and Gauge
        List<String> registeredMetric = new ArrayList<>();
        newAmetricList.forEach(z -> {
            for (int i = 0; i < z.getNames().length; i++) {
                if (z.getType().equals(MetricType.COUNTER)) {
                    registeredMetric.add(z.getNames()[i]);
                    Counter c = Counter.builder()
                            .name(z.getNames()[i])
                            .help(z.getHelps()[i])
                            .labelNames(z.getLabelNames())
                            .register(registry);
                    metricCollectManager.storeCollector(z.getCollectorKey(targetConfig, i),
                            new AgentCounter(c));
                } else if (z.getType().equals(MetricType.GAUGE)) {
                    registeredMetric.add(z.getNames()[i]);
                    Gauge c = Gauge.builder()
                            .name(z.getNames()[i])
                            .help(z.getHelps()[i])
                            .labelNames(z.getLabelNames())
                            .register(registry);
                    metricCollectManager.storeCollector(z.getCollectorKey(targetConfig, i),
                            new AgentGauge(c));
                } else {
                    throw new InitClientException("Metric type not supported:" + z.getType());
                }
            }
        });
        log.debug("registered metric:{}", registeredMetric);
        try {
            opengaussMetrics.register(targetConfig, registry);
        } catch (IOException e) {
            log.error("register opengaussMetrics error:{}", registry);
            throw new InitClientException(e);
        }

        int port = agentExporterConfig.getStartPort();
        try {
            while (isPortInUse(port)) {
                port++;
            }
            log.debug("Prometheus Java Client Port:{}", port);
            HTTPServer server = HTTPServer.builder()
                    .port(port)
                    .registry(registry).buildAndStart();
            servers.add(server);
        } catch (IOException e) {
            throw new InitClientException(e);
        }
        return OptionalInt.of(port);
    }

    /**
     * Check is port used
     *
     * @param port Port to check
     * @return boolean
     * @since 2023/12/1
     */
    public static boolean isPortInUse(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void clear() {
        servers.forEach(z -> z.close());
        servers = new ArrayList<>();
    }
}
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

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.nctigba.observability.instance.agent.config.model.AgentExporterConfig;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.service.ClientService;
import com.nctigba.observability.instance.agent.service.TargetService;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client Service implement
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
public class ClientServiceImpl implements ClientService {
    private HTTPServer server;

    @Autowired
    AgentExporterConfig agentExporterConfig;
    @Autowired
    TargetService targetService;
    @Autowired
    MultiCollector multiCollector;

    /**
     * @inheritDoc
     */
    @Override
    public void initClient() {
        PrometheusRegistry.defaultRegistry.register(multiCollector);
        int port = agentExporterConfig.getPort();
        while (isPortInUse(port)) {
            port++;
        }
        try {
            close();
            server = HTTPServer.builder().port(port).registry(PrometheusRegistry.defaultRegistry).buildAndStart();
            if (port != agentExporterConfig.getPort()) {
                updateAgentExporterPortConfig(port);
            }
        } catch (IOException e) {
            server.close();
            throw new CollectException(e.getMessage());
        }
    }

    private void updateAgentExporterPortConfig(int port) throws IOException  {
        File application = targetService.getOrCreateYmlFile();
        try (InputStream inputStream = new FileInputStream(application)) {
            Yaml yaml = new Yaml();
            Map mapConfig = yaml.loadAs(inputStream, Map.class);
            if (mapConfig == null) {
                mapConfig = new LinkedHashMap();
            }
            Map portMap = new LinkedHashMap<>();
            portMap.put("port", port);
            Map exporterMap = new LinkedHashMap<>();
            exporterMap.put("exporter", portMap);
            mapConfig.put("agent", exporterMap);
            try (FileWriter writer = new FileWriter(application)) {
                writer.write(yaml.dumpAsMap(mapConfig));
            }
            agentExporterConfig.setPort(port);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public TargetConfig updateYmlTargetConfig(TargetConfig newConfig) throws IOException {
        File application = targetService.getOrCreateYmlFile();
        List<TargetConfig> targetConfigs = targetService.getTargetConfigs();

        try (InputStream inputStream = new FileInputStream(application);) {
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

            JSONObject mapTargets = new JSONObject(targetConfigs);
            mapConfig.put("targets", mapTargets.toString());

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
     * updateYmlTargetConfigs
     *
     * @param newConfigs New target config list
     * @return list
     * @throws IOException IOException
     */
    @Override
    public List<TargetConfig> updateYmlTargetConfigs(List<TargetConfig> newConfigs) throws IOException {
        File application = targetService.getOrCreateYmlFile();
        List<TargetConfig> targetConfigs = targetService.getTargetConfigs();

        try (InputStream inputStream = new FileInputStream(application)) {
            // remove old config and add new config
            newConfigs.forEach(newConfig -> {
                if (!targetConfigs.isEmpty()) {
                    targetConfigs.removeIf(z -> z.getNodeId().equals(newConfig.getNodeId()));
                }
                targetConfigs.add(newConfig);
            });
            // get yml file to map
            Yaml yaml = new Yaml();
            Map mapConfig = yaml.loadAs(inputStream, Map.class);
            if (mapConfig == null) {
                mapConfig = new LinkedHashMap();
            }
            log.info("yml now:{}", mapConfig);

            // remove targets node
            mapConfig.remove("targets");
            log.info("yml after remove targets:{}", mapConfig);

            JSONArray mapTargets = new JSONArray(targetConfigs);
            mapConfig.put("targets", mapTargets);
            log.info("mapConfig:{}", mapConfig);

            // overwrite application.yml
            try (FileWriter writer = new FileWriter(application)) {
                writer.write(yaml.dumpAsMap(mapConfig));
            }
        } catch (IOException e) {
            throw e;
        }
        return newConfigs;
    }

    /**
     * close
     */
    @Override
    @PreDestroy
    public void close() {
        if (server != null) {
            server.close();
        }
    }

    /**
     * Check is port used
     *
     * @param port Port to check
     * @return boolean
     * @since 2023/12/1
     */
    private boolean isPortInUse(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
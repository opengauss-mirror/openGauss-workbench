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
 *  TargetServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/impl/TargetServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.service.TargetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TargetService Implement
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
public class TargetServiceImpl implements TargetService {
    /**
     * @inheritDoc
     */
    @Override
    public List<TargetConfig> getTargetConfigs() {
        Yaml yaml = new Yaml();
        File file = getYmlFile();
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (InputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> configMap = yaml.load(inputStream);
            if (configMap == null) {
                return new ArrayList<>();
            }
            List<Map<String, Object>> targetsMapList = (List<Map<String, Object>>) configMap.get("targets");
            List<TargetConfig> targets = convertToTargetConfigList(targetsMapList);
            return targets == null ? new ArrayList<>() : targets;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<TargetConfig> convertToTargetConfigList(List<Map<String, Object>> targetsMapList) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(targetsMapList, new TypeReference<List<TargetConfig>>() {
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void clearTargets() throws IOException {
        File application = getYmlFile();
        if (!application.exists()) {
            return;
        }
        try (InputStream inputStream = new FileInputStream(application);) {
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

            // overwrite application.yml
            try (FileWriter writer = new FileWriter(application)) {
                writer.write(yaml.dumpAsMap(mapConfig));
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public File getOrCreateYmlFile() throws IOException {
        // get or create new file
        File file = getYmlFile();
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * @inheritDoc
     */
    @Override
    public File getYmlFile() {
        // get or create new file
        String filePath = System.getProperty("user.dir") + File.separator + "application.yml";
        return new File(filePath);
    }
}
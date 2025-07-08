/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.common.utils;

import cn.hutool.core.collection.CollUtil;

import org.opengauss.admin.common.exception.ops.OpsException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * YamlUtils
 *
 * @author: wangchao
 * @Date: 2025/4/26 17:32
 * @Description: YamlUtils
 * @since 7.0.0-RC2
 **/
public class YamlUtils {
    /**
     * read Yaml file
     *
     * @param filePath yaml file path
     * @return yaml file content
     */
    public static Map<String, Object> readYaml(String filePath) {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(Paths.get(filePath))) {
            return yaml.load(in);
        } catch (IOException e) {
            throw new OpsException("Failed to read YAML file " + filePath + " failed : " + e.getMessage());
        }
    }

    /**
     * write Yaml file
     *
     * @param filePath yaml file path
     * @param data yaml file content
     */
    public static void writeYaml(String filePath, Map<String, Object> data) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        Yaml yaml = new Yaml(options);
        try {
            String yamlContent = yaml.dump(data);
            Files.writeString(Paths.get(filePath), yamlContent);
        } catch (IOException e) {
            throw new OpsException("Failed to write YAML file" + e.getMessage());
        }
    }

    /**
     * dump Yaml content
     *
     * @param data yaml content
     * @return yaml content
     */
    public static String dumpYaml(Map<String, Object> data) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        Yaml yaml = new Yaml(options);
        if (CollUtil.isEmpty(data)) {
            return yaml.dumpAsMap(new HashMap<>());
        }
        return yaml.dump(data);
    }

    /**
     * update properties
     *
     * @param properties yaml content
     * @param key key
     * @param value value
     */
    public static void updateProperties(Map<String, Object> properties, String key, Object value) {
        String[] split = key.split("\\.");
        updateNestedMap(properties, split, 0, value);
    }

    /**
     * get properties
     *
     * @param properties yaml content
     * @param key key
     * @return value
     */
    public static Object getProperties(Map<String, Object> properties, String key) {
        String[] split = key.split("\\.");
        return getNestedMap(properties, split, 0);
    }

    private static Object getNestedMap(Map<String, Object> currentMap, String[] keys, int index) {
        String currentKey = keys[index];
        if (index == keys.length - 1) {
            return currentMap.get(currentKey);
        }
        Object nextLevel = currentMap.get(currentKey);
        if (nextLevel instanceof Map) {
            return getNestedMap((Map<String, Object>) nextLevel, keys, index + 1);
        } else {
            return nextLevel;
        }
    }

    private static void updateNestedMap(Map<String, Object> currentMap, String[] keys, int index, Object value) {
        String currentKey = keys[index];
        if (index == keys.length - 1) {
            currentMap.put(currentKey, value);
            return;
        }
        if (currentMap.get(currentKey) instanceof Map nextLevel) {
            updateNestedMap(nextLevel, keys, index + 1, value);
        } else {
            Map<String, Object> newMap = new HashMap<>();
            currentMap.put(currentKey, newMap);
            updateNestedMap(newMap, keys, index + 1, value);
        }
    }
}

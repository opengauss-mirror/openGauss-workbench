/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.utils;

import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * properties utils
 *
 * @since 2025/4/17
 */
public class PropertiesUtils {
    /**
     * Update remote properties file content
     * if originalTable key exists in filePath, update value
     * if originalTable key not exists in filePath, add key and value
     *
     * @param filePath properties file path
     * @param updateParams update params
     * @param shellInfo shell info
     */
    public static void updateRemoteProperties(
            String filePath, Map<String, String> updateParams, ShellInfoVo shellInfo) {
        if (updateParams == null || ObjectUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Update properties file path and updateParams cannot be null or empty");
        }

        List<String> lines = FileUtils.catRemoteFileContents(filePath, shellInfo).lines().collect(Collectors.toList());
        Set<String> processedKeys = new HashSet<>();
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#") || trimmedLine.startsWith("!")
                    || !trimmedLine.contains("=")) {
                newLines.add(line);
                continue;
            }

            int separatorIndex = line.indexOf('=');
            String key = line.substring(0, separatorIndex).trim();

            if (updateParams.containsKey(key)) {
                String newValue = updateParams.get(key);
                String newLine = line.substring(0, separatorIndex + 1) + newValue;
                newLines.add(newLine);
                processedKeys.add(key);
            } else {
                newLines.add(line);
            }
        }

        for (Map.Entry<String, String> entry : updateParams.entrySet()) {
            if (!processedKeys.contains(entry.getKey())) {
                newLines.add(entry.getKey() + "=" + entry.getValue());
            }
        }
        String fileContent = String.join("\n", newLines);
        ShellUtil.updateFileContent(shellInfo, fileContent, filePath);
    }

    /**
     * Write changeParams to remote properties file, overwrite all content
     *
     * @param filePath changeParams file path
     * @param changeParams changeParams
     * @param shellInfo shellInfo
     */
    public static void writeRemoteProperties(String filePath, Map<String, String> changeParams, ShellInfoVo shellInfo) {
        if (changeParams == null || ObjectUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Write changeParams file path and changeParams cannot be null or empty");
        }

        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : changeParams.entrySet()) {
            content.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        ShellUtil.updateFileContent(shellInfo, content.toString(), filePath);
    }

    /**
     * Read remote properties file content
     *
     * @param filePath properties file path
     * @param shellInfo shell info
     * @return Properties
     * @throws IOException if an I/O error occurs
     */
    public static Properties readRemoteProperties(String filePath, ShellInfoVo shellInfo) throws IOException {
        if (ObjectUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Read properties file path cannot be null or empty");
        }

        String fileContents = FileUtils.catRemoteFileContents(filePath, shellInfo);
        Properties properties = new Properties();
        properties.load(new StringReader(fileContents));
        return properties;
    }

    /**
     * Read remote properties file content as map
     *
     * @param filePath properties file path
     * @param shellInfo shell info
     * @return Map<String, String>
     * @throws IOException if an I/O error occurs
     */
    public static Map<String, String> readPropertiesAsMap(String filePath, ShellInfoVo shellInfo) throws IOException {
        Properties properties = readRemoteProperties(filePath, shellInfo);

        Map<String, String> result = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            result.put(key, properties.getProperty(key));
        }
        return result;
    }
}

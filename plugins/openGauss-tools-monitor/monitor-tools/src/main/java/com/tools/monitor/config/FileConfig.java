/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * FileConfig
 *
 * @author liu
 * @since 2022-10-01
 */
@Component
@ConfigurationProperties(prefix = "file")
public class FileConfig {
    private static String relationConfig;

    private static String dataSourceConfig;

    private static String taskConfig;

    public static String getRelationConfig() {
        return relationConfig;
    }

    public void setRelationConfig(String relationConfig) {
        FileConfig.relationConfig = relationConfig;
    }

    public static String getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setDataSourceConfig(String dataSourceConfig) {
        FileConfig.dataSourceConfig = dataSourceConfig;
    }

    public static String getTaskConfig() {
        return taskConfig;
    }

    public void setTaskConfig(String taskConfig) {
        FileConfig.taskConfig = taskConfig;
    }
}

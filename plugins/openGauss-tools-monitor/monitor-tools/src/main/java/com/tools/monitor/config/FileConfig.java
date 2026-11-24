/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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

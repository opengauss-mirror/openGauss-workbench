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

package org.opengauss.admin.web.core.config;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

import java.net.URI;

/**
 * Log4j2ConfigurationFactory
 * Custom log4j2 configuration factory to ignore log4j2 configuration in lib/milvus-sdk-java
 *
 * @since 2026/01/15
 */
public class Log4j2ConfigurationFactory extends ConfigurationFactory {
    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
        if (configLocation != null && configLocation.toString().contains("milvus-sdk-java")) {
            return null;
        }

        return super.getConfiguration(loggerContext, name, configLocation);
    }

    @Override
    protected String[] getSupportedTypes() {
        return new String[] {".xml", "*"};
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource configurationSource) {
        return new XmlConfiguration(loggerContext, configurationSource);
    }
}

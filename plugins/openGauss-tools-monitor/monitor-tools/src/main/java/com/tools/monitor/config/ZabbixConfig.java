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
 * ZabbixConfig
 *
 * @author liu
 * @since 2022-10-01
 */
@Component
@ConfigurationProperties(prefix = "mysql")
public class ZabbixConfig {
    private static String prefix;

    private static String suffix;

    private static String driver;

    private static Integer timeout;

    public static String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        ZabbixConfig.prefix = prefix;
    }

    public static String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        ZabbixConfig.suffix = suffix;
    }

    public static String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        ZabbixConfig.driver = driver;
    }

    public static Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        ZabbixConfig.timeout = timeout;
    }
}

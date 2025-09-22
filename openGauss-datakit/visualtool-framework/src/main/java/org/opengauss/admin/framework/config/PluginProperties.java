/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.framework.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PluginProperties
 *
 * @author: wangchao
 * @Date: 2025/9/17 11:14
 * @since 7.0.0-RC2
 **/
@Data
@Component
@ConfigurationProperties(prefix = "plugin")
public class PluginProperties {
    private String mainPackage;
    private String runMode;
    private List<String> pluginPath;
}

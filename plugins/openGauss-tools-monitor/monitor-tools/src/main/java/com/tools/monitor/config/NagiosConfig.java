/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * NagiosConfig
 *
 * @author liu
 * @since 2022-10-01
 */
@Component
@ConfigurationProperties(prefix = "nagios")
public class NagiosConfig {
    private static long delayTime;

    public static long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        NagiosConfig.delayTime = delayTime;
    }
}

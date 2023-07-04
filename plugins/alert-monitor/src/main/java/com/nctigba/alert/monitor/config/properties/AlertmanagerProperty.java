/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.properties;

import lombok.Data;

/**
 * @author wuyuebin
 * @date 2023/4/27 15:35
 * @description
 */
@Data
public class AlertmanagerProperty {
    private String pathPrefix;
    private String apiVersion;
}

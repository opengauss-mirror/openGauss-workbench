/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import com.nctigba.alert.monitor.entity.AlertConfig;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/6/10 11:08
 * @description
 */
@Data
@Accessors(chain = true)
public class PrometheusEnvDto {
    private String promIp;
    private Integer hostPort;
    private Integer promPort;
    private String promUsername;
    private String promPasswd;
    private String path;
    private List<AlertConfig> configList;
    private List<AlertConfig> delConfigList;
}

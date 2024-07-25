package com.nctigba.alert.monitor.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AlertPluginInfoDTO
 *
 * @author wuyuebin
 * @since 2024/7/22 18:36
 */
@Data
public class AlertPluginInfoDTO {
    private String pluginCode;
    private String ruleCode;
    private String instanceId;
    private String instance;
    private String ip;
    private String port;
    private LocalDateTime alertTime;
}

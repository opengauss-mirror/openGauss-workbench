/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.model.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author wuyuebin
 * @date 2023/4/30 01:59
 * @description The additional information included in the alert messages pushed by Prometheus.
 */
@Data
public class AlertLabels {
    private String alertname;
    private String instance; // cluster_node_id
    private String level;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateRuleId;
}

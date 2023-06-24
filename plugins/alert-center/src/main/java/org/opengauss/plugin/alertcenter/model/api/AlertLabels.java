/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.model.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author wuyuebin
 * @date 2023/4/30 01:59
 * @description prometheus推送的告警信息额外信息
 */
@Data
public class AlertLabels {
    private String alertname;
    private String instance; // 对应cluster_node_id
    private String level;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateRuleId;
}

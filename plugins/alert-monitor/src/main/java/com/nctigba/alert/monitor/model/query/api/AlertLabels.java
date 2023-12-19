/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AlertLabels.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/query/api/AlertLabels.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.query.api;

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

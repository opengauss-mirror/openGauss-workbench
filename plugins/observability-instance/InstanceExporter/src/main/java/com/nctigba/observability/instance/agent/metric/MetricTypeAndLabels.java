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
 *  MetricTypeAndLabels.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/MetricTypeAndLabels.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Type and labels for metric
 *
 * @since 2023/12/1
 */
@Data
@NoArgsConstructor
@Generated
public class MetricTypeAndLabels {
    MetricType type;
    List<String> labelNames = new ArrayList<>();

    public MetricTypeAndLabels(MetricType type, List<String> labelNames) {
        this.type = type;
        this.labelNames = labelNames;
    }
}
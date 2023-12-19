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
 *  MetricType.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/MetricType.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric;

/**
 * Enum for metric type, base on prometheus metric
 *
 * @since 2023/12/1
 */
public enum MetricType {
    UNKNOWN, // This is untyped in Prometheus text format.
    COUNTER, GAUGE, STATE_SET, INFO, HISTOGRAM, GAUGE_HISTOGRAM, SUMMARY,
}

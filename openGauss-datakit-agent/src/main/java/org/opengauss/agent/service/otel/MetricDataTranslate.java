/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.service.otel;

import static java.util.stream.Collectors.toList;
import static org.opengauss.agent.constant.AgentConstants.MetricTranslate.DEFAULT_DESC;
import static org.opengauss.agent.constant.AgentConstants.MetricTranslate.DEFAULT_NAME;
import static org.opengauss.agent.constant.AgentConstants.MetricTranslate.DEFAULT_TYPE;
import static org.opengauss.agent.constant.AgentConstants.MetricTranslate.DEFAULT_UNIT;

import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.constant.AgentConstants;
import org.opengauss.agent.entity.DataPoint;
import org.opengauss.agent.entity.Metric;
import org.opengauss.agent.exception.AgentException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MetricDataTranslate
 *
 * @author: wangchao
 * @Date: 2025/3/15 14:14
 * @Description: MetricDataTranslate
 * @since 7.0.0-RC2
 **/
@Slf4j
public class MetricDataTranslate {
    /**
     * doTranslate
     *
     * @param metrics metrics
     * @return List<Metric>
     */
    public List<Metric> doTranslate(Collection<MetricData> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            log.warn("Input metrics collection is empty");
            return Collections.emptyList();
        }
        return metrics.stream()
            .filter(Objects::nonNull)
            .map(this::convertMetric)
            .collect(toList());
    }

    private Metric convertMetric(MetricData metric) {
        Metric converted = new Metric(getName(metric), getDescription(metric), getUnit(metric), getType(metric));
        converted.setPoints(getMetricDataPoint(metric));
        log.debug("Converted metric: {} {}", converted, metric.getData());
        return converted;
    }

    private List<DataPoint> getMetricDataPoint(MetricData metric) {
        return Optional.ofNullable(metric.getData())
            .map(Data::getPoints)
            .orElse(Collections.emptyList())
            .stream()
            .map(this::convertDataPoint)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private DataPoint convertDataPoint(PointData point) {
        DataPoint dp;
        try {
            dp = new DataPoint();
            dp.setValue(getPointValue(point));  // 保留原始数值类型
            dp.setEpochNanos(translateEpochNanos(point.getEpochNanos()));
            dp.setStartEpochNanos(translateEpochNanos(point.getStartEpochNanos()));
            convertAttributes(point, dp);
        } catch (AgentException ex) {
            log.error("DataPoint conversion error: {}", ex.getMessage());
            dp = null;
        }
        return dp;
    }

    private void convertAttributes(PointData point, DataPoint dp) {
        point.getAttributes()
            .forEach((key, value) -> dp.getAttributes().put(attributeConvert(key), attributeConvert(value)));
    }

    private String attributeConvert(Object obj) {
        if (obj == null) {
            return AgentConstants.NULL;
        }
        try {
            if (obj instanceof Collection<?> collection) {
                return collection.stream().map(this::attributeConvert).collect(Collectors.joining(","));
            } else if (obj.getClass().isArray()) {
                return Arrays.stream((Object[]) obj).map(this::attributeConvert).collect(Collectors.joining(","));
            } else {
                return obj.toString();
            }
        } catch (Exception e) {
            log.error("Attribute conversion error: {}", e.getMessage());
            throw new AgentException("Attribute conversion error " + e.getMessage());
        }
    }

    private static Instant translateEpochNanos(Long epochNanos) {
        long seconds = epochNanos / 1_000_000_000L;    // 提取秒部分
        int nanoAdjustment = (int) (epochNanos % 1_000_000_000L); // 剩余纳秒
        return Instant.ofEpochSecond(seconds, nanoAdjustment);
    }

    private String getName(MetricData metric) {
        return Optional.ofNullable(metric.getName()).orElse(DEFAULT_NAME);
    }

    private String getDescription(MetricData metric) {
        return Optional.ofNullable(metric.getDescription()).orElse(DEFAULT_DESC);
    }

    private String getUnit(MetricData metric) {
        return Optional.ofNullable(metric.getUnit()).orElse(DEFAULT_UNIT);
    }

    private String getType(MetricData metric) {
        return Optional.ofNullable(metric.getType()).map(Enum::name).orElse(DEFAULT_TYPE);
    }

    private String getPointValue(PointData point) {
        if (point instanceof LongPointData longPointData) {
            return String.valueOf(longPointData.getValue());
        } else if (point instanceof DoublePointData doublePointData) {
            return String.valueOf(doublePointData.getValue());
        } else if (point instanceof HistogramPointData histogramPointData) {
            return String.valueOf(histogramPointData.getSum());
        } else if (point instanceof SummaryPointData summaryPointData) {
            return String.valueOf(summaryPointData.getSum());
        } else {
            throw new AgentException("Unsupported point data type");
        }
    }
}

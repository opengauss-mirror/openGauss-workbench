/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.service.task.core;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.entity.task.TaskMetricsDefinitionVo;
import org.opengauss.agent.service.otel.HttpMetricExporter;
import org.opengauss.agent.vo.MetricData;
import org.opengauss.agent.vo.MetricKey;
import org.opengauss.agent.vo.MultiValueMetric;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MeterProviderContext
 *
 * @author: wangchao
 * @Date: 2025/5/16 16:43
 * @since 7.0.0-RC2
 **/
@Slf4j
public class MeterProviderContext {
    @Getter
    boolean isRegister = false;
    final OpenTelemetrySdk sdk;
    final SdkMeterProvider meterProvider;
    final HttpMetricExporter exporter;
    final Meter meter;
    ConcurrentMap<MetricKey, MetricData> gaugesMetricDataMap = new ConcurrentHashMap<>();
    ConcurrentMap<MetricKey, ObservableDoubleGauge> gaugesMap = new ConcurrentHashMap<>();
    List<TaskMetricsDefinitionVo> metricsDefinitions;
    List<String> collectors;
    Map<Long, List<MetricKey>> taskMetricKeys = new ConcurrentHashMap<>();

    /**
     * Constructor for MeterProviderContext
     *
     * @param sdk OpenTelemetrySdk
     * @param meterProvider meter provider
     * @param exporter exporter
     * @param meter meter
     */
    public MeterProviderContext(OpenTelemetrySdk sdk, SdkMeterProvider meterProvider, HttpMetricExporter exporter,
        Meter meter) {
        this.sdk = sdk;
        this.meterProvider = meterProvider;
        this.exporter = exporter;
        this.meter = meter;
    }

    /**
     * Initialize the MeterProviderContext
     *
     * @param metrics metrics
     * @param collectors collectors
     * @param taskMetricKeys taskMetricKeys
     */
    public void initialize(List<TaskMetricsDefinitionVo> metrics, List<String> collectors,
        Map<Long, List<MetricKey>> taskMetricKeys) {
        this.metricsDefinitions = metrics;
        this.collectors = collectors;
        this.taskMetricKeys = taskMetricKeys;
        registerMetrics();
    }

    /**
     * Register metrics
     */
    void registerMetrics() {
        isRegister = true;
        for (TaskMetricsDefinitionVo metric : metricsDefinitions) {
            String prop = metric.getProp();
            if (StrUtil.isEmpty(prop)) {
                MetricKey metricKey = MetricKey.of(metric.getName());
                gaugesMetricDataMap.put(metricKey, new MetricData());
                gaugesMap.put(metricKey, meter.gaugeBuilder(metric.getName())
                    .setUnit(Objects.isNull(metric.getUnit()) ? "" : metric.getUnit())
                    .setDescription(Objects.isNull(metric.getDescription()) ? "" : metric.getDescription())
                    .buildWithCallback(measurement -> {
                        MetricData data = gaugesMetricDataMap.get(metricKey);
                        measurement.record(data.getValue().get());
                    }));
            }
        }
    }

    /**
     * Refresh dynamic metrics value
     *
     * @param dynamicMetrics dynamic metrics
     */
    public void refreshDynamicMetrics(Map<String, Double> dynamicMetrics) {
        if (dynamicMetrics == null || dynamicMetrics.isEmpty()) {
            return;
        }
        dynamicMetrics.forEach((metricName, value) -> {
            MetricKey metricKey = MetricKey.of(metricName);
            MetricData metricData = gaugesMetricDataMap.get(metricKey);
            if (metricData != null) {
                metricData.getValue().set(value);
            } else {
                log.warn("============== Metric {} not found in gaugesMetricDataMap ===============", metricName);
            }
        });
    }

    /**
     * Refresh multi value dynamic metrics
     *
     * @param multiDynamicMetrics multi dynamic metrics
     * @param activeTaskIds active task ids
     */
    public void refreshMultiDynamicMetrics(List<MultiValueMetric> multiDynamicMetrics, Set<Long> activeTaskIds) {
        // 清理无效任务指标
        cleanupStaleMetrics(activeTaskIds);
        // 更新当前指标
        Map<String, TaskMetricsDefinitionVo> metricsDefinitionVoMap = metricsDefinitions.stream()
            .collect(Collectors.toMap(TaskMetricsDefinitionVo::getName, Function.identity()));
        multiDynamicMetrics.forEach(metricValue -> {
            updateOtelMetrics(metricsDefinitionVoMap.get(metricValue.getName()), metricValue.getPropValue(),
                metricValue.getPropValue().getKey());
        });
    }

    /**
     * Update otel metrics
     *
     * @param metric metric
     * @param doublePair value
     * @param propValue property value
     */
    void updateOtelMetrics(TaskMetricsDefinitionVo metric, Pair<String, Double> doublePair, String propValue) {
        // 仅更新数值，不重建指标
        MetricKey key = MetricKey.of(metric.getName(), propValue);
        // 根据属性值动态创建或者更新指标实例
        MetricData metricData = gaugesMetricDataMap.computeIfAbsent(key, k -> {
            // 动态注册新属性实例
            ObservableDoubleGauge gauge = meter.gaugeBuilder(metric.getName())
                .setUnit(metric.getUnit())
                .setDescription(metric.getDescription())
                .buildWithCallback(measurement -> {
                    MetricData data = gaugesMetricDataMap.get(key);
                    measurement.record(data.getValue().get(), data.getAttributes());
                });
            gaugesMap.put(key, gauge);
            return new MetricData();
        });
        // 更新数值和属性
        metricData.getValue().set(doublePair.getValue());
        if (StrUtil.isNotEmpty(propValue)) {
            metricData.setAttributes(Attributes.of(AttributeKey.stringKey(metric.getProp()), propValue));
        }
    }

    /**
     * Cleanup stale metrics
     *
     * @param activeTaskIds the active task ids
     */
    void cleanupStaleMetrics(Set<Long> activeTaskIds) {
        Set<Long> staleTasks = new HashSet<>(taskMetricKeys.keySet());
        staleTasks.removeAll(activeTaskIds);
        staleTasks.forEach(taskId -> {
            taskMetricKeys.get(taskId).forEach(gaugesMetricDataMap::remove);
            taskMetricKeys.remove(taskId);
        });
    }

    /**
     * Shutdown the MeterProviderContext
     */
    public void shutdown() {
        gaugesMap.forEach((metric, metricGauges) -> {
            metricGauges.close();
        });
        gaugesMetricDataMap.clear();
        gaugesMap.clear();
        if (exporter != null) {
            exporter.shutdown().join(100, TimeUnit.MILLISECONDS);
        }
        if (meterProvider != null) {
            meterProvider.shutdown().join(100, TimeUnit.MILLISECONDS);
        }
        if (sdk != null) {
            sdk.shutdown().join(100, TimeUnit.MILLISECONDS);
        }
        collectors.clear();
        metricsDefinitions.clear();
        taskMetricKeys.clear();
        isRegister = false;
    }

    /**
     * Export metrics
     */
    public void exportMetrics() {
        exporter.flush();
    }
}

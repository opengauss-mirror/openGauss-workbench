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

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

import org.opengauss.agent.client.DynamicHttpClientBuilder;
import org.opengauss.agent.client.MetricHttpClient;
import org.opengauss.agent.entity.Metric;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HttpMetricExporter
 *
 * @author: wangchao
 * @Date: 2025/3/14 17:13
 * @Description: HttpMetricExporter
 * @since 7.0.0-RC2
 **/
@Slf4j
public class HttpMetricExporter implements MetricExporter {
    private final MetricHttpClient metricHttpClient;
    private final String baseUrl;
    private final AtomicBoolean isShutdown;
    private final AggregationTemporality aggregationTemporality;
    private final MetricDataTranslate metricDataTranslate = new MetricDataTranslate();
    private final Map<String, Object> commonParams = new ConcurrentHashMap<>();

    /**
     * constructor
     *
     * @param baseUrl URL Template
     * @param aggregationTemporality otel aggregation temporality
     * @param commonParams common params
     */
    public HttpMetricExporter(String baseUrl, AggregationTemporality aggregationTemporality,
        Map<String, Object> commonParams) {
        this.baseUrl = baseUrl;
        this.aggregationTemporality = aggregationTemporality;
        this.commonParams.putAll(commonParams);
        this.isShutdown = new AtomicBoolean(false);
        this.metricHttpClient = DynamicHttpClientBuilder.createHttpClient(baseUrl);
    }

    /**
     * create http metric exporter with params
     *
     * @param baseUrl URL Template
     * @param commonParams common params
     * @return exporter
     */
    public static HttpMetricExporter createWithParams(String baseUrl, Map<String, Object> commonParams) {
        return new HttpMetricExporter(baseUrl, AggregationTemporality.CUMULATIVE, commonParams);
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
        try {
            String path = resolvePath(baseUrl, commonParams);
            List<Metric> payload = metricDataTranslate.doTranslate(metrics);
            Object objAgentId = commonParams.get("agentId");
            Long agentId = objAgentId instanceof Long ? (Long) objAgentId : Long.valueOf(objAgentId.toString());
            retrofit2.Call<Void> call = metricHttpClient.sendMetrics(path, (List<Long>) commonParams.get("taskIds"),
                agentId, payload);
            log.debug("Export http info | {}: {}", baseUrl, commonParams);
            Response<Void> response = call.execute(); // 同步执行（或使用enqueue异步）
            if (!response.isSuccessful()) {
                log.error("Export failed | Code: {}", response.code());
                return CompletableResultCode.ofFailure();
            }
            return CompletableResultCode.ofSuccess();
        } catch (Exception e) {
            log.error("Export failed", e);
            return CompletableResultCode.ofFailure();
        }
    }

    private String resolvePath(String urlTemplate, Map<String, Object> params) {
        String path = urlTemplate;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (path.contains(placeholder)) {
                path = path.replace(placeholder, entry.getValue().toString());
            }
        }
        return path;
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        if (isShutdown.getAndSet(true)) {
            return CompletableResultCode.ofSuccess();
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return aggregationTemporality;
    }
}

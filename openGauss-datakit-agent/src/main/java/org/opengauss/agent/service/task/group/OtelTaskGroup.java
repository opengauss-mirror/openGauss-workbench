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

package org.opengauss.agent.service.task.group;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.utils.URIBuilder;
import org.opengauss.agent.config.AppConfig;
import org.opengauss.agent.exception.TaskExecutionException;
import org.opengauss.agent.service.otel.HttpMetricExporter;
import org.opengauss.agent.service.task.core.MeterProviderContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * OtelTaskGroup
 *
 * @author: wangchao
 * @Date: 2025/5/16 16:44
 * @since 7.0.0-RC2
 **/
@Slf4j
public class OtelTaskGroup extends TaskGroup {
    /**
     * PARAM_TASK instrumentation scope name
     */
    protected static final String INSTRUMENTATION_SCOPE_NAME = "datakit.agent.metrics";

    volatile MeterProviderContext meterProviderContext;

    /**
     * Constructor for OtelTaskGroup
     *
     * @param key group key
     */
    public OtelTaskGroup(GroupKey key) {
        super(key);
    }

    /**
     * getMeterProviderContext
     *
     * @return MeterProviderContext
     */
    public MeterProviderContext getMeterProviderContext() {
        return meterProviderContext;
    }

    /**
     * createContext
     *
     * @param appConfig AppConfig
     * @throws TaskExecutionException TaskExecutionException
     */
    public void createContext(AppConfig appConfig) throws TaskExecutionException {
        try {
            if (meterProviderContext != null && meterProviderContext.isRegister()) {
                return;
            }
            String httpTarget = new URIBuilder(appConfig.getAppServerUrl()).setPath(getGroupKey().getDataSendTarget())
                .build()
                .toString();
            Map<String, Object> params = new HashMap<>();
            params.put(PARAM_TASK, new ArrayList<>(getTasks().keySet()));
            params.put(PARAM_AGENT, appConfig.getAgentId());
            log.info("recreateContext httpTarget={},params={}", httpTarget, params);
            HttpMetricExporter exporter = HttpMetricExporter.createWithParams(httpTarget, params);
            PeriodicMetricReader reader = PeriodicMetricReader.builder(exporter)
                .setInterval(Duration.ofMillis(getGroupKey().getPeriod()))
                .build();
            SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();
            OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build();
            Meter meter = sdk.getMeter(INSTRUMENTATION_SCOPE_NAME);
            meterProviderContext = new MeterProviderContext(sdk, meterProvider, exporter, meter);
            meterProviderContext.initialize(getMergedMetrics(), getMergedCollectorList(), getTaskMetricKeys());
        } catch (Exception e) {
            throw new TaskExecutionException("Invalid create MeterProviderContext : " + e.getMessage());
        }
    }

    /**
     * recreateContext
     *
     * @param appConfig AppConfig
     * @throws TaskExecutionException TaskExecutionException
     */
    public void recreateContext(AppConfig appConfig) throws TaskExecutionException {
        try {
            if (meterProviderContext != null) {
                meterProviderContext.shutdown();
            }
            createContext(appConfig);
        } catch (TaskExecutionException e) {
            throw new TaskExecutionException("recreate Context Invalid target URL: " + e.getMessage());
        }
    }

    /**
     * stop periodic collection
     */
    public void stopPeriodicCollection() {
        super.stopPeriodicCollection();
        if (meterProviderContext != null) {
            meterProviderContext.shutdown();
        }
    }
}

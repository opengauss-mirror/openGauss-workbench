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

package org.opengauss.agent.service.task.core;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.config.AppConfig;
import org.opengauss.agent.entity.TaskDefinition;
import org.opengauss.agent.exception.AgentException;
import org.opengauss.agent.exception.TaskExecutionException;
import org.opengauss.agent.service.task.TaskExecutor;
import org.opengauss.agent.service.task.group.GroupKey;
import org.opengauss.agent.service.task.group.OtelTaskGroup;
import org.opengauss.agent.vo.MultiValueMetric;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * HostDynamicExecutor
 *
 * @author: wangchao
 * @Date: 2025/4/9 10:35
 * @Description: HostStaticCollector
 * @since 7.0.0-RC2
 **/
@Slf4j
@ApplicationScoped
public class HostDynamicExecutor implements TaskExecutor {
    @Inject
    AppConfig appConfig;

    private OtelCommonExecutor otelCommonExecutor;
    private final OshiServerCollector oshiServerCollector = new OshiServerCollector();

    /**
     * start
     *
     * @param event startup event
     */
    void onStart(@Observes StartupEvent event) {
        otelCommonExecutor = new OtelCommonExecutor(appConfig, log);
    }

    @Override
    public void initialize(TaskDefinition task) throws TaskExecutionException {
        otelCommonExecutor.initialize(task);
    }

    @Override
    public void execute() throws TaskExecutionException {
        ConcurrentMap<GroupKey, OtelTaskGroup> targetGroups = otelCommonExecutor.getTargetGroups();
        targetGroups.forEach((target, group) -> {
            if (!group.hasTask()) {
                log.warn("No tasks configured for group {}", target);
                return;
            }
            startGroupTask(target, group);
        });
    }

    private void startGroupTask(GroupKey key, OtelTaskGroup group) {
        log.info("Starting group task: {}:{}", key, group.getTaskIds());
        group.startGroupTask(() -> collectMetricsForGroup(group, key));
    }

    private void collectMetricsForGroup(OtelTaskGroup group, GroupKey key) {
        try {
            Set<Long> taskSet = group.getTaskIds();
            MeterProviderContext meterProviderContext = group.getMeterProviderContext();
            // 实际收集逻辑
            if (meterProviderContext != null) {
                List<String> collectors = group.getMergedCollectorList();
                Map<String, Double> dynamicMetrics = oshiServerCollector.dynamicCollectMap(collectors);
                List<MultiValueMetric> multiMetrics = oshiServerCollector.multiDynamicCollect(collectors);
                log.info("Successfully collected {} host metrics and {} host multi-metrics for group [{}, tasks={}] "
                    + "collectors ->{} ", dynamicMetrics.size(), multiMetrics.size(), key, taskSet, collectors.size());
                meterProviderContext.refreshDynamicMetrics(dynamicMetrics);
                meterProviderContext.refreshMultiDynamicMetrics(multiMetrics, taskSet);
                meterProviderContext.exportMetrics();
            }
        } catch (AgentException ex) {
            log.error("Metric collection failed for group {}", key.getDataSendTarget(), ex);
        }
    }

    /**
     * stop
     *
     * @param event shutdown event
     */
    void onStop(@Observes ShutdownEvent event) {
        otelCommonExecutor.stop();
    }

    @Override
    public boolean hasTaskRunning() {
        return otelCommonExecutor.hasTaskRunning();
    }

    /**
     * Remove task
     *
     * @param taskId Task ID
     */
    @Override
    public void cancel(Long taskId) {
        otelCommonExecutor.cancal(taskId);
    }
}

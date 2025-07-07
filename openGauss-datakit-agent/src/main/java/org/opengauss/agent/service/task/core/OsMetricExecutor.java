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

import cn.hutool.core.util.StrUtil;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.config.AppConfig;
import org.opengauss.agent.entity.OsCmdResult;
import org.opengauss.agent.entity.TaskDefinition;
import org.opengauss.agent.entity.task.TaskMetricsDefinitionVo;
import org.opengauss.agent.exception.TaskExecutionException;
import org.opengauss.agent.service.task.TaskExecutor;
import org.opengauss.agent.service.task.group.GroupKey;
import org.opengauss.agent.service.task.group.OtelTaskGroup;
import org.opengauss.agent.utils.MathUtils;
import org.opengauss.agent.utils.OsCommandUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * OsMetricExecutor
 *
 * @author: wangchao
 * @Date: 2025/4/9 10:35
 * @Description: OsMetricExecutor
 * @since 7.0.0-RC2
 **/
@Slf4j
@ApplicationScoped
public class OsMetricExecutor implements TaskExecutor {
    OtelCommonExecutor otelCommonExecutor;
    @Inject
    AppConfig appConfig;

    /**
     * initialize OsMetricExecutor instance when application start
     *
     * @param event startup event
     */
    void onStart(@Observes StartupEvent event) {
        otelCommonExecutor = new OtelCommonExecutor(appConfig, log);
        log.info("initialize os metric task executor : {}", appConfig);
    }

    @Override
    public void initialize(TaskDefinition task) throws TaskExecutionException {
        log.info("initialize os metric task: {}", task);
        otelCommonExecutor.initialize(task);
    }

    @Override
    public void execute() throws TaskExecutionException {
        ConcurrentMap<GroupKey, OtelTaskGroup> targetGroups = otelCommonExecutor.getTargetGroups();
        targetGroups.forEach((target, group) -> {
            if (!group.hasTask()) {
                log.warn("No os metric tasks configured for group {}", target);
                return;
            }
            // start schedule
            startGroupTaskOfOsMetricExecutor(target, group);
        });
    }

    private void startGroupTaskOfOsMetricExecutor(GroupKey key, OtelTaskGroup group) {
        log.info("Starting group task: {}:{}", key, group.getTaskIds());
        group.startGroupTask(new OsMetricsCollectorTask(group));
    }

    @Override
    public void cancel(Long taskId) {
        otelCommonExecutor.cancal(taskId);
    }

    /**
     * Stop the executor.
     *
     * @param event the shutdown event
     */
    void onStop(@Observes ShutdownEvent event) {
        otelCommonExecutor.stop();
    }

    @Override
    public boolean hasTaskRunning() {
        return otelCommonExecutor.hasTaskRunning();
    }

    static class OsMetricsCollectorTask implements Runnable {
        private final GroupKey groupKey;
        private final OtelTaskGroup group;

        public OsMetricsCollectorTask(OtelTaskGroup group) {
            this.groupKey = group.getGroupKey();
            this.group = group;
        }

        @Override
        public void run() {
            MeterProviderContext meterProviderContext = group.getMeterProviderContext();
            if (meterProviderContext == null) {
                log.warn("MeterProviderContext is null for group {}", groupKey);
                return;
            }
            Map<String, Double> dynamicMetrics = new ConcurrentHashMap<>();
            group.getTasks().forEach((taskId, taskDefinition) -> {
                collectTaskMetrics(taskDefinition, dynamicMetrics);
            });
            meterProviderContext.refreshDynamicMetrics(dynamicMetrics);
            log.info("Successfully collected {} os metrics for group [{}, tasks={}] collectors", dynamicMetrics.size(),
                groupKey, group.getTaskIds());
            meterProviderContext.exportMetrics();
        }

        private void collectTaskMetrics(TaskDefinition taskDefinition, Map<String, Double> dynamicMetrics) {
            if (StrUtil.isNotEmpty(taskDefinition.getOperateObj())) {
                log.warn("not support os task collect {},it must be metric collect {} .  task={}",
                    taskDefinition.getTaskName(), taskDefinition.getOperateObj(), taskDefinition.getTaskId());
            }
            List<TaskMetricsDefinitionVo> metricsDefinitionList = taskDefinition.getMetricsDefinitionList();
            for (TaskMetricsDefinitionVo definitionVo : metricsDefinitionList) {
                OsCmdResult res = OsCommandUtils.execute(definitionVo.getCollectCmd());
                if (res.isSuccess()) {
                    dynamicMetrics.put(definitionVo.getName(), MathUtils.doubleValueOf(res.output()));
                } else {
                    log.error("Failed to collect os metric {} {} for task {}: {}", definitionVo.getName(),
                        definitionVo.getCollectCmd(), taskDefinition.getTaskId(), res.output());
                }
            }
        }
    }
}

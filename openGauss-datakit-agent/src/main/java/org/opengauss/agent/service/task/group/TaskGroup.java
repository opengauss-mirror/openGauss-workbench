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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Response;

import org.apache.http.client.utils.URIBuilder;
import org.opengauss.agent.client.DynamicHttpClientBuilder;
import org.opengauss.agent.client.MetricHttpClient;
import org.opengauss.agent.config.AppConfig;
import org.opengauss.agent.entity.TaskDefinition;
import org.opengauss.agent.entity.task.TaskMetricsDefinitionVo;
import org.opengauss.agent.exception.TaskExecutionException;
import org.opengauss.agent.vo.MetricKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TaskGroup
 *
 * @author: wangchao
 * @Date: 2025/5/16 16:44
 * @since 7.0.0-RC2
 **/
@Slf4j
public class TaskGroup {
    /**
     * TaskGroup agentId
     */
    protected static final String PARAM_AGENT = "agentId";

    /**
     * TaskGroup taskIds
     */
    protected static final String PARAM_TASK = "taskIds";

    private ScheduledExecutorService groupScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> collectionTask = null;
    @Getter
    private final GroupKey groupKey;
    @Getter
    private Map<Long, TaskDefinition> tasks = new ConcurrentHashMap<>();
    private MetricHttpClient metricHttpClient;
    private Map<String, Object> params = new HashMap<>();
    private String httpTarget;

    /**
     * Constructor for OtelTaskGroup
     *
     * @param key group key
     */
    public TaskGroup(GroupKey key) {
        this.groupKey = key;
    }

    /**
     * Create context
     *
     * @param appConfig appConfig
     * @throws TaskExecutionException TaskExecutionException
     */
    public void createContext(AppConfig appConfig) throws TaskExecutionException {
        try {
            if (metricHttpClient != null) {
                return;
            }
            httpTarget = new URIBuilder(appConfig.getAppServerUrl()).setPath(groupKey.getDataSendTarget())
                .build()
                .toString();
            metricHttpClient = DynamicHttpClientBuilder.createHttpClient(httpTarget);
            params.put(PARAM_TASK, new ArrayList<>(tasks.keySet()));
            params.put(PARAM_AGENT, appConfig.getAgentId());
            log.info("create Context httpTarget={},params={}", httpTarget, params);
        } catch (Exception e) {
            throw new TaskExecutionException("Invalid create MeterProviderContext : " + e.getMessage());
        }
    }

    /**
     * Send data
     *
     * @param taskId taskId
     * @param dataList dataList
     */
    public void sendData(Long taskId, List<Map<String, Object>> dataList) {
        try {
            Call<Void> voidCall = metricHttpClient.sendDataMetrics(httpTarget, taskId, getAgentId(), dataList);
            log.debug("Export http info | {}: {}", httpTarget, taskId);
            Response<Void> response = voidCall.execute(); // 同步执行（或使用enqueue异步）
            if (!response.isSuccessful()) {
                log.error("Export failed | Code: {}", response.code());
            }
        } catch (IOException e) {
            log.error("Export http error | {}:{}: {}", httpTarget, taskId, e.getMessage());
        }
    }

    private Long getAgentId() {
        Object object = params.get(PARAM_AGENT);
        if (object == null) {
            throw new TaskExecutionException("Invalid create MeterProviderContext : agentId is null");
        }
        if (object instanceof Long) {
            return (Long) object;
        } else {
            throw new TaskExecutionException("Invalid create MeterProviderContext : agentId is not Long");
        }
    }

    /**
     * Refresh context
     *
     * @throws TaskExecutionException TaskExecutionException
     */
    public void refreshContext() throws TaskExecutionException {
        try {
            params.remove(PARAM_TASK);
            params.put(PARAM_TASK, new ArrayList<>(tasks.keySet()));
        } catch (Exception e) {
            throw new TaskExecutionException("Invalid create MeterProviderContext : " + e.getMessage());
        }
    }

    /**
     * Get taskIds
     *
     * @return Set<Long> taskIds
     */
    public Set<Long> getTaskIds() {
        return tasks.keySet();
    }

    /**
     * task is empty
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return !tasks.isEmpty();
    }

    /**
     * add task
     *
     * @param task TaskDefinition
     */
    public void addTask(TaskDefinition task) {
        tasks.put(task.getTaskId(), task);
    }

    /**
     * Remove task
     *
     * @param taskId taskId
     */
    public void removeTask(Long taskId) {
        tasks.remove(taskId);
    }

    /**
     * Start the group task
     *
     * @param runnable the runnable task
     */
    public void startGroupTask(Runnable runnable) {
        if (this.collectionTask == null) {
            log.info("Started metric collection schedule for group {} ,tasks={}", groupKey, tasks.keySet());
            // 按组周期启动定时收集
            this.collectionTask = groupScheduler.scheduleAtFixedRate(runnable, initialDelay(), groupKey.getPeriod(),
                TimeUnit.MILLISECONDS);
        }
    }

    /**
     * half of group period as initial delay
     *
     * @return initial delay
     */
    private long initialDelay() {
        return groupKey.getPeriod() / 2;
    }

    /**
     * check if the group has task
     *
     * @return boolean
     */
    public boolean hasTask() {
        return !tasks.isEmpty();
    }

    /**
     * stop periodic collection
     */
    public void stopPeriodicCollection() {
        if (collectionTask != null) {
            collectionTask.cancel(false);
        }
        if (groupScheduler != null) {
            groupScheduler.shutdownNow();
            try {
                if (!groupScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    groupScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                groupScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if (tasks != null) {
            tasks.clear();
        }
    }

    /**
     * Get the merged collector list
     *
     * @return collector list
     */
    public List<String> getMergedCollectorList() {
        return Optional.ofNullable(tasks)
            .orElseGet(Collections::emptyMap)
            .values()
            .stream()
            .flatMap(task -> task.getCollectorList().stream())
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Get the collector list of the task
     *
     * @param taskId task id
     * @return collector list
     */
    public List<String> getCollectorList(Long taskId) {
        return tasks.get(taskId).getCollectorList();
    }

    /**
     * Get the task metric keys
     *
     * @return task metric keys
     */
    public Map<Long, List<MetricKey>> getTaskMetricKeys() {
        return tasks.entrySet()
            .stream()
            .filter(entry -> entry.getKey() != null && entry.getValue() != null)
            .flatMap(entry -> {
                final Long taskId = entry.getKey();
                final List<TaskMetricsDefinitionVo> taskMetricDefs = entry.getValue().getMetricsDefinitionList();
                if (CollUtil.isEmpty(taskMetricDefs)) {
                    return Stream.empty();
                }
                return taskMetricDefs.stream()
                    .filter(Objects::nonNull)
                    .map(metric -> Pair.of(taskId, MetricKey.of(metric.getName(), metric.getProp())));
            })
            .collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())));
    }

    /**
     * Validate the task consistency
     *
     * @param newTask new task
     * @return true if consistent, false otherwise
     */
    public boolean validateTaskConsistency(TaskDefinition newTask) {
        return Optional.ofNullable(tasks)
            .orElseGet(Collections::emptyMap)
            .values()
            .stream()
            .allMatch(existingTask -> existingTask.getPeriod() == newTask.getPeriod() && existingTask.getStoragePolicy()
                .equals(newTask.getStoragePolicy()));
    }

    /**
     * Get all metrics from all tasks
     *
     * @return a list of metrics
     */
    public List<TaskMetricsDefinitionVo> getMergedMetrics() {
        return new ArrayList<>(Optional.ofNullable(tasks)
            .orElseGet(Collections::emptyMap)
            .values()
            .stream()
            .flatMap(task -> task.getMetricsDefinitionList().stream())
            .collect(Collectors.toMap(TaskMetricsDefinitionVo::getName, Function.identity(), (a, b) -> a))
            .values());
    }
}

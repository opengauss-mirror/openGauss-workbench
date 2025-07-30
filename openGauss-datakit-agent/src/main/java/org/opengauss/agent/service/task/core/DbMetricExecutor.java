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
import org.opengauss.agent.entity.task.AgentClusterVo;
import org.opengauss.agent.entity.task.TaskMetricsDefinitionVo;
import org.opengauss.agent.enums.DatabaseType;
import org.opengauss.agent.exception.TaskExecutionException;
import org.opengauss.agent.service.task.TaskExecutor;
import org.opengauss.agent.service.task.group.GroupKey;
import org.opengauss.agent.service.task.group.OtelTaskGroup;
import org.opengauss.agent.utils.RsaUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * DbMetricExecutor
 *
 * @author: wangchao
 * @Date: 2025/4/9 10:35
 * @Description: DbMetricExecutor
 * @since 7.0.0-RC2
 **/
@Slf4j
@ApplicationScoped
public class DbMetricExecutor implements TaskExecutor {
    OtelCommonExecutor otelCommonExecutor;
    @Inject
    AppConfig appConfig;

    /**
     * initialize DbMetricExecutor instance when application start
     *
     * @param event startup event
     */
    void onStart(@Observes StartupEvent event) {
        otelCommonExecutor = new OtelCommonExecutor(appConfig, log);
    }

    @Override
    public void initialize(TaskDefinition task) throws TaskExecutionException {
        log.info("initialize task: {}", task);
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
            // start schedule
            startGroupTask(target, group);
        });
    }

    private void startGroupTask(GroupKey key, OtelTaskGroup group) {
        log.info("Starting group task: {}:{}", key, group.getTaskIds());
        group.startGroupTask(new DbMetricsCollectorTask(group, key));
    }

    static class DbMetricsCollectorTask implements Runnable {
        private final OtelTaskGroup group;
        private final GroupKey key;

        /**
         * DbMetricsCollectorTask
         *
         * @param group OtelTaskGroup
         * @param key GroupKey
         */
        DbMetricsCollectorTask(OtelTaskGroup group, GroupKey key) {
            this.group = group;
            this.key = key;
        }

        @Override
        public void run() {
            MeterProviderContext meterProviderContext = group.getMeterProviderContext();
            if (meterProviderContext == null) {
                log.warn("MeterProviderContext is null for group {}", key);
                return;
            }
            Map<String, Double> dynamicMetrics = new ConcurrentHashMap<>();
            group.getTasks().forEach((taskId, taskDefinition) -> {
                collectSingleTaskMetrics(key, taskId, taskDefinition, dynamicMetrics);
            });
            meterProviderContext.refreshDynamicMetrics(dynamicMetrics);
            log.info("Successfully collected {} db metrics for group [{}, tasks={}] collectors", dynamicMetrics.size(),
                key, group.getTaskIds());
            meterProviderContext.exportMetrics();
        }

        /**
         * collect metrics for single task
         *
         * @param key group key
         * @param taskId task id
         * @param taskDefinition task definition
         * @param dynamicMetrics dynamic metrics
         */
        private void collectSingleTaskMetrics(GroupKey key, Long taskId, TaskDefinition taskDefinition,
            Map<String, Double> dynamicMetrics) {
            AgentClusterVo clusterConfig = taskDefinition.getClusterConfig();
            try (Connection connection = createConnection(clusterConfig);) {
                processMetricsDefinition(taskDefinition, connection, dynamicMetrics);
            } catch (SQLException e) {
                handleDatabaseError(key, taskId, clusterConfig, e);
            } catch (Exception ex) {
                log.error("Metric collection failed for group {}", key.getDataSendTarget(), ex);
            }
        }

        /**
         * handle metrics definition
         *
         * @param taskDefinition task definition
         * @param connection database connection
         * @param dynamicMetrics dynamic metrics
         */
        private void processMetricsDefinition(TaskDefinition taskDefinition, Connection connection,
            Map<String, Double> dynamicMetrics) {
            List<TaskMetricsDefinitionVo> list = taskDefinition.getMetricsDefinitionList();
            list.forEach(metric -> {
                processMetric(connection, metric, dynamicMetrics);
            });
        }

        /**
         * execute metric collect sql and collect metrics
         *
         * @param connection database connection
         * @param metricDef metric definition
         * @param dynamicMetrics dynamic metrics
         */
        private void processMetric(Connection connection, TaskMetricsDefinitionVo metricDef,
            Map<String, Double> dynamicMetrics) {
            String name = metricDef.getName();
            String cmd = metricDef.getCollectCmd();
            try (PreparedStatement ps = connection.prepareStatement(cmd); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dynamicMetrics.put(name, rs.getDouble(metricDef.getFieldName()));
                } else {
                    log.warn("No data queried for metrics: {}", metricDef);
                }
            } catch (SQLException e) {
                handleMetricError(name, e);
            }
        }

        private void handleMetricError(String name, SQLException e) {
            log.error("Metric collection error: Metric: {} Error Code: {}  SQL State: {}  Message: {}", name,
                e.getErrorCode(), e.getSQLState(), e.getMessage());
        }

        private void handleDatabaseError(GroupKey key, Long taskId, AgentClusterVo clusterConfig, SQLException e) {
            log.error("Database error occurred: Group: {} Task ID: {} Database: {} URL: {} "
                    + "Error Code: {} SQL State: {} Message: {}", key, taskId, clusterConfig.getDataBaseType(),
                clusterConfig.getUrl(), e.getErrorCode(), e.getSQLState(), e.getMessage());
        }

        /**
         * Create a connection to the database.
         *
         * @param clusterConfig the cluster configuration
         * @return the connection
         * @throws SQLException if a database access error occurs
         */
        private Connection createConnection(AgentClusterVo clusterConfig) throws SQLException {
            return DatabaseType.valueOf(clusterConfig.getDataBaseType())
                .getConnection(clusterConfig.getUrl(), clusterConfig.getUsername(),
                    RsaUtils.decrypt(clusterConfig.getDbPassword()));
        }
    }

    /**
     * Stop the executor.
     *
     * @param event the shutdown event
     */
    void onStop(@Observes ShutdownEvent event) {
        otelCommonExecutor.stop();
        DatabaseType.shutdownAll();
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

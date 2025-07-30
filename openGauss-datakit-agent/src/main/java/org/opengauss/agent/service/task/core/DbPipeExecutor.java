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

import cn.hutool.core.collection.CollUtil;
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
import org.opengauss.agent.service.task.group.TaskGroup;
import org.opengauss.agent.utils.RsaUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * DbPipeExecutor
 *
 * @author: wangchao
 * @Date: 2025/4/9 10:43
 * @Description: SqlPipelineExecutor
 * @since 7.0.0-RC2
 **/
@Slf4j
@ApplicationScoped
public class DbPipeExecutor implements TaskExecutor {
    @Inject
    AppConfig appConfig;
    private CommonExecutor commonExecutor;

    /**
     * initialize DbPipeExecutor instance when application start
     *
     * @param event startup event
     */
    void onStart(@Observes StartupEvent event) {
        commonExecutor = new CommonExecutor(appConfig, log);
    }

    @Override
    public void initialize(TaskDefinition task) throws TaskExecutionException {
        commonExecutor.initialize(task);
    }

    @Override
    public void execute() throws TaskExecutionException {
        ConcurrentMap<GroupKey, TaskGroup> targetGroups = commonExecutor.getTargetGroups();
        targetGroups.forEach((groupKey, group) -> {
            if (!group.hasTask()) {
                log.warn("No tasks configured for group {}", groupKey);
                return;
            }
            // start schedule
            group.startGroupTask(new DatabasePipeTask(groupKey, group));
        });
    }

    @Override
    public void cancel(Long taskId) {
        commonExecutor.cancal(taskId);
    }

    @Override
    public boolean hasTaskRunning() {
        return false;
    }

    static class DatabasePipeTask implements Runnable {
        private static final String NAN = "NaN";

        private final GroupKey groupKey;
        private final TaskGroup group;

        public DatabasePipeTask(GroupKey groupKey, TaskGroup group) {
            this.groupKey = groupKey;
            this.group = group;
        }

        @Override
        public void run() {
            group.getTasks().forEach((taskId, taskDefinition) -> {
                List<Map<String, Object>> taskPipeData = collectSingleTaskMetrics(taskId, taskDefinition);
                pushDataToAgentPipe(taskId, taskPipeData);
                log.info("Successfully collected {} db metrics for group [{}, task={}] collectors", taskPipeData.size(),
                    groupKey, taskId);
            });
        }

        private void pushDataToAgentPipe(Long taskId, List<Map<String, Object>> taskPipeData) {
            if (CollUtil.isNotEmpty(taskPipeData)) {
                group.sendData(taskId, taskPipeData);
            }
        }

        /**
         * collect metrics for single task
         *
         * @param taskId task id
         * @param taskDefinition task definition
         * @return metrics data list
         */
        private List<Map<String, Object>> collectSingleTaskMetrics(Long taskId, TaskDefinition taskDefinition) {
            AgentClusterVo clusterConfig = taskDefinition.getClusterConfig();
            List<Map<String, Object>> dataList = new LinkedList<>();
            try (Connection connection = createConnection(clusterConfig);) {
                processMetricsDefinition(taskDefinition, connection, dataList);
            } catch (SQLException e) {
                handleDatabaseError(taskId, clusterConfig, e);
            } catch (Exception ex) {
                log.error("Pipe Metric collection failed for group {}", groupKey.getDataSendTarget(), ex);
            }
            return dataList;
        }

        private void handleDatabaseError(Long taskId, AgentClusterVo clusterConfig, SQLException e) {
            log.error("Database error occurred: Group: {} Task ID: {} Database: {} URL: {} "
                    + "Error Code: {} SQL State: {} Message: {}", groupKey, taskId, clusterConfig.getDataBaseType(),
                clusterConfig.getUrl(), e.getErrorCode(), e.getSQLState(), e.getMessage());
        }

        /**
         * handle metrics definition
         *
         * @param taskDefinition task definition
         * @param connection database connection
         * @param dataList dataList
         */
        private void processMetricsDefinition(TaskDefinition taskDefinition, Connection connection,
            List<Map<String, Object>> dataList) {
            List<TaskMetricsDefinitionVo> list = taskDefinition.getMetricsDefinitionList();
            String execQuerySql = taskDefinition.getOperateObj();
            try (PreparedStatement ps = connection.prepareStatement(execQuerySql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> rowData = createRowData(list, rs);
                    dataList.add(rowData);
                }
            } catch (SQLException e) {
                handleMetricError(taskDefinition.getTaskName(), e);
            }
        }

        private Map<String, Object> createRowData(List<TaskMetricsDefinitionVo> metricsDefinitions, ResultSet rs)
            throws SQLException {
            Map<String, Object> rowData = new HashMap<>();
            for (TaskMetricsDefinitionVo metricDef : metricsDefinitions) {
                Object value = getValueFromResultSet(metricDef, rs);
                rowData.put(metricDef.getName(), Optional.ofNullable(value).orElse(NAN));
            }
            return rowData;
        }

        private Object getValueFromResultSet(TaskMetricsDefinitionVo metricDef, ResultSet rs) {
            String fieldName = metricDef.getFieldName();
            String dataType = Optional.ofNullable(metricDef.getDataType()).orElse("STRING").toUpperCase(Locale.ROOT);
            Object result = null;
            try {
                result = switch (dataType) {
                    case "INT" -> rs.getInt(fieldName);
                    case "LONG" -> rs.getLong(fieldName);
                    case "DOUBLE" -> rs.getDouble(fieldName);
                    case "STRING" -> rs.getString(fieldName);
                    default -> rs.getObject(fieldName);
                };
            } catch (SQLException e) {
                log.error("Error retrieving value for task.field -> {}.{} :{}", metricDef.getName(), fieldName,
                    e.getMessage());
            }
            return result;
        }

        private void handleMetricError(String name, SQLException e) {
            log.error("Pipe Metric collection error: task: {} Error Code: {}  SQL State: {}  Message: {}", name,
                e.getErrorCode(), e.getSQLState(), e.getMessage());
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
}

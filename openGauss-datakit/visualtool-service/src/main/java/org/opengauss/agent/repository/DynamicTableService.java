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

package org.opengauss.agent.repository;

import static org.opengauss.admin.common.constant.AgentConstants.Field.COMMON_FIELD_LIST;
import static org.opengauss.admin.common.constant.AgentConstants.Field.FINGERPRINT_TABLE_REQUIRED_FIELD;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.common.core.domain.entity.agent.TaskTemplateDefinition;
import org.opengauss.admin.common.enums.agent.StoragePolicy;
import org.opengauss.admin.common.exception.ops.AgentTaskException;
import org.opengauss.admin.system.base.DbDataLocation;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

/**
 * DynamicTableService
 *
 * @author: wangchao
 * @Date: 2025/5/26 16:25
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class DynamicTableService {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private TaskTableRelationService taskTableRelationService;

    @Resource
    private DbDataLocation dbDataLocation;

    /**
     * <pre>
     *     建表类型包含四种类型：
     *       实时数据类型表 real_data_type
     *       历史数据类型表 history_data_type
     *       树状结构数据表 tree_data_type
     *       指纹去除类型数据表 fingerprint_data_type
     *     表名规范： agent_pipe_${taskName}
     *    通用字段：
     *       task_id long
     *       agent_id long
     *       cluster_id String
     *       create_time timestamp
     * </pre>
     *
     * @param taskTemplate taskTemplate
     * @param columnList columnList
     */
    @Transactional
    public void createPipeDataStorageTable(TaskTemplateDefinition taskTemplate,
        List<TaskMetricsDefinition> columnList) {
        Objects.requireNonNull(columnList, "columnList must not be null");
        String nodeTableFields = buildTableColumnDefinition(columnList);
        String templateName = taskTemplate.getName();
        TableNameGenerator.validateTaskId(taskTemplate.getName());
        switch (taskTemplate.getStoragePolicy()) {
            case REAL_TIME:
                jdbcTemplate.execute(SqlTemplate.buildRealTimeTableSql(templateName, nodeTableFields,
                    dbDataLocation.getDataLocationEnum()));
                break;
            case HISTORY:
                jdbcTemplate.execute(SqlTemplate.buildHistoryTableSql(templateName, nodeTableFields,
                    dbDataLocation.getDataLocationEnum()));
                break;
            case FINGERPRINT:
                // fingerprint table definition ,must include the field: fingerprint_id
                // check if the column list contains the required field fingerprint_id
                boolean hasFingerprintId = columnList.stream()
                    .map(TaskMetricsDefinition::getFieldName)
                    .anyMatch(FINGERPRINT_TABLE_REQUIRED_FIELD::equalsIgnoreCase);
                // if the column list does not contain the required field, throw an exception
                if (!hasFingerprintId) {
                    String existFields = columnList.stream()
                        .map(TaskMetricsDefinition::getFieldName)
                        .collect(Collectors.joining(",", "[", "]"));
                    throw new AgentTaskException(String.format(
                        "fingerprint table definition must include the field: fingerprint_id, but found: %s",
                        existFields));
                }
                jdbcTemplate.execute(SqlTemplate.buildFingerprintTableSql(templateName, nodeTableFields,
                    dbDataLocation.getDataLocationEnum()));
                break;
            case TREE:
                // 校验任务ID合法性
                TableNameGenerator.validateTaskId(templateName);
                // 执行建表语句
                jdbcTemplate.execute(SqlTemplate.buildTreeTableSql(templateName, dbDataLocation.getDataLocationEnum()));
                jdbcTemplate.execute(SqlTemplate.buildTreeNodeTableSql(templateName, nodeTableFields,
                    dbDataLocation.getDataLocationEnum()));
                // 执行索引创建
                Arrays.stream(SqlTemplate.buildTreeIndexSql(templateName).split(";")).forEach(jdbcTemplate::execute);
                break;
            default:
                throw new IllegalArgumentException("Unsupported storage policy: " + taskTemplate.getStoragePolicy());
        }
        String realTableName = getTemplateRealTableName(templateName, taskTemplate.getStoragePolicy());
        log.info("database data location {}", dbDataLocation);
        taskTableRelationService.saveTaskTemplateTable(templateName, realTableName);
        log.info("create table {} success", realTableName);
    }

    private String buildTableColumnDefinition(List<TaskMetricsDefinition> columnList) {
        return columnList.stream()
            .filter(columnDef -> !COMMON_FIELD_LIST.contains(columnDef.getFieldName().toLowerCase(Locale.ROOT)))
            .map(columnDef -> columnDef.getFieldName() + " " + translateJavaDataTypeOpenGauss(columnDef.getDataType()))
            .collect(Collectors.joining(","));
    }

    private String translateJavaDataTypeOpenGauss(String dataType) {
        String openGaussDataType = "";
        switch (dataType) {
            case "integer":
                openGaussDataType = "INT";
                break;
            case "long":
                openGaussDataType = "BIGINT";
                break;
            case "float":
                openGaussDataType = "FLOAT";
                break;
            case "double":
                openGaussDataType = "DOUBLE";
                break;
            case "boolean":
                openGaussDataType = "BOOLEAN";
                break;
            case "date":
            case "timestamp":
                openGaussDataType = "TIMESTAMP";
                break;
            case "string":
            default:
                openGaussDataType = "VARCHAR(255)";
        }
        return openGaussDataType;
    }

    /**
     * check table exists
     *
     * @param name task name
     * @param storagePolicy storage policy
     * @return true if exists
     */
    public boolean checkTemplateTableExist(String name, StoragePolicy storagePolicy) {
        String realTableName = getTemplateRealTableName(name, storagePolicy);
        String sql = SqlTemplate.buildCheckTableExistsSql(realTableName, dbDataLocation.getDataLocationEnum());
        return jdbcTemplate.queryForObject(sql, Boolean.class) == true;
    }

    /**
     * get template real table name
     *
     * @param name task name
     * @param storagePolicy storage policy
     * @return real table name
     */
    public String getTemplateRealTableName(String name, StoragePolicy storagePolicy) {
        String realTableName = "";
        switch (storagePolicy) {
            case REAL_TIME:
                realTableName = TableNameGenerator.getRealTimeTableName(name);
                break;
            case HISTORY:
                realTableName = TableNameGenerator.getHistoryTableName(name);
                break;
            case FINGERPRINT:
                realTableName = TableNameGenerator.getFingerprintTableName(name);
                break;
            case TREE:
                realTableName = TableNameGenerator.getTreeTableName(name);
                break;
            default:
                realTableName = null;
        }
        return realTableName;
    }

    /**
     * insert data into pipe table
     *
     * @param sql insert sql
     * @param nameList task definition field name list,but not include common field
     * @param commonVal common field value
     * @param rowData row data
     * @return result id
     */
    public long saveHistoryRowData(String sql, List<String> nameList, Map<String, Object> commonVal,
        Map<String, Object> rowData) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.BATCH_ID));
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.TASK_ID));
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.AGENT_ID));
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.CLUSTER_NODE_ID));
            for (String columnDef : nameList) {
                setPreparedStatement(ps, index++, rowData.get(columnDef));
            }
            setPreparedStatement(ps, index, commonVal.getOrDefault(AgentConstants.Field.CREATE_TIME, Instant.now()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void setPreparedStatement(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value instanceof Long longVal) {
            ps.setLong(index, longVal);
        } else if (value instanceof String stringVal) {
            ps.setString(index, stringVal);
        } else if (value instanceof Instant instantVal) {
            ps.setTimestamp(index, Timestamp.from(instantVal));
        } else {
            ps.setObject(index, value);
        }
    }

    /**
     * insert data into pipe table
     *
     * @param sql insert sql
     * @param nameList task definition field name list,but not include common field
     * @param commonVal common field value
     * @param rowData row data
     */
    public void saveRealtimeRowData(String sql, List<String> nameList, Map<String, Object> commonVal,
        Map<String, Object> rowData) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.ID));
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.TASK_ID));
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.AGENT_ID));
            setPreparedStatement(ps, index++, commonVal.getOrDefault(AgentConstants.Field.CLUSTER_NODE_ID, ""));
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.ROW_ID));
            for (String columnDef : nameList) {
                setPreparedStatement(ps, index++, rowData.get(columnDef));
            }
            setPreparedStatement(ps, index, commonVal.getOrDefault(AgentConstants.Field.CREATE_TIME, Instant.now()));
            return ps;
        });
    }

    /**
     * check row exists
     *
     * @param existsSql exists sql
     * @param rowId row id
     * @return true if exists
     */
    public boolean checkRowExists(String existsSql, long rowId) {
        try {
            return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(existsSql, new Object[] {rowId}, (rs, rowNum) -> rs.getBoolean(1)));
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * update row data
     *
     * @param updateSql update sql
     * @param nameList task definition field name list,but not include common field
     * @param commonVal common field value
     * @param rowData row data
     */
    public void updateRowData(String updateSql, List<String> nameList, Map<String, Object> commonVal,
        Map<String, Object> rowData) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(updateSql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            for (String columnDef : nameList) {
                ps.setObject(index++, rowData.get(columnDef));
            }
            setPreparedStatement(ps, index++, commonVal.getOrDefault(AgentConstants.Field.CREATE_TIME, Instant.now()));
            setPreparedStatement(ps, index, commonVal.get(AgentConstants.Field.ID));
            return ps;
        });
    }

    /**
     * delete row data
     *
     * @param deleteSql delete sql
     * @param commonVal common field value
     */
    public void deleteRealtimeExpiredData(String deleteSql, Map<String, Object> commonVal) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(deleteSql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.TASK_ID));
            setPreparedStatement(ps, index++, commonVal.get(AgentConstants.Field.AGENT_ID));
            setPreparedStatement(ps, index, commonVal.getOrDefault(AgentConstants.Field.CLUSTER_NODE_ID, ""));
            return ps;
        });
    }

    /**
     * delete pipe history storage data by keep period
     * <pre>
     *     delete condition: task_id and create_time less than current time - keep_period
     *     delete sql: delete from table_name where task_id = ? and create_time < ?
     * </pre>
     *
     * @param deleteSql delete sql
     * @param commonProperty delete condition
     * @return delete count
     */
    public int deleteHistoryExpiredData(final String deleteSql, Map<String, Object> commonProperty) {
        return jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(deleteSql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            setPreparedStatement(ps, index++, commonProperty.get(AgentConstants.Field.TASK_ID));
            setPreparedStatement(ps, index,
                commonProperty.getOrDefault(AgentConstants.Field.CREATE_TIME, Instant.now()));
            return ps;
        });
    }
}
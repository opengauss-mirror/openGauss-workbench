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

package org.opengauss.agent.utils;

import static org.opengauss.admin.common.constant.AgentConstants.Default.NONE;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import org.apache.commons.codec.digest.MurmurHash3;
import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * AgentTaskUtils
 *
 * @author: wangchao
 * @Date: 2025/6/21 14:51
 * @since 7.0.0-RC2
 **/
public class AgentTaskUtils {
    private static final String TASK_NAME_TEMPLATE = "task_%s";

    /**
     * Generate a task name based on the task template name.
     *
     * @param taskTemplateName task template name
     * @return generated task name
     */
    public static String generateTaskName(String taskTemplateName) {
        return String.format(TASK_NAME_TEMPLATE, taskTemplateName);
    }

    /**
     * Get the task template ID based on the database type.
     *
     * @param dbTypeEnum database type
     * @return task template ID
     */
    public static String getDbTaskTemplateId(DbTypeEnum dbTypeEnum) {
        return Objects.equals(dbTypeEnum, DbTypeEnum.MYSQL)
            ? AgentConstants.TaskTemplate.MYSQL_MONITOR_TASK_TEMPLATE
            : AgentConstants.TaskTemplate.OPENGAUSS_MONITOR_TASK_TEMPLATE;
    }

    /**
     * Calculate the primary ID of a row based on the task ID, agent ID, cluster node ID, and row index.
     *
     * @param taskId task ID
     * @param agentId agent id
     * @param clusterNodeId cluster node id
     * @param rowIdx row index
     * @return primary ID of the row
     */
    public static long calcRowPrimaryId(Long taskId, String agentId, String clusterNodeId, long rowIdx) {
        return MurmurHash3.hash32x86((taskId + agentId + clusterNodeId + rowIdx).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Get the property value based on the metric name, attribute value map, and property key map.
     *
     * @param metricName metric name
     * @param attrValueMap attribute value map
     * @param propertyKeyMap property key map
     * @return property value
     */
    public static String getProperty(String metricName, Map<String, Object> attrValueMap,
        Map<String, String> propertyKeyMap) {
        if (CollUtil.isEmpty(attrValueMap) || CollUtil.isEmpty(propertyKeyMap)) {
            return NONE;
        }
        String key = propertyKeyMap.getOrDefault(metricName, AgentConstants.Default.EMPTY);
        if (StrUtil.equals(key, AgentConstants.Default.EMPTY)) {
            return NONE;
        }
        return attrValueMap.getOrDefault(key, NONE).toString();
    }
}

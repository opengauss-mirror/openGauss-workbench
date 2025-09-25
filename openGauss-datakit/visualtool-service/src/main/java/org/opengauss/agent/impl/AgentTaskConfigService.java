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

package org.opengauss.agent.impl;

import static org.opengauss.admin.common.constant.AgentConstants.Field.COMMON_FIELD_LIST;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskConfig;
import org.opengauss.agent.service.AgentTaskManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

/**
 * AgentTaskConfigService
 *
 * @author: wangchao
 * @Date: 2025/5/27 19:15
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class AgentTaskConfigService {
    private final ConcurrentMap<Long, AgentTaskConfig> taskConfigMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, List<String>> taskFieldNamesMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, List<String>> taskMetricNamesMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, String> taskTemplateNameMap = new ConcurrentHashMap<>();
    @Resource
    private AgentTaskManager agentTaskManager;

    /**
     * get agent task config
     *
     * @param taskId task id
     * @return agent task config
     */
    public AgentTaskConfig getAgentTaskConfig(Long taskId) {
        return taskConfigMap.compute(taskId, (k, v) -> {
            if (v == null) {
                AgentTaskConfig newConfig = agentTaskManager.queryAgentTaskInstance(taskId);
                if (newConfig == null) {
                    log.warn("taskId={} config not found", taskId);
                    return null;
                }
                return newConfig;
            }
            return v;
        });
    }

    /**
     * get agent task fields
     *
     * @param taskId task id
     * @return agent task fields
     */
    public List<String> getAgentTaskFields(Long taskId) {
        return taskFieldNamesMap.compute(taskId, (k, v) -> {
            if (v == null) {
                AgentTaskConfig taskConfig = getAgentTaskConfig(taskId);
                if (taskConfig == null) {
                    return null;
                }
                return taskConfig.getMetricsDefinitionList()
                    .stream()
                    .filter(columnDef -> !COMMON_FIELD_LIST.contains(columnDef.getFieldName().toLowerCase(Locale.ROOT)))
                    .map(TaskMetricsDefinition::getFieldName)
                    .collect(Collectors.toList());
            }
            return v;
        });
    }

    /**
     * get agent task metric names
     *
     * @param taskId task id
     * @return agent task metric names
     */
    public List<String> getAgentTaskNames(Long taskId) {
        return taskMetricNamesMap.compute(taskId, (k, v) -> {
            if (v == null) {
                AgentTaskConfig taskConfig = getAgentTaskConfig(taskId);
                if (taskConfig == null) {
                    return null;
                }
                return taskConfig.getMetricsDefinitionList()
                    .stream()
                    .filter(columnDef -> !COMMON_FIELD_LIST.contains(columnDef.getFieldName().toLowerCase(Locale.ROOT)))
                    .map(TaskMetricsDefinition::getName)
                    .collect(Collectors.toList());
            }
            return v;
        });
    }

    /**
     * get agent task template name
     *
     * @param taskId task id
     * @return agent task template name
     */
    public String getAgentTaskTemplateName(Long taskId) {
        return taskTemplateNameMap.compute(taskId, (k, v) -> {
            if (v == null) {
                AgentTaskConfig newConfig = agentTaskManager.queryAgentTaskInstance(taskId);
                if (newConfig == null) {
                    log.warn("taskId={} config not found", taskId);
                    return null;
                }
                return newConfig.getTemplateDefinition().getName();
            }
            return v;
        });
    }
}

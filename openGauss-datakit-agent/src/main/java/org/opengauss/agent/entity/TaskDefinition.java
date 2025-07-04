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

package org.opengauss.agent.entity;

import lombok.Data;

import org.opengauss.agent.entity.task.AgentClusterVo;
import org.opengauss.agent.entity.task.TaskMetricsDefinitionVo;
import org.opengauss.agent.enums.ObjectType;
import org.opengauss.agent.enums.StoragePolicy;
import org.opengauss.agent.enums.TaskType;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TaskDefinition
 *
 * @author: wangchao
 * @Date: 2025/4/8 15:44
 * @Description: TaskDefinition
 * @since 7.0.0-RC2
 **/
@Data
public class TaskDefinition {
    private Long taskId;
    private String taskName;
    private String agentId;
    private TaskType taskType;
    private String groupTag;
    private String pluginsTag;
    private ObjectType operateObjType;
    private String operateObj;
    private String receiveApi;
    private long period;
    private long estimatedExecutionTime;
    private TimeUnit unit;
    private StoragePolicy storagePolicy;
    private String dataSendTarget;
    private boolean isDbTemplate;
    private AgentClusterVo clusterConfig;
    private List<String> collectorList;
    private List<TaskMetricsDefinitionVo> metricsDefinitionList;
}

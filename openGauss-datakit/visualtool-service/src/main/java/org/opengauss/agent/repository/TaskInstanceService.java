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

package org.opengauss.agent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.agent.TaskInstanceEntity;
import org.opengauss.admin.common.core.domain.model.agent.AgentTaskStatus;
import org.opengauss.admin.system.mapper.agent.TaskInstanceMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * TaskInstanceService
 *
 * @author: wangchao
 * @Date: 2025/4/25 16:15
 * @Description: TaskInstanceService
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class TaskInstanceService extends ServiceImpl<TaskInstanceMapper, TaskInstanceEntity>
    implements IService<TaskInstanceEntity> {
    /**
     * batch update task status to failed
     *
     * @param taskIds task ids
     * @param errorMessage error message
     */
    public void batchUpdateTaskFailedStatus(List<Long> taskIds, String errorMessage) {
        LambdaUpdateWrapper<TaskInstanceEntity> updateWrapper = Wrappers.lambdaUpdate(TaskInstanceEntity.class);
        updateWrapper.set(TaskInstanceEntity::getTaskStatus, AgentTaskStatus.FAILED)
            .set(TaskInstanceEntity::getEndTime, Instant.now())
            .set(TaskInstanceEntity::getErrorMsg, errorMessage)
            .in(TaskInstanceEntity::getId, taskIds);
        update(updateWrapper);
        log.info("update agent task instance status to failed, taskId: {}", taskIds);
    }

    /**
     * batch update task status to success
     *
     * @param taskId task id
     */
    public void startTask(Long taskId) {
        LambdaUpdateWrapper<TaskInstanceEntity> updateWrapper = Wrappers.lambdaUpdate(TaskInstanceEntity.class);
        updateWrapper.set(TaskInstanceEntity::getTaskStatus, AgentTaskStatus.RUNNING)
            .set(TaskInstanceEntity::getStartTime, Instant.now())
            .eq(TaskInstanceEntity::getId, taskId);
        update(updateWrapper);
        log.info("update agent task instance status to running, taskId: {}", taskId);
    }

    /**
     * query agent task instance
     *
     * @param agentId agent id
     * @return task instance list
     */
    public List<TaskInstanceEntity> queryAgentTaskInstance(String agentId) {
        LambdaQueryWrapper<TaskInstanceEntity> wrapper = Wrappers.lambdaQuery(TaskInstanceEntity.class);
        wrapper.eq(TaskInstanceEntity::getAgentId, agentId);
        return list(wrapper);
    }

    /**
     * batch remove agent task instance
     *
     * @param agentId agent id
     * @return remove count
     */
    public int removeAgentAllTask(String agentId) {
        LambdaQueryWrapper<TaskInstanceEntity> wrapper = Wrappers.lambdaQuery(TaskInstanceEntity.class);
        wrapper.select(TaskInstanceEntity::getId);
        wrapper.eq(TaskInstanceEntity::getAgentId, agentId);
        List<TaskInstanceEntity> list = list(wrapper);
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        if (removeByIds(list.stream().map(TaskInstanceEntity::getId).toList())) {
            log.info("remove agent task instance by agent id: {} ,size={}", agentId, list.size());
            return list.size();
        } else {
            return 0;
        }
    }

    /**
     * remove agent task instance by cluster node id
     *
     * @param clusterNodeId cluster node id
     */
    public void removeAgentTaskByClusterNodeId(String clusterNodeId) {
        LambdaQueryWrapper<TaskInstanceEntity> wrapper = Wrappers.lambdaQuery(TaskInstanceEntity.class);
        wrapper.eq(TaskInstanceEntity::getClusterNodeId, clusterNodeId);
        remove(wrapper);
        log.info("remove agent task instance by cluster node id: {}", clusterNodeId);
    }
}

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

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.common.core.domain.entity.agent.TaskTableRelation;
import org.opengauss.admin.system.mapper.agent.TaskTableRelationMapper;
import org.springframework.stereotype.Service;

/**
 * AgentClusterRelationService
 *
 * @author: wangchao
 * @Date: 2025/4/25 16:10
 * @Description: AgentClusterRelationService
 * @since 7.0.0-RC2
 **/
@Service
public class TaskTableRelationService extends ServiceImpl<TaskTableRelationMapper, TaskTableRelation>
    implements IService<TaskTableRelation> {
    /**
     * save task template table
     *
     * @param templateName templateName
     * @param realTableName realTableName
     */
    public void saveTaskTemplateTable(String templateName, String realTableName) {
        TaskTableRelation taskTableRelation = new TaskTableRelation();
        taskTableRelation.setTaskId(templateName);
        taskTableRelation.setTaskTableName(realTableName);
        saveOrUpdate(taskTableRelation);
    }
}

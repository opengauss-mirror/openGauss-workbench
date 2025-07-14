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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.common.core.domain.entity.agent.TaskTemplateDefinition;
import org.opengauss.admin.common.enums.agent.TaskType;
import org.opengauss.admin.system.mapper.agent.TaskTemplateDefinitionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TaskTemplateDefinitionService
 *
 * @author: wangchao
 * @Date: 2025/4/25 16:23
 * @Description: TaskTemplateDefinitionService
 * @since 7.0.0-RC2
 **/
@Service
public class TaskTemplateDefinitionService extends ServiceImpl<TaskTemplateDefinitionMapper, TaskTemplateDefinition>
    implements IService<TaskTemplateDefinition> {
    /**
     * queryPipeTaskTemplateDefinition
     *
     * @return List<TaskTemplateDefinition>
     */
    public List<TaskTemplateDefinition> queryPipeTaskTemplateDefinition() {
        LambdaQueryWrapper<TaskTemplateDefinition> wrapper = Wrappers.lambdaQuery(TaskTemplateDefinition.class);
        wrapper.in(TaskTemplateDefinition::getType, TaskType.DB_PIPE, TaskType.OS_PIPE);
        return list(wrapper);
    }
}

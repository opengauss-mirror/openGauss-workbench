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

import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.system.mapper.agent.TaskMetricsDefinitionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TaskMetricsDefinitionService
 *
 * @author: wangchao
 * @Date: 2025/4/25 16:18
 * @Description: TaskMetricsDefinitionService
 * @since 7.0.0-RC2
 **/
@Service
public class TaskMetricsDefinitionService extends ServiceImpl<TaskMetricsDefinitionMapper, TaskMetricsDefinition>
    implements IService<TaskMetricsDefinition> {
    /**
     * query task metrics definition by name in list
     *
     * @param collectMetric collect metric
     * @return task metrics definition list
     */
    public List<TaskMetricsDefinition> findByNameIn(List<String> collectMetric) {
        LambdaQueryWrapper<TaskMetricsDefinition> queryWrapper = Wrappers.lambdaQuery(TaskMetricsDefinition.class)
            .in(TaskMetricsDefinition::getName, collectMetric);
        return list(queryWrapper);
    }
}

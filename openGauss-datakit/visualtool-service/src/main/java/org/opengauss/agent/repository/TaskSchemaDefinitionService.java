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

import cn.hutool.core.util.StrUtil;

import org.opengauss.admin.common.core.domain.entity.agent.TaskSchemaDefinition;
import org.opengauss.admin.system.mapper.agent.TaskSchemaDefinitionMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TaskSchemaDefinitionService
 *
 * @author: wangchao
 * @Date: 2025/4/25 16:21
 * @Description: TaskSchemaDefinitionService
 * @since 7.0.0-RC2
 **/
@Service
public class TaskSchemaDefinitionService extends ServiceImpl<TaskSchemaDefinitionMapper, TaskSchemaDefinition>
    implements IService<TaskSchemaDefinition> {
    /**
     * query task schema definition by name in list
     *
     * @param collectMetric collectMetric
     * @return list
     */
    public List<String> getMetricsByName(String collectMetric) {
        if (StrUtil.isBlank(collectMetric)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<TaskSchemaDefinition> queryWrapper = Wrappers.lambdaQuery(TaskSchemaDefinition.class)
            .select(TaskSchemaDefinition::getMetric)
            .eq(TaskSchemaDefinition::getName, collectMetric);
        return baseMapper.selectList(queryWrapper)
            .stream()
            .map(TaskSchemaDefinition::getMetric)
            .distinct()
            .collect(Collectors.toList());
    }
}

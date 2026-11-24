/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.ReverseMigrationProgress;
import org.opengauss.admin.plugin.mapper.ReverseMigrationProgressMapper;
import org.opengauss.admin.plugin.service.ReverseMigrationProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Reverse migration progress service implementation
 *
 * @since 2025/06/23
 */
@Service
public class ReverseMigrationProgressServiceImpl
        extends ServiceImpl<ReverseMigrationProgressMapper, ReverseMigrationProgress>
        implements ReverseMigrationProgressService {
    @Autowired
    private ReverseMigrationProgressMapper mapper;

    @Override
    public void updateByTaskId(ReverseMigrationProgress reverseMigrationProgress) {
        LambdaQueryWrapper<ReverseMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReverseMigrationProgress::getTaskId, reverseMigrationProgress.getTaskId());
        if (mapper.selectCount(queryWrapper) > 0) {
            mapper.update(reverseMigrationProgress, queryWrapper);
        } else {
            mapper.insert(reverseMigrationProgress);
        }
    }

    @Override
    public ReverseMigrationProgress getOneByTaskId(Integer taskId) {
        LambdaQueryWrapper<ReverseMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReverseMigrationProgress::getTaskId, taskId);
        return mapper.selectOne(queryWrapper);
    }
}

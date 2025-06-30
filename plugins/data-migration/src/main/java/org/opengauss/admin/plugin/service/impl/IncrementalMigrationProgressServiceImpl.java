/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.IncrementalMigrationProgress;
import org.opengauss.admin.plugin.mapper.IncrementalMigrationProgressMapper;
import org.opengauss.admin.plugin.service.IncrementalMigrationProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Incremental migration progress service implementation
 *
 * @since 2025/06/23
 */
@Service
public class IncrementalMigrationProgressServiceImpl
        extends ServiceImpl<IncrementalMigrationProgressMapper, IncrementalMigrationProgress>
        implements IncrementalMigrationProgressService {
    @Autowired
    private IncrementalMigrationProgressMapper mapper;

    @Override
    public void updateByTaskId(IncrementalMigrationProgress incrementalMigrationProgress) {
        LambdaQueryWrapper<IncrementalMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IncrementalMigrationProgress::getTaskId, incrementalMigrationProgress.getTaskId());
        if (mapper.selectCount(queryWrapper) > 0) {
            mapper.update(incrementalMigrationProgress, queryWrapper);
        } else {
            mapper.insert(incrementalMigrationProgress);
        }
    }

    @Override
    public IncrementalMigrationProgress getOneByTaskId(Integer taskId) {
        LambdaQueryWrapper<IncrementalMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IncrementalMigrationProgress::getTaskId, taskId);
        return mapper.selectOne(queryWrapper);
    }
}

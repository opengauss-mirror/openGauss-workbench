/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.FullMigrationProgress;
import org.opengauss.admin.plugin.enums.FullMigrationDbObjEnum;
import org.opengauss.admin.plugin.mapper.FullMigrationProgressMapper;
import org.opengauss.admin.plugin.service.FullMigrationProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Full migration progress service
 *
 * @since 2025/06/23
 */
@Service
public class FullMigrationProgressServiceImpl extends ServiceImpl<FullMigrationProgressMapper, FullMigrationProgress>
        implements FullMigrationProgressService {
    @Autowired
    private FullMigrationProgressMapper mapper;

    @Override
    public void deleteByTaskIdAndObjectType(Integer taskId, FullMigrationDbObjEnum objectType) {
        LambdaQueryWrapper<FullMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FullMigrationProgress::getTaskId, taskId);
        queryWrapper.eq(FullMigrationProgress::getObjectType, objectType.getObjectType());
        mapper.delete(queryWrapper);
    }

    @Override
    public List<FullMigrationProgress> getListByTaskIdAndObjectType(Integer taskId, FullMigrationDbObjEnum objectType) {
        LambdaQueryWrapper<FullMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FullMigrationProgress::getTaskId, taskId);
        queryWrapper.eq(FullMigrationProgress::getObjectType, objectType.getObjectType());
        return mapper.selectList(queryWrapper);
    }
}

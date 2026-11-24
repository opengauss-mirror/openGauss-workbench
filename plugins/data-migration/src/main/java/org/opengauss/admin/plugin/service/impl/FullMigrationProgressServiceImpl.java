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
    public void deleteByTaskIdAndStatus(Integer taskId, Integer status) {
        LambdaQueryWrapper<FullMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FullMigrationProgress::getTaskId, taskId);
        queryWrapper.eq(FullMigrationProgress::getStatus, status);
        mapper.delete(queryWrapper);
    }

    @Override
    public void deleteByTaskIdAndObjectType(Integer taskId, FullMigrationDbObjEnum objectType) {
        LambdaQueryWrapper<FullMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FullMigrationProgress::getTaskId, taskId);
        queryWrapper.eq(FullMigrationProgress::getObjectType, objectType.getObjectType());
        mapper.delete(queryWrapper);
    }

    @Override
    public List<FullMigrationProgress> getListByTaskId(Integer taskId) {
        LambdaQueryWrapper<FullMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FullMigrationProgress::getTaskId, taskId);
        return mapper.selectList(queryWrapper);
    }

    @Override
    public List<FullMigrationProgress> getListByTaskIdAndObjectType(Integer taskId, FullMigrationDbObjEnum objectType) {
        LambdaQueryWrapper<FullMigrationProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FullMigrationProgress::getTaskId, taskId);
        queryWrapper.eq(FullMigrationProgress::getObjectType, objectType.getObjectType());
        return mapper.selectList(queryWrapper);
    }
}

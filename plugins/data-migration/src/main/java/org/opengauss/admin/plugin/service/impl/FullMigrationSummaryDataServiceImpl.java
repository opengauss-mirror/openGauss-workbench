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
import org.opengauss.admin.plugin.domain.FullMigrationSummaryData;
import org.opengauss.admin.plugin.mapper.FullMigrationSummaryDataMapper;
import org.opengauss.admin.plugin.service.FullMigrationSummaryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Full migration summary data service implementation
 *
 * @since 2025/06/23
 */
@Service
public class FullMigrationSummaryDataServiceImpl
        extends ServiceImpl<FullMigrationSummaryDataMapper, FullMigrationSummaryData>
        implements FullMigrationSummaryDataService {
    @Autowired
    private FullMigrationSummaryDataMapper mapper;

    @Override
    public void updateByTaskId(FullMigrationSummaryData summaryData) {
        LambdaQueryWrapper<FullMigrationSummaryData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FullMigrationSummaryData::getTaskId, summaryData.getTaskId());
        if (mapper.selectCount(queryWrapper) > 0) {
            mapper.update(summaryData, queryWrapper);
        } else {
            mapper.insert(summaryData);
        }
    }

    @Override
    public FullMigrationSummaryData getOneByTaskId(Integer taskId) {
        LambdaQueryWrapper<FullMigrationSummaryData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FullMigrationSummaryData::getTaskId, taskId);
        return mapper.selectOne(queryWrapper);
    }
}

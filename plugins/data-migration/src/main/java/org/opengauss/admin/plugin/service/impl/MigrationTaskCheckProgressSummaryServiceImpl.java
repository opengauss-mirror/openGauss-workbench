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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.plugin.domain.MigrationTaskCheckProgressSummary;
import org.opengauss.admin.plugin.mapper.MigrationTaskCheckProgressSummaryMapper;
import org.opengauss.admin.plugin.service.MigrationTaskCheckProgressSummaryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MigrationTaskCheckProgressSummaryService
 *
 * @author: wangchao
 * @Description: MigrationTaskCheckProgressService
 * @since 2025/01/14
 **/
@Service
public class MigrationTaskCheckProgressSummaryServiceImpl
    extends ServiceImpl<MigrationTaskCheckProgressSummaryMapper, MigrationTaskCheckProgressSummary>
    implements MigrationTaskCheckProgressSummaryService {
    @Override
    public MigrationTaskCheckProgressSummary queryFullCheckSummaryOfMigrationTask(Integer taskId) {
        LambdaQueryWrapper<MigrationTaskCheckProgressSummary> lambdaQuery = Wrappers.lambdaQuery(
            MigrationTaskCheckProgressSummary.class);
        lambdaQuery.eq(MigrationTaskCheckProgressSummary::getTaskId, taskId);
        return getOne(lambdaQuery);
    }

    @Override
    public void removeByTaskIds(List<Integer> ids) {
        LambdaQueryWrapper<MigrationTaskCheckProgressSummary> query = Wrappers.lambdaQuery(
            MigrationTaskCheckProgressSummary.class);
        query.in(MigrationTaskCheckProgressSummary::getTaskId, ids);
        remove(query);
    }
}

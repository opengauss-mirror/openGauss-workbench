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
 * -------------------------------------------------------------------------
 *
 * MigrationTaskCheckProgressSummaryServiceImpl.java
 *
 * IDENTIFICATION
 * plugins/data-migration/src/main/java/org/opengauss/admin/plugin/service/impl
 * /MigrationTaskCheckProgressSummaryServiceImpl.java
 *
 * -------------------------------------------------------------------------
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

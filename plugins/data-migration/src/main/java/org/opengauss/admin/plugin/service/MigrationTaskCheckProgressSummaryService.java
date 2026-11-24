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

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.opengauss.admin.plugin.domain.MigrationTaskCheckProgressSummary;

import java.util.List;

/**
 * MigrationTaskCheckProgressSummaryService
 *
 * @author wangchao
 * @since 2025/01/14 09:01
 */
public interface MigrationTaskCheckProgressSummaryService extends IService<MigrationTaskCheckProgressSummary> {
    /**
     * Query the summary of the check progress of the specified task
     *
     * @param taskId task id
     * @return MigrationTaskCheckProgressSummary
     */
    MigrationTaskCheckProgressSummary queryFullCheckSummaryOfMigrationTask(Integer taskId);

    /**
     * remove the summary of the check progress of the specified task
     *
     * @param ids ids
     */
    void removeByTaskIds(List<Integer> ids);
}

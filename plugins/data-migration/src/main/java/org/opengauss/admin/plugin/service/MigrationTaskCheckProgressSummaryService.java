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
 * MigrationTaskCheckProgressSummaryService.java
 *
 * IDENTIFICATION
 * plugins/data-migration/src/main/java/org/opengauss/admin/plugin/service/MigrationTaskCheckProgressSummaryService.java
 *
 * -------------------------------------------------------------------------
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

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
import org.opengauss.admin.plugin.domain.FullMigrationSummaryData;

/**
 * Full migration summary data service
 *
 * @since 2025/06/23
 */
public interface FullMigrationSummaryDataService extends IService<FullMigrationSummaryData> {
    /**
     * Update by task id
     *
     * @param summaryData summary data
     */
    void updateByTaskId(FullMigrationSummaryData summaryData);

    /**
     * Get one by task id
     *
     * @param taskId taskId
     * @return summary data
     */
    FullMigrationSummaryData getOneByTaskId(Integer taskId);
}

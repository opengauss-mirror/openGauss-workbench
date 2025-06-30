/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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

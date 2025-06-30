/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.IncrementalMigrationProgress;

/**
 * Incremental migration progress service
 *
 * @since 2025/06/23
 */
public interface IncrementalMigrationProgressService extends IService<IncrementalMigrationProgress> {
    /**
     * Update by taskId
     *
     * @param incrementalMigrationProgress incrementalMigrationProgress
     */
    void updateByTaskId(IncrementalMigrationProgress incrementalMigrationProgress);

    /**
     * Get one by taskId
     *
     * @param taskId taskId
     * @return incrementalMigrationProgress
     */
    IncrementalMigrationProgress getOneByTaskId(Integer taskId);
}

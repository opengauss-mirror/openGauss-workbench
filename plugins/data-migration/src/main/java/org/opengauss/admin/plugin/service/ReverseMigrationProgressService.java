/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.ReverseMigrationProgress;

/**
 * Reverse migration progress service
 *
 * @since 2025/06/23
 */
public interface ReverseMigrationProgressService extends IService<ReverseMigrationProgress> {
    /**
     * Update by task id
     *
     * @param reverseMigrationProgress reverseMigrationProgress
     */
    void updateByTaskId(ReverseMigrationProgress reverseMigrationProgress);

    /**
     * Get one by task id
     *
     * @param taskId taskId
     * @return reverseMigrationProgress
     */
    ReverseMigrationProgress getOneByTaskId(Integer taskId);
}

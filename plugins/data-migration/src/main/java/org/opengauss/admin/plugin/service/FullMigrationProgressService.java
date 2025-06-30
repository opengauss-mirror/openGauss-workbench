/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.FullMigrationProgress;
import org.opengauss.admin.plugin.enums.FullMigrationDbObjEnum;

import java.util.List;

/**
 * Full migration progress service
 *
 * @since 2025/06/23
 */
public interface FullMigrationProgressService extends IService<FullMigrationProgress> {
    /**
     * Delete all records by taskId and objectType
     *
     * @param taskId     taskId
     * @param objectType objectType
     */
    void deleteByTaskIdAndObjectType(Integer taskId, FullMigrationDbObjEnum objectType);

    /**
     * Get list by taskId and objectType
     *
     * @param taskId     taskId
     * @param objectType objectType
     * @return list
     */
    List<FullMigrationProgress> getListByTaskIdAndObjectType(Integer taskId, FullMigrationDbObjEnum objectType);
}

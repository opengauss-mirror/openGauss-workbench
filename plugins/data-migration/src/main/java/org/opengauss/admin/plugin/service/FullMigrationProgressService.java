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
     * Delete all records by taskId and status
     *
     * @param taskId taskId
     * @param status migration status
     */
    void deleteByTaskIdAndStatus(Integer taskId, Integer status);

    /**
     * Delete all records by taskId and objectType
     *
     * @param taskId     taskId
     * @param objectType objectType
     */
    void deleteByTaskIdAndObjectType(Integer taskId, FullMigrationDbObjEnum objectType);

    /**
     * Get list by taskId
     *
     * @param taskId taskId
     * @return list
     */
    List<FullMigrationProgress> getListByTaskId(Integer taskId);

    /**
     * Get list by taskId and objectType
     *
     * @param taskId     taskId
     * @param objectType objectType
     * @return list
     */
    List<FullMigrationProgress> getListByTaskIdAndObjectType(Integer taskId, FullMigrationDbObjEnum objectType);
}

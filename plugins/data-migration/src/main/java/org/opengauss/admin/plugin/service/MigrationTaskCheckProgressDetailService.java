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
 * MigrationTaskCheckProgressDetailService.java
 *
 * IDENTIFICATION
 * plugins/data-migration/src/main/java/org/opengauss/admin/plugin/service/MigrationTaskCheckProgressDetailService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.opengauss.admin.plugin.domain.MigrationTaskCheckProgressDetail;

import java.util.List;

/**
 * MigrationTaskCheckProgressDetailService
 *
 * @author wangchao
 * @since 2025/01/14 09:01
 */
public interface MigrationTaskCheckProgressDetailService extends IService<MigrationTaskCheckProgressDetail> {
    /**
     * remove by task id
     *
     * @param ids ids
     */
    void removeByTaskIds(List<Integer> ids);
}

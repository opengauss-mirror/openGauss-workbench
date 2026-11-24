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

package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Full migration progress
 *
 * @since 2025/06/23
 */
@Data
@TableName("tb_migration_full_migration_progress")
public class FullMigrationProgress {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer taskId;
    private String name;
    private String schema;
    private Double percent;
    private String error;

    /**
     * Object type: table, view, trigger, function, procedure
     */
    private String objectType;

    /**
     * 1: wait, 2: running, 3: success, 6: error
     */
    private Integer status;
}

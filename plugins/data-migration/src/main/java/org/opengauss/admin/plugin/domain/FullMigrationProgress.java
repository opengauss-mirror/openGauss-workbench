/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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

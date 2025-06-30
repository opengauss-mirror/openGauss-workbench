/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Full migration summary data
 *
 * @since 2025/06/23
 */
@Data
@TableName("tb_migration_full_migration_summary_data")
public class FullMigrationSummaryData {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer taskId;
    private Double data;
    private Long record;
    private Double speed;
    private Integer time;
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Reverse migration progress
 *
 * @since 2025/06/23
 */
@Data
@TableName("tb_migration_reverse_migration_progress")
public class ReverseMigrationProgress {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer taskId;
    @JSONField(name = "count")
    private Long totalCount;
    @JSONField(name = "failCount")
    private Long failedCount;
    private Long replayedCount;
    private Long successCount;
    private Long skippedCount;
    private Long rest;
    private Integer sinkSpeed;
    private Integer sourceSpeed;
}

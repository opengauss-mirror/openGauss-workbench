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

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Incremental migration progress
 *
 * @since 2025/06/23
 */
@Data
@TableName("tb_migration_incremental_migration_progress")
public class IncrementalMigrationProgress {
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

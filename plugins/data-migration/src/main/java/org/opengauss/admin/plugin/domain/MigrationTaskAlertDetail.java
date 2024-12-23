/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * tb migration task alert detail
 *
 * @since 2024/12/16
 */
@Data
@Builder
@TableName("tb_migration_task_alert_detail")
public class MigrationTaskAlertDetail {
    @TableId(value = "alert_id")
    private int alertId;
    private String detail;
}

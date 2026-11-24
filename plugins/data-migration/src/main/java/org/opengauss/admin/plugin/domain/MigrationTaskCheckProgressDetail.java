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

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;

/**
 * MigrationTaskCheckProgressDetail
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Data
@Builder
@TableName("tb_migration_task_check_progress_detail")
public class MigrationTaskCheckProgressDetail {
    private String id;
    private Integer taskId;
    private String schemaName;
    private String sourceName;
    private String sinkName;
    private String status;
    private Integer failedRows;
    private String message;
    private String repairFileName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Tolerate
    public MigrationTaskCheckProgressDetail() {
    }
}

/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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
 * MigrationInfo.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/dto/MigrationInfo.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.dto;

import lombok.Data;

import java.time.Instant;

/**
 * MigrationInfo
 *
 * @since 2025-06-09
 * @author ybx
 */
@Data
public class MigrationInfoDto {
    private Integer subTaskId;
    private String taskName;
    private Integer execMode;
    private String sourceDb;
    private String targetDb;
    private Long executedTime;
    private Instant createTime;
    private Instant startTime;
    private Instant finishTime;
    private String sourceDbType;
}

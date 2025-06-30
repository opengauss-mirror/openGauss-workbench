/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;
import org.opengauss.admin.plugin.domain.FullMigrationProgress;

/**
 * Full Migration Progress VO
 *
 * @since 2025/6/24
 */
@Data
public class FullMigrationProgressVo {
    private Long id;
    private Integer taskId;
    private String name;
    private String schema;
    private Double percent;
    private String errorMsg;

    /**
     * Object type: table, view, trigger, function, procedure
     */
    private String objectType;

    /**
     * 1: wait, 2: running, 3: success, 6: error
     */
    private Integer status;

    public FullMigrationProgressVo(FullMigrationProgress fullMigrationProgress) {
        this.id = fullMigrationProgress.getId();
        this.taskId = fullMigrationProgress.getTaskId();
        this.objectType = fullMigrationProgress.getObjectType();
        this.name = fullMigrationProgress.getName();
        this.schema = fullMigrationProgress.getSchema();
        this.status = fullMigrationProgress.getStatus();
        this.percent = fullMigrationProgress.getPercent();
        this.errorMsg = fullMigrationProgress.getError();
    }
}

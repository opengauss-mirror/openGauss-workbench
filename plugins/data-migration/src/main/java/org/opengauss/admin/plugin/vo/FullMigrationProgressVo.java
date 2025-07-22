/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;
import org.opengauss.admin.plugin.domain.FullMigrationProgress;

import java.util.Map;

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
    private String sourceSchema;
    private String targetSchema;
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

    public FullMigrationProgressVo() {
    }

    public FullMigrationProgressVo(FullMigrationProgress fullMigrationProgress, Map<String, String> schemaMapping) {
        this.id = fullMigrationProgress.getId();
        this.taskId = fullMigrationProgress.getTaskId();
        this.objectType = fullMigrationProgress.getObjectType();
        this.name = fullMigrationProgress.getName();
        this.status = fullMigrationProgress.getStatus();
        this.percent = fullMigrationProgress.getPercent();
        this.errorMsg = fullMigrationProgress.getError();
        this.sourceSchema = fullMigrationProgress.getSchema();
        this.targetSchema = schemaMapping.get(sourceSchema);
    }
}

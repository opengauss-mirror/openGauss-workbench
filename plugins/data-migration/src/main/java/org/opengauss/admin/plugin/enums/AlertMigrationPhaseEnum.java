/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.enums;

import lombok.Getter;

/**
 * migration phase of migration alert
 *
 * @since 2024/12/17
 */
@Getter
public enum AlertMigrationPhaseEnum {
    FULL_MIGRATION(1, "full_migration", "full migration phase"),
    FULL_MIGRATION_CHECK(2, "full_migration_check", "full migration data check phase"),
    INCREMENTAL_MIGRATION(3, "incremental_migration",
            "incremental migration and incremental migration data check phase"),
    REVERSE_MIGRATION(4, "reverse_migration", "reverse migration phase");

    private final Integer phaseId;
    private final String phaseName;
    private final String description;

    AlertMigrationPhaseEnum(Integer phaseId, String phaseName, String description) {
        this.phaseId = phaseId;
        this.phaseName = phaseName;
        this.description = description;
    }
}

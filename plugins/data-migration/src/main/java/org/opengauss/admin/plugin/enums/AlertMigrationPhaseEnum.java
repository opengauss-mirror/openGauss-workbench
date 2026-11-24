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

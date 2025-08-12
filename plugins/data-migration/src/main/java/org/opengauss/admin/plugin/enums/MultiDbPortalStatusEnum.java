/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.enums;

import lombok.Getter;

/**
 * MULTI_DB portal status
 *
 * @since 2025/06/23
 */
@Getter
public enum MultiDbPortalStatusEnum {
    NOT_START(0, 0, "Migration not started"),
    MIGRATION_STARTING(1, 0, "Migration starting"),

    START_FULL_MIGRATION(100, 1, "Full migration started"),
    FULL_MIGRATION_RUNNING(101, 2, "Full migration running"),
    FULL_MIGRATION_FINISHED(102, 3, "Full migration finished"),

    START_FULL_DATA_CHECK(200, 4, "Full data check started"),
    FULL_DATA_CHECK_RUNNING(201, 5, "Full data check running"),
    FULL_DATA_CHECK_FINISHED(202, 6, "Full data check finished"),

    START_INCREMENTAL_MIGRATION(300, 7, "Incremental migration started"),
    INCREMENTAL_MIGRATION_RUNNING(301, 8, "Incremental migration running"),
    INCREMENTAL_MIGRATION_STOPPING(302, 9, "Incremental migration stopping"),
    INCREMENTAL_MIGRATION_FINISHED(303, 10, "Incremental migration finished"),

    START_REVERSE_MIGRATION(400, 11, "Reverse migration started"),
    REVERSE_MIGRATION_RUNNING(401, 12, "Reverse migration running"),
    REVERSE_MIGRATION_STOPPING(402, 13, "Reverse migration stopping"),
    REVERSE_MIGRATION_FINISHED(403, 13, "Reverse migration finished"),

    MIGRATION_FINISHED(600, 100, "Migration finished"),
    MIGRATION_STOPPING(601, 100, "Migration stopping"),

    MIGRATION_FAILED(500, 500, "Migration failed"),
    INCREMENTAL_MIGRATION_INTERRUPTED(501, 30, "Incremental migration interrupted"),
    REVERSE_MIGRATION_INTERRUPTED(502, 40, "Reverse migration interrupted"),

    PRE_MIGRATION_VERIFY_FAILED(700, 3000, "Pre migration verify failed"),
    PRE_REVERSE_PHASE_VERIFY_FAILED(701, 3000, "Pre reverse phase verify failed"),
    ;

    MultiDbPortalStatusEnum(int status, int datakitStatus, String description) {
        this.status = status;
        this.datakitStatus = datakitStatus;
        this.description = description;
    }

    private final int status;
    private final int datakitStatus;
    private final String description;
}

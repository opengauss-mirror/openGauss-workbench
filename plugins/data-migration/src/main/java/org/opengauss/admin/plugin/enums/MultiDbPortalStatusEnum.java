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

    START_FULL_MIGRATION(100, 1, "Full migration started"),
    FULL_MIGRATION_RUNNING(101, 2, "Full migration running"),
    FULL_MIGRATION_FINISHED(102, 3, "Full migration finished"),

    START_FULL_DATA_CHECK(200, 4, "Full data check started"),
    FULL_DATA_CHECK_RUNNING(201, 5, "Full data check running"),
    FULL_DATA_CHECK_FINISHED(202, 6, "Full data check finished"),

    START_INCREMENTAL_MIGRATION(300, 7, "Incremental migration started"),
    INCREMENTAL_MIGRATION_RUNNING(301, 8, "Incremental migration running"),
    INCREMENTAL_MIGRATION_FINISHED(302, 10, "Incremental migration finished"),

    START_REVERSE_MIGRATION(401, 11, "Reverse migration started"),
    REVERSE_MIGRATION_RUNNING(402, 12, "Reverse migration running"),
    REVERSE_MIGRATION_FINISHED(403, 13, "Reverse migration finished"),

    MIGRATION_FINISHED(600, 100, "Migration finished"),
    PRE_MIGRATION_VERIFY_FAILED(601, 3000, "Pre migration verify failed"),
    PRE_REVERSE_PHASE_VERIFY_FAILED(602, 3000, "Pre reverse phase verify failed"),
    MIGRATION_FAILED(500, 500, "Migration failed"),

    INCREMENTAL_MIGRATION_INTERRUPTED(501, 30, "Incremental migration interrupted"),
    REVERSE_MIGRATION_INTERRUPTED(502, 40, "Reverse migration interrupted"),
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

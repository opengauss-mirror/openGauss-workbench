/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.dto;

import lombok.Data;
import org.opengauss.admin.plugin.enums.MultiDbPortalStatusEnum;

/**
 * MULTI_DB portal migration status entry
 *
 * @since 2025/6/23
 */
@Data
public class MultiDbPortalMigrationStatus {
    private long timestamp;
    private MultiDbPortalStatusEnum status;
}

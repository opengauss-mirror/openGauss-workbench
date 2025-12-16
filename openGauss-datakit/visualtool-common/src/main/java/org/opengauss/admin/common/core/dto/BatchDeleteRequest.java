/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.common.core.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * BatchDeleteRequest
 *
 * @since 2025/11/18
 */
@Data
public class BatchDeleteRequest {
    @NotEmpty(message = "ID list cannot be empty")
    private List<Long> ids;
}

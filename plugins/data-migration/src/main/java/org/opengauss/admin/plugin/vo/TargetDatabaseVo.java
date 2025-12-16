/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TargetDatabaseVo
 *
 * @since 2025/11/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetDatabaseVo {
    private String dbName;
    private String datcompatibility;
    private boolean isSelect;
}

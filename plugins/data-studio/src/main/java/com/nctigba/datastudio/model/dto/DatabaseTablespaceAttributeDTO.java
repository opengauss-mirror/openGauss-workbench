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

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DatabaseTablespaceAttributeDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseTablespaceAttributeDTO {
    private String tablespaceName;
    private String owner;
    private String path;
    private boolean isRelativePath;
    private String maxStorage;
    private String sequentialOverhead;
    private String nonSequentialOverhead;
    private String comment;
}

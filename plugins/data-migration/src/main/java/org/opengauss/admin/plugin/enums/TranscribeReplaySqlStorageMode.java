/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TranscribeReplaySqlStorageMode
 *
 * @author: ybx
 * @since: 2025/02/11
 */
@AllArgsConstructor
@Getter
public enum TranscribeReplaySqlStorageMode {
    JSON("json"),
    DB("db");

    private final String mode;
}

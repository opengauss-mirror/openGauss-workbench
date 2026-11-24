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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Migration task mode
 *
 * @since 2026/05/25
 */
@AllArgsConstructor
@Getter
public enum MigrationMode {
    OFFLINE(1),
    ONLINE(2),
    OFFLINE_WITHOUT_DATA_CHECK(3),
    ONLINE_WITHOUT_DATA_CHECK(4);
    private Integer code;

    /**
     * Is mode has incremental and reverse migration
     *
     * @param migrationModelId migration model id
     * @return true if has incremental and reverse migration, false otherwise
     */
    public static boolean hasIncrementalAndReverse(Integer migrationModelId) {
        return ONLINE.getCode().equals(migrationModelId)
                || ONLINE_WITHOUT_DATA_CHECK.getCode().equals(migrationModelId);
    }
}

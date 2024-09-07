/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * MainTaskEnvErrorHost.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/domain/MainTaskEnvErrorHost.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.enums;

import lombok.Getter;

/**
 * full Migration database object enum
 *
 * @since 2024-09-07
 */
@Getter
public enum FullMigrationDbObjEnum {
    TABLE("table"),
    VIEW("view"),
    FUNCTION("function"),
    TRIGGER("trigger"),
    PROCEDURE("procedure");

    private final String objectType;

    FullMigrationDbObjEnum(String objectType) {
        this.objectType = objectType;
    }
}
